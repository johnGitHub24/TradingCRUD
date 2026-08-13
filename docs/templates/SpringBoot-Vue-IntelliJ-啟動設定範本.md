# Spring Boot + Vue + IntelliJ 啟動設定範本

> **用途：** 複製到其他 Gradle Spring Boot + Vue Vite 專案，一次套用 IDE、Console URL、前後端啟動。  
> **參考實作：** TradingCRUD（本 repo 已套用）  
> **適用：** IntelliJ IDEA 2022.3+ · JDK 21 · Gradle 8.x · Spring Boot 3.x · Vue 3 + Vite

---

## 1. 專案變數對照表（先填這張）

複製到新專案前，先建立專案專用數值，全文搜尋替換：

| 佔位符 | TradingCRUD 範例 | 你的新專案 |
|--------|------------------|------------|
| `{{PROJECT_NAME}}` | TradingCRUD | __________ |
| `{{MAIN_CLASS}}` | com.trading.crud.TradingCrudApplication | __________ |
| `{{MODULE_NAME}}` | TradingCRUD.main | __________ |
| `{{BACKEND_PORT}}` | 8083 | __________ |
| `{{FRONTEND_PORT}}` | 5173 | __________ |
| `{{FRONTEND_DIR}}` | frontend | __________ |
| `{{LOGIN_URL}}` | http://localhost:5173/login | __________ |
| `{{HOME_PATH}}` | /orders | __________ |
| `{{H2_JDBC}}` | jdbc:h2:mem:tradingcrud | __________ |
| `{{H2_USER}}` | sa | __________ |
| `{{H2_PASSWORD}}` | （空白） | __________ |
| `{{TABLE_USERS}}` | app_users | __________ |
| `{{TABLE_MAIN}}` | orders | __________ |
| `{{DEFAULT_USER}}` | admin | __________ |
| `{{DEFAULT_PASS}}` | admin123 | __________ |

---

## 2. 檔案清單（複製檢查表）

```
新專案根目錄/
├── build.gradle                          ← §3.1 mainClass + startFrontend
├── src/main/java/.../StartupInfoLogger.java   ← §3.2 Console 印 URL
├── frontend/
│   ├── vite.config.js                    ← §3.3 proxy + open
│   ├── .gitignore                        ← node_modules/
│   └── src/
│       ├── stores/auth.js                ← §3.4 getter + computed
│       ├── router/index.js               ← §3.5 unref(isLoggedIn)
│       └── api/client.js                 ← §3.6 401 排除 login
├── .idea/
│   ├── misc.xml                          ← JDK_21
│   ├── gradle.xml                        ← gradleJvm=21
│   └── runConfigurations/                ← §4 全部 XML
├── scripts/
│   ├── start.ps1                         ← §5.1
│   ├── start-frontend.ps1                ← §5.2
│   ├── fix-intellij-registry.ps1         ← §5.3（有 frontend 時）
│   └── env.ps1
└── docs/IntelliJ-IDE-啟動設定.md          ← §6 專案專用說明
```

**`.gitignore` 建議：**

```gitignore
# 保留版控 Run Configuration
.idea/workspace.xml
!.idea/runConfigurations/
```

---

## 3. 程式碼片段（複製後替換佔位符）

### 3.1 `build.gradle`

```gradle
springBoot {
    mainClass = '{{MAIN_CLASS}}'
}

tasks.named('bootRun') {
    group = 'application'
    description = 'Run backend at http://localhost:{{BACKEND_PORT}}'
}

tasks.register('startFrontend', Exec) {
    group = 'application'
    description = 'Start Vue frontend at http://localhost:{{FRONTEND_PORT}}'
    workingDir = file('{{FRONTEND_DIR}}')
    if (System.getProperty('os.name').toLowerCase().contains('windows')) {
        commandLine 'cmd', '/c', 'npm run dev'
    } else {
        commandLine 'npm', 'run', 'dev'
    }
    standardInput = System.in
    doFirst {
        if (!file('{{FRONTEND_DIR}}/node_modules').exists()) {
            exec {
                workingDir = file('{{FRONTEND_DIR}}')
                if (System.getProperty('os.name').toLowerCase().contains('windows')) {
                    commandLine 'cmd', '/c', 'npm install'
                } else {
                    commandLine 'npm', 'install'
                }
            }
        }
        logger.lifecycle('登入頁: {{LOGIN_URL}}')
    }
}
```

### 3.2 `StartupInfoLogger.java`

路徑：`src/main/java/{{PACKAGE_PATH}}/StartupInfoLogger.java`  
完整範本見：`docs/templates/files/StartupInfoLogger.java.template`

### 3.3 `frontend/vite.config.js`

```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    port: {{FRONTEND_PORT}},
    open: '/login',   // 啟動時自動開瀏覽器（類似 Grails run-app）
    proxy: {
      '/api': {
        target: 'http://localhost:{{BACKEND_PORT}}',
        changeOrigin: true
      }
    }
  }
});
```

### 3.4 `frontend/src/stores/auth.js`（關鍵）

```javascript
// token 用 getter；isLoggedIn 用 computed（模板用）
// 路由守衛必須 unref(auth.isLoggedIn)，不可直接 if (auth.isLoggedIn)
export function useAuthStore() {
  return {
    get token() { return state.token; },
    isLoggedIn: computed(() => !!state.token),
    isAdmin: computed(() => state.role === 'ADMIN'),
    setSession,
    logout
  };
}
```

### 3.5 `frontend/src/router/index.js`

```javascript
import { unref } from 'vue';

router.beforeEach((to) => {
  const auth = useAuthStore();
  const loggedIn = unref(auth.isLoggedIn);  // ← 必加，否則無限 401 循環
  if (to.meta.requiresAuth && !loggedIn) return '/login';
  if (to.meta.guest && loggedIn) return '{{HOME_PATH}}';
});
```

### 3.6 `frontend/src/api/client.js`（401 攔截）

```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLoginRequest = (error.config?.url || '').includes('/auth/login');
    if (error.response?.status === 401 && !isLoginRequest) {
      useAuthStore().logout();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

---

## 4. IntelliJ Run Configuration（`.idea/runConfigurations/`）

| 檔名 | 名稱 | 類型 | 用途 |
|------|------|------|------|
| `{{App}}Application.xml` | {{App}}Application | Spring Boot | 後端 |
| `bootRun__{{PROJECT}}_.xml` | bootRun ({{PROJECT}}) | Gradle bootRun | 後端（Gradle） |
| `Frontend__Vite_.xml` | Frontend (Vite) | Gradle startFrontend | 前端 |
| `Full_Stack.xml` | Full Stack | Compound | 後端 + 前端 |

**Spring Boot 範本**（`{{App}}Application.xml`）：

```xml
<configuration default="false" name="{{App}}Application" type="SpringBootApplicationConfigurationType" factoryName="Spring Boot">
  <module name="{{MODULE_NAME}}" />
  <option name="SPRING_BOOT_MAIN_CLASS" value="{{MAIN_CLASS}}" />
  <option name="ENABLE_BROWSER" value="true" />
  <option name="BROWSER_URL" value="http://localhost:{{BACKEND_PORT}}/swagger-ui.html" />
  <method v="2">
    <option name="Make" enabled="true" />
  </method>
</configuration>
```

**Frontend Gradle 範本**（不要用 npm 類型，易因 Node 外掛未設定而靜默失敗）：

```xml
<configuration default="false" name="Frontend (Vite)" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="taskNames">
      <list><option value="startFrontend" /></list>
    </option>
  </ExternalSystemSettings>
  <method v="2" />
</configuration>
```

**Full Stack 範本**：

```xml
<configuration default="false" name="Full Stack" type="CompoundRunConfigurationType">
  <toRun name="{{App}}Application" type="SpringBootApplicationConfigurationType" />
  <toRun name="Frontend (Vite)" type="GradleRunConfiguration" />
  <method v="2" />
</configuration>
```

---

## 5. PowerShell 腳本範本

### 5.1 `scripts/start.ps1`

見 `docs/templates/files/start.ps1.template`

### 5.2 `scripts/start-frontend.ps1`

見 `docs/templates/files/start-frontend.ps1.template`

### 5.3 `scripts/fix-intellij-registry.ps1`

有 `frontend/node_modules` 且 IntelliJ 關閉專案噴 `node_modules.use.workspace.model` 時執行。  
可直接複製 TradingCRUD 的 `scripts/fix-intellij-registry.ps1`（與專案無關）。

---

## 6. 後端 Console 應印出的 URL 清單

啟動後端後，Console 應顯示（`StartupInfoLogger`）：

| 用途 | URL 範本 |
|------|----------|
| 健康檢查 | `http://localhost:{{BACKEND_PORT}}/actuator/health` |
| 應用資訊 | `http://localhost:{{BACKEND_PORT}}/actuator/info` |
| Swagger UI | `http://localhost:{{BACKEND_PORT}}/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:{{BACKEND_PORT}}/v3/api-docs` |
| H2 Console | `http://localhost:{{BACKEND_PORT}}/h2-console` |
| H2 JDBC URL | `{{H2_JDBC}}`（帳號 `{{H2_USER}}`，密碼空白） |
| 前台登入 | `{{LOGIN_URL}}` |

**`application.yml` 需暴露 actuator：**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## 7. 新專案套用 SOP（10 步驟）

```
□ 1. 填寫 §1 變數對照表
□ 2. build.gradle 加 springBoot.mainClass + startFrontend
□ 3. 新增 StartupInfoLogger.java
□ 4. 設定 vite.config.js（port、proxy、open）
□ 5. 修正 auth store + router unref + api 401 攔截
□ 6. 複製 .idea/runConfigurations/*.xml 並替換佔位符
□ 7. misc.xml / gradle.xml 設 JDK 21
□ 8. 複製 scripts/*.ps1 並替換 URL
□ 9. frontend 執行 npm install
□ 10. IntelliJ：只開專案根目錄 → Gradle Sync → Run Full Stack
```

**驗證：**

```
後端  http://localhost:{{BACKEND_PORT}}/actuator/health  → {"status":"UP"}
前端  {{LOGIN_URL}}  → 登入頁可開啟
登入  {{DEFAULT_USER}} / {{DEFAULT_PASS}}  → 進入 {{HOME_PATH}}，無重複 401
```

---

## 8. 常見問題對照

| 症狀 | 原因 | 修正 |
|------|------|------|
| Run 選單跑到別的 Application | 開錯父資料夾 | 只開專案根目錄 |
| Full Stack 只有後端 log | npm Run Config 失敗 | 改用 Gradle `startFrontend` |
| `:5173` 連不上 | 前端未啟動 | Run Frontend 或 start-frontend.ps1 |
| Authentication required 重複跳 | router 未 `unref(isLoggedIn)` | §3.5 |
| Console 沒 URL | 未 Rebuild / 無 StartupInfoLogger | §3.2 + Rebuild |
| MissingResourceException registry | IntelliJ JS 外掛版本 | fix-intellij-registry.ps1 |
| H2 90149 `C:/Users/.../test` not found | JDBC URL 用預設 `~/test` | 改 `{{H2_JDBC}}`，見 §10 |

---

## 10. H2 Console 登入（公版）

完整說明見：**[H2-Console-登入範本.md](H2-Console-登入範本.md)**

**快速對照：**

| 欄位 | 值 |
|------|-----|
| 網址 | http://localhost:{{BACKEND_PORT}}/h2-console |
| JDBC URL | `{{H2_JDBC}}`（必須與 `application.yml` 一致） |
| User | `{{H2_USER}}` |
| Password | **留空** |

**常見 90149 錯誤：** 勿用 `jdbc:h2:~/test`，會變成 `C:/Users/你/test` 檔案庫。

**驗證 SQL 範本：** `files/h2-console-驗證.sql.template`  
**application.yml 範本：** `files/application-h2.yml.template`

---

## 9. 與 Grails 差異（給團隊說明用）

| Grails `run-app` | Spring Boot + Vue |
|------------------|-------------------|
| 一個程序、一個 URL | 後端 + 前端兩個程序 |
| Console 自動印 URL | 需 StartupInfoLogger + Vite open |
| 內建 GSP 畫面 | 登入頁在 `:5173` |

---

*範本版本：2026-07-09 · 來源專案：TradingCRUD*
