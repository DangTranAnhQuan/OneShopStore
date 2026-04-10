package nhom17.OneShop.service;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.WishListItem;

import java.util.List;

public interface WishlistService {
    List<WishListItem> getWishlistItems(User user);

    String toggleWishlist(User user, Integer productId);

    long countItems(User user);
}

