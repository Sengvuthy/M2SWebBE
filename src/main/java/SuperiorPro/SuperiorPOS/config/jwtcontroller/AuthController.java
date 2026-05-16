package SuperiorPro.SuperiorPOS.config.jwtcontroller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.exception.ResourceNotFoundException;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
		String refreshToken = request.getRefreshToken();
		if (refreshToken == null || !jwtTokenProvider.isTokenValid(refreshToken)) {
			return ResponseEntity.status(401).body(new AuthResponse(null, null, "❌ Refresh token expired or invalid"));
		}

		String username = jwtTokenProvider.getUsername(refreshToken);
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", username));

		List<String> roles = user.getUserRoles().stream().map(ur -> "ROLE_" + ur.getRole().getRoleName().toUpperCase())
				.collect(Collectors.toList());

		String newAccess = jwtTokenProvider.generateAccessToken(username, roles);
		String newRefresh = jwtTokenProvider.generateRefreshToken(username, roles);

		return ResponseEntity.ok(new AuthResponse(newAccess, newRefresh, "✅ Token refreshed"));
	}
}
