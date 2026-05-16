package SuperiorPro.SuperiorPOS.DTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
	
	private Long id;
	@NotBlank
    @Size(max = 255)
    private String username;
	@NotBlank
    @Size(max = 255)
    private String password;
	@Size(max = 20)
	private String phoneNumber;
	
	private List<String> roles;
}
