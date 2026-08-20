import { CheckCircleOutlined, ClockCircleOutlined, ReadOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Empty, List, Row, Space, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { studyApi, type StudyPlanDay } from './api';
import { studyQueryKeys } from './queryKeys';

function DayItems({ day }: { day: StudyPlanDay }) {
  const navigate = useNavigate();
  if (day.items.length === 0) {
    return <Typography.Text type="secondary">今天用于复盘、输出或查漏补缺。</Typography.Text>;
  }
  return <Space direction="vertical" size={4}>
    {day.items.map((item) => (
      <Button
        key={item.id}
        type="link"
        style={{ paddingInline: 0 }}
        onClick={() => navigate(
          item.targetType === 'QUESTION' ? `/questions/${item.targetId}` : `/questions?topicId=${item.targetId}`,
        )}
      >
        {item.targetType === 'TOPIC' ? '专题：' : '题目：'}{item.targetTitle ?? '关联内容'}
      </Button>
    ))}
  </Space>;
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

  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <div>
      <Typography.Title level={2} style={{ margin: 0 }}>开始学习</Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>先选择适合自己的路线，再按每日主题进入题库学习。</Typography.Paragraph>
    </div>

    {today.data ? (
      <Card title={<Space><ClockCircleOutlined /><span>今日任务</span></Space>} extra={<Tag color="blue">Day {today.data.currentPlan.timeProgressDay} / {today.data.currentPlan.durationDays}</Tag>}>
        <Typography.Title level={4}>{today.data.day.title}</Typography.Title>
        {today.data.day.description && <Typography.Paragraph type="secondary">{today.data.day.description}</Typography.Paragraph>}
        <DayItems day={today.data.day} />
      </Card>
    ) : (
      <Alert type="info" showIcon message="尚未选择学习路线" description="选择后才开始计算 Day N；中断学习不会把知识内容自动记为完成。" />
    )}

    <Card title={<Space><ReadOutlined /><span>系统预设路线</span></Space>}>
      <Row gutter={[16, 16]}>
        {plans.data.map((plan) => {
          const active = currentPlan.data?.planId === plan.id;
          return <Col xs={24} md={8} key={plan.id}>
            <Card size="small" hoverable={selectedPlanId === plan.id || (!selectedPlanId && plan.id === displayPlanId)} onClick={() => setSelectedPlanId(plan.id)}>
              <Space direction="vertical" size={8} style={{ width: '100%' }}>
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
    </Card>

    <Card title={detail.data ? `${detail.data.name} · 每日计划` : '每日计划'} loading={detail.isLoading}>
      {detail.isError && <Typography.Text type="danger">路线详情加载失败</Typography.Text>}
      {detail.data && <List
        dataSource={detail.data.days}
        renderItem={(day) => <List.Item><List.Item.Meta title={`Day ${day.dayNumber} · ${day.title}`} description={day.description} /><DayItems day={day} /></List.Item>}
      />}
      {!detail.isLoading && !detail.data && !detail.isError && <Empty description="暂无学习计划" />}
    </Card>
  </Space>;
}
