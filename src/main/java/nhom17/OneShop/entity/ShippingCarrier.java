package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ShippingCarriers")
public class ShippingCarrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarrierId")
    private Integer carrierId;

    @Column(name = "CarrierName")
    private String carrierName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Website")
    private String website;

    @Column(name = "IsActive")
    private boolean active = true;

    public ShippingCarrier() {
        // For JPA
    }

    public ShippingCarrier(String carrierName, String phoneNumber, String website) {
        this.active = true;
        updateInfo(carrierName, phoneNumber, website);
    }

    public void updateInfo(String carrierName, String phoneNumber, String website) {
        this.carrierName = carrierName;
        this.phoneNumber = phoneNumber;
        this.website = website;
    }

    public void deactivate() {
        this.active = false;
    }
}