package SuperiorPro.SuperiorPOS.config.jwtcontroller;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
	private final String secretKey = "abcddfdsf1243abcddfdsf1243abcddfdsf1243";
	private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes
	private static final long REFRESH_TOKEN_VALIDITY = 7L * 24 * 60 * 60 * 1000; // 7 days

	public String generateAccessToken(String username, List<String> authorities) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);
		return Jwts.builder().setSubject(username).claim("authorities", authorities).setIssuedAt(now).setExpiration(exp)
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes())).compact();
	}

	public String generateRefreshToken(String username, List<String> authorities) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);
		return Jwts.builder().setSubject(username).claim("authorities", authorities).setIssuedAt(now).setExpiration(exp)
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes())).compact();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public String getUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
				.parseClaimsJws(token).getBody().getSubject();
	}

	public Claims parseClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
				.parseClaimsJws(token).getBody();
	}
}
