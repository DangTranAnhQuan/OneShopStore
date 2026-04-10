package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "Brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandId")
    private Integer brandId;

    @Column(name = "BrandName")
    private String brandName;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Lob
    @Column(name = "Description")
    private String description;

    @Column(name = "IsActive")
    private boolean isActive;

    public Brand() {
        // For JPA
    }

    public Brand(String brandName, String imageUrl, String description, boolean isActive) {
        update(brandName, imageUrl, description, isActive);
    }

    public void update(String brandName, String imageUrl, String description, boolean isActive) {
        this.brandName = brandName;
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        this.description = description;
        this.isActive = isActive;
    }
}
