package SuperiorPro.SuperiorPOS.DTO;

import java.util.List;

import lombok.Data;

@Data
public class UserRoleBatchDTO {
	
    private Long userId;
    private List<Long> roleIds;
}
