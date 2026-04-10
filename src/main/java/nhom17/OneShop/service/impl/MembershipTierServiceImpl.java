package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.MembershipTierRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.request.MembershipTierRequest;
import nhom17.OneShop.service.MembershipTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipTierServiceImpl implements MembershipTierService {

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<MembershipTier> findAllSorted() {
        return membershipTierRepository.findAll(Sort.by("minPoints").ascending());
    }

    @Override
    @Transactional
    public void save(MembershipTierRequest request) {
        validateUniqueFields(request);
        MembershipTier tier = prepareTierEntity(request);
        mapRequestToEntity(request, tier);
        membershipTierRepository.save(tier);
    }

    private void validateUniqueFields(MembershipTierRequest request) {
        Integer tierId = request.getTierId();
        if (tierId == null) {
            if (membershipTierRepository.existsByTierNameIgnoreCase(request.getTierName())) {
                throw new DuplicateRecordException("Tên hạng '" + request.getTierName() + "' đã tồn tại.");
            }
            if (membershipTierRepository.existsByMinPoints(request.getMinPoints())) {
                throw new DuplicateRecordException("Điểm tối thiểu '" + request.getMinPoints() + "' đã được sử dụng.");
            }
        } else {
            if (membershipTierRepository.existsByTierNameIgnoreCaseAndTierIdNot(request.getTierName(), tierId)) {
                throw new DuplicateRecordException("Tên hạng '" + request.getTierName() + "' đã được sử dụng bởi hạng khác.");
            }
            if (membershipTierRepository.existsByMinPointsAndTierIdNot(request.getMinPoints(), tierId)) {
                throw new DuplicateRecordException("Điểm tối thiểu '" + request.getMinPoints() + "' đã được sử dụng cho hạng khác.");
            }
        }
    }

    private MembershipTier prepareTierEntity(MembershipTierRequest request) {
        if (request.getTierId() != null) {
            return membershipTierRepository.findById(request.getTierId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hạng thành viên với ID: " + request.getTierId()));
        }
        return new MembershipTier(request.getTierName(), request.getMinPoints(), request.getDiscountPercentage());
    }

    private void mapRequestToEntity(MembershipTierRequest request, MembershipTier tier) {
        tier.update(request.getTierName(), request.getMinPoints(), request.getDiscountPercentage());
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!membershipTierRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy hạng thành viên để xóa với ID: " + id);
        }
        // Gọi đến UserRepository để kiểm tra
        if (userRepository.existsByMembershipTier_TierId(id)) {
            throw new DataIntegrityViolationException("Không thể xóa hạng thành viên này vì đang có người dùng thuộc hạng đó.");
        }
        membershipTierRepository.deleteById(id);
    }
}
