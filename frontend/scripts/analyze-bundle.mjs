import { gzipSync } from 'node:zlib';
import { mkdir, readFile, readdir, stat, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

const DIST_DIRECTORY = resolve('dist');
const MANIFEST_PATH = resolve(DIST_DIRECTORY, '.vite', 'manifest.json');
const REPORT_PATH = resolve('..', 'docs', 'v03', 'validation', 'p08', 'bundle-report.md');
const MAX_CHUNK_BYTES = 500 * 1024;
const MAX_INITIAL_JS_BYTES = 850 * 1024;
const LAZY_ROUTE_SOURCES = [
  'src/features/lab/LabPage.tsx',
  'src/features/source/SourcePage.tsx',
];

const manifest = JSON.parse(await readFile(MANIFEST_PATH, 'utf8'));
const entries = Object.entries(manifest);
const entryPair = entries.find(([, value]) => value.isEntry);
if (!entryPair) throw new Error('Vite manifest 中不存在入口 chunk');

const collectStaticImports = (key, collected = new Set()) => {
  if (collected.has(key)) return collected;
  collected.add(key);
  for (const importedKey of manifest[key]?.imports ?? []) collectStaticImports(importedKey, collected);
  return collected;
};

const initialKeys = collectStaticImports(entryPair[0]);
const initialFiles = [...initialKeys]
  .map((key) => manifest[key]?.file)
  .filter((file) => file?.endsWith('.js'));
const initialJsBytes = (await Promise.all(initialFiles.map((file) => stat(resolve(DIST_DIRECTORY, file)))))
  .reduce((total, fileStat) => total + fileStat.size, 0);

const lazyFailures = LAZY_ROUTE_SOURCES.filter((source) => {
  const pair = entries.find(([key, value]) => key === source || value.src === source);
  if (!pair) throw new Error(`manifest 缺少懒加载路由：${source}`);
  return initialKeys.has(pair[0]);
});

const assetNames = (await readdir(resolve(DIST_DIRECTORY, 'assets'))).filter((name) => name.endsWith('.js'));
const chunks = await Promise.all(assetNames.map(async (name) => {
  const content = await readFile(resolve(DIST_DIRECTORY, 'assets', name));
  return { name, bytes: content.length, gzipBytes: gzipSync(content).length };
}));
chunks.sort((left, right) => right.bytes - left.bytes);

const oversizedChunks = chunks.filter((chunk) => chunk.bytes > MAX_CHUNK_BYTES);
const gateFailures = [
  ...(initialJsBytes > MAX_INITIAL_JS_BYTES
    ? [`首页初始 JS ${(initialJsBytes / 1024).toFixed(2)} KiB 超过 ${(MAX_INITIAL_JS_BYTES / 1024).toFixed(0)} KiB`]
    : []),
  ...oversizedChunks.map((chunk) => `${chunk.name} 超过 500 KiB`),
  ...lazyFailures.map((source) => `${source} 被首页静态加载`),
];

const table = chunks
  .map((chunk) => `| \`${chunk.name}\` | ${(chunk.bytes / 1024).toFixed(2)} | ${(chunk.gzipBytes / 1024).toFixed(2)} |`)
  .join('\n');
const report = `# P08 Bundle Report

- 首页初始静态 JS：${(initialJsBytes / 1024).toFixed(2)} KiB / ${(MAX_INITIAL_JS_BYTES / 1024).toFixed(0)} KiB
- 单 chunk 上限：${(MAX_CHUNK_BYTES / 1024).toFixed(0)} KiB
- Lab / Source 首页静态加载：${lazyFailures.length === 0 ? '否' : '是'}
- Gate：${gateFailures.length === 0 ? 'PASS' : 'FAIL'}

| Chunk | Raw KiB | Gzip KiB |
|---|---:|---:|
${table}
`;

await mkdir(resolve(REPORT_PATH, '..'), { recursive: true });
await writeFile(REPORT_PATH, report, 'utf8');
process.stdout.write(report);
if (gateFailures.length > 0) throw new Error(gateFailures.join('；'));
