package nhom17.OneShop.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nhom17.OneShop.entity.enums.ShippingStatus;

@Data
public class ShippingRequest {
    private Long shippingId;

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Vui lòng chọn nhà vận chuyển")
    private Integer carrierId;
    private ShippingStatus status;
}
