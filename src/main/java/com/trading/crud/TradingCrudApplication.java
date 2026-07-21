package com.trading.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 【職責】Spring Boot 應用程式進入點：啟動內嵌容器並載入 {@code com.trading.crud} 下所有元件。
 * 【技巧】{@code @SpringBootApplication} 合併自動設定、{@code @ComponentScan} 與 {@code @Configuration}；
 *         {@link SpringApplication#run} 讀取 {@code application.yml}、建立 IoC 容器並啟動內嵌 Tomcat。
 * 【概念】此類只負責「開機」，不含商業邏輯。HTTP 請求進入 Controller 後才開始業務流程；
 *         若把邏輯寫在 {@code main}，將無法被 Spring 管理、也難以單元測試。
 * 【邊界】不負責 API 路由、安全過濾鏈或資料庫種子（見各 Configuration／Controller）。
 */
@SpringBootApplication
public class TradingCrudApplication {

    /**
     * 【職責】啟動應用程式。
     * 【技巧】{@code SpringApplication.run(主類, args)}；可用 {@code --spring.profiles.active=dev} 切換設定。
     * 【概念】命令列參數會覆寫設定檔，方便同一份 JAR 在不同環境啟動。
     *
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        // SpringApplication.run 會讀取 application.yml、建立 Bean、啟動 Web 伺服器
        SpringApplication.run(TradingCrudApplication.class, args);
    }
}
