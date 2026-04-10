package nhom17.OneShop.service;

import nhom17.OneShop.entity.ShippingCarrier;
import nhom17.OneShop.request.ShippingCarrierRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ShippingCarrierService {
    Page<ShippingCarrier> search(String keyword, int page, int size);
    List<ShippingCarrier> findAll();
    ShippingCarrier findById(int id);
    void save(ShippingCarrierRequest request);
    void delete(int id);
}
