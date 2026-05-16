package SuperiorPro.SuperiorPOS.config.jwtcontroller;

import lombok.Data;

@Data
public class AuthRequest {
	
    private String username;
    private String password;
}
