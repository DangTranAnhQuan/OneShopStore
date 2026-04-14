package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.entity.enums.ShippingStatus;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.request.ShippingRequest;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.ShippingService;
import nhom17.OneShop.specification.ShippingSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    private ShippingRepository shippingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderService orderService;

    @Override
    public Page<Shipping> search(String keyword, Integer carrierId, String status, String shippingMethod, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("shippedAt").descending());
        Specification<Shipping> spec = ShippingSpecification.filterBy(keyword, carrierId, status, shippingMethod);
        return shippingRepository.findAll(spec, pageable);
    }

    @Override
    public Shipping findById(Long id) {
        return shippingRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ShippingRequest request) {
        // Bước 1: Validate
        validateShippingRequest(request);

        // Bước 2: Chuẩn bị Entity
        Shipping shipping = prepareShippingEntity(request);

        // Bước 3: Map dữ liệu và xử lý nghiệp vụ
        mapRequestToEntity(request, shipping);

        // Bước 4: Lưu
        shippingRepository.save(shipping);
    }

    private void validateShippingRequest(ShippingRequest request) {
        if (request.getShippingId() == null) { // Chỉ kiểm tra khi tạo mới
            if (shippingRepository.existsByOrder_OrderId(request.getOrderId())) {
                throw new DataIntegrityViolationException("Đơn hàng #" + request.getOrderId() + " đã có đơn vận chuyển. Không thể tạo thêm.");
            }

            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với mã: " + request.getOrderId()));
            if (OrderStatus.PENDING.equals(order.getOrderStatus())) {
                throw new DataIntegrityViolationException("Đơn hàng #" + request.getOrderId() + " đang ở trạng thái 'Đang xử lý'. Không thể tạo đơn vận chuyển.");
            }
            if (OrderStatus.CANCELED.equals(order.getOrderStatus())) {
                throw new DataIntegrityViolationException("Đơn hàng #" + request.getOrderId() + " đã hủy. Không thể tạo đơn vận chuyển.");
            }
        }
    }

    private Shipping prepareShippingEntity(ShippingRequest request) {
        if (request.getShippingId() == null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với mã: " + request.getOrderId()));
            ShippingCarrier carrier = shippingCarrierRepository.findById(request.getCarrierId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + request.getCarrierId()));
            String trackingCode = UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            return new Shipping(trackingCode, order, carrier);
        }
        return findById(request.getShippingId());
    }

    private void mapRequestToEntity(ShippingRequest request, Shipping shipping) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với mã: " + request.getOrderId()));

        if (request.getShippingId() != null
                && shipping.getOrder() != null
                && !Objects.equals(shipping.getOrder().getOrderId(), order.getOrderId())) {
            throw new DataIntegrityViolationException("Không thể đổi đơn hàng cho vận chuyển đã tồn tại.");
        }

        ShippingCarrier carrier = shippingCarrierRepository.findById(request.getCarrierId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + request.getCarrierId()));

        if (request.getShippingId() != null) {
            Integer existingCarrierId = shipping.getCarrier() != null ? shipping.getCarrier().getCarrierId() : null;
            if (!Objects.equals(existingCarrierId, carrier.getCarrierId())) {
                throw new DataIntegrityViolationException("Không thể thay đổi nhà vận chuyển sau khi đã tạo đơn vận chuyển.");
            }
        } else {
            shipping.changeCarrier(carrier);
        }

        OrderStatus oldOrderStatus = order.getOrderStatus();
        ShippingStatus newShippingStatus = request.getStatus();

        if (request.getShippingId() == null) { // Trường hợp Thêm mới
            shipping.updateStatus(ShippingStatus.CREATED);
        } else {
            if (!Objects.equals(shipping.getStatus(), newShippingStatus)) {
                shipping.updateStatus(newShippingStatus);
                if (ShippingStatus.DELIVERED.equals(newShippingStatus)) {
                    order.changePaymentStatus(PaymentStatus.PAID);
                }

                OrderStatus newOrderStatus = mapShippingStatusToOrderStatus(newShippingStatus);
                if (newOrderStatus != null) {
                    orderService.updateLoyaltyPoints(order, oldOrderStatus, newOrderStatus);
                    updateOrderStatusAndLog(order, newOrderStatus, oldOrderStatus);
                }
            }
        }
    }

    private OrderStatus mapShippingStatusToOrderStatus(ShippingStatus shippingStatus) {
        switch (shippingStatus) {
            case CREATED: return OrderStatus.CONFIRMED;
            case IN_TRANSIT: return OrderStatus.SHIPPING;
            case FAILED: return OrderStatus.DELIVERY_FAILED;
            case DELIVERED: return OrderStatus.DELIVERED;
            default: return null;
        }
    }

    private void updateOrderStatusAndLog(Order order, OrderStatus newStatus, OrderStatus oldStatus) {
        if (newStatus != null && !Objects.equals(oldStatus, newStatus)) {
            order.changeStatusWithHistory(newStatus, getCurrentUser());
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shipping shipping = findById(id);
        if (ShippingStatus.IN_TRANSIT.equals(shipping.getStatus()) || ShippingStatus.DELIVERED.equals(shipping.getStatus())) {
            throw new DataIntegrityViolationException("Không thể xóa đơn vận chuyển đang hoặc đã giao hàng.");
        }
        shippingRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(username).orElseThrow();
    }
}
