package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository
        extends JpaRepository<Inventory, Integer>, JpaSpecificationExecutor<Inventory> {

    Optional<Inventory> findByProduct(Product product);

    @Query("select coalesce(sum(i.stockQuantity), 0) from Inventory i where i.product = :product")
    Long totalQuantityByProduct(@Param("product") Product product);

    boolean existsByProduct(Product product);
}
