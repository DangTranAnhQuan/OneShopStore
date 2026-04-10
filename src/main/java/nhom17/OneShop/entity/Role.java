package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "Roles")
public class Role {
    @Id
    @Column(name = "RoleId")
    private Integer roleId;

    @Column(name = "RoleName")
    private String roleName;
}
