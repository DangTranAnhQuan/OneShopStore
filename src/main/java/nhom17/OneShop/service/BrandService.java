package nhom17.OneShop.service;

import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.request.BrandRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BrandService {
    Page<Brand> searchAndFilter(String keyword, Boolean status, int page, int size);
    List<Brand> findAll();
    Brand findById(int id);
    void save(BrandRequest brandRequest);
    void delete(int id);
}
