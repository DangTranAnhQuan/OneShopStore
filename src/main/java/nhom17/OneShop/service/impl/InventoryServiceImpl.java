package nhom17.OneShop.service.impl;

import jakarta.persistence.criteria.JoinType;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.OrderDetail;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public Page<Inventory> findAll(String keyword, String sort, int page, int size) {
        // Sắp xếp
        Sort sortable = Sort.by("productId").ascending();
        if (StringUtils.hasText(sort)) {
            switch (sort) {
                case "qty_asc":
                    sortable = Sort.by("stockQuantity").ascending();
                    break;
                case "qty_desc":
                    sortable = Sort.by("stockQuantity").descending();
                    break;
                default:
                    break;
            }
        }
        Pageable pageable = PageRequest.of(page - 1, size, sortable);

        // Tìm kiếm
        Specification<Inventory> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> {
                var product = root.join("product", JoinType.LEFT);
                return cb.like(cb.lower(product.get("name")), kw);
            });
        }

        return inventoryRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public void restockOrderItems(Order order) {
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }

        Map<Integer, Integer> quantityByProduct = new HashMap<>();
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail == null || detail.getProduct() == null || detail.getProduct().getProductId() == null) {
                continue;
            }
            int quantity = detail.getQuantity() == null ? 0 : detail.getQuantity();
            if (quantity <= 0) {
                continue;
            }
            Integer productId = detail.getProduct().getProductId();
            quantityByProduct.merge(productId, quantity, Integer::sum);
        }

        List<Inventory> inventoriesToSave = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : quantityByProduct.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            Inventory inventory = inventoryRepository.findById(productId)
                    .orElseGet(() -> new Inventory(order.getOrderDetails().stream()
                            .filter(d -> d.getProduct() != null && productId.equals(d.getProduct().getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Sản phẩm không hợp lệ cho tồn kho"))
                            .getProduct(), 0, null));
            inventory.increase(quantity);
            inventoriesToSave.add(inventory);
        }

        if (!inventoriesToSave.isEmpty()) {
            inventoryRepository.saveAll(inventoriesToSave);
        }
    }

    @Override
    @Transactional
    public void deductStockForCartItems(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        Map<Integer, Integer> quantityByProduct = new HashMap<>();
        Map<Integer, String> productNameById = new HashMap<>();
        List<Inventory> inventoriesToSave = new java.util.ArrayList<>();

        for (CartItem cartItem : cartItems) {
            if (cartItem == null || cartItem.getProduct() == null || cartItem.getProduct().getProductId() == null) {
                continue;
            }
            int quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            if (quantity <= 0) {
                continue;
            }
            Integer productId = cartItem.getProduct().getProductId();
            quantityByProduct.merge(productId, quantity, Integer::sum);
            productNameById.putIfAbsent(productId, cartItem.getProduct().getName());
        }

        for (Map.Entry<Integer, Integer> entry : quantityByProduct.entrySet()) {
            Integer productId = entry.getKey();
            Integer orderedQuantity = entry.getValue();
            Inventory inventory = inventoryRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Hết hàng tồn kho cho sản phẩm: " + productNameById.getOrDefault(productId, productId.toString())));
            inventory.decrease(orderedQuantity);
            inventoriesToSave.add(inventory);
        }

        if (!inventoriesToSave.isEmpty()) {
            inventoryRepository.saveAll(inventoriesToSave);
        }
    }
}
