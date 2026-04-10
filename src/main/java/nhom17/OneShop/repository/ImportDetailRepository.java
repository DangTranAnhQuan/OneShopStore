package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ImportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, Integer> {
    @Query("SELECT id FROM ImportDetail id " +
            "JOIN FETCH id.importReceipt ir " +
            "JOIN FETCH ir.supplier " +
            "WHERE id.product.productId = :productId " +
            "ORDER BY ir.createdAt DESC")
    List<ImportDetail> findHistoryByProductId(Integer productId);
}
