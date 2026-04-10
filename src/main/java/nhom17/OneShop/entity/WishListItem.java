package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(name = "WishListItems")
public class WishListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WishListItemId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WishListId", nullable = false)
    private WishList wishList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    public WishListItem() {
        // For JPA
    }

    public WishListItem(WishList wishList, Product product) {
        this.wishList = Objects.requireNonNull(wishList, "Danh sách yêu thích không hợp lệ");
        this.product = Objects.requireNonNull(product, "Sản phẩm không hợp lệ");
    }

    void attachToWishList(WishList wishList) {
        this.wishList = Objects.requireNonNull(wishList, "Danh sách yêu thích không hợp lệ");
    }

    void detachFromWishList() {
        this.wishList = null;
    }
}
