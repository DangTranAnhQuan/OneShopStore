package nhom17.OneShop.controller.admin;

import nhom17.OneShop.entity.ReturnRequest;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.ReturnRequestRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/returns")
public class AdminReturnController {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository; // Dùng để lấy thông tin admin

    // Trang danh sách tất cả yêu cầu
    @GetMapping
    public String listReturnRequests(Model model) {
        List<ReturnRequest> returnRequests = returnRequestRepository.findAll();
        model.addAttribute("returnRequests", returnRequests);
        return "admin/orders/returns"; // File HTML mới
    }

    // Trang chi tiết một yêu cầu
    @GetMapping("/{id}")
    public String viewReturnRequest(@PathVariable("id") Long id, Model model) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hoàn trả"));
        model.addAttribute("returnRequest", request);
        return "admin/orders/return-detail"; // File HTML mới
    }

    // Xử lý (Chấp thuận / Từ chối)
    @PostMapping("/process")
    public String processReturnRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam("action") String action, // "approve" or "reject"
            @RequestParam(value = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirectAttributes
    ) {
        ReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu"));

        User admin = getCurrentAdminUser();

        if (action.equals("approve")) {
            try {
                request.approve(admin, adminNotes);
                orderService.processAdminReturnApproval(request.getOrder().getOrderId(), admin);
                redirectAttributes.addFlashAttribute("success", "Đã chấp thuận yêu cầu hoàn trả. Đơn hàng đã cập nhật và hoàn kho.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi khi duyệt: " + e.getMessage());
                return "redirect:/admin/returns/" + requestId;
            }

        } else if (action.equals("reject")) {
            request.reject(admin, adminNotes);
            redirectAttributes.addFlashAttribute("warning", "Đã từ chối yêu cầu hoàn trả.");
        }

        returnRequestRepository.save(request);
        return "redirect:/admin/returns";
    }

    // Helper để lấy admin đang đăng nhập (giống trong service/controller khác của bạn)
    private User getCurrentAdminUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản Admin."));
    }
}