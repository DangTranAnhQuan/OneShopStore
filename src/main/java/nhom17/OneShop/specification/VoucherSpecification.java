package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class VoucherSpecification {

    public static Specification<Voucher> filterByCriteria(String keyword, VoucherStatus status, DiscountType type) {
        return (root, query, criteriaBuilder) -> {
            Specification<Voucher> spec = (root1, query1, cb) -> cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                spec = spec.and((r, q, cb) -> cb.like(r.get("voucherCode"), "%" + keyword + "%"));
            }
            if (status != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), status));
            }
            if (type != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("discountType"), type));
            }
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
