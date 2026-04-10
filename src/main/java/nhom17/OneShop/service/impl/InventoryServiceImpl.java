package nhom17.OneShop.service.impl;

import jakarta.persistence.criteria.JoinType;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
