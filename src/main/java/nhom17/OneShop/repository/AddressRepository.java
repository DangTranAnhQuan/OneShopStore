package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUser_UserId(Integer userId);

    List<Address> findByUser_UserIdAndIsActiveTrue(Integer userId);
}