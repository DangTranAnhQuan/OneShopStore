package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Inventories")
public class Inventory {

    @Id
    @Column(name = "ProductId")
    private Integer productId;

    @Column(name = "StockQuantity")
    private Integer stockQuantity;

    @Column(name = "LastRestockDate")
    private LocalDateTime lastRestockDate;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ProductId")
    private Product product;

    public Inventory() {
        // For JPA
    }

    public Inventory(Product product, int stockQuantity, LocalDateTime lastRestockDate) {
        this.product = product;
        this.stockQuantity = Math.max(0, stockQuantity);
        this.lastRestockDate = lastRestockDate;
    }

    public void increase(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng tăng phải lớn hơn 0");
        }
        this.stockQuantity = (this.stockQuantity == null ? 0 : this.stockQuantity) + quantity;
        this.lastRestockDate = LocalDateTime.now();
    }

    public void decrease(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng giảm phải lớn hơn 0");
        }
        int current = this.stockQuantity == null ? 0 : this.stockQuantity;
        if (current < quantity) {
            throw new IllegalStateException("Không đủ tồn kho");
        }
        this.stockQuantity = current - quantity;
    }
}
