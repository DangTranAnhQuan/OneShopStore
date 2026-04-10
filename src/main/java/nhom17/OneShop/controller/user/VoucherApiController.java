package nhom17.OneShop.controller.user;

import nhom17.OneShop.dto.VoucherApiDTO;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.repository.OrderRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class VoucherApiController {

    @Autowired private VoucherService voucherService;
    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepository; // Inject OrderRepository
    @Autowired private UserRepository userRepository;   // Inject UserRepository

    @GetMapping("/api/vouchers/available")
    public ResponseEntity<?> getAvailableVouchers() {
        Map<String, Object> response = new HashMap<>();
        List<VoucherApiDTO> eligibleVouchers = new ArrayList<>();
        BigDecimal subtotal;

        try {
            // Lấy người dùng hiện tại (cần để kiểm tra giới hạn người dùng)
            Optional<User> currentUserOpt = getCurrentUserOptional();
            // Lấy tổng phụ giỏ hàng (cần để kiểm tra chi tiêu tối thiểu)
            subtotal = cartService.getSubtotal();

            // 1. Lấy tất cả voucher có khả năng đang hoạt động
            List<Voucher> allActiveVouchers = voucherService.findActivePromotions();

            LocalDateTime now = LocalDateTime.now();
            List<OrderStatus> invalidOrderStatesForUsageCount = List.of(OrderStatus.CANCELED, OrderStatus.CONFIRMED);

            // 2. Lọc voucher dựa trên TẤT CẢ điều kiện
            for (Voucher voucher : allActiveVouchers) {
                boolean isEligible = true; // Ban đầu giả sử là hợp lệ

                if (!VoucherStatus.ACTIVE.equals(voucher.getStatus()) || voucher.getStartsAt().isAfter(now) || voucher.getEndsAt().isBefore(now)) {
                    isEligible = false;
                }

                // Kiểm tra Chi tiêu Tối thiểu
                if (isEligible && voucher.getMinimumOrderAmount() != null && subtotal.compareTo(voucher.getMinimumOrderAmount()) < 0) {
                    isEligible = false;
                }

                // Kiểm tra Giới hạn Sử dụng Tổng
                Integer totalLimit = voucher.getTotalUsageLimit();
                if (isEligible && totalLimit != null && totalLimit > 0) {
                    long totalUses = orderRepository.countByVoucher_VoucherCodeAndOrderStatusNotIn(voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (totalUses >= totalLimit) {
                        isEligible = false;
                    }
                }

                // Kiểm tra Giới hạn Sử dụng Mỗi người (Chỉ khi người dùng đã đăng nhập)
                Integer userLimit = voucher.getPerUserLimit();
                if (isEligible && currentUserOpt.isPresent() && userLimit != null && userLimit > 0) {
                    User currentUser = currentUserOpt.get();
                    long userUses = orderRepository.countByUserAndVoucher_VoucherCodeAndOrderStatusNotIn(currentUser, voucher.getVoucherCode(), invalidOrderStatesForUsageCount);
                    if (userUses >= userLimit) {
                        isEligible = false;
                    }
                }

                // Nếu tất cả kiểm tra đều qua, thêm vào danh sách hợp lệ
                if (isEligible) {
                    eligibleVouchers.add(VoucherApiDTO.fromEntity(voucher));
                }
            }

            response.put("vouchers", eligibleVouchers); // Trả về danh sách đã lọc
            response.put("subtotal", subtotal); // Vẫn trả về subtotal để tham khảo
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy voucher có sẵn: " + e.getMessage());
            // Trả về danh sách rỗng và subtotal bằng 0 khi có lỗi
            response.put("error", "Không thể lấy voucher: " + e.getMessage());
            response.put("vouchers", List.of());
            response.put("subtotal", BigDecimal.ZERO);
            return ResponseEntity.ok(response); // Vẫn trả về 200 OK
        }
    }

    // Hàm trợ giúp lấy người dùng hiện tại (sao chép từ CartServiceImpl)
    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username);
    }
}