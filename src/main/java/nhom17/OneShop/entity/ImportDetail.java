package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
@Table(name = "ImportDetails")
public class ImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImportDetailId")
    private Integer id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "ImportPrice")
    private BigDecimal importPrice;

    @Transient
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ImportId", nullable = false)
    private Import importReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    public ImportDetail() {
        // For JPA
    }

    public ImportDetail(Import importReceipt, Product product, int quantity, BigDecimal importPrice) {
        this.product = Objects.requireNonNull(product, "Sản phẩm không hợp lệ");
        attachToImport(importReceipt);
        setImportPriceInternal(importPrice);
        updateQuantity(quantity);
    }

    void attachToImport(Import importReceipt) {
        this.importReceipt = importReceipt;
    }

    private void updateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }
        this.quantity = quantity;
        recalculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        if (totalPrice == null) {
            recalculateTotalPrice();
        }
        return totalPrice;
    }

    private void setImportPriceInternal(BigDecimal importPrice) {
        if (importPrice == null || importPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá nhập phải lớn hơn hoặc bằng 0");
        }
        this.importPrice = importPrice;
    }

    private void recalculateTotalPrice() {
        if (importPrice == null || quantity == null) {
            this.totalPrice = BigDecimal.ZERO;
            return;
        }
        this.totalPrice = importPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
