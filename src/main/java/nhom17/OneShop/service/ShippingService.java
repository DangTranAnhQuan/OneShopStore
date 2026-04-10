package nhom17.OneShop.service;

import nhom17.OneShop.entity.Shipping;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.request.ShippingRequest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface ShippingService {
    Page<Shipping> search(String keyword, Integer carrierId, String status, String shippingMethod, int page, int size);
    Shipping findById(Long id);
    void save(ShippingRequest request);
    void delete(Long id);
}
