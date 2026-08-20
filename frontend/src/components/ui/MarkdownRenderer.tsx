import { Typography } from 'antd';

/** 内容目前由题库后台维护，先以安全的纯文本渲染保留 Markdown 换行与代码块可读性。 */
export function MarkdownRenderer({ content }: { content: string }) {
  const blocks = content.split(/\n{2,}/).filter(Boolean);
  return <div className="markdown-content">{blocks.map((block, index) => (
    block.startsWith('```')
      ? <pre key={index}><code>{block.replace(/^```\w*\n?|```$/g, '')}</code></pre>
      : <Typography.Paragraph key={index}>{block}</Typography.Paragraph>
  ))}</div>;
}
