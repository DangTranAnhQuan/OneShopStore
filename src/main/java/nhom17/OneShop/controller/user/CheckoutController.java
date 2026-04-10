package nhom17.OneShop.controller.user;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.dto.ShippingOptionDTO;
import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.entity.enums.VoucherStatus;

import nhom17.OneShop.repository.AddressRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.repository.VoucherRepository;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.CheckoutService;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.ShippingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CheckoutController {
    @Autowired private CartService cartService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShippingFeeService shippingFeeService;
    @Autowired private OrderService orderService;
    @Autowired private CookieUtil cookieUtil;
    @Autowired private VoucherRepository voucherRepository;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        String pendingOrderIdStr = cookieUtil.readCookie(request, "pendingOnlineOrderId");
        if (pendingOrderIdStr != null) {
            System.out.println("CheckoutController: Tìm thấy pendingOnlineOrderId trong cookie: " + pendingOrderIdStr);

            // Xóa cookie ngay lập tức
            cookieUtil.deleteCookie(response, "pendingOnlineOrderId");

            try {
                Long pendingOrderId = Long.parseLong(pendingOrderIdStr);
                User currentUser = getCurrentUser();
                orderService.cancelOrderIfPendingOnline(pendingOrderId, currentUser);
                model.addAttribute("infoMessage", "Đơn hàng thanh toán online trước đó (#"+ pendingOrderId +") đã được hủy do bạn quay lại.");
            } catch (IllegalStateException e) {
                System.err.println("CheckoutController: Lỗi khi hủy đơn hàng online chờ (chưa đăng nhập?): " + e.getMessage());
            } catch (Exception e) {
                System.err.println("CheckoutController: Lỗi khi hủy đơn hàng online chờ #" + pendingOrderIdStr + ": " + e.getMessage());
            }
        }
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        User currentUser = getCurrentUser();
        List<Address> addresses = addressRepository.findByUser_UserIdAndIsActiveTrue(currentUser.getUserId());

        BigDecimal subtotal = cartService.getSubtotal();

        BigDecimal membershipDiscount = BigDecimal.ZERO;
        Optional<User> userOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userOpt.isPresent() && userOpt.get().getMembershipTier() != null) {
            BigDecimal percent = userOpt.get().getMembershipTier().getDiscountPercentage();
            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                membershipDiscount = subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                model.addAttribute("membershipTier", userOpt.get().getMembershipTier());
            }
        }
        BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);
        String appliedCouponCode = cookieUtil.readCookie(request, "appliedCouponCode");
        BigDecimal actualCouponDiscount = calculateCouponDiscount(appliedCouponCode, priceAfterMembershipDiscount);


        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal total = priceAfterMembershipDiscount.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("couponDiscount", actualCouponDiscount);
        model.addAttribute("appliedCouponCode", appliedCouponCode);
        model.addAttribute("membershipDiscount", membershipDiscount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);

        return "user/shop/checkout";
    }

    @GetMapping("/api/available-shipping-options")
    @ResponseBody
    public ResponseEntity<?> getAvailableShippingOptions(@RequestParam("province") String province) {
        try {
            List<ShippingOptionDTO> options = shippingFeeService.findAvailableShippingOptions(province);

            if (options != null && !options.isEmpty()) {
                return ResponseEntity.ok(options);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy phương thức vận chuyển phù hợp cho tỉnh/thành này.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy phương thức vận chuyển: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("shipping_address") Integer addressId,
                             @RequestParam("payment_method") String paymentMethod,
                             @RequestParam("calculated_shipping_fee") BigDecimal shippingFee,
                             @RequestParam("shipping_method_name") String shippingMethodName,
                             @RequestParam(value = "notes", required = false) String note,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        try {
            if (shippingMethodName == null || shippingMethodName.isBlank()) {
                throw new IllegalArgumentException("Chưa chọn được phương thức vận chuyển hợp lệ. Vui lòng chọn lại địa chỉ.");
            }
            if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Phí vận chuyển không hợp lệ.");
            }

            String couponCode = cookieUtil.readCookie(request, "appliedCouponCode");

            ShippingMethod shippingMethod = ShippingMethod.fromValue(shippingMethodName);
            PaymentMethod parsedPaymentMethod = PaymentMethod.fromValue(paymentMethod);
            Order order = checkoutService.placeOrder(addressId, parsedPaymentMethod, shippingFee, shippingMethod, couponCode, note);

            cookieUtil.deleteCookie(response, "appliedCouponCode");
            cookieUtil.deleteCookie(response, "cartDiscount");

            if (PaymentMethod.COD.equals(parsedPaymentMethod)) {
                return "redirect:/order-success?method=COD";
            }
            if (PaymentMethod.VN_PAY.equals(parsedPaymentMethod)) {
                return "redirect:/thanh-toan/qr?orderId=" + order.getOrderId();
            }

            redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ.");
            return "redirect:/checkout";
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đặt hàng: " + errorMsg);

            if (errorMsg != null && (errorMsg.contains("khuyến mãi") || errorMsg.contains("hết lượt"))) {
                cookieUtil.deleteCookie(response, "appliedCouponCode");
                cookieUtil.deleteCookie(response, "cartDiscount");
            }

            e.printStackTrace();
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam(value = "method", required = false) String method, Model model) {
        model.addAttribute("method", method);
        return "user/shop/order-success";
    }

    @GetMapping("/checkout/edit-address/{id}")
    public String showEditAddressForm(@PathVariable("id") Integer addressId, Model model) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại."));
        model.addAttribute("address", address);
        return "user/shop/edit-address";
    }

    @PostMapping("/checkout/save-address")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "return", required = false) String returnUrl,
                              RedirectAttributes ra) {
        try {
            User currentUser = getCurrentUser();
            address.assignUser(currentUser);
            addressRepository.save(address);
            ra.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }


        if (returnUrl != null && !returnUrl.isBlank()) {
            if (returnUrl.startsWith("/")) {
                return "redirect:" + returnUrl;
            }
            return "redirect:/my-account?tab=addresses";
        }
        return "redirect:/checkout";
    }

    @GetMapping("/checkout/momo")
    public String momoGuide() { return "user/shop/momo-guide"; }

    @GetMapping("/checkout/vnpay")
    public String vnpayGuide() { return "user/shop/vnpay-guide"; }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại trong CSDL."));
    }

    private BigDecimal calculateCouponDiscount(String couponCode, BigDecimal baseAmount) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByVoucherCodeAndStatus(couponCode, VoucherStatus.ACTIVE);
        if (voucherOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherOpt.get();
        boolean isValidTime = voucher.getStartsAt().isBefore(java.time.LocalDateTime.now())
                && voucher.getEndsAt().isAfter(java.time.LocalDateTime.now());
        boolean isEligibleAmount = voucher.getMinimumOrderAmount() == null
                || baseAmount.compareTo(voucher.getMinimumOrderAmount()) >= 0;
        if (!isValidTime || !isEligibleAmount) {
            return BigDecimal.ZERO;
        }

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
