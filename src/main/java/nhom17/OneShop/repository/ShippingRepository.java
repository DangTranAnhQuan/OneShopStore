package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long>, JpaSpecificationExecutor<Shipping> {
    boolean existsByOrder_OrderId(Long orderId);
    List<Shipping> findByOrder_OrderIdIn(Collection<Long> orderIds);
}
