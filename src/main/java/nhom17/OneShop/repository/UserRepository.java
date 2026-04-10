package nhom17.OneShop.repository;

import nhom17.OneShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Integer userId);
    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, Integer userId);
    boolean existsByRole_RoleId(Integer roleId);
    boolean existsByMembershipTier_TierId(Integer tierId);

//    User
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.membershipTier WHERE u.email = :email")
    Optional<User> findByEmailWithMembership(@Param("email") String email);
}