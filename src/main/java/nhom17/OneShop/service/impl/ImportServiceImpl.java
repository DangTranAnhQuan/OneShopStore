package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.request.ImportDetailRequest;
import nhom17.OneShop.request.ImportRequest;
import nhom17.OneShop.service.ImportService;
import nhom17.OneShop.specification.ImportSpecification;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private ImportRepository importRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImportDetailRepository importDetailRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public Page<Import> findAll(String keyword, Integer supplierId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Specification<Import> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            try {
                Integer id = Integer.parseInt(keyword);
                spec = spec.and(ImportSpecification.hasId(id));
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        if (supplierId != null) {
            spec = spec.and(ImportSpecification.hasSupplier(supplierId));
        }

        return importRepository.findAll(spec, pageable);
    }

    @Override
    public Import findById(int id) {
        return importRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ImportRequest importRequest) {
        validateImportDetails(importRequest.getImportDetails());
        Supplier supplier = supplierRepository.findById(importRequest.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà cung cấp với ID: " + importRequest.getSupplierId()));

        Import importReceipt = prepareImportEntity(importRequest, supplier);
        Map<Integer, Inventory> inventoryByProduct = new HashMap<>();

        if (importReceipt.getImportId() != null) {
            handleInventoryForUpdate(importReceipt, inventoryByProduct);
        }

        processNewImportDetails(importRequest.getImportDetails(), importReceipt, inventoryByProduct);

        inventoryRepository.saveAll(inventoryByProduct.values());
        importRepository.save(importReceipt);
    }

    private void validateImportDetails(List<ImportDetailRequest> details) {
        Set<Integer> productIds = new HashSet<>();
        for (ImportDetailRequest detail : details) {
            if (!productIds.add(detail.getProductId())) {
                throw new IllegalArgumentException("Sản phẩm không được trùng lặp trong một phiếu nhập.");
            }
        }
    }

    private Import prepareImportEntity(ImportRequest importRequest, Supplier supplier) {
        if (importRequest.getImportId() != null) {
            Import existing = findById(importRequest.getImportId());
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy phiếu nhập với ID: " + importRequest.getImportId());
            }
            existing.assignSupplier(supplier);
            return existing;
        }
        return new Import(supplier);
    }

    private void handleInventoryForUpdate(Import importReceipt, Map<Integer, Inventory> inventoryByProduct) {
        for (ImportDetail oldDetail : importReceipt.getImportDetails()) {
            Inventory inventory = loadInventory(oldDetail.getProduct(), inventoryByProduct);
            inventory.decrease(oldDetail.getQuantity());
        }
        importReceipt.clearDetails();
    }

    private void processNewImportDetails(List<ImportDetailRequest> detailRequests, Import importReceipt, Map<Integer, Inventory> inventoryByProduct) {
        for (ImportDetailRequest detailRequest : detailRequests) {
            Product product = productRepository.findById(detailRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + detailRequest.getProductId()));

            ImportDetail detail = new ImportDetail(importReceipt, product, detailRequest.getQuantity(), detailRequest.getImportPrice());
            importReceipt.addDetail(detail);

            Inventory inventory = loadInventory(product, inventoryByProduct);
            inventory.increase(detailRequest.getQuantity());
        }
    }

    private Inventory loadInventory(Product product, Map<Integer, Inventory> inventoryByProduct) {
        return inventoryByProduct.computeIfAbsent(product.getProductId(), id ->
                inventoryRepository.findById(id).orElseGet(() -> new Inventory(product, 0, null))
        );
    }

    @Override
    @Transactional
    public void delete(int id) {
        Import importReceipt = findById(id);
        Map<Integer, Inventory> inventoryByProduct = new HashMap<>();
        for (ImportDetail detail : importReceipt.getImportDetails()) {
            Inventory inventory = loadInventory(detail.getProduct(), inventoryByProduct);
            inventory.decrease(detail.getQuantity());
        }
        inventoryRepository.saveAll(inventoryByProduct.values());
        importRepository.delete(importReceipt);
    }

    @Override
    public List<ImportDetail> getHistoryForProduct(Integer productId) {
        return importDetailRepository.findHistoryByProductId(productId);
    }
}
