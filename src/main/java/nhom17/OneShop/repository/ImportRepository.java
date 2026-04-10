package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRepository extends JpaRepository<Import, Integer>, JpaSpecificationExecutor<Import> {
    boolean existsBySupplier_SupplierId(Integer supplierId);
}
