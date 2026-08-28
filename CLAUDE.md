# TradingCRUD — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ **0.1.13**  
公版：`EngineeringOS/eos-minimal/`  
權威規格：[TradingCRUD 規格書.md](TradingCRUD%20規格書.md)

## 與公版差異

- Backend port: 8083
- Frontend: Vue 3 + Vite（已啟用 optional-frontend）
- BFF: Node Express :3000（正式）
- DB: H2（dev/test）／PostgreSQL（prod）
- Auth: JWT HS256；角色 ADMIN／USER
- 驗證入口：`.\scripts\check.ps1`

## 本專案專屬

- Domain: Order CRUD／BATCH、Auth 登入
- 架構摘要：`docs/architecture.md` → 詳見 `docs/architecture.md`
- 測試摘要：`docs/testing.md` → 詳見 `docs/testing.md`、`docs/testing.md`
- 教學文件：`docs/` 學習手冊／HTML（不回寫公版）

## 註解深度
- comment_verbosity: **detailed**
- 權威：`EngineeringOS/eos-minimal/knowledge/comments.md` §0／§3b（eos-minimal @ 0.1.13）
- 結構：【職責】【技巧】【概念】；簡單 getter 可併入類別說明


## Git Remote
- 帳號：`johnGitHub24`；一專案一 repo
- 規範：`EngineeringOS/eos-minimal/knowledge/專案上船-GitHub.md`

## 回寫

問題與公版改善建議 → `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
