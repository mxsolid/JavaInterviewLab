import { ArrowLeftOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Alert, Button, Card, Descriptions, Divider, Space, Tabs, Tag, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { EnglishTermSpeaker } from '../../components/EnglishTermSpeaker';
import { ErrorState, LoadingState } from '../../components/states';
import { contentApi } from './api';

const answerLabels: Record<string, string> = { QUICK_30S: '30 秒回答', STANDARD: '标准回答', DEEP: '深入回答' };
function TextSection({ title, content }: { title: string; content?: string }) { return content ? <Card size="small" title={title}><Typography.Paragraph style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{content}</Typography.Paragraph></Card> : null; }

export function QuestionDetailPage() {
  const { id } = useParams(); const navigate = useNavigate();
  const query = useQuery({ queryKey: ['question', id], queryFn: () => contentApi.question(Number(id)), enabled: Boolean(id) });
  if (query.isLoading) return <LoadingState />;
  if (query.isError || !query.data) return <ErrorState description="题目详情加载失败" />;
  const item = query.data;
  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/questions')}>返回题库</Button>
    <Card className="question-hero">
      <Typography.Title level={2}>{item.title}</Typography.Title>
      <Space wrap>{'★'.repeat(item.starLevel)} <Tag color="blue">{item.difficulty}</Tag><Tag color="purple">{item.frequencyLevel}</Tag>{item.tags.map((tag) => <Tag key={tag.id}>{tag.name}</Tag>)}</Space>
      <Divider />
      <Descriptions column={{ xs: 1, md: 2 }} items={[{ key: 'topic', label: '专题', children: item.topicName }, { key: 'category', label: '分类', children: item.categoryName }]} />
    </Card>
    <TextSection title="一句话理解" content={item.oneLiner} />
    <TextSection title="通俗讲解" content={item.plainExplanation} />
    <TextSection title="为什么这样设计" content={item.designReason} />
    {item.answers.length > 0 && <Card title="面试回答"><Tabs items={item.answers.map((answer) => ({ key: answer.answerType, label: answerLabels[answer.answerType] ?? answer.answerType, children: <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>{answer.content}</Typography.Paragraph> }))} /></Card>}
    <TextSection title="常见错误 / 易错点" content={item.commonMistakes} />
    <TextSection title="面试得分点" content={item.scorePoints} />
    {item.followUps.length > 0 && <Card title="高频追问"><Space direction="vertical" style={{ width: '100%' }}>{item.followUps.map((followUp) => <Alert key={followUp.id} type="info" message={followUp.title} description={followUp.referenceAnswer} />)}</Space></Card>}
    <Card size="small" title="英文术语发音"><Space><span>HashMap</span><EnglishTermSpeaker text="HashMap" /><span>Thread Pool</span><EnglishTermSpeaker text="Thread Pool" /></Space></Card>
  </Space>;
}
