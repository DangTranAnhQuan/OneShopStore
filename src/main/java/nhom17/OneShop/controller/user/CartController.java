package nhom17.OneShop.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.dto.CartItemDTO;
import nhom17.OneShop.entity.User; // Import User
import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.repository.UserRepository; // Import UserRepository
import nhom17.OneShop.repository.VoucherRepository;
import nhom17.OneShop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CookieUtil cookieUtil;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpServletRequest request, HttpServletResponse response) {
        List<CartItem> cartItems = cartService.getCartItems();

        BigDecimal subtotal = cartService.getSubtotal();

        User currentUser = null; // Khởi tạo là null
        try {
            currentUser = getCurrentUser(); // Lấy User từ phương thức helper của Controller
        } catch (IllegalStateException e) {
            // Người dùng chưa đăng nhập, bỏ qua tính giảm giá thành viên
            System.out.println("User not logged in, skipping membership discount.");
        }

        BigDecimal membershipDiscount = calculateMembershipDiscount(subtotal, currentUser, model);


        BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount);
        if (priceAfterMembershipDiscount.compareTo(BigDecimal.ZERO) < 0) {
            priceAfterMembershipDiscount = BigDecimal.ZERO;
        }

        CouponSummary couponSummary = resolveCouponDiscount(request, response, priceAfterMembershipDiscount);
        BigDecimal actualCouponDiscount = couponSummary.discountAmount;

        BigDecimal total = priceAfterMembershipDiscount.subtract(actualCouponDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("membershipDiscount", membershipDiscount);
        model.addAttribute("discount", actualCouponDiscount);
        model.addAttribute("total", total);
        model.addAttribute("appliedCouponCode", couponSummary.appliedCouponCode);

        return "user/shop/cart";
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam("productId") Integer productId,
                                       @RequestParam(name="quantity", defaultValue="1") int quantity,
                                       @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {

        if (principal == null) { // Kiểm tra đăng nhập
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm sản phẩm.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/sign-in")).build();
            }
        }

        try {
            cartService.addToCart(productId, quantity); // Thực hiện thêm vào giỏ
            List<CartItem> updatedCartEntities = cartService.getCartItems(); // Lấy lại danh sách entity

            // **Chuyển đổi List<Cart> thành List<CartItemDTO>**
            List<CartItemDTO> updatedCartDTOs = updatedCartEntities.stream()
                    .map(CartItemDTO::fromEntity)
                    .collect(Collectors.toList());
            // Tính tổng số lượng item (hoặc tổng số lượng sản phẩm tùy bạn muốn)
            int newCount = updatedCartDTOs.size(); // Số loại sản phẩm
            // int totalQuantity = updatedCartDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum(); // Tổng số lượng

            // Nếu là AJAX, trả về DTO
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Đã thêm sản phẩm vào giỏ hàng!");
                response.put("cartCount", newCount); // Gửi số lượng item
                response.put("cartItems", updatedCartDTOs); // **Gửi danh sách DTO**
                return ResponseEntity.ok(response);
            } else {
                // Nếu không phải AJAX, chuyển hướng như cũ
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/cart")).build();
            }
        } catch (RuntimeException e) { // Xử lý lỗi
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                String referer = "/product/" + productId;
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(referer)).build();
            }
        }
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("productId") Integer productId, @RequestParam("quantity") int quantity, RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
        try {
            cartService.updateQuantity(productId, quantity);
        } catch (Exception e) {
            // Gửi thông báo lỗi ra view
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update-ajax")
    public ResponseEntity<?> updateCartItemAjax(@RequestParam("productId") Integer productId,
                                                @RequestParam("quantity") int quantity,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        try {
            cartService.updateQuantity(productId, quantity);

            BigDecimal subtotal = cartService.getSubtotal();
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (IllegalStateException ignored) {
            }

            BigDecimal membershipDiscount = calculateMembershipDiscount(subtotal, currentUser, null);
            BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);
            CouponSummary couponSummary = resolveCouponDiscount(request, response, priceAfterMembershipDiscount);

            BigDecimal total = priceAfterMembershipDiscount.subtract(couponSummary.discountAmount).max(BigDecimal.ZERO);
            BigDecimal lineTotal = cartService.getCartItems().stream()
                    .filter(item -> item.getProduct().getProductId().equals(productId))
                    .findFirst()
                    .map(CartItem::getLineTotal)
                    .orElse(BigDecimal.ZERO);

            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("productId", productId);
            payload.put("lineTotal", lineTotal);
            payload.put("subtotal", subtotal);
            payload.put("membershipDiscount", membershipDiscount);
            payload.put("discount", couponSummary.discountAmount);
            payload.put("total", total);
            payload.put("appliedCouponCode", couponSummary.appliedCouponCode);
            payload.put("couponRemoved", couponSummary.couponRemoved);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", false);
            payload.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(payload);
        }
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("productId") Integer productId, RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
        try {
            cartService.removeItem(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng."); // Thêm thông báo thành công
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage()); // Thêm thông báo lỗi
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam("coupon_code") String couponCode, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        Optional<Voucher> couponOpt = voucherRepository.findByVoucherCodeAndStatus(couponCode, VoucherStatus.ACTIVE);

        BigDecimal subtotalAfterMembership = BigDecimal.ZERO;
        BigDecimal originalSubtotal = cartService.getSubtotal();
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        User currentUser = null;
        try { currentUser = getCurrentUser(); } catch (IllegalStateException e) {  }
        if (currentUser != null) {
            Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
            if (userWithTierOpt.isPresent() && userWithTierOpt.get().getMembershipTier() != null) {
                BigDecimal percent = userWithTierOpt.get().getMembershipTier().getDiscountPercentage();
                if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                    membershipDiscount = originalSubtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                }
            }
        }
        subtotalAfterMembership = originalSubtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);


        if (couponOpt.isPresent()
                && couponOpt.get().getStartsAt().isBefore(LocalDateTime.now())
                && couponOpt.get().getEndsAt().isAfter(LocalDateTime.now())
                && (couponOpt.get().getMinimumOrderAmount() == null || subtotalAfterMembership.compareTo(couponOpt.get().getMinimumOrderAmount()) >= 0)
        )
        {
            Voucher coupon = couponOpt.get();
            BigDecimal discountAmount = BigDecimal.ZERO;

            if (DiscountType.PERCENTAGE.equals(coupon.getDiscountType())) {
                discountAmount = subtotalAfterMembership.multiply(coupon.getValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                // Kiểm tra giới hạn giảm tối đa
                if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discountAmount = coupon.getMaxDiscountAmount();
                }
            } else if (DiscountType.FIXED_AMOUNT.equals(coupon.getDiscountType())) {
                discountAmount = coupon.getValue();
            }

            discountAmount = discountAmount.min(subtotalAfterMembership);
            int expiryInSeconds = 60 * 60;
            cookieUtil.createCookie(response, "appliedCouponCode", coupon.getVoucherCode(), expiryInSeconds);
            cookieUtil.deleteCookie(response, "cartDiscount");

            redirectAttributes.addFlashAttribute("success", "Áp dụng mã giảm giá thành công!");
        } else {
            cookieUtil.deleteCookie(response, "cartDiscount");
            cookieUtil.deleteCookie(response, "appliedCouponCode");

            redirectAttributes.addFlashAttribute("error", "Mã giảm giá không hợp lệ, hết hạn hoặc không đủ điều kiện.");
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove-coupon")
    public String removeCoupon(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        cookieUtil.deleteCookie(response, "cartDiscount");
        cookieUtil.deleteCookie(response, "appliedCouponCode");
        redirectAttributes.addFlashAttribute("success", "Đã gỡ mã giảm giá thành công.");
        return "redirect:/cart";
    }

    private BigDecimal calculateMembershipDiscount(BigDecimal subtotal, User currentUser, Model model) {
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userWithTierOpt.isEmpty() || userWithTierOpt.get().getMembershipTier() == null) {
            return BigDecimal.ZERO;
        }

        User userWithTier = userWithTierOpt.get();
        BigDecimal percent = userWithTier.getMembershipTier().getDiscountPercentage();
        if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (model != null) {
            model.addAttribute("membershipTier", userWithTier.getMembershipTier());
        }
        return subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
    }

    private CouponSummary resolveCouponDiscount(HttpServletRequest request,
                                                HttpServletResponse response,
                                                BigDecimal subtotalAfterMembership) {
        String appliedCouponCode = cookieUtil.readCookie(request, "appliedCouponCode");
        if (appliedCouponCode == null || appliedCouponCode.isBlank()) {
            return new CouponSummary(BigDecimal.ZERO, null, false);
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByVoucherCodeAndStatus(appliedCouponCode, VoucherStatus.ACTIVE);
        if (voucherOpt.isEmpty()) {
            cookieUtil.deleteCookie(response, "appliedCouponCode");
            cookieUtil.deleteCookie(response, "cartDiscount");
            return new CouponSummary(BigDecimal.ZERO, null, true);
        }

        Voucher voucher = voucherOpt.get();
        boolean isValidTime = voucher.getStartsAt().isBefore(LocalDateTime.now()) && voucher.getEndsAt().isAfter(LocalDateTime.now());
        boolean isEligibleAmount = voucher.getMinimumOrderAmount() == null
                || subtotalAfterMembership.compareTo(voucher.getMinimumOrderAmount()) >= 0;
        if (!isValidTime || !isEligibleAmount) {
            cookieUtil.deleteCookie(response, "appliedCouponCode");
            cookieUtil.deleteCookie(response, "cartDiscount");
            return new CouponSummary(BigDecimal.ZERO, null, true);
        }

        return new CouponSummary(calculateVoucherDiscount(voucher, subtotalAfterMembership), voucher.getVoucherCode(), false);
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal baseAmount) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
            discountAmount = baseAmount.multiply(voucher.getValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if (DiscountType.FIXED_AMOUNT.equals(voucher.getDiscountType())) {
            discountAmount = voucher.getValue();
        }
        return discountAmount.min(baseAmount).max(BigDecimal.ZERO);
    }

    private static class CouponSummary {
        private final BigDecimal discountAmount;
        private final String appliedCouponCode;
        private final boolean couponRemoved;

        private CouponSummary(BigDecimal discountAmount, String appliedCouponCode, boolean couponRemoved) {
            this.discountAmount = discountAmount;
            this.appliedCouponCode = appliedCouponCode;
            this.couponRemoved = couponRemoved;
        }
    }

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
}