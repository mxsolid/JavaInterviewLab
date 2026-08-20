import { ArrowLeftOutlined, StarFilled, StarOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Descriptions, Divider, Space, Tabs, Tag, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { EnglishTermSpeaker } from '../../components/EnglishTermSpeaker';
import { ErrorState, LoadingState } from '../../components/states';
import { contentApi } from './api';
import { NoteEditor } from '../study/NoteEditor';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';

const answerLabels: Record<string, string> = { QUICK_30S: '30 秒回答', STANDARD: '标准回答', DEEP: '深入回答' };
function TextSection({ title, content }: { title: string; content?: string }) { return content ? <Card size="small" title={title}><Typography.Paragraph style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{content}</Typography.Paragraph></Card> : null; }

export function QuestionDetailPage() {
  const { id } = useParams(); const navigate = useNavigate();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['question', id], queryFn: () => contentApi.question(Number(id)), enabled: Boolean(id) });
  const favorites = useQuery({ queryKey: studyQueryKeys.favorites, queryFn: studyApi.favorites });
  const favoriteMutation = useMutation({
    mutationFn: async (questionId: number) => {
      const favored = favorites.data?.some((favorite) => favorite.questionId === questionId) ?? false;
      return favored ? studyApi.unfavoriteQuestion(questionId) : studyApi.favoriteQuestion(questionId);
    },
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: studyQueryKeys.favorites }),
  });
  if (query.isLoading) return <LoadingState />;
  if (query.isError || !query.data) return <ErrorState description="题目详情加载失败" />;
  const item = query.data;
  const isFavorite = favorites.data?.some((favorite) => favorite.questionId === item.id) ?? false;
  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/questions')}>返回题库</Button>
    <Card className="question-hero">
      <Space align="start" style={{ width: '100%', justifyContent: 'space-between' }}><Typography.Title level={2}>{item.title}</Typography.Title><Button icon={isFavorite ? <StarFilled /> : <StarOutlined />} loading={favoriteMutation.isPending} onClick={() => favoriteMutation.mutate(item.id)}>{isFavorite ? '已收藏' : '收藏'}</Button></Space>
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
    <Card title="我的笔记"><NoteEditor targetType="QUESTION" targetId={item.id} /></Card>
    {item.followUps.length > 0 && <Card title="高频追问"><Space direction="vertical" style={{ width: '100%' }}>{item.followUps.map((followUp) => <Alert key={followUp.id} type="info" message={followUp.title} description={followUp.referenceAnswer} />)}</Space></Card>}
    <Card size="small" title="英文术语发音"><Space><span>HashMap</span><EnglishTermSpeaker text="HashMap" /><span>Thread Pool</span><EnglishTermSpeaker text="Thread Pool" /></Space></Card>
  </Space>;
}
