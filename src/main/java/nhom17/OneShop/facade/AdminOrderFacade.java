package nhom17.OneShop.facade;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.Shipping;
import nhom17.OneShop.entity.ShippingFee;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.request.ShippingRequest;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.ShippingFeeService;
import nhom17.OneShop.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FACADE PATTERN: Đóng gói logic truy vấn dữ liệu từ nhiều Service
 * để phục vụ cho giao diện Quản lý Đơn hàng của Admin.
 */
@Service
public class AdminOrderFacade {

    private final OrderService orderService;
    private final ShippingFeeService shippingFeeService;
    private final ShippingRepository shippingRepository;

    @Autowired
    public AdminOrderFacade(OrderService orderService,
                            ShippingFeeService shippingFeeService,
                            ShippingRepository shippingRepository) {
        this.orderService = orderService;
        this.shippingFeeService = shippingFeeService;
        this.shippingRepository = shippingRepository;
    }

    public void prepareOrderListPage(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod, int page, int size, Model model) {
        Page<Order> orderPage = orderService.findAll(keyword, status, paymentMethod, paymentStatus, shippingMethod, page, size);
        Map<Long, List<ShippingFee>> carriersWithFeesByOrder = orderService.getCarriersWithFeesByOrder(orderPage.getContent());
        Map<Long, String> shippingTrackingCodeByOrderId = new HashMap<>();
        List<Long> orderIds = orderPage.getContent().stream().map(Order::getOrderId).toList();
        if (!orderIds.isEmpty()) {
            List<Shipping> shippings = shippingRepository.findByOrder_OrderIdIn(orderIds);
            shippingTrackingCodeByOrderId = shippings.stream()
                    .filter(shipping -> shipping.getOrder() != null && shipping.getOrder().getOrderId() != null)
                    .collect(Collectors.toMap(
                            shipping -> shipping.getOrder().getOrderId(),
                            Shipping::getTrackingCode,
                            (existing, replacement) -> existing,
                            HashMap::new
                    ));
        }
        List<ShippingMethod> shippingMethods = shippingFeeService.findDistinctShippingMethods();
        
        model.addAttribute("carriersWithFeesByOrder", carriersWithFeesByOrder);
        model.addAttribute("shippingTrackingCodeByOrderId", shippingTrackingCodeByOrderId);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("shippingMethods", shippingMethods);
        model.addAttribute("shippingRequest", new ShippingRequest());
    }
}