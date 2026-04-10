package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    List<Rating> findByProduct_ProductIdOrderByCreatedAtDesc(Integer productId);
    boolean existsByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
    List<Rating> findByProduct(Product product);

    long countByProduct(Product product);

    default Double avgScoreByProduct(Product product) {
        List<Rating> list = findByProduct(product);
        if (list == null || list.isEmpty()) return null;

        double avg = list.stream()
                .map(Rating::getRatingScore)
                .filter(score -> score != null)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(Double.NaN);

        return Double.isNaN(avg) ? null : avg;
    }

    Optional<Rating> findByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
}
