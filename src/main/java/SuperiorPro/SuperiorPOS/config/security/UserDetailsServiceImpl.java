package SuperiorPro.SuperiorPOS.config.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import SuperiorPro.SuperiorPOS.entity.User;
import SuperiorPro.SuperiorPOS.entity.UserRole;
import SuperiorPro.SuperiorPOS.repository.UserRepository;
import SuperiorPro.SuperiorPOS.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String trimmedUsername = username.trim();

        User user = userRepository.findByUserNameIgnoreCase(trimmedUsername)
            .orElseThrow(() -> new UsernameNotFoundException("❌ User '%s' not found".formatted(trimmedUsername)));

        List<UserRole> roles = userRoleRepository.findByIdUserId(user.getId());

        List<GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase()))
            .collect(Collectors.toList());

        log.debug("🔐 Loaded user '{}' with roles {}", user.getUserName(), authorities);

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUserName())
            .password(user.getPassword())
            .authorities(authorities)
            .build();
    }
}
