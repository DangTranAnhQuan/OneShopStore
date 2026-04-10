package nhom17.OneShop.controller.admin;

import jakarta.validation.Valid;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.UserStatus;
import nhom17.OneShop.repository.MembershipTierRepository;
import nhom17.OneShop.repository.RoleRepository;
import nhom17.OneShop.request.UserRequest;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired private
    RoleRepository roleRepository;
    @Autowired private
    MembershipTierRepository membershipTierRepository;
    @Autowired
    private StorageService storageService;

    @GetMapping
    public String listUsers(@RequestParam(name = "filterKeyword", required = false) String keyword,
                            @RequestParam(name = "filterRoleId", required = false) Integer roleId,
                            @RequestParam(name = "filterTierId", required = false) Integer tierId,
                            @RequestParam(name = "filterStatus", required = false) UserStatus status,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        Page<User> userPage = userService.findAll(keyword, roleId, tierId, status, page, size);
        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("tiers", membershipTierRepository.findAll());

        // Giữ lại state cho bộ lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("tierId", tierId);
        model.addAttribute("status", status);

        return "admin/user/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable int id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/user";
        }
        model.addAttribute("user", user);
        return "admin/user/userDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showUserForm(@PathVariable(name = "id", required = false) Integer id,
                               Model model,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(name = "filterKeyword", required = false) String keyword,
                               @RequestParam(name = "filterRoleId", required = false) Integer roleId,
                               @RequestParam(name = "filterTierId", required = false) Integer tierId,
                               @RequestParam(name = "filterStatus", required = false) UserStatus status) {
        UserRequest userRequest = new UserRequest();
        if (id != null) {
            User user = userService.findById(id);

            userRequest.setUserId(user.getUserId());
            userRequest.setFullName(user.getFullName());
            userRequest.setEmail(user.getEmail());
            userRequest.setUsername(user.getUsername());
            userRequest.setPhoneNumber(user.getPhoneNumber());
            userRequest.setStatus(user.getStatus());
            userRequest.setRoleId(user.getRole().getRoleId());
            userRequest.setAvatarUrl(user.getAvatarUrl());
            if(user.getMembershipTier() != null){
                userRequest.setMembershipTierId(user.getMembershipTier().getTierId());
            }
        }

        model.addAttribute("userRequest", userRequest);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("tiers", membershipTierRepository.findAll());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("tierId", tierId);
        model.addAttribute("status", status);
        return "admin/user/addOrEditUser";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("userRequest") UserRequest userRequest,
                           BindingResult bindingResult,
                           @RequestParam("avatarFile") MultipartFile avatarFile,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(name = "filterKeyword", required = false) String keyword,
                           @RequestParam(name = "filterRoleId", required = false) Integer filterRoleId,
                           @RequestParam(name = "filterTierId", required = false) Integer filterTierId,
                           @RequestParam(name = "filterStatus", required = false) UserStatus filterStatus) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("tiers", membershipTierRepository.findAll());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("roleId", filterRoleId);
            model.addAttribute("tierId", filterTierId);
            model.addAttribute("status", filterStatus);
            return "admin/user/addOrEditUser";
        }
        try {
            if (!avatarFile.isEmpty()) {
                String fileName = storageService.storeFile(avatarFile, "avatars");
                userRequest.setAvatarUrl(fileName);
            }

            userService.save(userRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu người dùng thành công!");
            redirectAttributes.addAttribute("page", page)
                    .addAttribute("size", size)
                    .addAttribute("filterKeyword", keyword)
                    .addAttribute("filterRoleId", filterRoleId)
                    .addAttribute("filterTierId", filterTierId)
                    .addAttribute("filterStatus", filterStatus);
            return "redirect:/admin/user";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("tiers", membershipTierRepository.findAll());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("roleId", filterRoleId);
            model.addAttribute("tierId", filterTierId);
            model.addAttribute("status", filterStatus);
            return "admin/user/addOrEditUser";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(name = "filterKeyword", required = false) String keyword,
                             @RequestParam(name = "filterRoleId", required = false) Integer roleId,
                             @RequestParam(name = "filterTierId", required = false) Integer tierId,
                             @RequestParam(name = "filterStatus", required = false) UserStatus status) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page)
                .addAttribute("size", size)
                .addAttribute("filterKeyword", keyword)
                .addAttribute("filterRoleId", roleId)
                .addAttribute("filterTierId", tierId)
                .addAttribute("filterStatus", status);
        return "redirect:/admin/user";
    }
}
