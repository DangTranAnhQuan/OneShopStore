package nhom17.OneShop.service;

import nhom17.OneShop.entity.Rating;

import java.util.List;

public interface RatingService {
    List<Rating> findByProductId(int productId);
}
