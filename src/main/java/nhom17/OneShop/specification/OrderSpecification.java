package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.entity.enums.ShippingMethod;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class OrderSpecification {
    public static Specification<Order> filterOrders(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod) {
        return (root, query, criteriaBuilder) -> {
            Specification<Order> spec = (r, q, cb) -> cb.conjunction();

            // Lọc theo Mã đơn hàng (keyword)
            if (StringUtils.hasText(keyword)) {
                try {
                    Long orderId = Long.parseLong(keyword);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("orderId"), orderId));
                } catch (NumberFormatException e) {
                    // Nếu người dùng nhập chữ, ta có thể trả về 1 điều kiện luôn sai để không có kết quả nào
                    spec = spec.and((r, q, cb) -> cb.disjunction());
                }
            }

            // Lọc theo Trạng thái đơn hàng
            if (StringUtils.hasText(status)) {
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("orderStatus"), orderStatus));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status filter value: ignore this criterion.
                }
            }

            // Lọc theo Phương thức thanh toán
            if (StringUtils.hasText(paymentMethod)) {
                try {
                    PaymentMethod method = PaymentMethod.fromValue(paymentMethod);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("paymentMethod"), method));
                } catch (IllegalArgumentException ignored) {
                    // Invalid payment method filter value: ignore this criterion.
                }
            }

            // Lọc theo Trạng thái thanh toán
            if (StringUtils.hasText(paymentStatus)) {
                try {
                    PaymentStatus statusValue = PaymentStatus.valueOf(paymentStatus.trim().toUpperCase(Locale.ROOT));
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("paymentStatus"), statusValue));
                } catch (IllegalArgumentException ignored) {
                    // Invalid payment status filter value: ignore this criterion.
                }
            }

            if (StringUtils.hasText(shippingMethod)) {
                try {
                    ShippingMethod method = ShippingMethod.fromValue(shippingMethod);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("shippingMethod"), method));
                } catch (IllegalArgumentException ignored) {
                    // Invalid shipping method filter value: ignore this criterion.
                }
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
