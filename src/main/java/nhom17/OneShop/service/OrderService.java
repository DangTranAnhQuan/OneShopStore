package nhom17.OneShop.service;

import nhom17.OneShop.dto.DashboardDataDTO;
import nhom17.OneShop.dto.adapter.IPaymentWebhookAdapter;
import nhom17.OneShop.entity.*;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.request.OrderUpdateRequest;
import nhom17.OneShop.request.OrderRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Page<Order> findOrdersForCurrentUser(int page, int size);

    Order findOrderByIdForCurrentUser(Long orderId);

    //    Admin
    Page<Order> findAll(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod, int page, int size);

    List<OrderStatusHistory> findHistoryByOrderId(long orderId);

    Order findById(long id);

    Map<Long, List<ShippingFee>> getCarriersWithFeesByOrder(List<Order> orders);

    void updateLoyaltyPoints(Order order, OrderStatus oldStatus, OrderStatus newStatus);

    void update(Long orderId, OrderUpdateRequest request);

    void cancelOrder(Long orderId, User currentUser);

    void cancelOrderIfPendingOnline(Long orderId, User currentUser);

    DashboardDataDTO getDashboardData(int year, int month);

    boolean hasCompletedPurchase(Integer userId, Integer productId);

    boolean canUserReviewProduct(Integer userId, Integer productId);

    BigDecimal calculateMembershipDiscount(User user, BigDecimal subtotal);

    Voucher resolveApplicableVoucher(String couponCode, User user, BigDecimal baseAmount);

    BigDecimal calculateCouponDiscount(Voucher voucher, BigDecimal baseAmount);

    Order createAndSaveOrderForCheckout(User user,
                                        Address shippingAddress,
                                        List<CartItem> cartItems,
                                        OrderRequest request);
}
