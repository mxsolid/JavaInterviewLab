import { ArrowRightOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Card, Col, Input, Progress, Row, Select, Space, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { EmptyState, ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { StatCard } from '../../components/ui/StatCard';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { StarRating } from '../../components/ui/StudyTags';
import { studyQueryKeys } from '../study/queryKeys';
import { knowledgeApi, type KnowledgeTopic } from './api';

type KnowledgeState = 'ALL' | 'NOT_STARTED' | 'LEARNING' | 'MASTERED';

const stateOptions = [
  { value: 'ALL', label: '全部状态' },
  { value: 'NOT_STARTED', label: '未开始' },
  { value: 'LEARNING', label: '学习中' },
  { value: 'MASTERED', label: '已掌握' },
];

const stateTone = (state?: string): 'info' | 'warning' | 'success' => {
  if (state === 'MASTERED') return 'success';
  if (state === 'LEARNING') return 'warning';
  return 'info';
};

export function KnowledgePage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState<string>('ALL');
  const [state, setState] = useState<KnowledgeState>('ALL');
  const knowledge = useQuery({ queryKey: studyQueryKeys.knowledgeMap, queryFn: knowledgeApi.map });

  const topics = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLocaleLowerCase('zh-CN');
    return (knowledge.data?.categories ?? []).flatMap((item) => (item.topics ?? []).map((topic) => ({ category: item, topic })))
      .filter(({ category: item, topic }) => category === 'ALL' || item.code === category)
      .filter(({ topic }) => state === 'ALL' || topic.state === state)
      .filter(({ category: item, topic }) => !normalizedKeyword || `${item.name ?? ''} ${topic.name ?? ''} ${topic.description ?? ''}`.toLocaleLowerCase('zh-CN').includes(normalizedKeyword));
  }, [category, keyword, knowledge.data?.categories, state]);

  if (knowledge.isLoading) return <LoadingState />;
  if (knowledge.isError || !knowledge.data) return <ErrorState description="知识地图加载失败" />;

  const data = knowledge.data;
  const total = data.totalQuestionCount ?? 0;
  const touched = data.touchedQuestionCount ?? 0;
  const mastered = data.masteredQuestionCount ?? 0;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="知识地图" description="按知识域查看真实题量、学习触达和掌握状态。" />
    <Row gutter={[16, 16]}>
      <Col xs={12} md={6}><StatCard title="启用题目" value={total} suffix="题" /></Col>
      <Col xs={12} md={6}><StatCard title="已触达" value={touched} suffix="题" tone="violet" /></Col>
      <Col xs={12} md={6}><StatCard title="较熟练及以上" value={mastered} suffix="题" tone="teal" /></Col>
      <Col xs={12} md={6}><StatCard title="真实掌握率" value={total ? Math.round(mastered / total * 100) : 0} suffix="%" tone="orange" /></Col>
    </Row>

    <Card className="section-card knowledge-filters">
      <Space wrap size={12}>
        <Input allowClear prefix={<SearchOutlined />} placeholder="搜索知识域或专题" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        <Select value={category} onChange={setCategory} options={[{ value: 'ALL', label: '全部知识域' }, ...(data.categories ?? []).map((item) => ({ value: item.code ?? '', label: item.name ?? item.code ?? '未命名' }))]} />
        <Select value={state} onChange={setState} options={stateOptions} />
      </Space>
    </Card>

    {topics.length ? <Row gutter={[16, 16]}>{topics.map(({ category: categoryItem, topic }) => <Col xs={24} md={12} xl={8} key={`${categoryItem.id}-${topic.id}`}>
      <Card className="section-card section-card-hoverable knowledge-topic-card" actions={[<Button type="link" icon={<ArrowRightOutlined />} iconPlacement="end" onClick={() => navigate(`/questions?topicId=${topic.id}`)}>查看题目</Button>]}>
        <Space orientation="vertical" size={12} style={{ width: '100%' }}>
          <Space wrap style={{ justifyContent: 'space-between', width: '100%' }}>
            <Typography.Text type="secondary">{categoryItem.name}</Typography.Text>
            <StatusBadge tone={stateTone(topic.state)}>{topic.stateDescription ?? '未开始'}</StatusBadge>
          </Space>
          <Typography.Title level={4} style={{ margin: 0 }}>{topic.name}</Typography.Title>
          <StarRating value={topic.starLevel ?? 0} />
          <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }} style={{ minHeight: 44, margin: 0 }}>{topic.description || '该专题暂未补充说明。'}</Typography.Paragraph>
          <TopicProgress topic={topic} />
        </Space>
      </Card>
    </Col>)}</Row> : <EmptyState description="没有符合当前筛选条件的专题" />}
  </Space>;
}

function TopicProgress({ topic }: { topic: KnowledgeTopic }) {
  const rate = Math.round((topic.masteryRate ?? 0) * 100);
  return <Space orientation="vertical" size={4} style={{ width: '100%' }}>
    <Space style={{ justifyContent: 'space-between', width: '100%' }}>
      <Typography.Text type="secondary">已触达 {topic.touchedQuestionCount ?? 0} / {topic.totalQuestionCount ?? 0}</Typography.Text>
      <Typography.Text strong>{rate}%</Typography.Text>
    </Space>
    <Progress percent={rate} showInfo={false} strokeColor="var(--jil-success)" />
  </Space>;
}
