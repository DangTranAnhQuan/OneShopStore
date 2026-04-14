package nhom17.OneShop.service.impl;

import jakarta.servlet.http.HttpSession;
import nhom17.OneShop.entity.*;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.dto.DashboardDataDTO;
import nhom17.OneShop.dto.TopSellingProductDTO;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.OrderRepository;
import nhom17.OneShop.repository.OrderStatusHistoryRepository;
import nhom17.OneShop.repository.RatingRepository;
import nhom17.OneShop.request.OrderUpdateRequest;
import nhom17.OneShop.request.OrderRequest;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.specification.OrderSpecification;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.entity.enums.VoucherStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.OrderDetail;
import nhom17.OneShop.repository.InventoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Autowired
    ShippingFeeRepository shippingFeeRepository;

    @Autowired
    MembershipTierRepository membershipTierRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private CartService cartService;

    @Override
    public Page<Order> findAll(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("orderedAt").descending());

        return orderRepository.findAll(
                OrderSpecification.filterOrders(keyword, status, paymentMethod, paymentStatus, shippingMethod),
                pageable
        );
    }

    @Override
    @Transactional
    public Order createAndSaveOrderForCheckout(User currentUser, Address shippingAddress, List<CartItem> cartItems, OrderRequest request) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        if (shippingAddress == null || shippingAddress.getAddressId() == null) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không hợp lệ.");
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }

        BigDecimal shippingFee = Optional.ofNullable(request.getShippingFee()).orElse(BigDecimal.ZERO);
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí vận chuyển không hợp lệ.");
        }

        PaymentMethod selectedPaymentMethod = PaymentMethod.fromValue(request.getPaymentMethod());
        ShippingMethod selectedShippingMethod = ShippingMethod.fromValue(request.getShippingMethod());
        Voucher voucher = resolveVoucherForCheckout(request.getAppliedCouponCode(), cartItems);

        Order order = Order.builder()
                .user(currentUser)
                .address(shippingAddress)
                .receiverName(shippingAddress.getReceiverName())
                .receiverPhone(shippingAddress.getPhoneNumber())
                .receiverAddress(buildReceiverAddress(shippingAddress))
                .shippingMethod(selectedShippingMethod)
                .paymentMethod(selectedPaymentMethod)
                .shippingFee(shippingFee)
                .voucher(voucher)
                .note(request.getNote())
                .build();

        for (CartItem item : cartItems) {
            if (item == null || item.getProduct() == null || item.getQuantity() == null) {
                continue;
            }
            if (item.getQuantity() <= 0) {
                continue;
            }
            Product product = item.getProduct();
            BigDecimal unitPrice = Optional.ofNullable(product.getPrice())
                    .orElseThrow(() -> new IllegalStateException("Giá sản phẩm không hợp lệ."));
            order.addDetail(new OrderDetail(product, product.getName(), unitPrice, item.getQuantity()));
        }

        if (order.getOrderDetails().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng không có sản phẩm hợp lệ để tạo đơn.");
        }

        if (voucher != null) {
            BigDecimal baseAmount = order.getSubtotal();
            BigDecimal discountAmount = calculateVoucherDiscount(voucher, baseAmount);
            BigDecimal payableAmount = baseAmount.subtract(discountAmount).add(shippingFee).max(BigDecimal.ZERO);
            order.applyPayableAmount(payableAmount);
        }

        return orderRepository.save(order);
    }

    @Override
    public List<OrderStatusHistory> findHistoryByOrderId(long orderId) {
        return historyRepository.findByOrder_OrderIdOrderByChangedAtDesc(orderId);
    }

    @Override
    public Order findById(long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Map<Long, List<ShippingFee>> getCarriersWithFeesByOrder(List<Order> orders) {
        Map<Long, List<ShippingFee>> result = new HashMap<>();

        for (Order order : orders) {
            ShippingMethod shippingMethod = order.getShippingMethod();
            String province = null;

            if (order.getAddress() != null && order.getAddress().getProvince() != null) {
                province = order.getAddress().getProvince().trim();
            }

            if (shippingMethod == null || province == null) {
                result.put(order.getOrderId(), Collections.emptyList());
                continue;
            }

            List<ShippingFee> fees = shippingFeeRepository
                    .findByShippingMethodAndAppliedProvinces_ProvinceName(shippingMethod, province);

            List<ShippingFee> distinctFees = new ArrayList<>();
            Set<Integer> carrierIds = new HashSet<>();

            for (ShippingFee fee : fees) {
                ShippingCarrier carrier = fee.getShippingCarrier();
                if (carrier != null && carrierIds.add(carrier.getCarrierId())) {
                    distinctFees.add(fee);
                }
            }

            result.put(order.getOrderId(), distinctFees);
        }
        return result;
    }



    @Override
    @Transactional
    public void update(Long orderId, OrderUpdateRequest request) {
        Order order = findById(orderId);
        if (order == null) {
            throw new NotFoundException("Không tìm thấy đơn hàng #" + orderId);
        }

        OrderStatus oldStatus = order.getOrderStatus();
        OrderStatus newStatus = request.getOrderStatus();
        User currentUser = getCurrentUser();

        if (newStatus != null && !Objects.equals(oldStatus, newStatus)) {
            if (!OrderStatus.PENDING.equals(oldStatus)) {
                throw new IllegalStateException("Chỉ có thể cập nhật trạng thái đơn khi đơn đang ở trạng thái 'Đang xử lý'.");
            }
            if (!OrderStatus.CONFIRMED.equals(newStatus)) {
                throw new IllegalStateException("Admin chỉ được chuyển đơn từ 'Đang xử lý' sang 'Đã xác nhận'.");
            }
            order.changeStatusWithHistory(newStatus, currentUser);
            updateLoyaltyPoints(order, oldStatus, newStatus);
        }

        order.changePaymentStatus(request.getPaymentStatus());

        orderRepository.save(order);
    }
    @Override
    @Transactional
    public void cancelOrder(Long orderId, User currentUser) {
        // 1. Tìm đơn hàng và kiểm tra tồn tại
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng #" + orderId));

        Order orderWithDetails = orderRepository.findByIdWithDetails(orderId).orElse(order);
        Map<Integer, Inventory> inventoryByProduct = loadInventoriesForOrder(orderWithDetails);
        restockInventoriesForOrder(orderWithDetails, inventoryByProduct);
        orderWithDetails.cancelByCustomer(currentUser);
        inventoryRepository.saveAll(inventoryByProduct.values());
        orderRepository.save(orderWithDetails);
    }

    @Override
    @Transactional
    public void updateLoyaltyPoints(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        User user = order.getUser();
        if (user == null || user.getUserId() == null) {
            return;
        }

        int delta = order.loyaltyPointsDelta(oldStatus, newStatus);
        if (delta == 0) {
            return;
        }
        adjustUserPoints(user, delta);
    }

    private void adjustUserPoints(User user, int pointsToAdjust) {
        User userToUpdate = userRepository.findById(user.getUserId()).orElse(null);
        if (userToUpdate == null) return;

        userToUpdate.adjustPoints(pointsToAdjust);

        updateMembershipTier(userToUpdate);

        userRepository.save(userToUpdate);
    }

    private void updateMembershipTier(User user) {
        int currentPoints = user.getRewardPoints();

        List<MembershipTier> eligibleTiers = membershipTierRepository.findByMinPointsLessThanEqualOrderByMinPointsDesc(currentPoints);

        if (!eligibleTiers.isEmpty()) {
            MembershipTier newTier = eligibleTiers.get(0);
            if (user.getMembershipTier() == null || !newTier.getTierId().equals(user.getMembershipTier().getTierId())) {
                user.assignMembership(newTier);
            }
        }
    }

    @Override
    public DashboardDataDTO getDashboardData(int year, int month) {
        DashboardDataDTO data = new DashboardDataDTO();
        YearMonth yearMonth = YearMonth.of(year, month);

        Timestamp startOfMonth = Timestamp.valueOf(yearMonth.atDay(1).atStartOfDay());
        Timestamp endOfMonth = Timestamp.valueOf(yearMonth.atEndOfMonth().plusDays(1).atStartOfDay());

        // 1. Lấy KPI Doanh thu và Tổng đơn hàng
        List<Object[]> kpiResults = orderRepository.findKpiDataBetween(startOfMonth, endOfMonth);
        if (!kpiResults.isEmpty() && kpiResults.get(0) != null) {
            Object[] kpiData = kpiResults.get(0);
            if (kpiData[0] != null) data.setTotalRevenue(new BigDecimal(kpiData[0].toString()));
            if (kpiData[1] != null) data.setTotalOrders(Long.parseLong(kpiData[1].toString()));
        }

        // 2. Lấy KPI Sản phẩm đã bán và Giá vốn
        List<Object[]> productsAndCogsResults = orderRepository.findProductsAndCogsDataBetween(startOfMonth, endOfMonth);
        if (!productsAndCogsResults.isEmpty() && productsAndCogsResults.get(0) != null) {
            Object[] cogsData = productsAndCogsResults.get(0);
            if (cogsData[0] != null) data.setTotalProductsSold(Long.parseLong(cogsData[0].toString()));
            if (cogsData[1] != null) data.setTotalCostOfGoodsSold(new BigDecimal(cogsData[1].toString()));
        }

        // 3. Tính toán các KPI còn lại
        data.setTotalProfit(data.getTotalRevenue().subtract(data.getTotalCostOfGoodsSold()));
        if (data.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
            double margin = data.getTotalProfit().doubleValue() / data.getTotalRevenue().doubleValue() * 100;
            data.setProfitMargin(margin);
        }
        data.setAverageOrderValue(data.getTotalOrders() > 0 ? data.getTotalRevenue().divide(BigDecimal.valueOf(data.getTotalOrders()), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 4. Dữ liệu biểu đồ doanh thu theo ngày
        List<Object[]> revenueByDayRaw = orderRepository.findRevenueByDayBetween(startOfMonth, endOfMonth);
        Map<Integer, BigDecimal> revenueMap = revenueByDayRaw.stream()
                .collect(Collectors.toMap(
                        row -> ((Date) row[0]).toLocalDate().getDayOfMonth(),
                        row -> new BigDecimal(row[1].toString())
                ));
        List<String> dayLabels = new ArrayList<>();
        List<BigDecimal> dayChartData = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            dayLabels.add("Ngày " + day);
            dayChartData.add(revenueMap.getOrDefault(day, BigDecimal.ZERO));
        }
        data.setRevenueByDayLabels(dayLabels);
        data.setRevenueByDayData(dayChartData);

        // 5. Dữ liệu Top sản phẩm
        List<Object[]> topProductsRaw = orderRepository.findTopSellingProductsBetween(startOfMonth, endOfMonth, 5);
        data.setTopSellingProducts(topProductsRaw.stream()
                .map(row -> new TopSellingProductDTO((String) row[0], (String) row[1], ((Number) row[2]).longValue(), new BigDecimal(row[3].toString())))
                .collect(Collectors.toList()));

        // 6. Dữ liệu biểu đồ doanh thu theo danh mục
        List<Object[]> revenueByCategoryRaw = orderRepository.findRevenueByCategoryBetween(startOfMonth, endOfMonth);
        data.setRevenueByCategoryLabels(revenueByCategoryRaw.stream().map(row -> (String) row[0]).collect(Collectors.toList()));
        data.setRevenueByCategoryData(revenueByCategoryRaw.stream().map(row -> new BigDecimal(row[1].toString())).collect(Collectors.toList()));

        return data;
    }

//    User

    @Override
    public Page<Order> findOrdersForCurrentUser(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("orderedAt").descending());
        return orderRepository.findByUser(currentUser, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderByIdForCurrentUser(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        // Kiểm tra bảo mật: đảm bảo đơn hàng này thuộc về người dùng đang đăng nhập
        if (!order.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
        }
        return order;
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(username).orElseThrow();
    }

    private Map<Integer, Inventory> loadInventoriesForOrder(Order order) {
        Map<Integer, Inventory> inventoryByProduct = new HashMap<>();
        for (OrderDetail detail : order.getOrderDetails()) {
            Integer productId = detail.getProduct().getProductId();
            Inventory inventory = inventoryRepository.findById(productId)
                    .orElseGet(() -> new Inventory(detail.getProduct(), 0, null));
            inventoryByProduct.put(productId, inventory);
        }
        return inventoryByProduct;
    }

    private void restockInventoriesForOrder(Order order, Map<Integer, Inventory> inventoryByProduct) {
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail == null || detail.getProduct() == null || detail.getProduct().getProductId() == null) {
                continue;
            }
            int quantity = detail.getQuantity() == null ? 0 : detail.getQuantity();
            if (quantity <= 0) {
                continue;
            }
            Inventory inventory = inventoryByProduct.get(detail.getProduct().getProductId());
            if (inventory != null) {
                inventory.increase(quantity);
            }
        }
    }

    private Voucher resolveVoucherForCheckout(String couponCode, List<CartItem> cartItems) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }

        Voucher voucher = voucherRepository.findByVoucherCodeAndStatus(couponCode, VoucherStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Mã giảm giá không hợp lệ hoặc đã hết hiệu lực."));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartsAt() != null && voucher.getStartsAt().isAfter(now)) {
            throw new IllegalStateException("Mã giảm giá chưa đến thời gian áp dụng.");
        }
        if (voucher.getEndsAt() != null && voucher.getEndsAt().isBefore(now)) {
            throw new IllegalStateException("Mã giảm giá đã hết hạn.");
        }

        BigDecimal baseAmount = cartItems.stream()
                .filter(Objects::nonNull)
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (voucher.getMinimumOrderAmount() != null && baseAmount.compareTo(voucher.getMinimumOrderAmount()) < 0) {
            throw new IllegalStateException("Đơn hàng chưa đạt điều kiện tối thiểu để áp dụng mã giảm giá.");
        }
        return voucher;
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal baseAmount) {
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

    private String buildReceiverAddress(Address shippingAddress) {
        return java.util.stream.Stream.of(
                        shippingAddress.getStreetAddress(),
                        shippingAddress.getWard(),
                        shippingAddress.getDistrict(),
                        shippingAddress.getProvince())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean hasCompletedPurchase(Integer userId, Integer productId) {
        return orderRepository.hasCompletedPurchase(userId, productId);
    }

    @Override
    public boolean canUserReviewProduct(Integer userId, Integer productId) {
        if (userId == null || productId == null) {
            return false;
        }
        // Kiểm tra xem đã mua và đơn hàng đã hoàn thành chưa
        boolean hasPurchased = orderRepository.hasCompletedPurchase(userId, productId);
        if (!hasPurchased) {
            return false;
        }
        // Kiểm tra xem đã đánh giá sản phẩm này bao giờ chưa
        boolean hasReviewed = ratingRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId);
        return !hasReviewed;
    }
    @Override
    @Transactional
    public void cancelOrderIfPendingOnline(Long orderId, User currentUser) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            System.out.println("cancelOrderIfPendingOnline: Không tìm thấy đơn hàng #" + orderId);
            return;
        }

        if (!order.getUser().getUserId().equals(currentUser.getUserId())) {
            System.err.println("cancelOrderIfPendingOnline: User không có quyền hủy đơn #" + orderId);
            return;
        }

        try {
            Order orderWithDetails = orderRepository.findByIdWithDetails(orderId).orElse(order);
            Map<Integer, Inventory> inventoryByProduct = loadInventoriesForOrder(orderWithDetails);
            restockInventoriesForOrder(orderWithDetails, inventoryByProduct);
            orderWithDetails.cancelPendingOnline(currentUser);
            inventoryRepository.saveAll(inventoryByProduct.values());
            orderRepository.save(orderWithDetails);
            System.out.println("cancelOrderIfPendingOnline: Đã hủy thành công đơn hàng #" + orderId);

        } catch (Exception e) {
            System.err.println("cancelOrderIfPendingOnline: Lỗi nghiêm trọng khi hoàn kho/hủy đơn #" + orderId + ": " + e.getMessage());
            e.printStackTrace(); // In stack trace để debug
            // Giao dịch sẽ tự rollback nếu có lỗi ở đây
            throw new RuntimeException("Lỗi khi xử lý hủy đơn hàng.", e); // Ném lại lỗi để controller biết
        }
    }
}
