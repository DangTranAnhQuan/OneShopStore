package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer>, JpaSpecificationExecutor<Brand> {
    Optional<Brand> findByBrandNameIgnoreCase(String brandName);
    boolean existsByBrandNameIgnoreCase(String brandName);
    boolean existsByBrandNameIgnoreCaseAndBrandIdNot(String brandName, Integer brandId);
}
