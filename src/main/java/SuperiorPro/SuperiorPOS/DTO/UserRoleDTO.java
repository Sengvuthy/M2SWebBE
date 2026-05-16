package SuperiorPro.SuperiorPOS.DTO;

import lombok.Data;

@Data
public class UserRoleDTO {
	
    private Long userId;
    private String userName;
    private Long roleId;
    private String roleName;
}
