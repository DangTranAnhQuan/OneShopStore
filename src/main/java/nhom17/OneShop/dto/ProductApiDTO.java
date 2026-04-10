package nhom17.OneShop.dto;

import nhom17.OneShop.entity.Product;

import java.math.BigDecimal;

public record ProductApiDTO(
        Integer productId,
        String name,
        BigDecimal price,
        BigDecimal originalPrice,
        String imageUrl,
        Integer reviewCount,
        Double averageRating
) {
    public static ProductApiDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductApiDTO(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getImageUrl(),
                product.getReviewCount(),
                product.getAverageRating()
        );
    }
}

