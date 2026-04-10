package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "Categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private Integer categoryId;

    @Column(name = "CategoryName")
    private String categoryName;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "IsActive")
    private boolean isActive;

    public Category() {
        // For JPA
    }

    public Category(String categoryName, String imageUrl, boolean isActive) {
        update(categoryName, imageUrl, isActive);
    }

    public void update(String categoryName, String imageUrl, boolean isActive) {
        this.categoryName = categoryName;
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        this.isActive = isActive;
    }
}
