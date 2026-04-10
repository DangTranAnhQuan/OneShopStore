package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.entity.Category;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.BrandRepository;
import nhom17.OneShop.repository.CategoryRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.request.ProductRequest;
import nhom17.OneShop.service.ProductService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private StorageService storageService;

    @Override
    public Page<Product> searchProducts(String keyword, Boolean status, Integer categoryId, Integer brandId, String sort, int page, int size) {
        // 1. Xử lý logic sắp xếp (giữ nguyên như cũ)
        Sort sortable = Sort.by("productId").ascending();
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc": sortable = Sort.by("price").ascending(); break;
                case "price_desc": sortable = Sort.by("price").descending(); break;
            }
        }
        Pageable pageable = PageRequest.of(page - 1, size, sortable);

        Specification<Product> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(ProductSpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(ProductSpecification.hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.inCategory(categoryId));
        }
        if (brandId != null) {
            spec = spec.and(ProductSpecification.inBrand(brandId));
        }
        return productRepository.findAll(spec, pageable);
    }

    @Override
    public List<Product> findAll(Sort sort) {
        return productRepository.findAll(sort);
    }

    @Override
    public Product findById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ProductRequest productRequest) {
        validateUniqueProductName(productRequest);

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục với ID: " + productRequest.getCategoryId()));
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thương hiệu với ID: " + productRequest.getBrandId()));

        Product product;
        boolean isNew = productRequest.getProductId() == null;
        if (isNew) {
            product = new Product(
                    productRequest.getName(),
                    productRequest.getDescription(),
                    productRequest.getPrice(),
                    productRequest.getOriginalPrice(),
                    productRequest.getExpirationDays(),
                    productRequest.isActive(),
                    productRequest.getImageUrl(),
                    category,
                    brand
            );
        } else {
            product = findById(productRequest.getProductId());
            if (product == null) {
                throw new NotFoundException("Không tìm thấy sản phẩm với ID: " + productRequest.getProductId());
            }
            product.updateDetails(
                    productRequest.getName(),
                    productRequest.getDescription(),
                    productRequest.getPrice(),
                    productRequest.getOriginalPrice(),
                    productRequest.getExpirationDays(),
                    productRequest.isActive(),
                    productRequest.getImageUrl(),
                    category,
                    brand,
                    false
            );
        }

        String oldImage = product.getImageUrl();
        productRepository.save(product);

        if (StringUtils.hasText(productRequest.getImageUrl()) && StringUtils.hasText(oldImage) && !oldImage.equals(productRequest.getImageUrl())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueProductName(ProductRequest request) {
        if (request.getProductId() == null) {
            if (productRepository.existsByNameIgnoreCase(request.getName())) {
                throw new DuplicateRecordException("Tên sản phẩm '" + request.getName() + "' đã tồn tại.");
            }
        }
        else {
            if (productRepository.existsByNameIgnoreCaseAndProductIdNot(request.getName(), request.getProductId())) {
                throw new DuplicateRecordException("Tên sản phẩm '" + request.getName() + "' đã được sử dụng.");
            }
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Product productToDelete = findById(id);

        if (productToDelete == null) {
            throw new NotFoundException("Không tìm thấy sản phẩm với ID: " + id);
        }

        productToDelete.deactivate();

        productRepository.save(productToDelete);
    }

    @Override
    public Page<Product> searchUserProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortOption, List<Integer> brandIds, int page, int size) {
        int pageNumber = page > 0 ? page - 1 : 0;

        Sort sort;
        if (sortOption == null || sortOption.isEmpty() || sortOption.equals("newest")) {
            sort = Sort.by("createdAt").descending();
        } else {
            switch (sortOption) {
                case "price_asc":
                    sort = Sort.by("price").ascending();
                    break;
                case "price_desc":
                    sort = Sort.by("price").descending();
                    break;
                case "oldest":
                    sort = Sort.by("createdAt").ascending();
                    break;
                default:
                    sort = Sort.by("createdAt").descending();
                    break;
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        Specification<Product> spec = (root, query, cb) -> cb.isTrue(root.get("isActive"));

        if (categoryId != null) {
            spec = spec.and(ProductSpecification.inCategory(categoryId));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (brandIds != null && !brandIds.isEmpty()) {
            spec = spec.and(ProductSpecification.inBrands(brandIds));
        }

        return productRepository.findAll(spec, pageable);
    }
    @Override
    public Page<Product> searchProductsForUser(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return productRepository.searchForUser(keyword, pageable);
    }
    @Override
    public List<Product> findNewestProducts(int limit) {
        return productRepository.findTop8ByIsActiveIsTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Product> findMostDiscountedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopDiscountedProducts(pageable);
    }

    @Override
    public List<Product> findTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopSellingProducts(OrderStatus.DELIVERED, pageable);
    }
}

