package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private VoucherRepository voucherRepository;


    @Override
    @Transactional
    public Order placeOrder(Integer addressId,
                            PaymentMethod paymentMethod,
                            BigDecimal shippingFee,
                            ShippingMethod shippingMethod,
                            String appliedCouponCode,
                            String note) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập."));
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }
        Address shippingAddress = addressRepository.findById(addressId)
                .filter(addr -> Objects.equals(addr.getUser().getUserId(), currentUser.getUserId()))
                .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ."));

        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userWithTierOpt.isPresent() && userWithTierOpt.get().getMembershipTier() != null) {
            BigDecimal percent = userWithTierOpt.get().getMembershipTier().getDiscountPercentage();
            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                membershipDiscount = subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            }
        }
        BigDecimal priceAfterMembership = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);

        BigDecimal actualCouponDiscount = BigDecimal.ZERO;
        Voucher appliedVoucher = null; // Khởi tạo voucher áp dụng là null

        // Biến để lưu thông báo lỗi nếu có
        String voucherErrorMessage = null;

        if (appliedCouponCode != null) {
            Optional<Voucher> voucherOpt = voucherRepository.findByVoucherCodeAndStatus(appliedCouponCode, VoucherStatus.ACTIVE); // Tìm voucher đang active

            // 1. Kiểm tra cơ bản (Tồn tại, còn hạn, đủ tiền tối thiểu)
            if (voucherOpt.isPresent()
                    && voucherOpt.get().getStartsAt().isBefore(LocalDateTime.now())
                    && voucherOpt.get().getEndsAt().isAfter(LocalDateTime.now())
                    && (voucherOpt.get().getMinimumOrderAmount() == null || priceAfterMembership.compareTo(voucherOpt.get().getMinimumOrderAmount()) >= 0))
            {
                Voucher voucher = voucherOpt.get(); // Lấy voucher ra

                // ==== BẮT ĐẦU THÊM KIỂM TRA GIỚI HẠN ====
                boolean limitsOk = true;
                List<OrderStatus> invalidOrderStatesForUsageCount = List.of(OrderStatus.CANCELED, OrderStatus.PENDING);

                // 2. Kiểm tra giới hạn tổng số lượt sử dụng
                Integer totalLimit = voucher.getTotalUsageLimit();
                if (totalLimit != null && totalLimit > 0) {
                    // Gọi hàm count mới với danh sách trạng thái không hợp lệ
                    long totalUses = orderRepository.countByVoucher_VoucherCodeAndOrderStatusNotIn(voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (totalUses >= totalLimit) {
                        voucherErrorMessage = "Mã khuyến mãi '" + voucher.getVoucherCode() + "' đã hết lượt sử dụng.";
                        limitsOk = false;
                    }
                }

                // 3. Kiểm tra giới hạn mỗi người
                Integer userLimit = voucher.getPerUserLimit();
                if (limitsOk && userLimit != null && userLimit > 0) {
                    // Gọi hàm count mới với danh sách trạng thái không hợp lệ
                    long userUses = orderRepository.countByUserAndVoucher_VoucherCodeAndOrderStatusNotIn(currentUser, voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (userUses >= userLimit) {
                        voucherErrorMessage = "Bạn đã hết lượt sử dụng mã khuyến mãi '" + voucher.getVoucherCode() + "'.";
                        limitsOk = false;
                    }
                }
                // ==== KẾT THÚC THÊM KIỂM TRA GIỚI HẠN ====

                // 4. Nếu tất cả đều OK -> Tính giảm giá
                if (limitsOk) {
                    appliedVoucher = voucher; // Gán voucher hợp lệ
                    BigDecimal discountValue = appliedVoucher.getValue();
                    // Tính giá trị giảm thực tế
                    if (DiscountType.PERCENTAGE.equals(appliedVoucher.getDiscountType())) {
                        actualCouponDiscount = priceAfterMembership.multiply(discountValue.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                        if (appliedVoucher.getMaxDiscountAmount() != null && actualCouponDiscount.compareTo(appliedVoucher.getMaxDiscountAmount()) > 0) {
                            actualCouponDiscount = appliedVoucher.getMaxDiscountAmount();
                        }
                    } else { // Fixed amount
                        actualCouponDiscount = discountValue;
                    }
                    actualCouponDiscount = actualCouponDiscount.min(priceAfterMembership); // Đảm bảo không âm
                }
                // Nếu không OK (limitsOk == false), không làm gì cả, appliedVoucher vẫn là null, actualCouponDiscount là 0

            } else { // Nếu voucher không hợp lệ ngay từ đầu (hết hạn, ko đủ tiền...)
                if (voucherOpt.isPresent()) { // Chỉ đặt thông báo lỗi nếu voucher tồn tại nhưng ko hợp lệ
                    voucherErrorMessage = "Mã khuyến mãi '" + appliedCouponCode + "' không hợp lệ hoặc không đủ điều kiện.";
                } else {
                    voucherErrorMessage = "Không tìm thấy mã khuyến mãi '" + appliedCouponCode + "'.";
                }
            }

            // 5. Nếu có lỗi (không hợp lệ HOẶC hết lượt) -> Xóa khỏi session
            if (appliedVoucher == null) { // appliedVoucher chỉ được gán nếu mọi thứ OK
                actualCouponDiscount = BigDecimal.ZERO; // Đảm bảo không có giảm giá

                // QUAN TRỌNG: Ném Exception để báo lỗi cho người dùng biết tại sao mã bị gỡ
                if (voucherErrorMessage != null) {
                    throw new IllegalStateException(voucherErrorMessage);
                } else if(appliedCouponCode != null) {
                    // Trường hợp lỗi không xác định
                    throw new IllegalStateException("Mã khuyến mãi '" + appliedCouponCode + "' không thể áp dụng.");
                }
            }
        }
        BigDecimal finalTotal = priceAfterMembership.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        String fullAddress = String.format("%s, %s, %s, %s",
                shippingAddress.getStreetAddress(), shippingAddress.getWard(),
                shippingAddress.getDistrict(), shippingAddress.getProvince());

        Order order = new Order(
                currentUser,
                shippingAddress,
                shippingAddress.getReceiverName(),
                shippingAddress.getPhoneNumber(),
                fullAddress,
                shippingMethod,
                paymentMethod,
                shippingFee,
                appliedVoucher
        );
        order.updateNote(note);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int orderedQuantity = cartItem.getQuantity();

            OrderDetail detail = new OrderDetail(product, product.getName(), product.getPrice(), orderedQuantity);
            order.addDetail(detail);

            Inventory inventory = inventoryRepository.findById(product.getProductId())
                    .orElseThrow(() -> new RuntimeException("Hết hàng tồn kho cho sản phẩm: " + product.getName()));
            inventory.decrease(orderedQuantity);
            inventoryRepository.save(inventory);
        }

        // Persist net payable amount (after membership + voucher discounts)
        order.applyPayableAmount(finalTotal);

        Order savedOrder = orderRepository.save(order);

        if (PaymentMethod.COD.equals(order.getPaymentMethod())) {
            cartService.clearCart(); // Xóa giỏ hàng cho người dùng hiện tại

            // Xóa thông tin giảm giá khỏi session (chỉ khi COD thành công)
            System.out.println("Giỏ hàng đã được xóa cho đơn hàng COD #" + savedOrder.getOrderId());
        } else {
            // Đối với thanh toán ONLINE, không xóa giỏ hàng ở đây.
            // Giỏ hàng sẽ được xóa sau khi Webhook xác nhận thanh toán thành công.
            System.out.println("Đơn hàng ONLINE #" + savedOrder.getOrderId() + " được tạo, giỏ hàng chưa xóa.");
        }

        return savedOrder;
    }

    // Helper method
    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(username);
    }
}