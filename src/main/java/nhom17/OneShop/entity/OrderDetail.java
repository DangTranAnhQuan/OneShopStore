package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderDetailId")
    private Integer id;

    @Column(name = "ProductName")
    private String productName;

    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;

    @Column(name = "Quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    public OrderDetail() {
        // For JPA
    }

    public OrderDetail(Product product, String productName, BigDecimal unitPrice, int quantity) {
        this.product = Objects.requireNonNull(product, "Sản phẩm không hợp lệ");
        this.productName = Objects.requireNonNull(productName, "Tên sản phẩm không hợp lệ");
        setUnitPriceInternal(unitPrice);
        updateQuantity(quantity);
    }

    public OrderDetail(Order order, Product product, String productName, BigDecimal unitPrice, int quantity) {
        this(product, productName, unitPrice, quantity);
        attachToOrder(order);
    }

    void attachToOrder(Order order) {
        this.order = order;
    }


    public void updateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }
        this.quantity = quantity;
    }

    @Transient
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void setUnitPriceInternal(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0");
        }
        this.unitPrice = unitPrice;
    }
}
