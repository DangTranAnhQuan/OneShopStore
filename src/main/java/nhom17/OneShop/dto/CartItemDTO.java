package nhom17.OneShop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer productId;
    private String productName;
    private String productImage;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static CartItemDTO fromEntity(nhom17.OneShop.entity.CartItem cartEntity) {
        CartItemDTO dto = new CartItemDTO();
        if (cartEntity.getProduct() != null) {
            dto.setProductId(cartEntity.getProduct().getProductId());
            dto.setProductName(cartEntity.getProduct().getName());
            dto.setProductImage(cartEntity.getProduct().getImageUrl());
            dto.setUnitPrice(cartEntity.getProduct().getPrice());
        }
        dto.setQuantity(cartEntity.getQuantity());
        dto.setLineTotal(cartEntity.getLineTotal());
        return dto;
    }
}