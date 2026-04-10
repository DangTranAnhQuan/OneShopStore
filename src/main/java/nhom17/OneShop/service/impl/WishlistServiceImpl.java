package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.WishList;
import nhom17.OneShop.entity.WishListItem;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.repository.WishlistRepository;
import nhom17.OneShop.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WishListItem> getWishlistItems(User user) {
        if (user == null) {
            return List.of();
        }
        return wishlistRepository.findFirstByUser(user)
                .map(WishList::getWishListItems)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public String toggleWishlist(User user, Integer productId) {
        if (user == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        WishList wishList = wishlistRepository.findFirstByUser(user)
                .orElseGet(() -> wishlistRepository.save(new WishList(user)));

        WishListItem existingItem = wishList.getWishListItems().stream()
                .filter(item -> item.getProduct() != null
                        && Objects.equals(item.getProduct().getProductId(), productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            wishList.removeItem(existingItem);
            wishlistRepository.save(wishList);
            return "removed";
        }

        WishListItem newItem = new WishListItem(wishList, product);
        wishList.addItem(newItem);
        wishlistRepository.save(wishList);
        return "added";
    }

    @Override
    @Transactional(readOnly = true)
    public long countItems(User user) {
        if (user == null) {
            return 0;
        }
        return wishlistRepository.findFirstByUser(user)
                .map(wishList -> (long) wishList.getWishListItems().size())
                .orElse(0L);
    }
}

