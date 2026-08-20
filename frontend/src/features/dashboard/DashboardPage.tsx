import { BookOutlined, ClockCircleOutlined, EditOutlined, ReadOutlined, UndoOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Row, Space, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { ProgressRing } from '../../components/ui/ProgressRing';
import { SectionCard } from '../../components/ui/SectionCard';
import { StatCard } from '../../components/ui/StatCard';
import { MasteryBadge, StarRating } from '../../components/ui/StudyTags';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';
import { dashboardApi } from './api';

const shortcuts = [
  { title: '今日学习', description: '按当前路线进入任务', route: '/study', icon: <ReadOutlined /> },
  { title: '开始练习', description: '从题库选择一道题', route: '/questions', icon: <EditOutlined /> },
  { title: '知识地图', description: '按专题查看掌握状态', route: '/knowledge', icon: <BookOutlined /> },
  { title: '复习中心', description: '处理到期复习任务', route: '/review', icon: <UndoOutlined /> },
  { title: '错题本', description: '处理仍未解决的错误', route: '/review', icon: <ClockCircleOutlined /> },
];

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const workbench = useQuery({ queryKey: studyQueryKeys.dashboard, queryFn: dashboardApi.getWorkbench });
  const resolveWrong = useMutation({
    mutationFn: studyApi.resolveWrongQuestion,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard });
      message.success('错题已标记解决');
    },
    onError: () => message.error('错题状态更新失败'),
  });

  if (workbench.isLoading) return <LoadingState />;
  const data = workbench.data;
  const overview = data?.overview;
  if (workbench.isError || !data || !overview) return <ErrorState description="学习工作台加载失败" />;
  const total = overview.totalQuestionCount ?? 0;
  const touched = overview.touchedQuestionCount ?? 0;
  const solid = (overview.solidQuestionCount ?? 0) + (overview.masteredQuestionCount ?? 0);
  const practicedPercent = total === 0 ? 0 : Math.round(touched / total * 100);
  const dueReviews = data.dueReviews ?? [];
  const wrongQuestions = data.wrongQuestions ?? [];
  const recentItems = overview.recentStudyItems ?? [];

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="首页 / 工作台" description="今天该学什么、该复习什么，以及数据库中的真实掌握状态。" />
    {overview.currentPlan ? <Card className="hero-card">
      <Row gutter={[24, 20]} align="middle">
        <Col xs={24} lg={17}>
          <Space orientation="vertical" size={10}>
            <Typography.Title level={2} style={{ margin: 0 }}>今天继续 Java 面试冲刺</Typography.Title>
            <Typography.Text type="secondary">当前：{overview.currentPlan.planName} · Day {overview.timeProgressDay ?? 1} / {overview.planDurationDays ?? overview.currentPlan.durationDays}</Typography.Text>
            <Typography.Text>已练习 {touched} / {total} 题；较熟练及以上 {solid} 题。</Typography.Text>
            <Button type="primary" onClick={() => navigate('/study')}>查看今日任务</Button>
          </Space>
        </Col>
        <Col xs={24} lg={7}><ProgressRing percent={practicedPercent} label="已练习" /></Col>
      </Row>
    </Card> : <Alert type="info" showIcon title="尚未选择学习路线" description="选择路线后，系统才会生成当天学习任务。" action={<Button size="small" onClick={() => navigate('/study')}>选择路线</Button>} />}

    <Row gutter={[16, 16]}>{shortcuts.map((item) => <Col xs={12} sm={8} lg={4} key={item.title}>
      <SectionCard className="shortcut-card">
        <Space orientation="vertical">
          <Typography.Text className="shortcut-icon">{item.icon}</Typography.Text>
          <Typography.Text strong>{item.title}</Typography.Text>
          <Typography.Text type="secondary">{item.description}</Typography.Text>
          <Button type="link" onClick={() => navigate(item.route)} style={{ paddingInline: 0 }}>进入</Button>
        </Space>
      </SectionCard>
    </Col>)}</Row>

    <Row gutter={[16, 16]}>
      <Col xs={12} md={6}><StatCard title="今日计划" value={overview.todayPlanItemCount ?? 0} suffix="项" /></Col>
      <Col xs={12} md={6}><StatCard title="待复习" value={overview.dueReviewCount ?? 0} suffix="项" tone="orange" /></Col>
      <Col xs={12} md={6}><StatCard title="激活错题" value={overview.activeWrongQuestionCount ?? 0} suffix="题" tone="violet" /></Col>
      <Col xs={12} md={6}><StatCard title="五星掌握率" value={Math.round((overview.fiveStarMasteryRate ?? 0) * 100)} suffix="%" tone="teal" /></Col>
    </Row>

    <Row gutter={[16, 16]}>
      <Col xs={24} lg={12}><SectionCard title="待复习" extra={<Button type="link" onClick={() => navigate('/review')}>查看全部</Button>}>
        {dueReviews.length ? <div className="dashboard-list">{dueReviews.slice(0, 5).map((item) => <div className="dashboard-list-item" key={item.id}>
          <Space wrap><Typography.Text>{item.title}</Typography.Text><StarRating value={item.starLevel ?? 0} /></Space>
          <Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>开始复习</Button>
        </div>)}</div> : <div className="dashboard-list-empty">暂无待复习内容</div>}
      </SectionCard></Col>
      <Col xs={24} lg={12}><SectionCard title="错题本" extra={<Button type="link" onClick={() => navigate('/review')}>查看全部</Button>}>
        {wrongQuestions.length ? <div className="dashboard-list">{wrongQuestions.slice(0, 5).map((item) => <div className="dashboard-list-item" key={item.questionId}>
          <Space wrap><Typography.Text>{item.title}</Typography.Text><MasteryBadge value={item.masteryLevel ?? 'UNSEEN'} /><Typography.Text type="secondary">错误 {item.wrongCount ?? 0} 次</Typography.Text></Space>
          <Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button><Button loading={resolveWrong.isPending && resolveWrong.variables === item.questionId} onClick={() => item.questionId && resolveWrong.mutate(item.questionId)}>标记解决</Button></Space>
        </div>)}</div> : <div className="dashboard-list-empty">暂无激活错题</div>}
      </SectionCard></Col>
    </Row>

    <Row gutter={[16, 16]}>
      <Col xs={24} lg={16}><SectionCard title="最近学习">
        {recentItems.length ? <div className="dashboard-list">{recentItems.slice(0, 6).map((item) => <div className="dashboard-list-item" key={`${item.questionId}-${item.lastStudiedAt}`}>
          <Space wrap><Typography.Text>{item.title}</Typography.Text><MasteryBadge value={item.masteryLevel ?? 'UNSEEN'} /></Space>
          <Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>继续</Button>
        </div>)}</div> : <div className="dashboard-list-empty">提交一次练习后会显示在这里</div>}
      </SectionCard></Col>
      <Col xs={24} lg={8}><SectionCard title="知识焦点">
        <Space orientation="vertical" size={12} style={{ width: '100%' }}>
          <Typography.Text>已触达 {touched} 题，较熟练及以上 {solid} 题。</Typography.Text>
          <Typography.Text type="secondary">掌握率只由真实练习结果推进，不按浏览次数计算。</Typography.Text>
          <Button onClick={() => navigate('/knowledge')}>查看知识地图</Button>
        </Space>
      </SectionCard></Col>
    </Row>
  </Space>;
}
