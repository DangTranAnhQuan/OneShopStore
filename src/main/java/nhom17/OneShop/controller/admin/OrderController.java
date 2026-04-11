package nhom17.OneShop.controller.admin;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.request.OrderUpdateRequest;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.facade.AdminOrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired private OrderService orderService;
    
    // TIÊM FACADE VÀO ĐÂY
    @Autowired private AdminOrderFacade adminOrderFacade;

    @GetMapping
    public String listOrders(@RequestParam(name = "filterKeyword", required = false) String keyword,
                             @RequestParam(name = "filterStatus", required = false) String status,
                             @RequestParam(name = "filterPaymentMethod", required = false) String paymentMethod,
                             @RequestParam(name = "filterPaymentStatus", required = false) String paymentStatus,
                             @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {

        // Facade tiếp nhận và xử lý việc gọi Service + Đổ Model
        adminOrderFacade.prepareOrderListPage(keyword, status, paymentMethod, paymentStatus, shippingMethod, page, size, model);
        return "admin/orders/orders";
    }

    @GetMapping("/{id}")
    public String showOrderDetail(@PathVariable long id, Model model,
                                  @RequestParam(name = "filterKeyword", required = false) String keyword,
                                  @RequestParam(name = "filterStatus", required = false) String status,
                                  @RequestParam(name = "filterPaymentMethod", required = false) String paymentMethod,
                                  @RequestParam(name = "filterPaymentStatus", required = false) String paymentStatus,
                                  @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Order order = orderService.findById(id);
        if (order == null) return "redirect:/admin/order";
        
        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setOrderStatus(order.getOrderStatus());
        updateRequest.setPaymentMethod(order.getPaymentMethod());
        updateRequest.setPaymentStatus(order.getPaymentStatus());

        model.addAttribute("order", order);
        model.addAttribute("orderUpdateRequest", updateRequest);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/orders/orderDetail";
    }

    @GetMapping("/{id}/history")
    public String viewOrderHistory(@PathVariable long id, Model model,
                                   @RequestParam(name = "filterKeyword", required = false) String keyword,
                                   @RequestParam(name = "filterStatus", required = false) String status,
                                   @RequestParam(name = "filterPaymentMethod", required = false) String paymentMethod,
                                   @RequestParam(name = "filterPaymentStatus", required = false) String paymentStatus,
                                   @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Order order = orderService.findById(id);
        if (order == null) return "redirect:/admin/order";
        
        List<OrderStatusHistory> historyList = orderService.findHistoryByOrderId(id);
        model.addAttribute("order", order);
        model.addAttribute("historyList", historyList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/orders/orderHistory";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable long id,
                              @ModelAttribute("orderUpdateRequest") OrderUpdateRequest request,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(name = "filterKeyword", required = false) String keyword,
                              @RequestParam(name = "filterStatus", required = false) String status,
                              @RequestParam(name = "filterPaymentMethod", required = false) String paymentMethod,
                              @RequestParam(name = "filterPaymentStatus", required = false) String paymentStatus,
                              @RequestParam(name = "filterShippingMethod", required = false) String shippingMethod,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        try {
            orderService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("filterKeyword", keyword);
        redirectAttributes.addAttribute("filterStatus", status);
        redirectAttributes.addAttribute("filterPaymentMethod", paymentMethod);
        redirectAttributes.addAttribute("filterPaymentStatus", paymentStatus);
        redirectAttributes.addAttribute("filterShippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/order/" + id;
    }
}