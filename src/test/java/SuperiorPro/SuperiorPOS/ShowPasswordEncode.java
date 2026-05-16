package SuperiorPro.SuperiorPOS;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ShowPasswordEncode {

	@Test
	public void showPassword() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encode = passwordEncoder.encode("testuser123");
		System.out.println(encode);
	}
}
