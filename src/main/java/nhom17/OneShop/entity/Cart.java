package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "Carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false, unique = true)
    private User user;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart() {
        // For JPA
    }

    public Cart(User user) {
        assignUser(user);
    }

    public void assignUser(User user) {
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
    }

    public void addItem(CartItem item) {
        Objects.requireNonNull(item, "Chi tiết giỏ hàng không hợp lệ");
        item.attachToCart(this);
        this.cartItems.add(item);
    }

    public void removeItem(CartItem item) {
        if (item == null) {
            return;
        }
        this.cartItems.remove(item);
        item.detachFromCart();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}