package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.ShippingCarrier;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.ShippingCarrierRepository;
import nhom17.OneShop.request.ShippingCarrierRequest;
import nhom17.OneShop.service.ShippingCarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ShippingCarrierServiceImpl implements ShippingCarrierService {

    @Autowired
    private ShippingCarrierRepository shippingCarrierRepository;

    @Override
    public Page<ShippingCarrier> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("carrierId").ascending());
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<ShippingCarrier> filtered = shippingCarrierRepository.findAll(Sort.by("carrierId").ascending()).stream()
                .filter(ShippingCarrier::isActive)
                .filter(c -> !StringUtils.hasText(normalizedKeyword)
                        || (c.getCarrierName() != null && c.getCarrierName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @Override
    public List<ShippingCarrier> findAll() {
        return shippingCarrierRepository.findAll().stream()
                .filter(ShippingCarrier::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public ShippingCarrier findById(int id) {
        return shippingCarrierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + id));
    }

    @Override
    @Transactional
    public void save(ShippingCarrierRequest request) {
        validateCarrier(request);
        ShippingCarrier carrier = prepareCarrierEntity(request);
        mapRequestToEntity(request, carrier);
        shippingCarrierRepository.save(carrier);
    }

    private void validateCarrier(ShippingCarrierRequest request) {
        Integer carrierId = request.getCarrierId();
        String carrierName = request.getCarrierName();
        String normalizedName = carrierName == null ? "" : carrierName.trim().toLowerCase(Locale.ROOT);

        boolean duplicated = shippingCarrierRepository.findAll().stream()
                .filter(ShippingCarrier::isActive)
                .anyMatch(c -> c.getCarrierName() != null
                        && c.getCarrierName().trim().toLowerCase(Locale.ROOT).equals(normalizedName)
                        && (carrierId == null || !c.getCarrierId().equals(carrierId)));

        if (duplicated) {
            if (carrierId == null) {
                throw new DuplicateRecordException("Tên nhà vận chuyển '" + carrierName + "' đã tồn tại.");
            }
            throw new DuplicateRecordException("Tên nhà vận chuyển '" + carrierName + "' đã được sử dụng bởi một nhà vận chuyển khác.");
        }
    }

    private ShippingCarrier prepareCarrierEntity(ShippingCarrierRequest request) {
        if (request.getCarrierId() == null) {
            return new ShippingCarrier(request.getCarrierName(), request.getPhoneNumber(), request.getWebsite());
        }
        return findById(request.getCarrierId());
    }

    private void mapRequestToEntity(ShippingCarrierRequest request, ShippingCarrier carrier) {
        carrier.updateInfo(request.getCarrierName(), request.getPhoneNumber(), request.getWebsite());
    }

    @Override
    @Transactional
    public void delete(int id) {
        ShippingCarrier carrier = findById(id);
        carrier.deactivate();
        shippingCarrierRepository.save(carrier);
    }
}
