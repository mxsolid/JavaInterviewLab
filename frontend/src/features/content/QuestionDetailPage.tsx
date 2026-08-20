import { ArrowLeftOutlined, StarFilled, StarOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Descriptions, Segmented, Space, Tabs, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { EnglishTermSpeaker } from '../../components/EnglishTermSpeaker';
import { MarkdownRenderer } from '../../components/ui/MarkdownRenderer';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { DifficultyTag, FrequencyTag, StarRating } from '../../components/ui/StudyTags';
import { ErrorState, LoadingState } from '../../components/states';
import { NoteEditor } from '../study/NoteEditor';
import { PracticePanel } from '../study/PracticePanel';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';
import { contentApi } from './api';
import { findTechnicalTerms } from './technicalTerms';

const answerLabels: Record<string, string> = { QUICK_30S: '30 秒回答', STANDARD: '标准回答', DEEP: '深入回答' };

function KnowledgeSection({ title, content }: { title: string; content?: string }) {
  return content ? <SectionCard title={title}><MarkdownRenderer content={content} /></SectionCard> : null;
}

export function QuestionDetailPage() {
  const { id } = useParams();
  const questionId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [mode, setMode] = useState<'学习模式' | '练习模式'>('练习模式');
  const question = useQuery({ queryKey: studyQueryKeys.question(questionId), queryFn: () => contentApi.question(questionId), enabled: Number.isInteger(questionId) });
  const favorites = useQuery({ queryKey: studyQueryKeys.favorites, queryFn: studyApi.favorites });
  const favoriteMutation = useMutation({
    mutationFn: (favorite: boolean) => favorite ? studyApi.unfavoriteQuestion(questionId) : studyApi.favoriteQuestion(questionId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: studyQueryKeys.favorites });
      message.success('收藏状态已更新');
    },
    onError: () => message.error('收藏状态更新失败'),
  });

  if (question.isLoading) return <LoadingState />;
  if (question.isError || !question.data) return <ErrorState description="题目详情加载失败" />;
  const item = question.data;
  const isFavorite = favorites.data?.some((favorite) => favorite.questionId === item.id) ?? false;
  const termText = [item.title, item.oneLiner, item.plainExplanation, item.designReason, ...item.answers.map((answer) => answer.content)].filter(Boolean).join('\n');
  const terms = findTechnicalTerms(termText);
  const standardAnswer = item.answers.find((answer) => answer.answerType === 'STANDARD')?.content ?? item.answers[0]?.content;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回题库</Button>
    <PageHeader title="题目学习" description="先理解，再输出；掌握度只在提交练习后更新。" />
    <SectionCard className="question-hero">
      <Space orientation="vertical" size={14} style={{ width: '100%' }}>
        <Space align="start" style={{ width: '100%', justifyContent: 'space-between' }}>
          <Typography.Title level={2} style={{ margin: 0 }}>{item.title}</Typography.Title>
          <Button icon={isFavorite ? <StarFilled /> : <StarOutlined />} loading={favoriteMutation.isPending} onClick={() => favoriteMutation.mutate(isFavorite)}>{isFavorite ? '已收藏' : '收藏'}</Button>
        </Space>
        <Space wrap><StarRating value={item.starLevel} /><DifficultyTag value={item.difficulty} /><FrequencyTag value={item.frequencyLevel} />{item.tags.map((tag) => <Tag key={tag.id}>{tag.name}</Tag>)}</Space>
        <Descriptions column={{ xs: 1, md: 2 }} items={[{ key: 'topic', label: '专题', children: item.topicName }, { key: 'category', label: '分类', children: item.categoryName }]} />
      </Space>
    </SectionCard>
    <Segmented value={mode} options={['学习模式', '练习模式']} onChange={(value) => setMode(value as '学习模式' | '练习模式')} />
    {mode === '学习模式' ? <Space orientation="vertical" size={16} style={{ width: '100%' }}>
      <KnowledgeSection title="一句话理解" content={item.oneLiner} />
      <KnowledgeSection title="通俗讲解" content={item.plainExplanation} />
      <KnowledgeSection title="为什么这样设计" content={item.designReason} />
      {item.answers.length > 0 && <SectionCard title="面试回答"><Tabs items={item.answers.map((answer) => ({ key: answer.answerType, label: answerLabels[answer.answerType] ?? answer.answerType, children: <MarkdownRenderer content={answer.content} /> }))} /></SectionCard>}
      <KnowledgeSection title="常见错误 / 易错点" content={item.commonMistakes} />
      <KnowledgeSection title="面试得分点" content={item.scorePoints} />
    </Space> : <PracticePanel questionId={item.id} referenceAnswer={standardAnswer} />}
    <SectionCard title="我的笔记"><NoteEditor targetType="QUESTION" targetId={item.id} /></SectionCard>
    {item.followUps.length > 0 && <SectionCard title="高频追问"><Space orientation="vertical" style={{ width: '100%' }}>{item.followUps.map((followUp) => <Alert key={followUp.id} type="info" message={followUp.title} description={followUp.referenceAnswer} />)}</Space></SectionCard>}
    {terms.length > 0 && <SectionCard title="相关英文术语发音"><Space wrap>{terms.map((term) => <Space key={term.key}><span>{term.label}</span><EnglishTermSpeaker text={term.label} speechText={term.speechText} /></Space>)}</Space></SectionCard>}
  </Space>;
}
