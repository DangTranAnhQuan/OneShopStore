package nhom17.OneShop.service;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.request.VoucherRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VoucherService {
    Page<Voucher> findAll(String keyword, VoucherStatus status, Integer type, int page, int size);
    Voucher findById(String id);
    void save(VoucherRequest request);
    void delete(String id);
    List<Voucher> findActivePromotions();
}
