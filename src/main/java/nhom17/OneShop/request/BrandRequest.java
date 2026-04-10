package nhom17.OneShop.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {
    private Integer brandId;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 150, message = "Tên thương hiệu không được vượt quá 150 ký tự")
    private String brandName;
    private String imageUrl;
    private String description;
    private boolean active;
}
