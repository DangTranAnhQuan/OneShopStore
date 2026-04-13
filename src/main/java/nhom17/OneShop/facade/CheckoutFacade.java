package nhom17.OneShop.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.repository.AddressRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.repository.VoucherRepository;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * FACADE PATTERN: Cung cấp giao diện đơn giản cho CheckoutController.
 * Bao bọc và ẩn giấu sự phức tạp của việc gọi nhiều Service/Repository/Util khác nhau.
 */
@Service
public class CheckoutFacade {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private CookieUtil cookieUtil;
    @Autowired private SecurityContextUtil securityContextUtil;

    // Đóng gói toàn bộ logic chuẩn bị dữ liệu cho trang Checkout
    public boolean prepareCheckoutPageData(Model model, HttpServletRequest request, HttpServletResponse response) {
        // 1. Xử lý logic hủy đơn hàng online cũ nếu người dùng back lại
        handlePendingOnlineOrder(request, response, model);

        // 2. Lấy giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return false; // Báo cho Controller biết để redirect về /cart
        }

        // 3. Lấy thông tin user và địa chỉ
        User currentUser = securityContextUtil.getCurrentUser();
        List<Address> addresses = addressRepository.findByUser_UserIdAndIsActiveTrue(currentUser.getUserId());

        // 4. Tính toán tiền nong (Subtotal, Membership, Coupon)
        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal membershipDiscount = calculateMembershipDiscount(currentUser, subtotal, model);
        BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);
        
        String appliedCouponCode = cookieUtil.readCookie(request, "appliedCouponCode");
        BigDecimal actualCouponDiscount = calculateCouponDiscount(appliedCouponCode, priceAfterMembershipDiscount);
        
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal total = priceAfterMembershipDiscount.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        // 5. Đổ dữ liệu ra Model
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("couponDiscount", actualCouponDiscount);
        model.addAttribute("appliedCouponCode", appliedCouponCode);
        model.addAttribute("membershipDiscount", membershipDiscount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);

        return true; // Cho phép hiển thị trang checkout
    }

    private void handlePendingOnlineOrder(HttpServletRequest request, HttpServletResponse response, Model model) {
        String pendingOrderIdStr = cookieUtil.readCookie(request, "pendingOnlineOrderId");
        if (pendingOrderIdStr != null) {
            cookieUtil.deleteCookie(response, "pendingOnlineOrderId");
            try {
                Long pendingOrderId = Long.parseLong(pendingOrderIdStr);
                User currentUser = securityContextUtil.getCurrentUser();
                orderService.cancelOrderIfPendingOnline(pendingOrderId, currentUser);
                model.addAttribute("infoMessage", "Đơn hàng thanh toán online trước đó (#"+ pendingOrderId +") đã được hủy do bạn quay lại.");
            } catch (Exception e) {
                // Log exception silently
            }
        }
    }

    private BigDecimal calculateMembershipDiscount(User currentUser, BigDecimal subtotal, Model model) {
        Optional<User> userOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userOpt.isPresent() && userOpt.get().getMembershipTier() != null) {
            BigDecimal percent = userOpt.get().getMembershipTier().getDiscountPercentage();
            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                model.addAttribute("membershipTier", userOpt.get().getMembershipTier());
                return subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateCouponDiscount(String couponCode, BigDecimal baseAmount) {
        if (couponCode == null || couponCode.isBlank()) return BigDecimal.ZERO;

        Optional<Voucher> voucherOpt = voucherRepository.findByVoucherCodeAndStatus(couponCode, VoucherStatus.ACTIVE);
        if (voucherOpt.isEmpty()) return BigDecimal.ZERO;

        Voucher voucher = voucherOpt.get();
        boolean isValidTime = voucher.getStartsAt().isBefore(java.time.LocalDateTime.now()) && voucher.getEndsAt().isAfter(java.time.LocalDateTime.now());
        boolean isEligibleAmount = voucher.getMinimumOrderAmount() == null || baseAmount.compareTo(voucher.getMinimumOrderAmount()) >= 0;
        
        if (!isValidTime || !isEligibleAmount) return BigDecimal.ZERO;

        BigDecimal discountAmount;
        if (DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
            discountAmount = baseAmount.multiply(voucher.getValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else {
            discountAmount = voucher.getValue();
        }
        return discountAmount.min(baseAmount).max(BigDecimal.ZERO);
    }
}