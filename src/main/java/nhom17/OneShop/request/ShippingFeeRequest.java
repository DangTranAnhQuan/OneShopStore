package nhom17.OneShop.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import nhom17.OneShop.entity.enums.ShippingMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingFeeRequest {
    private Integer shippingFeeId;

    @NotBlank(message = "Tên gói cước không được để trống")
    private String packageName;

    @NotNull(message = "Vui lòng chọn nhà vận chuyển")
    private Integer carrierId;

    @NotNull(message = "Phương thức vận chuyển không được để trống")
    private ShippingMethod shippingMethod;

    @NotNull(message = "Chi phí không được để trống")
    @DecimalMin(value = "0.0", message = "Chi phí không được là số âm")
    private BigDecimal feeAmount;

    @NotNull(message = "Thời gian giao sớm nhất không được để trống")
    @Min(value = 0, message = "Thời gian không được là số âm")
    private Integer minDeliveryDays;

    @NotNull(message = "Thời gian giao muộn nhất không được để trống")
    @Min(value = 0, message = "Thời gian không được là số âm")
    private Integer maxDeliveryDays;

    @NotBlank(message = "Vui lòng chọn đơn vị thời gian")
    private String timeUnit;

    @NotEmpty(message = "Vui lòng chọn ít nhất một tỉnh thành áp dụng")
    private List<String> appliedProvinces;
}
