package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "Ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RatingId")
    private Integer ratingId;

    @Column(name = "RatingScore")
    private Integer ratingScore;

    @Column(name = "Comment")
    private String comment;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "VideoUrl")
    private String videoUrl;

    public Rating() {
        // For JPA
    }

    public Rating(Product product, User user, int ratingScore, String comment, String imageUrl, String videoUrl) {
        this.product = Objects.requireNonNull(product, "Sản phẩm không hợp lệ");
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
        setRatingScore(ratingScore);
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.createdAt = LocalDateTime.now();
    }

    public void updateContent(int ratingScore, String comment, String imageUrl, String videoUrl) {
        setRatingScore(ratingScore);
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.createdAt = LocalDateTime.now();
    }

    private void setRatingScore(int ratingScore) {
        if (ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }
        this.ratingScore = ratingScore;
    }
}
