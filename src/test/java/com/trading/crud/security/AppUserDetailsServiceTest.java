package com.trading.crud.security;

import com.trading.crud.user.domain.Role;
import com.trading.crud.user.infrastructure.UserEntity;
import com.trading.crud.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 【職責】AppUserDetailsService 單元測試：載入既有使用者、查無帳號拋例外。
 * 【技巧】Mock UserRepository；斷言 ROLE_ 前綴與 disabled 旗標。
 * 【概念】登入路徑靠 UserDetails；與 AUTH-001 and AUTH-002 同一帳號契約。
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    /**
     * CASE USER-001 / AUTH-001：既有啟用 ADMIN 轉成 UserDetails。
     * Given: findByUsername=admin；When: loadUserByUsername；Then: ROLE_ADMIN、未停用。
     */
    @Test
    void USER_001_AUTH_001_loadExistingAdmin_mapsAuthorities() {
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPasswordHash("$2a$hashed");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails details = appUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$hashed");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    /**
     * CASE USER-002 / AUTH-002：查無使用者拋 UsernameNotFoundException。
     * Given: Optional.empty；When: loadUserByUsername；Then: 拋例外。
     */
    @Test
    void USER_002_AUTH_002_unknownUsername_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    /**
     * CASE SEC-002 / SEC-003：一般 USER 載入為 ROLE_USER（寫入 403／讀取允許由整合層驗證）。
     * Given: trader／USER；When: loadUserByUsername；Then: 權限為 ROLE_USER。
     */
    @Test
    void SEC_002_SEC_003_loadRegularUser_mapsRoleUser() {
        UserEntity user = new UserEntity();
        user.setUsername("trader");
        user.setPasswordHash("$2a$hashed");
        user.setRole(Role.USER);
        user.setEnabled(true);
        when(userRepository.findByUsername("trader")).thenReturn(Optional.of(user));

        UserDetails details = appUserDetailsService.loadUserByUsername("trader");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }
}
