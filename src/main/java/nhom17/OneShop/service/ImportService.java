package nhom17.OneShop.service;

import nhom17.OneShop.entity.Import;
import nhom17.OneShop.entity.ImportDetail;
import nhom17.OneShop.request.ImportRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ImportService {
    Page<Import> findAll(String keyword, Integer supplierId, int page, int size);
    Import findById(int id);
    void save(ImportRequest importRequest);
    void delete(int id);
    List<ImportDetail> getHistoryForProduct(Integer productId);
}
