import { ArrowLeftOutlined, StarFilled, StarOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Col, Descriptions, Row, Segmented, Space, Tag, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { EnglishTermSpeaker } from '../../components/EnglishTermSpeaker';
import { ErrorState, LoadingState } from '../../components/states';
import { MarkdownRenderer } from '../../components/ui/MarkdownRenderer';
import { PageHeader } from '../../components/ui/PageHeader';
import { QuestionAnswerTabs } from '../../components/ui/QuestionAnswerTabs';
import { SectionCard } from '../../components/ui/SectionCard';
import { DifficultyTag, FrequencyTag, MasteryBadge, StarRating } from '../../components/ui/StudyTags';
import { NoteEditor } from '../study/NoteEditor';
import { PracticePanel } from '../study/PracticePanel';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';
import { contentApi } from './api';
import { questionWorkspaceApi, type QuestionLearning } from './questionWorkspaceApi';
import { findTechnicalTerms } from './technicalTerms';

type WorkspaceMode = 'PRACTICE' | 'LEARN';

function KnowledgeSection({ title, content }: { title: string; content?: string }) {
  return content ? <SectionCard title={title}><MarkdownRenderer content={content} /></SectionCard> : null;
}

function LearningContent({ learning }: { learning: QuestionLearning }) {
  return <Space orientation="vertical" size={16} style={{ width: '100%' }}>
    <KnowledgeSection title="一句话理解" content={learning.oneLiner} />
    <KnowledgeSection title="通俗讲解" content={learning.plainExplanation} />
    <KnowledgeSection title="为什么这样设计" content={learning.designReason} />
    <SectionCard title="面试回答"><QuestionAnswerTabs answers={learning.answers ?? []} /></SectionCard>
    <KnowledgeSection title="常见错误 / 易错点" content={learning.commonMistakes} />
    <KnowledgeSection title="面试得分点" content={learning.scorePoints} />
    {(learning.followUps ?? []).length > 0 && <SectionCard title="高频追问">
      <Space orientation="vertical" style={{ width: '100%' }}>{learning.followUps?.map((item) => <Alert key={item.id ?? item.title} type="info" title={item.title} description={item.referenceAnswer || '该追问暂未维护参考答案。'} />)}</Space>
    </SectionCard>}
  </Space>;
}

export function QuestionDetailPage() {
  const { id } = useParams();
  const questionId = Number(id);
  const validQuestionId = Number.isInteger(questionId) && questionId > 0;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [mode, setMode] = useState<WorkspaceMode>('PRACTICE');
  const [revealedLearning, setRevealedLearning] = useState<QuestionLearning>();
  const question = useQuery({ queryKey: studyQueryKeys.question(questionId), queryFn: () => questionWorkspaceApi.question(questionId), enabled: validQuestionId });
  const learning = useQuery({ queryKey: studyQueryKeys.questionLearning(questionId), queryFn: () => questionWorkspaceApi.learning(questionId), enabled: validQuestionId && mode === 'LEARN' });
  const progress = useQuery({ queryKey: studyQueryKeys.questionProgress(questionId), queryFn: () => studyApi.getQuestionProgress(questionId), enabled: validQuestionId });
  const favorites = useQuery({ queryKey: studyQueryKeys.favorites, queryFn: studyApi.favorites, enabled: validQuestionId });
  const related = useQuery({
    queryKey: ['content', 'related-questions', question.data?.topicId],
    queryFn: () => contentApi.questions({ topicId: question.data?.topicId, status: 'ENABLED', page: 1, pageSize: 6 }),
    enabled: Boolean(question.data?.topicId),
  });
  const favoriteMutation = useMutation({
    mutationFn: (favorite: boolean) => favorite ? studyApi.unfavoriteQuestion(questionId) : studyApi.favoriteQuestion(questionId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.favorites }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard }),
      ]);
      message.success('收藏状态已更新');
    },
    onError: () => message.error('收藏状态更新失败'),
  });

  useEffect(() => {
    setMode('PRACTICE');
    setRevealedLearning(undefined);
  }, [questionId]);

  const activeLearning = mode === 'LEARN' ? learning.data : revealedLearning;
  const terms = useMemo(() => findTechnicalTerms([
    question.data?.title,
    activeLearning?.oneLiner,
    activeLearning?.plainExplanation,
    activeLearning?.designReason,
    ...(activeLearning?.answers ?? []).map((answer) => answer.content),
  ].filter(Boolean).join('\n')), [activeLearning, question.data?.title]);

  if (!validQuestionId) return <ErrorState description="题目编号无效" />;
  if (question.isLoading) return <LoadingState />;
  if (question.isError || !question.data?.id) return <ErrorState description="题目工作区加载失败" />;

  const item = question.data;
  const currentQuestionId = item.id as number;
  const isFavorite = favorites.data?.some((favorite) => favorite.questionId === item.id) ?? false;
  const relatedItems = (related.data?.items ?? []).filter((relatedItem) => relatedItem.id !== item.id).slice(0, 5);

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/questions')}>返回题库</Button>
    <PageHeader title="题目学习" description="先输出，再披露答案；掌握度只在提交练习后更新。" />
    <SectionCard className="question-hero">
      <Space orientation="vertical" size={14} style={{ width: '100%' }}>
        <Space align="start" style={{ width: '100%', justifyContent: 'space-between' }}>
          <Typography.Title level={2} style={{ margin: 0 }}>{item.title}</Typography.Title>
          <Button icon={isFavorite ? <StarFilled /> : <StarOutlined />} loading={favoriteMutation.isPending} onClick={() => favoriteMutation.mutate(isFavorite)}>{isFavorite ? '已收藏' : '收藏'}</Button>
        </Space>
        <Space wrap><StarRating value={item.starLevel ?? 0} /><DifficultyTag value={item.difficulty ?? 'MEDIUM'} /><FrequencyTag value={item.frequencyLevel ?? 'MEDIUM'} />{item.tags?.map((tag) => <Tag key={tag.id}>{tag.name}</Tag>)}</Space>
        <Descriptions column={{ xs: 1, md: 3 }} items={[
          { key: 'topic', label: '专题', children: item.topicName },
          { key: 'category', label: '分类', children: item.categoryName },
          { key: 'version', label: '内容版本', children: item.sourceVersion ?? '未标注' },
        ]} />
      </Space>
    </SectionCard>

    <Row gutter={[20, 20]} className="question-workspace">
      <Col xs={24} xl={16}>
        <Space orientation="vertical" size={16} style={{ width: '100%' }}>
          <Segmented block value={mode} options={[{ value: 'PRACTICE', label: '练习模式' }, { value: 'LEARN', label: '学习模式' }]} onChange={(value) => setMode(value as WorkspaceMode)} />
          {mode === 'PRACTICE' && <PracticePanel questionId={currentQuestionId} onLearningLoaded={setRevealedLearning} />}
          {mode === 'LEARN' && learning.isLoading && <LoadingState />}
          {mode === 'LEARN' && learning.isError && <ErrorState description="教学内容加载失败" />}
          {mode === 'LEARN' && learning.data && <LearningContent learning={learning.data} />}
        </Space>
      </Col>
      <Col xs={24} xl={8}>
        <Space orientation="vertical" size={16} style={{ width: '100%' }}>
          <SectionCard title="学习进度">
            {progress.isLoading && <LoadingState />}
            {progress.isError && <ErrorState description="进度加载失败" />}
            {progress.data && <Space orientation="vertical" size={10}>
              <MasteryBadge value={progress.data.masteryLevel} description={progress.data.masteryDescription} />
              <Typography.Text>阶段：{progress.data.stageDescription}</Typography.Text>
              <Typography.Text type="secondary">练习 {progress.data.attemptCount} 次 · 错误 {progress.data.wrongCount} 次</Typography.Text>
            </Space>}
          </SectionCard>
          <SectionCard title="我的笔记"><NoteEditor targetType="QUESTION" targetId={currentQuestionId} /></SectionCard>
          {relatedItems.length > 0 && <SectionCard title="相关题目"><div className="data-list">{relatedItems.map((relatedItem) => <div className="data-list-item" key={relatedItem.id}>
            <Typography.Text>{relatedItem.title}</Typography.Text><Button type="link" onClick={() => navigate(`/questions/${relatedItem.id}`)}>打开</Button>
          </div>)}</div></SectionCard>}
          {terms.length > 0 && <SectionCard title="英文术语"><Space wrap>{terms.map((term) => <Space key={term.key}><span>{term.label}</span><EnglishTermSpeaker text={term.label} speechText={term.speechText} /></Space>)}</Space></SectionCard>}
        </Space>
      </Col>
    </Row>
  </Space>;
}
