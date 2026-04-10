package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Rating;
import nhom17.OneShop.repository.RatingRepository;
import nhom17.OneShop.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    @Override
    public List<Rating> findByProductId(int productId) {
        return ratingRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId);
    }
}
