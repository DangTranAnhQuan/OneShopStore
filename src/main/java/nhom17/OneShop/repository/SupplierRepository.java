package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Page<Supplier> findBySupplierNameContainingIgnoreCase(String keyword, Pageable pageable);
    Optional<Supplier> findBySupplierNameIgnoreCase(String supplierName);
    boolean existsBySupplierNameIgnoreCase(String supplierName);
    boolean existsBySupplierNameIgnoreCaseAndSupplierIdNot(String supplierName, Integer supplierId);
}
