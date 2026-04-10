package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String>, JpaSpecificationExecutor<Voucher> {
    boolean existsByCampaignNameIgnoreCase(String campaignName);

    Optional<Voucher> findByVoucherCodeAndStatus(String voucherCode, VoucherStatus status);
    List<Voucher> findByStatusAndStartsAtBeforeAndEndsAtAfter(VoucherStatus status, LocalDateTime currentTime1, LocalDateTime currentTime2);
}