import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
const outputPath = resolve(scriptDirectory, '..', 'openapi', 'openapi.json');
const endpoint = process.env.JIL_OPENAPI_URL ?? 'http://127.0.0.1:8080/v3/api-docs';
const response = await fetch(endpoint);

if (!response.ok) {
  throw new Error(`OpenAPI 拉取失败：HTTP ${response.status}`);
}

const document = await response.json();
await mkdir(dirname(outputPath), { recursive: true });
await writeFile(outputPath, `${JSON.stringify(document, null, 2)}\n`, 'utf8');
console.log(`OpenAPI 已保存：${outputPath}`);
