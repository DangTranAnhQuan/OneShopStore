package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "WishLists")
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WishListId")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false, unique = true)
    private User user;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishListItem> wishListItems = new ArrayList<>();

    public WishList() {
        // For JPA
    }

    public WishList(User user) {
        assignUser(user);
    }

    public void assignUser(User user) {
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
    }

    public void addItem(WishListItem item) {
        Objects.requireNonNull(item, "Chi tiết yêu thích không hợp lệ");
        item.attachToWishList(this);
        this.wishListItems.add(item);
    }

    public void removeItem(WishListItem item) {
        if (item == null) {
            return;
        }
        this.wishListItems.remove(item);
        item.detachFromWishList();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
