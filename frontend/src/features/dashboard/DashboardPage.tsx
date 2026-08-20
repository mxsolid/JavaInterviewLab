import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, List, Progress, Row, Space, Statistic, Tag, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';
import { dashboardApi } from './api';

const dashboardKey = ['dashboard'] as const;

/** 首页只展示后端计算的统计，避免日期进度和学习完成度被前端混算。 */
export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const dashboard = useQuery({ queryKey: dashboardKey, queryFn: dashboardApi.get });
  const wrongQuestions = useQuery({ queryKey: studyQueryKeys.wrongQuestions, queryFn: studyApi.wrongQuestions });
  const reviews = useQuery({ queryKey: studyQueryKeys.todayReviews, queryFn: studyApi.todayReviews });
  const resolveWrong = useMutation({
    mutationFn: studyApi.resolveWrongQuestion,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: studyQueryKeys.wrongQuestions });
      void queryClient.invalidateQueries({ queryKey: dashboardKey });
    },
  });
  if (dashboard.isLoading) return <LoadingState />;
  if (dashboard.isError || !dashboard.data) return <ErrorState description="学习看板加载失败" />;
  const data = dashboard.data;
  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <div>
      <Typography.Title level={2} style={{ margin: 0 }}>学习进度</Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>时间推进不等于已经掌握，完成情况以答题快照为准。</Typography.Paragraph>
    </div>
    {data.currentPlan ? <Card title={data.currentPlan.planName}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <div>时间：Day {data.timeProgressDay} / {data.planDurationDays}　学习完成：{data.touchedQuestionCount} / {data.totalQuestionCount}</div>
        <Progress percent={data.totalQuestionCount === 0 ? 0 : Math.round(data.touchedQuestionCount / data.totalQuestionCount * 100)} />
      </Space>
    </Card> : <Alert type="info" message="尚未选择学习路线" action={<Button size="small" onClick={() => navigate('/study')}>选择路线</Button>} />}
    <Row gutter={[16, 16]}>
      <Col xs={12} md={6}><Card><Statistic title="今日计划" value={data.todayPlanItemCount} suffix="项" /></Card></Col>
      <Col xs={12} md={6}><Card><Statistic title="今日复习" value={data.todayReviewCount} suffix="项" /></Card></Col>
      <Col xs={12} md={6}><Card><Statistic title="激活错题" value={data.activeWrongQuestionCount} suffix="题" /></Card></Col>
      <Col xs={12} md={6}><Card><Statistic title="五星掌握率" value={Math.round(data.fiveStarMasteryRate * 100)} suffix="%" /></Card></Col>
    </Row>
    <Row gutter={[16, 16]}>
      <Col xs={24} lg={12}><Card title="今日复习" extra={<Button type="link" onClick={() => navigate('/study')}>学习路线</Button>}>
        <List dataSource={reviews.data ?? []} locale={{ emptyText: '今天没有到期复习' }} renderItem={(item) => <List.Item actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>学习</Button>]}>{item.title}</List.Item>} />
      </Card></Col>
      <Col xs={24} lg={12}><Card title="错题本">
        <List dataSource={wrongQuestions.data ?? []} locale={{ emptyText: '暂无激活错题' }} renderItem={(item) => <List.Item actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button>, <Button key="resolve" loading={resolveWrong.isPending} onClick={() => resolveWrong.mutate(item.questionId)}>标记解决</Button>]}><Space><span>{item.title}</span><Tag>{item.masteryLevel}</Tag><span>错误 {item.wrongCount} 次</span></Space></List.Item>} />
      </Card></Col>
    </Row>
    <Card title="最近学习">
      <List dataSource={data.recentStudyItems} locale={{ emptyText: '提交一次练习后会显示在这里' }} renderItem={(item) => <List.Item actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button>]}><Space><span>{item.title}</span><Tag>{item.masteryLevel}</Tag></Space></List.Item>} />
    </Card>
  </Space>;
}
