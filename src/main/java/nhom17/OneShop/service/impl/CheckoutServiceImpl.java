package nhom17.OneShop.service.impl;

import jakarta.annotation.PostConstruct;
import nhom17.OneShop.entity.*;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.event.OrderEventPublisher;
import nhom17.OneShop.listener.ClearCartListener;
import nhom17.OneShop.listener.EmailNotificationListener;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private VoucherRepository voucherRepository;

    @Autowired private OrderEventPublisher orderEventPublisher;
    @Autowired private ClearCartListener clearCartListener;
    @Autowired private EmailNotificationListener emailNotificationListener;

    @PostConstruct
    public void registerOrderListeners() {
        orderEventPublisher.registerListener(clearCartListener);
        orderEventPublisher.registerListener(emailNotificationListener);
    }


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
        Voucher appliedVoucher = null;

        String voucherErrorMessage = null;

        if (appliedCouponCode != null) {
            Optional<Voucher> voucherOpt = voucherRepository.findByVoucherCodeAndStatus(appliedCouponCode, VoucherStatus.ACTIVE);

            if (voucherOpt.isPresent()
                    && voucherOpt.get().getStartsAt().isBefore(LocalDateTime.now())
                    && voucherOpt.get().getEndsAt().isAfter(LocalDateTime.now())
                    && (voucherOpt.get().getMinimumOrderAmount() == null || priceAfterMembership.compareTo(voucherOpt.get().getMinimumOrderAmount()) >= 0)) {
                Voucher voucher = voucherOpt.get();

                boolean limitsOk = true;
                List<OrderStatus> invalidOrderStatesForUsageCount = List.of(OrderStatus.CANCELED, OrderStatus.PENDING);

                Integer totalLimit = voucher.getTotalUsageLimit();
                if (totalLimit != null && totalLimit > 0) {
                    long totalUses = orderRepository.countByVoucher_VoucherCodeAndOrderStatusNotIn(voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (totalUses >= totalLimit) {
                        voucherErrorMessage = "Mã khuyến mãi '" + voucher.getVoucherCode() + "' đã hết lượt sử dụng.";
                        limitsOk = false;
                    }
                }

                Integer userLimit = voucher.getPerUserLimit();
                if (limitsOk && userLimit != null && userLimit > 0) {
                    long userUses = orderRepository.countByUserAndVoucher_VoucherCodeAndOrderStatusNotIn(currentUser, voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (userUses >= userLimit) {
                        voucherErrorMessage = "Bạn đã hết lượt sử dụng mã khuyến mãi '" + voucher.getVoucherCode() + "'.";
                        limitsOk = false;
                    }
                }

                if (limitsOk) {
                    appliedVoucher = voucher;
                    BigDecimal discountValue = appliedVoucher.getValue();
                    if (DiscountType.PERCENTAGE.equals(appliedVoucher.getDiscountType())) {
                        actualCouponDiscount = priceAfterMembership.multiply(discountValue.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                        if (appliedVoucher.getMaxDiscountAmount() != null && actualCouponDiscount.compareTo(appliedVoucher.getMaxDiscountAmount()) > 0) {
                            actualCouponDiscount = appliedVoucher.getMaxDiscountAmount();
                        }
                    } else {
                        actualCouponDiscount = discountValue;
                    }
                    actualCouponDiscount = actualCouponDiscount.min(priceAfterMembership);
                }

            } else {
                if (voucherOpt.isPresent()) {
                    voucherErrorMessage = "Mã khuyến mãi '" + appliedCouponCode + "' không hợp lệ hoặc không đủ điều kiện.";
                } else {
                    voucherErrorMessage = "Không tìm thấy mã khuyến mãi '" + appliedCouponCode + "'.";
                }
            }

            if (appliedVoucher == null) {
                actualCouponDiscount = BigDecimal.ZERO;

                if (voucherErrorMessage != null) {
                    throw new IllegalStateException(voucherErrorMessage);
                } else if (appliedCouponCode != null) {
                    throw new IllegalStateException("Mã khuyến mãi '" + appliedCouponCode + "' không thể áp dụng.");
                }
            }
        }

        BigDecimal finalTotal = priceAfterMembership.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        String fullAddress = String.format("%s, %s, %s, %s",
                shippingAddress.getStreetAddress(), shippingAddress.getWard(),
                shippingAddress.getDistrict(), shippingAddress.getProvince());

        Order order = Order.builder()
                .user(currentUser)
                .address(shippingAddress)
                .receiverName(shippingAddress.getReceiverName())
                .receiverPhone(shippingAddress.getPhoneNumber())
                .receiverAddress(fullAddress)
                .shippingMethod(shippingMethod)
                .paymentMethod(paymentMethod)
                .shippingFee(shippingFee)
                .voucher(appliedVoucher)
                .note(note)
                .build();

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

        order.applyPayableAmount(finalTotal);

        Order savedOrder = orderRepository.save(order);
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);

        return savedOrder;
    }

    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(username);
    }
}
