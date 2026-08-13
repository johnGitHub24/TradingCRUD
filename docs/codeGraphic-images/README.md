# CodeGraphic image export

Source: docs/codeGraphic.html
Tool: @mermaid-js/mermaid-cli@11 (dark)
Script: EngineeringOS/eos-minimal/hooks/export-codeGraphic-images.ps1

| File | Tab |
|------|-----|
| `01-auth.svg` / `.png` | JWT |
| `02-order.svg` / `.png` | Order CRUD |
| `03-frontend.svg` / `.png` | 前端/BFF |
| `04-packages.svg` / `.png` | 套件 |

Re-run from project root:

    & "..\EngineeringOS\eos-minimal\hooks\export-codeGraphic-images.ps1" -ProjectRoot .
