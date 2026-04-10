package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.OrderRepository;
import nhom17.OneShop.repository.VoucherRepository;
import nhom17.OneShop.request.VoucherRequest;
import nhom17.OneShop.service.VoucherService;
import nhom17.OneShop.specification.VoucherSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Page<Voucher> findAll(String keyword, VoucherStatus status, Integer type, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        DiscountType discountType = type == null ? null : DiscountType.fromCode(type);
        return voucherRepository.findAll(VoucherSpecification.filterByCriteria(keyword, status, discountType), pageable);
    }

    @Override
    public Voucher findById(String id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(VoucherRequest request) {
        Voucher voucher = prepareVoucherEntity(request);
        mapRequestToEntity(request, voucher);
        voucherRepository.save(voucher);
    }

    private Voucher prepareVoucherEntity(VoucherRequest request) {
        String voucherCode = request.getVoucherCode().toUpperCase();
        return voucherRepository.findById(voucherCode)
                .orElseGet(() -> {
                    if (voucherRepository.existsByCampaignNameIgnoreCase(request.getCampaignName())) {
                        throw new DuplicateRecordException("Tên chiến dịch '" + request.getCampaignName() + "' đã tồn tại.");
                    }
                    return new Voucher(voucherCode, LocalDateTime.now());
                });
    }

    private void mapRequestToEntity(VoucherRequest request, Voucher voucher) {
        voucher.configure(
                request.getCampaignName(),
                request.getDiscountType(),
                request.getValue(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getMinimumOrderAmount(),
                request.getMaxDiscountAmount(),
                request.getTotalUsageLimit(),
                request.getPerUserLimit(),
                request.getStatus()
        );
    }

    @Override
    @Transactional
    public void delete(String id) {
        String voucherCode = id.toUpperCase();
        Voucher voucher = voucherRepository.findById(voucherCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khuyến mãi để xóa với mã: " + voucherCode));
        boolean isUsedInOrders = orderRepository.existsByVoucher_VoucherCode(voucherCode);

        if (isUsedInOrders) {
            voucher.setStatus(VoucherStatus.DISABLED);
            voucherRepository.save(voucher);
        } else {
            voucherRepository.delete(voucher);
        }
    }

    @Override
    public List<Voucher> findActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByStatusAndStartsAtBeforeAndEndsAtAfter(VoucherStatus.ACTIVE, now, now);
    }
}
