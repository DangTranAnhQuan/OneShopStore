package nhom17.OneShop.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShippingCarrierRequest {
    private Integer carrierId;

    @NotBlank(message = "Tên nhà vận chuyển không được để trống")
    @Size(max = 150, message = "Tên nhà vận chuyển không được vượt quá 150 ký tự")
    private String carrierName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^((0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})|1[89]00(\\s|\\.)?\\d{4,6}|(02\\d{1,2})(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3,4}))$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @URL(message = "Website không hợp lệ")
    private String website;
}
