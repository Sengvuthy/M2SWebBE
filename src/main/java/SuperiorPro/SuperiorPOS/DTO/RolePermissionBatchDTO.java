package SuperiorPro.SuperiorPOS.DTO;

import java.util.List;

import lombok.Data;

@Data
public class RolePermissionBatchDTO {
	
    private Long roleId;
    private List<Long> permissionIds;
}
