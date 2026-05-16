package SuperiorPro.SuperiorPOS.config.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import SuperiorPro.SuperiorPOS.DTO.LoginRequest;
import SuperiorPro.SuperiorPOS.config.jwtcontroller.AuthResponse;
import SuperiorPro.SuperiorPOS.config.jwtcontroller.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
			String raw = br.lines().collect(Collectors.joining("\n"));
			LoginRequest login = new ObjectMapper().readValue(raw, LoginRequest.class);
			return authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
		} catch (IOException e) {
			throw new RuntimeException("Invalid login payload", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication auth) throws IOException {
		List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		String accessToken = jwtTokenProvider.generateAccessToken(auth.getName(), roles);
		String refreshToken = jwtTokenProvider.generateRefreshToken(auth.getName(), roles);

		response.setHeader("Authorization", "Bearer " + accessToken);
		response.setContentType("application/json");
		response.getWriter().write(new ObjectMapper()
				.writeValueAsString(new AuthResponse(accessToken, refreshToken, "✅ Login successful")));
	}
}
