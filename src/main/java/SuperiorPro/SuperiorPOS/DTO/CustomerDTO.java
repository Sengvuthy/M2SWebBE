package SuperiorPro.SuperiorPOS.DTO;

import lombok.Data;
import java.util.List;

@Data
public class CustomerDTO {
	
    private Long id;
    private String name;
    private List<String> phones;
    private List<String> addresses;
    private Boolean isDefault;
    private Long telegramId;
}
