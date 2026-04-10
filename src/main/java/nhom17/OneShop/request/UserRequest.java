package nhom17.OneShop.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import nhom17.OneShop.entity.enums.UserStatus;
import org.springframework.util.StringUtils;

@Data
public class UserRequest {
    private Integer userId;

    @NotBlank(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 100, message = "Tên đăng nhập phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ chứa chữ cái, số và dấu gạch dưới")
    private String username;

    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
    private String fullName;

    @Pattern(regexp = "^((0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})|1[89]00(\\s|\\.)?\\d{4,6})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Trạng thái không được để trống")
    private UserStatus status;

    private String avatarUrl;

    @NotNull(message = "Vui lòng chọn vai trò")
    private Integer roleId;

    private Integer membershipTierId;

    @AssertTrue(message = "Mật khẩu phải có ít nhất 6 ký tự")
    public boolean isPasswordValid() {
        if (userId != null && !StringUtils.hasText(password)) {
            return true;
        }

        if (userId == null && !StringUtils.hasText(password)) {
            return false;
        }

        if (StringUtils.hasText(password) && password.length() < 6) {
            return false;
        }
        return true;
    }
}
