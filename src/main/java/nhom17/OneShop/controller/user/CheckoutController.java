package nhom17.OneShop.controller.user;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.dto.ShippingOptionDTO;
import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.AddressRepository;
import nhom17.OneShop.service.CheckoutService;
import nhom17.OneShop.service.ShippingFeeService;
import nhom17.OneShop.facade.CheckoutFacade;
import nhom17.OneShop.request.OrderRequest;
import nhom17.OneShop.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CheckoutController {

    @Autowired private CheckoutService checkoutService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private ShippingFeeService shippingFeeService;
    @Autowired private CookieUtil cookieUtil;
    @Autowired private SecurityContextUtil securityContextUtil;
    
    // TIÊM FACADE VÀO ĐÂY
    @Autowired private CheckoutFacade checkoutFacade;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        // Mọi nghiệp vụ lấy giỏ hàng, tính giảm giá, đọc cookie... đã được đẩy vào Facade
        boolean canCheckout = checkoutFacade.prepareCheckoutPageData(model, request, response);
        
        if (!canCheckout) {
            return "redirect:/cart";
        }
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/place-order")
    public String placeOrder(@ModelAttribute OrderRequest request,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest httpRequest,
                             HttpServletResponse response) {
        try {
            if (request.getShippingMethod() == null || request.getShippingMethod().isBlank()) throw new IllegalArgumentException("Chưa chọn được phương thức vận chuyển hợp lệ.");
            if (request.getShippingFee() == null || request.getShippingFee().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Phí vận chuyển không hợp lệ.");

            request.setAppliedCouponCode(cookieUtil.readCookie(httpRequest, "appliedCouponCode"));
            User currentUser = securityContextUtil.getCurrentUser();

            Order order = checkoutService.placeOrder(request, currentUser);

            cookieUtil.deleteCookie(response, "appliedCouponCode");
            cookieUtil.deleteCookie(response, "cartDiscount");

            if ("COD".equalsIgnoreCase(request.getPaymentMethod())) return "redirect:/order-success?method=COD";
            if ("VN_PAY".equalsIgnoreCase(request.getPaymentMethod())) return "redirect:/thanh-toan/qr?orderId=" + order.getOrderId();

            redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ.");
            return "redirect:/checkout";
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đặt hàng: " + errorMsg);
            if (errorMsg != null && (errorMsg.contains("khuyến mãi") || errorMsg.contains("hết lượt"))) {
                cookieUtil.deleteCookie(response, "appliedCouponCode");
                cookieUtil.deleteCookie(response, "cartDiscount");
            }
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
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại."));
        model.addAttribute("address", address);
        return "user/shop/edit-address";
    }

    @PostMapping("/checkout/save-address")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "return", required = false) String returnUrl,
                              RedirectAttributes ra) {
        try {
            User currentUser = securityContextUtil.getCurrentUser(); // Sử dụng Singleton Utility
            address.assignUser(currentUser);
            addressRepository.save(address);
            ra.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            if (returnUrl.startsWith("/")) return "redirect:" + returnUrl;
            return "redirect:/my-account?tab=addresses";
        }
        return "redirect:/checkout";
    }

    @GetMapping("/checkout/momo")
    public String momoGuide() { return "user/shop/momo-guide"; }

    @GetMapping("/checkout/vnpay")
    public String vnpayGuide() { return "user/shop/vnpay-guide"; }
}