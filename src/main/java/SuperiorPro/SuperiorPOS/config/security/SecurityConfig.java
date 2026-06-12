package SuperiorPro.SuperiorPOS.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import SuperiorPro.SuperiorPOS.config.jwtcontroller.JwtTokenProvider;
import SuperiorPro.SuperiorPOS.exception.FilterChainExceptionHandler;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final FilterChainExceptionHandler filterChainExceptionHandler;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(FilterChainExceptionHandler fceh, JwtTokenProvider jtp) {
        this.filterChainExceptionHandler = fceh;
        this.jwtTokenProvider = jtp;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        JwtLoginFilter loginFilter = new JwtLoginFilter(authManager, jwtTokenProvider);
        loginFilter.setFilterProcessesUrl("/api/auth/login");

        http.csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Unauthorized\"}");
            }))
            .addFilterBefore(filterChainExceptionHandler, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
            	    // --- Public static resources ---
            	    .requestMatchers("/uploads/products/**").permitAll()   // ✅ allow images
            	    .requestMatchers("/", "/index.html", "/favicon.ico", "/static/**", "/js/**", "/css/**", "/assets/**").permitAll()

            	    // --- Public endpoints ---
            	    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/categories/search").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/categories/{id}").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
            	    .requestMatchers(HttpMethod.PUT, "/api/customers/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/customers/search-by-phone").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/customers/**").permitAll()
            	    .requestMatchers("/signup", "/login-phone", "/verify-code").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/sales").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/sales/invoice/**").permitAll()
            	    .requestMatchers("/api/customers/bind-telegram-by-phone").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/customers/webhook").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/telegram/webhook").permitAll()

            	    // --- Exchange rate ---
            	    .requestMatchers(HttpMethod.GET, "/api/exchange-rate").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/exchange-rate").hasAnyRole("OWNER","ADMIN")

            	    // --- Restricted endpoints ---
            	    .requestMatchers("/api/products/**").hasAnyRole("OWNER","ADMIN")
            	    .requestMatchers("/checkout/**").authenticated()
            	    .requestMatchers("/orders/**").authenticated()
            	    .requestMatchers("/role-permissions/**").hasAnyRole("OWNER","ADMIN")
            	    .requestMatchers("/users/**").hasAnyRole("OWNER","ADMIN")

            	    // --- Reports ---
            	    .requestMatchers("/sale_report/import/**").permitAll()
            	    .requestMatchers("/api/excel/sale-reports/**").permitAll()

            	    // --- Everything else ---
            	    .anyRequest().authenticated()
            	)

            .addFilter(loginFilter)
            .addFilterAfter(new TokenVerifyFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost",
            "http://localhost:4200",
            "http://localhost:8887",
            "http://192.168.1.55:4200",
            "http://192.168.1.55:8887",
            "http://192.168.1.55",
            "http://192.168.1.99"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public HttpFirewall allowDoubleSlashFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true); // ✅ prevent RequestRejectedException
        return firewall;
    }
}
