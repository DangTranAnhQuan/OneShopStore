package nhom17.OneShop.controller.user;

import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.ReturnRequest;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.AddressRepository;
import nhom17.OneShop.repository.MembershipTierRepository;
import nhom17.OneShop.repository.ReturnRequestRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class MyAccountController {

    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private StorageService storageService;
    @Autowired private OrderService orderService;
    @Autowired private MembershipTierRepository membershipTierRepository;

    @Autowired private ReturnRequestRepository returnRequestRepository;

    @GetMapping("/my-membership")
    public String membershipPage(Model model) {
        User currentUser = getCurrentUser();
        List<MembershipTier> allTiers = membershipTierRepository.findAll(Sort.by("minPoints").ascending());

        model.addAttribute("user", currentUser);
        model.addAttribute("allTiers", allTiers);
        return "user/account/membership";
    }

    @GetMapping("/my-account")
    public String myAccountPage(Model model,
                                @RequestParam(name = "tab", required = false, defaultValue = "account") String activeTab) {
        User currentUser = getCurrentUser();
        List<Address> addresses = addressRepository.findByUser_UserIdAndIsActiveTrue(currentUser.getUserId());
        model.addAttribute("user", currentUser);
        model.addAttribute("addresses", addresses);
        model.addAttribute("activeTab", activeTab);
        return "user/account/my-account";
    }

    @PostMapping("/my-account/update-details")
    public String updateDetails(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                RedirectAttributes ra) {
        if (!StringUtils.hasText(fullName)) {
            ra.addFlashAttribute("error", "Họ và tên không được để trống.");
            return "redirect:/my-account?tab=account";
        }

        try {
            User currentUser = getCurrentUser();
            currentUser.updateContactInfo(fullName.trim(), StringUtils.hasText(phoneNumber) ? phoneNumber.trim() : null);
            userRepository.save(currentUser);
            ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/my-account?tab=account";
    }

    @PostMapping("/my-account/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/my-account?tab=account";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu mới không khớp.");
            return "redirect:/my-account?tab=account";
        }
        currentUser.updatePasswordAndTimestamp(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
        ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/my-account?tab=account";
    }

    @GetMapping("/my-account/add-address")
    public String showAddAddressForm(Model model) {
        model.addAttribute("address", new Address());
        return "user/account/address-form";
    }

    @PostMapping("/my-account/add-address")
    public String saveNewAddress(@ModelAttribute Address address, RedirectAttributes ra) {
        User currentUser = getCurrentUser();
        address.assignUser(currentUser);
        addressRepository.save(address);
        ra.addFlashAttribute("success", "Thêm địa chỉ mới thành công!");
        return "redirect:/my-account?tab=addresses";
    }

    @PostMapping("/my-account/delete-address/{id}")
    public String deleteAddress(@PathVariable("id") Integer addressId, RedirectAttributes ra) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(addressId).orElseThrow();

        if (address.getUser().getUserId().equals(currentUser.getUserId())) {
            // Soft-delete address instead of physical deletion.
            address.deactivate();
            addressRepository.save(address);
            ra.addFlashAttribute("success", "Xóa địa chỉ thành công!");
        } else {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa địa chỉ này.");
        }
        return "redirect:/my-account?tab=addresses";
    }

    @PostMapping("/my-account/update-avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                               RedirectAttributes redirectAttributes) {
        if (avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn một file ảnh.");
            return "redirect:/my-account?tab=account";
        }

        try {
            User currentUser = getCurrentUser();
            String fileName = storageService.storeFile(avatarFile, "avatars");
            String oldAvatar = currentUser.getAvatarUrl();
            if (oldAvatar != null && !oldAvatar.isEmpty()) {
                storageService.deleteFile(oldAvatar);
            }
            currentUser.changeAvatar(fileName);
            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải lên ảnh: " + e.getMessage());
        }
        return "redirect:/my-account?tab=account";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, @RequestParam(name = "page", defaultValue = "1") int page) {
        int size = 5;
        User currentUser = getCurrentUser();

        Page<Order> orderPage = orderService.findOrdersForCurrentUser(page, size);

        model.addAttribute("user", currentUser);
        model.addAttribute("orderPage", orderPage);

        return "user/account/my-orders";
    }

    @GetMapping("/order-details/{id}")
    public String orderDetails(@PathVariable("id") Long id, Model model) {
        User currentUser = getCurrentUser();
        try {
            Order order = orderService.findOrderByIdForCurrentUser(id);

            // Tìm yêu cầu hoàn trả của đơn hàng này
            List<ReturnRequest> requests = returnRequestRepository.findByOrder_OrderId(id);
            ReturnRequest returnRequest = requests.isEmpty() ? null : requests.getFirst();

            model.addAttribute("order", order);
            model.addAttribute("returnReq", returnRequest);
            model.addAttribute("viewerUserId", currentUser.getUserId());

            return "user/account/order-details";
        } catch (Exception e) {
            return "redirect:/my-orders";
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(username).orElseThrow();
    }

    @PostMapping("/my-orders/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            orderService.cancelOrder(orderId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + orderId + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/my-orders";
    }

    // Phương thức xử lý Yêu cầu Hoàn trả
    @PostMapping("/my-orders/request-return/{id}")
    public String handleReturnRequest(
            @PathVariable("id") Long orderId,
            @RequestParam("reason") String reason,
            @RequestParam("evidenceFile") MultipartFile evidenceFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User currentUser = getCurrentUser();

            // 1. Lấy đơn hàng và kiểm tra quyền sở hữu (an toàn)
            Order order = orderService.findOrderByIdForCurrentUser(orderId);

            // 2. Chỉ cho phép yêu cầu khi 'Đã giao'
            if (!OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể yêu cầu hoàn trả cho đơn hàng đã giao.");
                return "redirect:/order-details/" + orderId;
            }

            // 3. Kiểm tra xem đã yêu cầu trước đó chưa
            if (returnRequestRepository.existsByOrder_OrderId(orderId)) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã gửi yêu cầu hoàn trả cho đơn hàng này rồi.");
                return "redirect:/order-details/" + orderId;
            }

            String evidenceFileName = null;
            if (evidenceFile != null && !evidenceFile.isEmpty()) {
                evidenceFileName = storageService.storeFile(evidenceFile, "returns");
            }

            ReturnRequest returnRequest = new ReturnRequest(order, currentUser, reason, evidenceFileName);
            returnRequestRepository.save(returnRequest);

            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu hoàn trả thành công. Chúng tôi sẽ xử lý sớm.");

        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi yêu cầu. Vui lòng thử lại.");
            e.printStackTrace();
        }

        return "redirect:/order-details/" + orderId;
    }

    @GetMapping("/my-account/edit-address/{id}")
    public String showEditAddressForm(@PathVariable("id") Integer addressId, Model model, RedirectAttributes ra) {
        User currentUser = getCurrentUser();

        // Tìm địa chỉ, nếu không có ném lỗi 404
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ"));

        // Bảo mật: Kiểm tra xem địa chỉ này có đúng là của user đang đăng nhập không
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền sửa địa chỉ này.");
            return "redirect:/my-account?tab=addresses";
        }

        model.addAttribute("address", address);
        return "user/account/address-form"; // Dùng chung form với chức năng Thêm mới
    }

    // 2. Hàm POST để lưu dữ liệu sau khi người dùng sửa xong
    @PostMapping("/my-account/edit-address/{id}")
    public String updateAddress(@PathVariable("id") Integer addressId,
                                @ModelAttribute Address addressDetails,
                                RedirectAttributes ra) {
        User currentUser = getCurrentUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa chỉ"));

        // Bảo mật: Kiểm tra quyền sở hữu lần nữa trước khi lưu
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền sửa địa chỉ này.");
            return "redirect:/my-account?tab=addresses";
        }

        // Dùng hàm update đã có sẵn trong Entity của bạn để cập nhật
        address.update(
                addressDetails.getReceiverName(),
                addressDetails.getPhoneNumber(),
                addressDetails.getProvince(),
                addressDetails.getDistrict(),
                addressDetails.getWard(),
                addressDetails.getStreetAddress()
        );

        addressRepository.save(address);
        ra.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        return "redirect:/my-account?tab=addresses";
    }
}

