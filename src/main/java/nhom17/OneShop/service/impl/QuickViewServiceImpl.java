package nhom17.OneShop.service.impl;

import jakarta.transaction.Transactional;
import nhom17.OneShop.dto.QuickViewDTO;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.service.QuickViewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class QuickViewServiceImpl implements QuickViewService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public QuickViewServiceImpl(ProductRepository productRepo,
                                InventoryRepository inventoryRepo) {
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public QuickViewDTO build(Integer productId) {
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return null;

        Integer stockQuantity = inventoryRepo.findByProduct(p)
                .map(i -> i.getStockQuantity() != null ? i.getStockQuantity() : 0)
                .orElse(0);
        boolean inStock = stockQuantity > 0;

        String imageUrl = (p.getImageUrl() == null || p.getImageUrl().isBlank())
                ? "/web/assets/images/product/electric/product-01.png"
                : "/uploads/" + p.getImageUrl();

        long price = p.getPrice() != null ? p.getPrice().longValue() : 0L;
        long oldPrice = p.getOriginalPrice() != null ? p.getOriginalPrice().longValue() : 0L;

        String name = p.getName();
        String shortDesc = p.getDescription();
        String brandName = (p.getBrand() != null) ? p.getBrand().getBrandName() : null;
        String categoryName = (p.getCategory() != null) ? p.getCategory().getCategoryName() : null;

        int reviewCount = p.getReviewCount();
        Double avgRating = p.getAverageRating() != null ? p.getAverageRating() : 0.0;

        return new QuickViewDTO(
                p.getProductId(), name, brandName, categoryName,
                shortDesc, price, oldPrice, inStock, stockQuantity,
                avgRating, reviewCount, List.of(imageUrl)
        );
    }
}