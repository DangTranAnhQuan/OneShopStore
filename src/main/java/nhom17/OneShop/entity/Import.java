package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "Imports")
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImportId")
    private Integer importId;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId")
    private Supplier supplier;

    @Transient
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ImportDetail> importDetails = new ArrayList<>();

    public Import() {
        // For JPA
    }

    public Import(Supplier supplier) {
        assignSupplier(supplier);
    }

    public void assignSupplier(Supplier supplier) {
        this.supplier = Objects.requireNonNull(supplier, "Nhà cung cấp không hợp lệ");
    }

    public void addDetail(ImportDetail detail) {
        Objects.requireNonNull(detail, "Chi tiết phiếu nhập không hợp lệ");
        detail.attachToImport(this);
        this.importDetails.add(detail);
        recalculateTotalAmount();
    }

    public void clearDetails() {
        this.importDetails.clear();
        recalculateTotalAmount();
    }

    public BigDecimal getTotalAmount() {
        if (totalAmount == null) {
            recalculateTotalAmount();
        }
        return totalAmount;
    }

    private void recalculateTotalAmount() {
        this.totalAmount = importDetails.stream()
                .map(ImportDetail::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
