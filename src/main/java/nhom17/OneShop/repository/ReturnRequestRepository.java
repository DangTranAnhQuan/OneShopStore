package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByOrder_OrderId(Long orderId);
    boolean existsByOrder_OrderId(Long orderId);
}