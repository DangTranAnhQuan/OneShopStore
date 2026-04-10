package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Cart;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByCart_UserAndProduct(User user, Product product);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.user = :user")
    List<CartItem> findByUserWithProduct(@Param("user") User user);

    @Transactional
    void deleteByCart_UserAndProduct(User user, Product product);

    @Transactional
    void deleteByCart(Cart cart);
}

