package nhom17.OneShop.facade;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.ShippingFee;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.request.ShippingRequest;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.ShippingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

/**
 * FACADE PATTERN: Đóng gói logic truy vấn dữ liệu từ nhiều Service
 * để phục vụ cho giao diện Quản lý Đơn hàng của Admin.
 */
@Service
public class AdminOrderFacade {

    @Autowired private OrderService orderService;
    @Autowired private ShippingFeeService shippingFeeService;

    public void prepareOrderListPage(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod, int page, int size, Model model) {
        Page<Order> orderPage = orderService.findAll(keyword, status, paymentMethod, paymentStatus, shippingMethod, page, size);
        Map<Long, List<ShippingFee>> carriersWithFeesByOrder = orderService.getCarriersWithFeesByOrder(orderPage.getContent());
        List<ShippingMethod> shippingMethods = shippingFeeService.findDistinctShippingMethods();
        
        model.addAttribute("carriersWithFeesByOrder", carriersWithFeesByOrder);
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