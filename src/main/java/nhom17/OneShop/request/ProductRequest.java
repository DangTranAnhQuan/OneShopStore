package nhom17.OneShop.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Integer productId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "1", message = "Giá bán phải là số dương")
    private BigDecimal price;

    @NotNull(message = "Giá niêm yết không được để trống")
    @DecimalMin(value = "1", message = "Giá niêm yết phải là số dương")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "Hạn sử dụng không được là số âm")
    private int expirationDays;

    private String imageUrl;
    private boolean active = true;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer categoryId;

    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Integer brandId;
}
