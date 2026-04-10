package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "Addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AddressId")
    private Integer addressId;

    @Column(name = "ReceiverName")
    private String receiverName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Province")
    private String province;

    @Column(name = "District")
    private String district;

    @Column(name = "Ward")
    private String ward;

    @Column(name = "StreetAddress")
    private String streetAddress;

    @Column(name = "IsActive")
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    public Address() {
        // For JPA
    }


    public void assignUser(User user) {
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
    }

    public void update(String receiverName, String phoneNumber, String province, String district, String ward, String streetAddress) {
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.streetAddress = streetAddress;
    }

    public void deactivate() {
        this.isActive = false;
    }
}