package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndProductIdNot(String name, Integer productId);
    boolean existsByCategory_CategoryId(Integer categoryId);
    boolean existsByBrand_BrandId(Integer brandId);

    List<Product> findTop8ByIsActiveIsTrueOrderByCreatedAtDesc();


    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.originalPrice > p.price ORDER BY ((p.originalPrice - p.price) / p.originalPrice) DESC")
    List<Product> findTopDiscountedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchForUser(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY (" +
            "SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od JOIN od.order o " +
            "WHERE od.product = p AND o.orderStatus = :deliveredStatus" +
            ") DESC")
    List<Product> findTopSellingProducts(@Param("deliveredStatus") OrderStatus deliveredStatus, Pageable pageable);
}