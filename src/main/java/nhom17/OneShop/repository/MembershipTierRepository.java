package nhom17.OneShop.repository;

import nhom17.OneShop.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Integer> {
    Optional<MembershipTier> findByTierNameIgnoreCase(String tierName);
    Optional<MembershipTier> findByMinPoints(Integer minPoints);
    boolean existsByTierNameIgnoreCase(String tierName);
    boolean existsByMinPoints(Integer minPoints);
    boolean existsByTierNameIgnoreCaseAndTierIdNot(String tierName, Integer id);
    boolean existsByMinPointsAndTierIdNot(Integer minPoints, Integer id);
    List<MembershipTier> findByMinPointsLessThanEqualOrderByMinPointsDesc(int minPoints);
}
