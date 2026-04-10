package nhom17.OneShop.specification;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> cb.like(root.get("username"), "%" + username + "%");
    }

    public static Specification<User> hasRole(Integer roleId) {
        return (root, query, cb) -> cb.equal(root.get("role").get("roleId"), roleId);
    }

    public static Specification<User> hasMembershipTier(Integer tierId) {
        return (root, query, cb) -> cb.equal(root.get("membershipTier").get("tierId"), tierId);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
