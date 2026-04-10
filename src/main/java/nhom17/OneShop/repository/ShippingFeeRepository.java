package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ShippingFee;
import nhom17.OneShop.entity.enums.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingFeeRepository extends JpaRepository<ShippingFee, Integer> {
    boolean existsByShippingMethodAndPackageNameIgnoreCaseAndShippingCarrier_CarrierId(ShippingMethod shippingMethod, String packageName, Integer carrierId);
    List<ShippingFee> findByShippingMethodAndAppliedProvinces_ProvinceName(ShippingMethod shippingMethod, String provinceName);

    boolean existsByShippingMethodAndPackageNameIgnoreCaseAndShippingCarrier_CarrierIdAndShippingFeeIdNot(
            ShippingMethod shippingMethod, String packageName, Integer carrierId, Integer shippingFeeId);

    List<ShippingFee> findByShippingCarrier_CarrierIdOrderByShippingFeeIdDesc(int carrierId);

    @Query("SELECT DISTINCT f.shippingMethod FROM ShippingFee f ORDER BY f.shippingMethod ASC")
    List<ShippingMethod> findDistinctShippingMethod();

    @Query("SELECT sf FROM ShippingFee sf JOIN sf.appliedProvinces ap WHERE ap.provinceName = :province ORDER BY sf.feeAmount ASC")
    List<ShippingFee> findApplicableFeesByProvinceOrderedByCost(@Param("province") String province);

    @Query("SELECT sf FROM ShippingFee sf JOIN FETCH sf.shippingCarrier c JOIN sf.appliedProvinces ap WHERE ap.provinceName = :province")
    List<ShippingFee> findShippingFeesByProvince(@Param("province") String province);
}
