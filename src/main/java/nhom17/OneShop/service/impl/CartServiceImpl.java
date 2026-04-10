package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Cart;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.repository.CartItemRepository;
import nhom17.OneShop.repository.CartRepository;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void addToCart(Integer productId, int quantity) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        Inventory inventory = inventoryRepository.findById(product.getProductId()).orElse(null);
        int stockQuantity = inventory != null ? inventory.getStockQuantity() : 0;

        Cart cart = cartRepository.findByUserWithItems(currentUser)
                .orElseGet(() -> cartRepository.save(new Cart(currentUser)));

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingCartItemOpt.isPresent()) {
            CartItem cartItem = existingCartItemOpt.get();
            cartItem.increaseQuantity(quantity, stockQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem(cart, product, quantity, stockQuantity);
            cart.addItem(newCartItem);
            cartRepository.save(cart);
        }
    }

    @Override
    public List<CartItem> getCartItems() {
        Optional<User> currentUserOpt = getCurrentUserOptional();
        if (currentUserOpt.isPresent()) {
            return cartItemRepository.findByUserWithProduct(currentUserOpt.get());
        }
        return List.of();
    }

    @Override
    @Transactional
    public void updateQuantity(Integer productId, int quantity) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        Inventory inventory = inventoryRepository.findById(product.getProductId()).orElse(null);
        int stockQuantity = inventory != null ? inventory.getStockQuantity() : 0;

        CartItem cartItem = cartItemRepository.findByCart_UserAndProduct(currentUser, product)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng."));
        cartItem.updateQuantity(quantity, stockQuantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeItem(Integer productId) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa khỏi giỏ hàng."));
        cartItemRepository.deleteByCart_UserAndProduct(currentUser, product);
    }

    @Override
    public BigDecimal getSubtotal() {
        List<CartItem> cartItems = getCartItems();
        return cartItems.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void clearCart() {
        getCurrentUserOptional().ifPresent(this::clearCartByUser);
    }

    @Override
    @Transactional
    public void clearCartByUserId(Integer userId) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId).ifPresent(this::clearCartByUser);
    }


    private void clearCartByUser(User user) {
        cartRepository.findByUserWithItems(user).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

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