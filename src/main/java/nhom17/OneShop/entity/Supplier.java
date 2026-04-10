package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "Suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SupplierId")
    private Integer supplierId;

    @Column(name = "SupplierName")
    private String supplierName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Address")
    private String address;

    public Supplier() {
        // For JPA
    }

    public Supplier(String supplierName, String phoneNumber, String address) {
        updateInfo(supplierName, phoneNumber, address);
    }

    public void updateInfo(String supplierName, String phoneNumber, String address) {
        this.supplierName = supplierName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
