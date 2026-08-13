# Testing and Verification — TradingCRUD

> 衝突以 [TradingCRUD 規格書.md](../TradingCRUD%20規格書.md) 與 [測試規格書.md](測試規格書.md) 為準。  
> 本檔為 EOS 精簡入口；Case ID／腳本細節見下方中文文件。

## Check command

```powershell
.\scripts\check.ps1
```

（內部執行 `gradlew check`：單元 + 整合；需 JDK 21。）

## Test layers

| Layer | Gradle / Script | 說明 |
|-------|-----------------|------|
| 單元 | `gradlew test` | JwtService、DTO、Service（Mock） |
| 整合 | `gradlew integrationTest` | MockMvc + H2 + Security Filter |
| Smoke | `.\scripts\verify-db.ps1` | 真實 HTTP（需後端 :8083） |

## Minimum case types

| Type | Coverage（摘要） |
|------|------------------|
| Happy Path | AUTH-001、ORDER-001～、BATCH 成功 |
| Error Path | AUTH-002/003、SEC-001/002、DTO／冪等失敗 |

## DoD

- [x] Unit + integration via `.\scripts\check.ps1`
- [x] 公開 API 有 Happy + Error Path（見測試規格書）
- [ ] Smoke（可選）：後端啟動後跑 `verify-db.ps1`

## 詳細文件（請由此深入）

| 文件 | 說明 |
|------|------|
| [測試規格書.md](測試規格書.md) | Case ID 完整對照、fixture |
| [測試與CI.md](測試與CI.md) | Gradle 任務、CI、DoD 清單 |
| [test-data/](test-data/) | AUTH／ORDER／BATCH JSON fixtures |
