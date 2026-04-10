package nhom17.OneShop.repository;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Integer> {
    List<WishList> findByUser(User user);

    @Query("SELECT COUNT(i) FROM WishList w JOIN w.wishListItems i WHERE w.user = :user")
    long countByUser(User user);

    @Query("SELECT DISTINCT w FROM WishList w JOIN w.wishListItems i WHERE w.user = :user AND i.product.productId = :productId")
    Optional<WishList> findByUserAndProduct_ProductId(@Param("user") User user, @Param("productId") Integer productId);

    Optional<WishList> findFirstByUser(User user);
}