package nhom17.OneShop.dto;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherApiDTO(
        String voucherCode,
        String campaignName,
        DiscountType discountType,
        String discountTypeLabel,
        VoucherStatus status,
        String statusLabel,
        BigDecimal value,
        LocalDateTime endsAt,
        BigDecimal minimumOrderAmount,
        BigDecimal maxDiscountAmount
) {
    public static VoucherApiDTO fromEntity(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        return new VoucherApiDTO(
                voucher.getVoucherCode(),
                voucher.getCampaignName(),
                voucher.getDiscountType(),
                voucher.getDiscountType() == null ? null : voucher.getDiscountType().getLabel(),
                voucher.getStatus(),
                voucher.getStatus() == null ? null : voucher.getStatus().getLabel(),
                voucher.getValue(),
                voucher.getEndsAt(),
                voucher.getMinimumOrderAmount(),
                voucher.getMaxDiscountAmount()
        );
    }
}

