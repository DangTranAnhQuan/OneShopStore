package nhom17.OneShop.service.returnrequest;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.OrderStatusHistory;
import nhom17.OneShop.entity.ReturnRequest;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.OrderRepository;
import nhom17.OneShop.repository.ReturnRequestRepository;
import nhom17.OneShop.service.InventoryService;
import nhom17.OneShop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service("coreReturnRequestService")
public class CoreReturnRequestService implements IReturnRequestService {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService; // For updateLoyaltyPoints

    @Override
    @Transactional
    public void processRequest(Long requestId, String action, String adminNotes, User admin) {
        ReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hoàn trả"));

        if ("approve".equalsIgnoreCase(action)) {
            // Duyệt yêu cầu
            request.approve(admin, adminNotes);

            Order order = orderRepository.findByIdWithDetails(request.getOrder().getOrderId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng #" + request.getOrder().getOrderId()));

            OrderStatus oldStatus = order.getOrderStatus();

            order.approveReturn();
            inventoryService.restockOrderItems(order);

            // Add history
            OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, order.getOrderStatus(), admin, LocalDateTime.now());
            order.addStatusHistory(history);

            // Update loyalty points
            orderService.updateLoyaltyPoints(order, oldStatus, order.getOrderStatus());
            
            orderRepository.save(order);

        } else if ("reject".equalsIgnoreCase(action)) {
            // Từ chối yêu cầu
            request.reject(admin, adminNotes);
        } else {
            throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
        }

        returnRequestRepository.save(request);
    }
}
