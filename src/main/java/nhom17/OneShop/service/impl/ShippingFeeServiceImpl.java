package nhom17.OneShop.service.impl;

import nhom17.OneShop.dto.ShippingOptionDTO;
import nhom17.OneShop.entity.AppliedProvince;
import nhom17.OneShop.entity.ShippingCarrier;
import nhom17.OneShop.entity.ShippingFee;
import nhom17.OneShop.entity.enums.ShippingMethod;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.ShippingCarrierRepository;
import nhom17.OneShop.repository.ShippingFeeRepository;
import nhom17.OneShop.request.ShippingFeeRequest;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.ShippingFeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ShippingFeeServiceImpl implements ShippingFeeService {

    @Autowired private ShippingFeeRepository shippingFeeRepository;
    @Autowired private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired private CartService cartService;

    @Override
    public List<ShippingFee> findAllByProvider(int providerId) {
        return shippingFeeRepository.findByShippingCarrier_CarrierIdOrderByShippingFeeIdDesc(providerId);
    }

    @Override
    public ShippingFee findById(int id) {
        return shippingFeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy gói phí vận chuyển với ID: " + id));
    }

    @Override
    @Transactional
    public void save(ShippingFeeRequest request) {
        validateRequest(request);
        ShippingFee entity = prepareEntity(request);
        mapToEntity(request, entity);
    }

    private void validateRequest(ShippingFeeRequest request) {
        Integer feeId = request.getShippingFeeId();
        Integer carrierId = request.getCarrierId();
        ShippingMethod method = request.getShippingMethod();

        if (request.getMaxDeliveryDays() <= request.getMinDeliveryDays()) {
            throw new IllegalArgumentException("Ngày giao muộn nhất không được nhỏ hơn ngày giao sớm nhất.");
        }

        if (feeId == null) {
            if (shippingFeeRepository.existsByShippingMethodAndPackageNameIgnoreCaseAndShippingCarrier_CarrierId(
                    method, request.getPackageName().trim(), carrierId)) {
                throw new DuplicateRecordException("Phương thức vận chuyển '" + method.name() + "' đã tồn tại trong gói cước '" + request.getPackageName() + "' của nhà vận chuyển này.");
            }
        } else {
            if (shippingFeeRepository.existsByShippingMethodAndPackageNameIgnoreCaseAndShippingCarrier_CarrierIdAndShippingFeeIdNot(
                    method, request.getPackageName().trim(), carrierId, feeId)) {
                throw new DuplicateRecordException("Phương thức vận chuyển '" + method.name() + "' đã bị trùng trong gói cước '" + request.getPackageName() + "' của nhà vận chuyển này.");
            }
        }


        if (request.getAppliedProvinces() != null && !request.getAppliedProvinces().isEmpty()) {
            List<ShippingFee> existingFees = shippingFeeRepository.findByShippingCarrier_CarrierIdOrderByShippingFeeIdDesc(carrierId);

            for (ShippingFee fee : existingFees) {
                if (fee.getShippingFeeId().equals(feeId)) continue;

                for (AppliedProvince province : fee.getAppliedProvinces()) {
                    if (request.getAppliedProvinces().contains(province.getProvinceName())) {
                        if (fee.getShippingMethod() == request.getShippingMethod()) {
                            throw new IllegalArgumentException("Tỉnh '" + province.getProvinceName() + "' đã được áp dụng cho phương thức '" + fee.getShippingMethod().name() + "' trong gói '" + fee.getPackageName() + "'.");
                        }
                    }
                }
            }
        }
    }


    private ShippingFee prepareEntity(ShippingFeeRequest request) {
        if (request.getShippingFeeId() != null) {
            return this.findById(request.getShippingFeeId());
        }
        return new ShippingFee();
    }

    private void mapToEntity(ShippingFeeRequest request, ShippingFee entity) {
        ShippingCarrier provider = shippingCarrierRepository.findById(request.getCarrierId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + request.getCarrierId()));

        entity.setPackageName(request.getPackageName().trim());
        entity.setShippingCarrier(provider);
        entity.setShippingMethod(request.getShippingMethod());
        entity.setFeeAmount(request.getFeeAmount());
        entity.setMinDeliveryDays(request.getMinDeliveryDays());
        entity.setMaxDeliveryDays(request.getMaxDeliveryDays());
        entity.setTimeUnit(request.getTimeUnit());

        updateAppliedProvinces(request, entity);
    }

    private void updateAppliedProvinces(ShippingFeeRequest request, ShippingFee entity) {
        boolean isNew = entity.getShippingFeeId() == null;
        if (isNew) {
            shippingFeeRepository.saveAndFlush(entity);
        }

        if (entity.getAppliedProvinces() == null) {
            entity.setAppliedProvinces(new HashSet<>());
        }

        Set<AppliedProvince> newProvinces = new HashSet<>();
        if (request.getAppliedProvinces() != null) {
            for (String provinceName : request.getAppliedProvinces()) {
                AppliedProvince appliedProvince = new AppliedProvince();
                appliedProvince.setProvinceName(provinceName);
                appliedProvince.setShippingFee(entity);
                newProvinces.add(appliedProvince);
            }
        }

        entity.getAppliedProvinces().clear();
        entity.getAppliedProvinces().addAll(newProvinces);

        if (!isNew) {
            shippingFeeRepository.save(entity);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!shippingFeeRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy gói phí vận chuyển để xóa.");
        }
        shippingFeeRepository.deleteById(id);
    }

    private BigDecimal calculateFinalShippingCost(BigDecimal subtotal, BigDecimal originalCost) {
        BigDecimal oneMillion = new BigDecimal("1000000");
        BigDecimal fiveHundredThousand = new BigDecimal("500000");

        if (subtotal.compareTo(oneMillion) >= 0) {
            return BigDecimal.ZERO;
        } else if (subtotal.compareTo(fiveHundredThousand) >= 0) {
            return originalCost.multiply(new BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP);
        } else {
            return originalCost;
        }

    }

    private ShippingOptionDTO convertToDTO(ShippingFee fee) {
        ShippingOptionDTO dto = new ShippingOptionDTO();

        dto.setShippingFeeId(fee.getShippingFeeId());
        dto.setShippingMethod(fee.getShippingMethod());
        dto.setShippingMethodLabel(fee.getShippingMethod() == null ? null : fee.getShippingMethod().getLabel());
        dto.setFeeAmount(fee.getFeeAmount());
        dto.setMinDeliveryDays(fee.getMinDeliveryDays());
        dto.setMaxDeliveryDays(fee.getMaxDeliveryDays());
        dto.setTimeUnit(fee.getTimeUnit());

        String carrierName = (fee.getShippingCarrier() != null) ? fee.getShippingCarrier().getCarrierName() : "N/A";
        dto.setCarrierName(carrierName);

        dto.setPackageName(fee.getPackageName());

        return dto;
    }

    @Override
    public Optional<ShippingOptionDTO> findCheapestShippingOption(String province, BigDecimal subtotal) {
        List<ShippingOptionDTO> options = findAvailableShippingOptions(province);
        if (options.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(options.getFirst());
    }

    @Override
    public List<ShippingOptionDTO> findAvailableShippingOptions(String province) {
        List<ShippingFee> applicableFees = shippingFeeRepository.findShippingFeesByProvince(province);
        BigDecimal subtotal = cartService.getSubtotal();

        Map<ShippingMethod, Optional<ShippingFee>> cheapestFeesByMethod = applicableFees.stream()
                .collect(Collectors.groupingBy(
                        ShippingFee::getShippingMethod,
                        Collectors.minBy(Comparator.comparing(ShippingFee::getFeeAmount))
                ));

        return cheapestFeesByMethod.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(fee -> {
                    ShippingOptionDTO dto = convertToDTO(fee);
                    BigDecimal finalCost = calculateFinalShippingCost(subtotal, fee.getFeeAmount());
                    dto.setFeeAmount(finalCost);
                    return dto;
                })
                .sorted(Comparator.comparing(ShippingOptionDTO::getFeeAmount))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShippingMethod> findDistinctShippingMethods() {
        return shippingFeeRepository.findDistinctShippingMethod();
    }
}