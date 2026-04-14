package nhom17.OneShop.service;

import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    Page<Inventory> findAll(String keyword, String sort, int page, int size);
    void restockOrderItems(Order order);
    void deductStockForCartItems(List<CartItem> cartItems);
}
