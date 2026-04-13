package nhom17.OneShop.entity.enums;

public enum OtpPurpose {
    SIGN_UP("Đăng ký"),
    RESET_PASSWORD("Quên mật khẩu");

    private final String value;

    OtpPurpose(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

