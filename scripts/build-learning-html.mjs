/**
 * build-learning-html.mjs
 * 將 docs/ 下所有 .md 合併為單一 HTML 學習手冊
 * 執行：node scripts/build-learning-html.mjs
 */
import { readFileSync, writeFileSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = join(__dirname, '..');
const docsDir = join(root, 'docs');
const outFile = join(docsDir, 'TradingCRUD-完整學習手冊.html');

/** 章節順序（學習路線） */
const chapters = [
  { id: 'ch01', file: '初學者學習說明書.md', title: '第 1 章　初學者學習說明書', group: '入門' },
  { id: 'ch02', file: '架構學習導引.md', title: '第 2 章　架構學習導引', group: '入門' },
  { id: 'ch03', file: '功能流程說明.md', title: '第 3 章　功能流程說明', group: '入門' },
  { id: 'ch04', file: 'Vue與Nodejs技術介紹.md', title: '第 4 章　Vue.js 與 Node.js 技術介紹', group: '前端' },
  { id: 'ch05', file: '前後端串接說明.md', title: '第 5 章　前後端 API 串接', group: '前端' },
  { id: 'ch06', file: '驗證設計.md', title: '第 6 章　驗證設計', group: '後端' },
  { id: 'ch07', file: '資料庫設計.md', title: '第 7 章　資料庫設計', group: '後端' },
  { id: 'ch08', file: 'IntelliJ-IDE-啟動設定.md', title: '第 8 章　IntelliJ IDE 啟動設定', group: '工具' },
  { id: 'ch09', file: '測試規格書.md', title: '第 9 章　測試規格書', group: '測試' },
  { id: 'ch10', file: '測試與CI.md', title: '第 10 章　測試與 CI', group: '測試' },
  { id: 'ch11', file: '註解與測試模板.md', title: '第 11 章　註解與測試模板', group: '測試' },
  { id: 'ch12', file: 'templates/H2-Console-登入範本.md', title: '附錄 A　H2 Console 登入範本', group: '附錄' },
  { id: 'ch13', file: 'templates/SpringBoot-Vue-IntelliJ-啟動設定範本.md', title: '附錄 B　IDE 啟動設定範本', group: '附錄' },
  { id: 'ch14', file: 'templates/套用檢查表.md', title: '附錄 C　新專案套用檢查表', group: '附錄' },
  { id: 'ch15', file: 'templates/README.md', title: '附錄 D　可複用範本目錄', group: '附錄' },
];

function escapeHtml(s) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function inlineFormat(text) {
  let s = escapeHtml(text);
  s = s.replace(/`([^`]+)`/g, '<code>$1</code>');
  s = s.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  s = s.replace(/\*([^*]+)\*/g, '<em>$1</em>');
  s = s.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, label, href) => {
    const ch = chapters.find(
      (c) => c.file === href || c.file.endsWith('/' + href) || c.file.endsWith(href)
    );
    const url = ch ? `#${ch.id}` : href;
    return `<a href="${url}">${label}</a>`;
  });
  return s;
}

function isTableRow(line) {
  return /^\|.+\|$/.test(line.trim());
}

function parseTable(lines, start) {
  const rows = [];
  let i = start;
  while (i < lines.length && isTableRow(lines[i])) {
    rows.push(
      lines[i]
        .trim()
        .slice(1, -1)
        .split('|')
        .map((c) => c.trim())
    );
    i++;
  }
  if (rows.length < 2) return null;
  const sep = rows[1];
  if (!sep.every((c) => /^:?-+:?$/.test(c))) return null;
  const header = rows[0];
  const body = rows.slice(2);
  let html = '<table><thead><tr>';
  for (const h of header) html += `<th>${inlineFormat(h)}</th>`;
  html += '</tr></thead><tbody>';
  for (const row of body) {
    html += '<tr>';
    for (const cell of row) html += `<td>${inlineFormat(cell)}</td>`;
    html += '</tr>';
  }
  html += '</tbody></table>';
  return { html, next: i };
}

function mdToHtml(md) {
  const lines = md.replace(/\r\n/g, '\n').split('\n');
  const out = [];
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];
    const trimmed = line.trim();

    if (trimmed === '') {
      i++;
      continue;
    }

    if (/^---+$/.test(trimmed) || /^\*\*\*+$/.test(trimmed)) {
      out.push('<hr />');
      i++;
      continue;
    }

    const hm = /^(#{1,6})\s+(.+)$/.exec(line);
    if (hm) {
      const level = hm[1].length;
      out.push(`<h${level}>${inlineFormat(hm[2])}</h${level}>`);
      i++;
      continue;
    }

    if (trimmed.startsWith('```')) {
      const lang = trimmed.slice(3).trim();
      i++;
      const codeLines = [];
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        codeLines.push(lines[i]);
        i++;
      }
      i++;
      const code = codeLines.join('\n');
      if (lang === 'mermaid') {
        out.push(`<div class="mermaid">\n${code}\n</div>`);
      } else {
        out.push(
          `<pre><code class="language-${escapeHtml(lang)}">${escapeHtml(code)}</code></pre>`
        );
      }
      continue;
    }

    if (isTableRow(line)) {
      const parsed = parseTable(lines, i);
      if (parsed) {
        out.push(parsed.html);
        i = parsed.next;
        continue;
      }
    }

    if (trimmed.startsWith('>')) {
      const quoteLines = [];
      while (i < lines.length && lines[i].trim().startsWith('>')) {
        quoteLines.push(lines[i].trim().replace(/^>\s?/, ''));
        i++;
      }
      out.push(`<blockquote><p>${inlineFormat(quoteLines.join(' '))}</p></blockquote>`);
      continue;
    }

    if (/^[-*]\s+/.test(trimmed)) {
      out.push('<ul>');
      while (i < lines.length && /^[-*]\s+/.test(lines[i].trim())) {
        out.push(`<li>${inlineFormat(lines[i].trim().replace(/^[-*]\s+/, ''))}</li>`);
        i++;
      }
      out.push('</ul>');
      continue;
    }

    if (/^\d+\.\s+/.test(trimmed)) {
      out.push('<ol>');
      while (i < lines.length && /^\d+\.\s+/.test(lines[i].trim())) {
        out.push(`<li>${inlineFormat(lines[i].trim().replace(/^\d+\.\s+/, ''))}</li>`);
        i++;
      }
      out.push('</ol>');
      continue;
    }

    const para = [];
    while (
      i < lines.length &&
      lines[i].trim() !== '' &&
      !lines[i].trim().startsWith('#') &&
      !lines[i].trim().startsWith('```') &&
      !lines[i].trim().startsWith('>') &&
      !/^[-*]\s+/.test(lines[i].trim()) &&
      !/^\d+\.\s+/.test(lines[i].trim()) &&
      !isTableRow(lines[i])
    ) {
      para.push(lines[i].trim());
      i++;
    }
    if (para.length) out.push(`<p>${inlineFormat(para.join(' '))}</p>`);
  }

  return out.join('\n');
}

function buildNav() {
  let html = '';
  let lastGroup = '';
  for (const ch of chapters) {
    if (ch.group !== lastGroup) {
      html += `<h2>${ch.group}</h2>\n`;
      lastGroup = ch.group;
    }
    html += `<a href="#${ch.id}">${ch.title}</a>\n`;
  }
  html += `<h2>互動</h2>\n`;
  html += `<a href="專案引導教學.html" target="_blank">專案引導教學（互動圖）</a>\n`;
  html += `<a href="Vue與Nodejs技術介紹.html" target="_blank">Vue/Node 單章 HTML</a>\n`;
  return html;
}

function buildBody() {
  let html = '';
  for (const ch of chapters) {
    const path = join(docsDir, ch.file);
    if (!existsSync(path)) {
      console.warn('Skip missing:', ch.file);
      continue;
    }
    const md = readFileSync(path, 'utf8');
    const content = mdToHtml(md);
    html += `
<article class="chapter" id="${ch.id}">
  <header class="chapter-header">
    <span class="chapter-label">${ch.group}</span>
    <h2>${ch.title}</h2>
    <p class="source-file">來源：<code>docs/${ch.file}</code></p>
  </header>
  <div class="chapter-body md-content">
    ${content}
  </div>
</article>
<hr class="chapter-divider" />
`;
  }
  return html;
}

const html = `<!DOCTYPE html>
<html lang="zh-Hant">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>TradingCRUD — 完整學習手冊</title>
  <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
  <style>
    :root {
      --bg: #0f1419; --surface: #1a2332; --surface2: #243044; --border: #2d3a4f;
      --text: #e6edf3; --muted: #8b9cb3; --accent: #3b82f6; --accent2: #10b981;
      --warn: #f59e0b; --vue: #42b883;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: "Segoe UI", "Microsoft JhengHei", sans-serif; background: var(--bg); color: var(--text); line-height: 1.7; }
    .layout { display: flex; min-height: 100vh; }
    nav.sidebar {
      width: 280px; background: var(--surface); border-right: 1px solid var(--border);
      padding: 1.25rem 1rem; position: sticky; top: 0; height: 100vh; overflow-y: auto; flex-shrink: 0;
    }
    nav.sidebar h1 { font-size: 1rem; color: var(--accent2); margin-bottom: 0.25rem; }
    nav.sidebar .nav-sub { font-size: 0.75rem; color: var(--muted); margin-bottom: 1rem; }
    nav.sidebar h2 { font-size: 0.7rem; text-transform: uppercase; color: var(--muted); margin: 1rem 0 0.4rem; letter-spacing: 0.05em; }
    nav.sidebar a { display: block; color: var(--muted); text-decoration: none; padding: 0.3rem 0.65rem; border-radius: 6px; font-size: 0.82rem; line-height: 1.4; }
    nav.sidebar a:hover { background: var(--surface2); color: var(--accent); }
    main { flex: 1; padding: 2rem 2.5rem 5rem; max-width: 960px; min-width: 0; }
    .hero {
      background: linear-gradient(135deg, #1e3a5f 0%, #1a3d2e 50%, #0f1419 100%);
      border: 1px solid var(--border); border-radius: 12px; padding: 2rem; margin-bottom: 2.5rem;
    }
    .hero h1 { font-size: 1.85rem; margin-bottom: 0.5rem; }
    .hero p { color: var(--muted); }
    .badges { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 1rem; }
    .badge { background: var(--surface2); border: 1px solid var(--border); padding: 0.2rem 0.65rem; border-radius: 999px; font-size: 0.78rem; color: var(--accent2); }
    .chapter { margin-bottom: 3rem; scroll-margin-top: 1.5rem; }
    .chapter-header { margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 2px solid var(--accent); }
    .chapter-label { font-size: 0.75rem; color: var(--warn); text-transform: uppercase; letter-spacing: 0.08em; }
    .chapter-header h2 { font-size: 1.45rem; margin-top: 0.35rem; color: var(--text); }
    .source-file { font-size: 0.8rem; color: var(--muted); margin-top: 0.5rem; }
    .chapter-divider { border: none; border-top: 1px dashed var(--border); margin: 3rem 0; }
    .md-content h1 { font-size: 1.35rem; margin: 1.5rem 0 0.75rem; color: var(--accent); }
    .md-content h2 { font-size: 1.15rem; margin: 1.25rem 0 0.6rem; color: var(--accent); border-bottom: 1px solid var(--border); padding-bottom: 0.3rem; }
    .md-content h3 { font-size: 1rem; margin: 1rem 0 0.5rem; color: var(--vue); }
    .md-content h4 { font-size: 0.95rem; margin: 0.75rem 0 0.4rem; color: var(--muted); }
    .md-content p, .md-content li { color: #c9d4e3; margin-bottom: 0.45rem; }
    .md-content ul, .md-content ol { padding-left: 1.5rem; margin: 0.5rem 0 1rem; }
    .md-content blockquote { border-left: 3px solid var(--warn); padding: 0.5rem 1rem; margin: 1rem 0; background: var(--surface2); color: var(--muted); font-size: 0.9rem; }
    .md-content table { width: 100%; border-collapse: collapse; font-size: 0.85rem; margin: 1rem 0; }
    .md-content th, .md-content td { border: 1px solid var(--border); padding: 0.45rem 0.65rem; text-align: left; vertical-align: top; }
    .md-content th { background: var(--surface2); color: var(--accent); }
    .md-content code { background: #0d1117; padding: 0.12rem 0.35rem; border-radius: 4px; color: #79c0ff; font-size: 0.85em; }
    .md-content pre { background: #0d1117; border: 1px solid var(--border); border-radius: 8px; padding: 1rem; overflow-x: auto; font-size: 0.8rem; color: #a5d6ff; margin: 1rem 0; }
    .md-content pre code { background: none; padding: 0; color: inherit; }
    .md-content a { color: var(--accent); }
    .md-content hr { border: none; border-top: 1px solid var(--border); margin: 1.5rem 0; }
    .mermaid { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 1rem; margin: 1rem 0; overflow-x: auto; }
    footer { margin-top: 3rem; padding-top: 1rem; border-top: 1px solid var(--border); color: var(--muted); font-size: 0.85rem; }
    @media (max-width: 900px) {
      .layout { flex-direction: column; }
      nav.sidebar { width: 100%; height: auto; position: relative; max-height: 40vh; }
      main { padding: 1.25rem; }
    }
  </style>
</head>
<body>
<div class="layout">
  <nav class="sidebar">
    <h1>TradingCRUD</h1>
    <p class="nav-sub">完整學習手冊 · ${chapters.length} 章</p>
    ${buildNav()}
  </nav>
  <main>
    <header class="hero" id="top">
      <h1>TradingCRUD 完整學習手冊</h1>
      <p>整合 docs/ 目錄所有 Markdown 文件，依學習路線分章。用瀏覽器一次讀完，左側目錄可快速跳轉。</p>
      <div class="badges">
        <span class="badge">Vue 3</span>
        <span class="badge">Node.js</span>
        <span class="badge">Spring Boot</span>
        <span class="badge">JWT</span>
        <span class="badge">JPA</span>
        <span class="badge">測試</span>
      </div>
    </header>
    ${buildBody()}
    <footer>
      由 <code>scripts/build-learning-html.mjs</code> 從 docs/*.md 自動產生 ·
      更新 Markdown 後請執行 <code>.\scripts\build-learning-html.ps1</code> 重建 · TradingCRUD · 2026-07-09
    </footer>
  </main>
</div>
<script>mermaid.initialize({ startOnLoad: true, theme: 'dark', securityLevel: 'loose' });</script>
</body>
</html>`;

writeFileSync(outFile, html, 'utf8');
console.log('Written:', outFile);
console.log('Chapters:', chapters.length);
