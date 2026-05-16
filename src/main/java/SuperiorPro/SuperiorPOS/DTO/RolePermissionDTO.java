package SuperiorPro.SuperiorPOS.DTO;

import lombok.Data;

@Data
public class RolePermissionDTO {
	
    private Long roleId;
    private String roleName;
    private Long permissionId;
    private String permissionName;
}
