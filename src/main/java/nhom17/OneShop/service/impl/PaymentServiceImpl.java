package nhom17.OneShop.service.impl;

import jakarta.servlet.http.HttpSession;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.OrderRepository;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.PaymentService;
import nhom17.OneShop.dto.adapter.IPaymentWebhookAdapter;
import nhom17.OneShop.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private HttpSession httpSession;

    @Override
    public Order getOrderForQrPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng " + orderId));
        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Đơn hàng này đã được thanh toán.");
        }
        if (!PaymentMethod.VN_PAY.equals(order.getPaymentMethod())
                || !PaymentStatus.UNPAID.equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Đơn hàng này không hợp lệ để thanh toán QR.");
        }
        return order;
    }

    @Override
    public String generatePaymentMemo(Long orderId) {
        return "SEVQR DH" + orderId;
    }

    @Override
    @Transactional
    public void processIpnPayment(IPaymentWebhookAdapter adapter) {
        String orderIdStr = adapter.extractOrderId();
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Webhook [" + adapter.getGatewayName() + "]: Order ID cannot be extracted from payload.");
        }
        Long orderId;
        try {
            orderId = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Webhook [" + adapter.getGatewayName() + "]: Invalid Order ID format: " + orderIdStr);
        }
        BigDecimal amountPaid = adapter.extractAmount();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Webhook: Không tìm thấy đơn hàng #" + orderId));

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            System.out.println("Webhook: Đơn hàng #" + orderId + " đã được thanh toán trước đó. Bỏ qua.");
            return;
        }
        order.confirmPayment(amountPaid);
        orderRepository.save(order);
        System.out.println("Webhook: Đã cập nhật thanh toán thành công cho đơn hàng #" + orderId);
        try {
            User orderUser = order.getUser();
            if (orderUser != null && orderUser.getUserId() != null) {
                cartService.clearCartByUserId(orderUser.getUserId());
                System.out.println("Webhook: Đã dọn giỏ hàng trực tiếp cho đơn hàng #" + orderId);

                Long pendingOrderIdInSession = (Long) httpSession.getAttribute("pendingOnlineOrderId");
                if (pendingOrderIdInSession != null && pendingOrderIdInSession.equals(orderId)) {
                    httpSession.removeAttribute("pendingOnlineOrderId");
                    System.out.println("Webhook: Đã xóa pendingOnlineOrderId khỏi session cho đơn hàng #" + orderId);
                } else {
                    System.out.println(
                            "Webhook: Không tìm thấy hoặc không khớp pendingOnlineOrderId trong session cho đơn hàng #"
                                    + orderId);
                }
            } else {
                System.err.println(
                        "Webhook Warning: Không tìm thấy người dùng cho đơn hàng #" + orderId + " để dọn giỏ hàng.");
            }
        } catch (Exception e) {
            System.err.println("Webhook Error: Lỗi khi dọn giỏ hàng cho đơn hàng #" + orderId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
