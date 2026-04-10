package nhom17.OneShop.service;

import nhom17.OneShop.entity.Supplier;
import nhom17.OneShop.request.SupplierRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface SupplierService {
    Page<Supplier> search(String keyword, int page, int size);
    List<Supplier> findAll(Sort sort);
    Supplier findById(int id);
    void save(SupplierRequest supplierRequest);
    void delete(int id);
}
