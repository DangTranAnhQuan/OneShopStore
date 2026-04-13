package nhom17.OneShop.controller.user;

import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.CookieUtil;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.dto.SepayWebhookDTO;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nhom17.OneShop.dto.adapter.IPaymentWebhookAdapter;
import nhom17.OneShop.dto.adapter.SepayWebhookAdapter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class PaymentController {
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService orderService;
    @Autowired private CookieUtil cookieUtil;

    @Value("${shop.sepay.bank-code}")
    private String SHOP_BANK_CODE;

    @Value("${shop.sepay.account-no}")
    private String SHOP_ACCOUNT_NO;

    @Value("${shop.sepay.account-name}")
    private String SHOP_ACCOUNT_NAME;

    @PostMapping("/payment/ipn/sepay")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(@RequestBody SepayWebhookDTO webhookData) {
        System.out.println("===== ĐÃ NHẬN ĐƯỢC WEBHOOK TỪ SEPAY =====");

        try {
            IPaymentWebhookAdapter adapter = new SepayWebhookAdapter(webhookData);

            if (!adapter.isValid()) {
                throw new IllegalArgumentException("Webhook data is invalid or missing fields");
            }

            if (!adapter.isIncomingTransaction()) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Not an incoming transaction, skipped."));
            }

            String orderIdString = adapter.extractOrderId();
            if (orderIdString == null) {
                throw new IllegalArgumentException("Không tìm thấy mã đơn hàng hợp lệ trong nội dung thanh toán.");
            }

            orderService.processIpnPayment(adapter);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/payment/status/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable("orderId") Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng."));

            return ResponseEntity.ok(Map.of("payment_status", String.valueOf(order.getPaymentStatus())));

        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("payment_status", "order_not_found"));
        }
    }

    //HÀM MỚI: Hiển thị trang thanh toán VietQR
    @GetMapping("/thanh-toan/qr")
    public String showVietQRPage(@RequestParam("orderId") Long orderId, Model model, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng " + orderId));

            // Kiểm tra xem đơn hàng đã thanh toán chưa
            if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("info", "Đơn hàng này đã được thanh toán.");
                return "redirect:/order-details/" + orderId;
            }

            // Chỉ cho phép thanh toán QR nếu PTTT là VIETQR và Chưa thanh toán
            if (!PaymentMethod.VN_PAY.equals(order.getPaymentMethod()) || !PaymentStatus.UNPAID.equals(order.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Đơn hàng này không hợp lệ để thanh toán QR.");
                return "redirect:/order-details/" + orderId;
            }

            String paymentMemo = "SEVQR DH" + order.getOrderId();

            model.addAttribute("order", order);
            model.addAttribute("shopBankCode", SHOP_BANK_CODE);
            model.addAttribute("shopAccountNo", SHOP_ACCOUNT_NO);
            model.addAttribute("shopAccountName", SHOP_ACCOUNT_NAME);
            model.addAttribute("paymentMemo", paymentMemo);

            int expiryInMinutes = 30 * 60;
            cookieUtil.createCookie(response, "pendingOnlineOrderId", orderId.toString(), expiryInMinutes);

            System.out.println("PaymentController: Lưu pendingOnlineOrderId vào cookie: " + orderId);
            return "user/shop/payment-vietqr";

        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/my-orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi tạo mã QR.");
            return "redirect:/checkout";
        }
    }
}