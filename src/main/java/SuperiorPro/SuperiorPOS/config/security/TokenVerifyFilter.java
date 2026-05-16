package SuperiorPro.SuperiorPOS.config.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import SuperiorPro.SuperiorPOS.config.jwtcontroller.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenVerifyFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenVerifyFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // Bypass verification for endpoints that must not require access token
        if (path.contains("/auth/refresh") || path.contains("/auth/login") || path.contains("/uploads/")) {
            chain.doFilter(req, res);
            return;
        }

        String hdr = req.getHeader("Authorization");
        if (hdr == null || !hdr.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = hdr.substring("Bearer ".length()).trim();

        try {
            if (!jwtTokenProvider.isTokenValid(token)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Token invalid or expired\"}");
                return;
            }

            Claims claims = jwtTokenProvider.parseClaims(token);

            List<String> authorities = claims.get("authorities", List.class);
            Set<SimpleGrantedAuthority> granted = authorities == null ? Set.of()
                    : authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, granted);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {}, roles: {}", claims.getSubject(), authorities);

            chain.doFilter(req, res);

        } catch (ExpiredJwtException e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token expired\"}");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token verification failed\"}");
        }
    }
}
