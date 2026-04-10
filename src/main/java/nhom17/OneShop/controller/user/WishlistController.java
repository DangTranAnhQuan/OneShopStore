package nhom17.OneShop.controller.user;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.WishListItem;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/wishlist")
    public String viewWishlist(Model model) {
        User currentUser = getCurrentUser();
        List<WishListItem> wishlistItems = wishlistService.getWishlistItems(currentUser);
        model.addAttribute("wishlistItems", wishlistItems);
        return "user/shop/wishlist";
    }

    @PostMapping("/wishlist/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@PathVariable("productId") Integer productId) {
        try {
            User currentUser = getCurrentUser();
            String status = wishlistService.toggleWishlist(currentUser, productId);
            long newCount = wishlistService.countItems(currentUser);
            return ResponseEntity.ok(Map.of("status", status, "count", newCount));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trong CSDL."));
        }
        throw new IllegalStateException("Người dùng chưa đăng nhập.");
    }
}