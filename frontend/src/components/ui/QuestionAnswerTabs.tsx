import { Empty, Tabs } from 'antd';
import type { components } from '../../api/generated/schema';
import { MarkdownRenderer } from './MarkdownRenderer';

type Answer = components['schemas']['AnswerItem'];

const answerTabs = [
  { key: 'QUICK_30S', label: '30 秒回答' },
  { key: 'STANDARD', label: '标准回答' },
  { key: 'DEEP', label: '深入原理' },
];

export function QuestionAnswerTabs({ answers }: { answers: Answer[] }) {
  return <Tabs items={answerTabs.map((tab) => {
    const answer = answers.find((item) => item.answerType === tab.key)?.content;
    return {
      key: tab.key,
      label: tab.label,
      children: answer ? <MarkdownRenderer content={answer} /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="该层级内容尚未维护" />,
    };
  })} />;
}
