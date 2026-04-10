package nhom17.OneShop.controller;

import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @ModelAttribute("globalCartItems")
    public List<CartItem> getGlobalCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                return cartService.getCartItems();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
    @ModelAttribute("globalCurrentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @ModelAttribute("stockByProductId")
    public Map<Integer, Integer> getStockByProductId() {
        return inventoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Inventory::getProductId,
                        inventory -> inventory.getStockQuantity() == null ? 0 : inventory.getStockQuantity(),
                        (left, right) -> right
                ));
    }
}