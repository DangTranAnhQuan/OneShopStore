package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Supplier;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.repository.ImportRepository;
import nhom17.OneShop.repository.SupplierRepository;
import nhom17.OneShop.request.SupplierRequest;
import nhom17.OneShop.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired private
    ImportRepository importRepository;

    @Override
    public Page<Supplier> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("supplierId").ascending());
        if (StringUtils.hasText(keyword)) {
            return supplierRepository.findBySupplierNameContainingIgnoreCase(keyword, pageable);
        }
        return supplierRepository.findAll(pageable);
    }

    @Override
    public List<Supplier> findAll(Sort sort) {
        return supplierRepository.findAll(sort);
    }

    @Override
    public Supplier findById(int id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(SupplierRequest supplierRequest) {
        validateUniqueSupplierName(supplierRequest);
        Supplier supplier = prepareSupplierEntity(supplierRequest);
        mapRequestToEntity(supplierRequest, supplier);
        supplierRepository.save(supplier);
    }

    private void validateUniqueSupplierName(SupplierRequest request) {
        if (request.getSupplierId() == null) {
            if (supplierRepository.existsBySupplierNameIgnoreCase(request.getSupplierName())) {
                throw new DuplicateRecordException("Tên nhà cung cấp '" + request.getSupplierName() + "' đã tồn tại.");
            }
        } else {
            if (supplierRepository.existsBySupplierNameIgnoreCaseAndSupplierIdNot(request.getSupplierName(), request.getSupplierId())) {
                throw new DuplicateRecordException("Tên nhà cung cấp '" + request.getSupplierName() + "' đã được sử dụng.");
            }
        }
    }

    private Supplier prepareSupplierEntity(SupplierRequest request) {
        if (request.getSupplierId() != null) {
            return findById(request.getSupplierId());
        }
        return new Supplier(request.getSupplierName(), request.getPhoneNumber(), request.getAddress());
    }

    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        supplier.updateInfo(request.getSupplierName(), request.getPhoneNumber(), request.getAddress());
    }

    @Override
    @Transactional
    public void delete(int id) {
        Supplier supplierToDelete = findById(id);
        if (importRepository.existsBySupplier_SupplierId(id)) {
            throw new DataIntegrityViolationException("Không thể xóa nhà cung cấp '" + supplierToDelete.getSupplierName() + "' vì đã có phiếu nhập liên quan.");
        }
        supplierRepository.delete(supplierToDelete);
    }
}
