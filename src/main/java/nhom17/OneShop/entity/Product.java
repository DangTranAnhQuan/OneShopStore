package nhom17.OneShop.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.Formula;

@Getter
@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Integer productId;

    @Column(name = "Name")
    private String name;

    @Lob
    @Column(name = "Description")
    private String description;

    @Column(name = "Price")
    private BigDecimal price;

    @Column(name = "OriginalPrice")
    private BigDecimal originalPrice;

    @Column(name = "ExpirationDays")
    private Integer expirationDays;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "IsActive")
    private boolean isActive;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BrandId", nullable = false)
    @JsonIgnore
    private Brand brand;

    @Formula("(SELECT SUM(od.Quantity) " +
            " FROM OrderDetails od JOIN Orders o ON od.OrderId = o.OrderId " +
            " WHERE od.ProductId = ProductId AND o.OrderStatus = 'DELIVERED')")
    private Long totalSold;

    @Formula("(SELECT COUNT(*) FROM Ratings r WHERE r.ProductId = ProductId)")
    private int reviewCount;

    @Formula("(SELECT AVG(CAST(r.RatingScore AS DECIMAL(10,2))) " +
            " FROM Ratings r WHERE r.ProductId = ProductId)")
    private Double averageRating;

    public Product() {
        // For JPA
    }

    public Product(String name, String description, BigDecimal price, BigDecimal originalPrice,
                   Integer expirationDays, boolean isActive, String imageUrl,
                   Category category, Brand brand) {
        updateDetails(name, description, price, originalPrice, expirationDays, isActive, imageUrl, category, brand, true);
    }

    public void updateDetails(String name, String description, BigDecimal price, BigDecimal originalPrice,
                              Integer expirationDays, boolean isActive, String imageUrl,
                              Category category, Brand brand, boolean requireImageIfMissing) {
        validatePricing(price, originalPrice);
        if (requireImageIfMissing && (imageUrl == null || imageUrl.isBlank())) {
            throw new IllegalArgumentException("Vui lòng chọn hình ảnh cho sản phẩm mới.");
        }
        this.name = Objects.requireNonNull(name, "Tên sản phẩm không được trống");
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.expirationDays = expirationDays;
        this.isActive = isActive;
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        this.category = Objects.requireNonNull(category, "Danh mục không hợp lệ");
        this.brand = Objects.requireNonNull(brand, "Thương hiệu không hợp lệ");
    }

    private void validatePricing(BigDecimal price, BigDecimal originalPrice) {
        if (price != null && originalPrice != null && price.compareTo(originalPrice) > 0) {
            throw new IllegalArgumentException("Giá bán không được lớn hơn giá niêm yết.");
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
