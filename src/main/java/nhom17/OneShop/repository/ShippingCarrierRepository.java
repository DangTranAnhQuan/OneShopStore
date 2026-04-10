package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ShippingCarrier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Integer> {
    Page<ShippingCarrier> findByCarrierNameContainingIgnoreCaseAndActiveTrue(String keyword, Pageable pageable);
    Page<ShippingCarrier> findByActiveTrue(Pageable pageable);
    List<ShippingCarrier> findByActiveTrue();

    boolean existsByCarrierNameIgnoreCaseAndActiveTrue(String carrierName);
    boolean existsByCarrierNameIgnoreCaseAndCarrierIdNotAndActiveTrue(String carrierName, Integer carrierId);
}