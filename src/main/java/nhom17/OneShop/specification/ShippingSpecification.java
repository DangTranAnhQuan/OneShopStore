package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Shipping;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.entity.enums.ShippingStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class ShippingSpecification {
    public static Specification<Shipping> filterBy(String keyword, Integer carrierId, String status, String shippingMethod) {
        return (root, query, cb) -> {
            Specification<Shipping> spec = (r, q, builder) -> builder.conjunction();

            if (StringUtils.hasText(keyword)) {
                try {
                    // Cố gắng chuyển đổi từ khóa thành số (cho Mã Đơn hàng)
                    Long orderId = Long.parseLong(keyword);
                    // Nếu thành công, tìm theo Mã ĐH HOẶC Mã vận đơn
                    spec = spec.and((r, q, builder) ->
                            builder.or(
                                    cb.equal(r.get("order").get("orderId"), orderId),
                                    cb.like(r.get("trackingCode"), "%" + keyword + "%")
                            )
                    );
                } catch (NumberFormatException e) {
                    // Nếu không phải là số, chỉ tìm theo Mã vận đơn
                    spec = spec.and((r, q, builder) -> cb.like(r.get("trackingCode"), "%" + keyword + "%"));
                }
            }

            if (carrierId != null) {
                spec = spec.and((r, q, builder) -> builder.equal(r.get("carrier").get("carrierId"), carrierId));
            }

            if (StringUtils.hasText(status)) {
                try {
                    ShippingStatus statusValue = ShippingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
                    spec = spec.and((r, q, builder) -> builder.equal(r.get("status"), statusValue));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status filter value: ignore this criterion.
                }
            }

            if (StringUtils.hasText(shippingMethod)) {
                try {
                    ShippingMethod method = ShippingMethod.fromValue(shippingMethod);
                    spec = spec.and((r, q, builder) -> builder.equal(r.get("order").get("shippingMethod"), method));
                } catch (IllegalArgumentException ignored) {
                    // Invalid filter value: keep current spec without adding shipping method condition.
                }
            }

            return spec.toPredicate(root, query, cb);
        };
    }
}
