package com.trading.crud.config;

import com.trading.crud.security.AdminProperties;
import com.trading.crud.user.domain.Role;
import com.trading.crud.user.infrastructure.UserEntity;
import com.trading.crud.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 【職責】啟動時種子資料：確保存在預設 ADMIN 帳號。
 * 【技巧】實作 {@link CommandLineRunner}；僅在 {@code existsByUsername} 為 false 時建立；密碼經 {@link PasswordEncoder} 雜湊。
 * 【概念】適合放「初始化資料」邏輯。密碼絕不可明文儲存；已存在帳號不重設密碼，避免每次重啟覆寫。
 * 【邊界】不負責一般使用者註冊 API；正式環境請用環境變數覆寫 {@code app.admin.*}。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    /**
     * 建構子注入使用者儲存庫、密碼編碼器與管理員設定。
     *
     * @param userRepository   使用者 Repository
     * @param passwordEncoder  BCrypt 密碼編碼器
     * @param adminProperties  預設管理員帳密（來自 {@code app.admin.*}）
     */
    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    /**
     * 【職責】應用啟動後執行種子邏輯。
     * 【技巧】{@link CommandLineRunner#run} 在 Spring 容器就緒後執行一次。
     * 【概念】比在建構子寫入 DB 更安全，因為此時 Bean 與交易管理都已就緒。
     *
     * @param args 命令列參數（未使用）
     */
    @Override
    public void run(String... args) {
        seedAdmin();
    }

    /** 建立預設 ADMIN 帳號（若尚不存在）。 */
    private void seedAdmin() {
        String username = adminProperties.getUsername();
        // 若已存在則跳過，避免每次重啟都覆寫管理員密碼
        if (userRepository.existsByUsername(username)) {
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(adminProperties.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("Seeded admin account: {}", username);
    }
}
