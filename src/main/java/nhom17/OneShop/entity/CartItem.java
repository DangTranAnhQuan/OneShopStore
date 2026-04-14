package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
@Table(name = "CartItems")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartItemId")
    private Integer id;

    @Column(name = "Quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartId", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    public CartItem() {
        // For JPA
    }

    public CartItem(Cart cart, Product product, int quantity, int stockQuantity) {
        this.cart = Objects.requireNonNull(cart, "Giỏ hàng không hợp lệ");
        this.product = Objects.requireNonNull(product, "Sản phẩm không hợp lệ");
        updateQuantityInternal(quantity, stockQuantity);
    }

    void attachToCart(Cart cart) {
        this.cart = Objects.requireNonNull(cart, "Giỏ hàng không hợp lệ");
    }

    void detachFromCart() {
        this.cart = null;
    }

    public void increaseQuantity(int delta, int stockQuantity) {
        int currentQuantity = this.quantity != null ? this.quantity : 0;
        updateQuantityInternal(currentQuantity + delta, stockQuantity);
    }

    public void updateQuantity(int newQuantity, int stockQuantity) {
        updateQuantityInternal(newQuantity, stockQuantity);
    }

    @Transient
    public BigDecimal getLineTotal() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private void updateQuantityInternal(int newQuantity, int stockQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }
        if (stockQuantity < newQuantity) {
            throw new IllegalStateException("Không đủ số lượng tồn kho cho sản phẩm");
        }
        this.quantity = newQuantity;
    }
}
