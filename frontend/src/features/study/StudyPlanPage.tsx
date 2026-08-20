import { CheckCircleOutlined, ClockCircleOutlined, ReadOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Empty, Row, Space, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { studyApi, type StudyPlanDay } from './api';
import { studyQueryKeys } from './queryKeys';

function DayItems({ day }: { day: StudyPlanDay }) {
  const navigate = useNavigate();
  if (day.items.length === 0) {
    return <Typography.Text type="secondary">今天用于复盘、输出或查漏补缺。</Typography.Text>;
  }
  return <Space orientation="vertical" size={4}>
    {day.items.map((item) => {
      const route = targetRoute(item);
      return <Button key={item.id} type="link" disabled={!route} style={{ paddingInline: 0 }} onClick={() => route && navigate(route)}>
        {item.targetType === 'TOPIC' ? '专题：' : item.targetType === 'QUESTION' ? '题目：' : '场景：'}{item.targetTitle ?? '关联内容'}{item.targetType === 'SCENARIO' ? '（场景训练将在后续版本开放）' : ''}
      </Button>;
    })}
  </Space>;
}

function targetRoute(item: StudyPlanDay['items'][number]) {
  switch (item.targetType) {
    case 'QUESTION': return `/questions/${item.targetId}`;
    case 'TOPIC': return `/questions?topicId=${item.targetId}`;
    case 'SCENARIO': return undefined;
  }
}

export function StudyPlanPage() {
  const client = useQueryClient();
  const [selectedPlanId, setSelectedPlanId] = useState<number>();
  const plans = useQuery({ queryKey: studyQueryKeys.plans, queryFn: studyApi.plans });
  const currentPlan = useQuery({ queryKey: studyQueryKeys.currentPlan, queryFn: studyApi.currentPlan });
  const today = useQuery({ queryKey: studyQueryKeys.today, queryFn: studyApi.today });
  const displayPlanId = selectedPlanId ?? currentPlan.data?.planId ?? plans.data?.[0]?.id;
  const detail = useQuery({
    queryKey: displayPlanId ? studyQueryKeys.planDetail(displayPlanId) : ['study', 'plans', 'empty'],
    queryFn: () => studyApi.plan(displayPlanId!),
    enabled: displayPlanId !== undefined,
  });
  const activate = useMutation({
    mutationFn: studyApi.activatePlan,
    onSuccess: async () => {
      await Promise.all([
        client.invalidateQueries({ queryKey: studyQueryKeys.currentPlan }),
        client.invalidateQueries({ queryKey: studyQueryKeys.today }),
      ]);
      message.success('学习路线已开始');
    },
    onError: () => {
      message.error('学习路线启动失败，请稍后重试');
    },
  });

  if (plans.isLoading || currentPlan.isLoading || today.isLoading) return <LoadingState />;
  if (plans.isError || currentPlan.isError || today.isError || !plans.data) return <ErrorState description="学习路线加载失败" />;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="开始学习" description="先选择适合自己的路线，再按每日主题进入题库学习。" />

    {today.data ? (
      <SectionCard title={<Space><ClockCircleOutlined /><span>今日任务</span></Space>} extra={<Tag className="study-tag study-tag-blue">Day {today.data.currentPlan.timeProgressDay} / {today.data.currentPlan.durationDays}</Tag>}>
        <Typography.Title level={4}>{today.data.day.title}</Typography.Title>
        {today.data.day.description && <Typography.Paragraph type="secondary">{today.data.day.description}</Typography.Paragraph>}
        <DayItems day={today.data.day} />
      </SectionCard>
    ) : (
      <Alert type="info" showIcon message="尚未选择学习路线" description="选择后才开始计算 Day N；中断学习不会把知识内容自动记为完成。" />
    )}

    <SectionCard title={<Space><ReadOutlined /><span>系统预设路线</span></Space>}>
      <Row gutter={[16, 16]}>
        {plans.data.map((plan) => {
          const active = currentPlan.data?.planId === plan.id;
          return <Col xs={24} md={8} key={plan.id}>
            <Card className="section-card" size="small" hoverable={selectedPlanId === plan.id || (!selectedPlanId && plan.id === displayPlanId)} onClick={() => setSelectedPlanId(plan.id)}>
              <Space orientation="vertical" size={8} style={{ width: '100%' }}>
                <Typography.Title level={4} style={{ margin: 0 }}>{plan.name}</Typography.Title>
                <Tag>{plan.durationDays} 天</Tag>
                <Typography.Text type="secondary">{plan.description}</Typography.Text>
                <Button type={active ? 'default' : 'primary'} icon={active ? <CheckCircleOutlined /> : undefined} loading={activate.isPending && activate.variables === plan.id} onClick={(event) => { event.stopPropagation(); if (!active) activate.mutate(plan.id); }}>
                  {active ? '当前路线' : '选择此路线'}
                </Button>
              </Space>
            </Card>
          </Col>;
        })}
      </Row>
    </SectionCard>

    <SectionCard title={detail.data ? `${detail.data.name} · 每日计划` : '每日计划'}>
      {detail.isError && <Typography.Text type="danger">路线详情加载失败</Typography.Text>}
      {detail.data && <div className="data-list">{detail.data.days.map((day) => <div className={`data-list-item ${today.data?.day.id === day.id ? 'plan-day-current' : ''}`} key={day.id}><div className="data-list-meta"><Typography.Text strong>{`Day ${day.dayNumber} · ${day.title}`}</Typography.Text>{day.description && <Typography.Text type="secondary">{day.description}</Typography.Text>}</div><DayItems day={day} /></div>)}</div>}
      {!detail.isLoading && !detail.data && !detail.isError && <Empty description="暂无学习计划" />}
    </SectionCard>
  </Space>;
}
