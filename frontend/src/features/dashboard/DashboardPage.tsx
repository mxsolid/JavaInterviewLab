import { BookOutlined, ClockCircleOutlined, EditOutlined, ReadOutlined, UndoOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Progress, Row, Space, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { StatCard } from '../../components/ui/StatCard';
import { MasteryBadge, StarRating } from '../../components/ui/StudyTags';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';
import { dashboardApi } from './api';

const shortcuts = [
  { title: '今日学习', description: '按当前路线进入任务', route: '/study', icon: <ReadOutlined /> },
  { title: '开始练习', description: '从题库选择一道题', route: '/questions', icon: <EditOutlined /> },
  { title: '复习中心', description: '处理逾期与今日复习', route: '/review', icon: <UndoOutlined /> },
  { title: '错题本', description: '查看待解决的错误', route: '/review', icon: <ClockCircleOutlined /> },
  { title: '题库', description: '搜索全部知识点', route: '/questions', icon: <BookOutlined /> },
];

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const dashboard = useQuery({ queryKey: studyQueryKeys.dashboard, queryFn: dashboardApi.get });
  const wrongQuestions = useQuery({ queryKey: studyQueryKeys.wrongQuestions, queryFn: studyApi.wrongQuestions });
  const dueReviews = useQuery({ queryKey: studyQueryKeys.dueReviews, queryFn: studyApi.dueReviews });
  const resolveWrong = useMutation({
    mutationFn: studyApi.resolveWrongQuestion,
    onSuccess: async () => {
      await Promise.all([queryClient.invalidateQueries({ queryKey: studyQueryKeys.wrongQuestions }), queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard })]);
      message.success('错题已标记解决');
    },
    onError: () => message.error('错题状态更新失败'),
  });
  if (dashboard.isLoading) return <LoadingState />;
  if (dashboard.isError || !dashboard.data) return <ErrorState description="学习看板加载失败" />;
  const data = dashboard.data;
  const practicedPercent = data.totalQuestionCount === 0 ? 0 : Math.round(data.touchedQuestionCount / data.totalQuestionCount * 100);
  const solidCount = data.solidQuestionCount + data.masteredQuestionCount;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="首页 / 工作台" description="今天该学什么、该复习什么，以及真实的掌握状态。" />
    {data.currentPlan ? <Card className="hero-card">
      <Row gutter={[24, 20]} align="middle"><Col xs={24} lg={16}><Space orientation="vertical" size={10}><Typography.Title level={2} style={{ margin: 0 }}>今天继续 Java 面试冲刺</Typography.Title><Typography.Text type="secondary">当前：{data.currentPlan.planName} · Day {data.timeProgressDay} / {data.planDurationDays}</Typography.Text><Typography.Text>已练习 {data.touchedQuestionCount} / {data.totalQuestionCount} 题；较熟练及以上 {solidCount} 题。</Typography.Text><Button type="primary" onClick={() => navigate('/study')}>查看今日任务</Button></Space></Col><Col xs={24} lg={8}><Space orientation="vertical" align="center" style={{ width: '100%' }}><Progress type="circle" percent={practicedPercent} format={() => `${practicedPercent}%`} /><Typography.Text type="secondary">已练习占比，不等于已完成</Typography.Text></Space></Col></Row>
    </Card> : <Alert type="info" showIcon message="尚未选择学习路线" description="选择路线后，系统才会生成当天学习任务。" action={<Button size="small" onClick={() => navigate('/study')}>选择路线</Button>} />}
    <Row gutter={[16, 16]}>{shortcuts.map((item) => <Col xs={12} sm={8} lg={4} key={item.title}><SectionCard className="shortcut-card"><Space orientation="vertical"><Typography.Text className="shortcut-icon">{item.icon}</Typography.Text><Typography.Text strong>{item.title}</Typography.Text><Typography.Text type="secondary">{item.description}</Typography.Text><Button type="link" onClick={() => navigate(item.route)} style={{ paddingInline: 0 }}>进入</Button></Space></SectionCard></Col>)}</Row>
    <Row gutter={[16, 16]}>
      <Col xs={12} md={6}><StatCard title="今日计划" value={data.todayPlanItemCount} suffix="项" /></Col>
      <Col xs={12} md={6}><StatCard title="待复习" value={data.dueReviewCount} suffix="项" tone="orange" /></Col>
      <Col xs={12} md={6}><StatCard title="激活错题" value={data.activeWrongQuestionCount} suffix="题" tone="violet" /></Col>
      <Col xs={12} md={6}><StatCard title="五星掌握率" value={Math.round(data.fiveStarMasteryRate * 100)} suffix="%" tone="teal" /></Col>
    </Row>
    <Row gutter={[16, 16]}>
      <Col xs={24} lg={12}><SectionCard title="待复习" extra={<Button type="link" onClick={() => navigate('/review')}>查看全部</Button>}>
        {dueReviews.isError ? <ErrorState description="待复习加载失败" /> : dueReviews.data?.length ? <div className="dashboard-list">{dueReviews.data.map((item) => <div className="dashboard-list-item" key={item.id}><Space wrap><Typography.Text>{item.title}</Typography.Text><StarRating value={item.starLevel} /></Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>开始复习</Button></div>)}</div> : <div className="dashboard-list-empty">暂无待复习内容</div>}
      </SectionCard></Col>
      <Col xs={24} lg={12}><SectionCard title="错题本" extra={<Button type="link" onClick={() => navigate('/review')}>查看全部</Button>}>
        {wrongQuestions.isError ? <ErrorState description="错题本加载失败" /> : wrongQuestions.data?.length ? <div className="dashboard-list">{wrongQuestions.data.map((item) => <div className="dashboard-list-item" key={item.questionId}><Space wrap><Typography.Text>{item.title}</Typography.Text><MasteryBadge value={item.masteryLevel} /><Typography.Text type="secondary">错误 {item.wrongCount} 次</Typography.Text></Space><Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button><Button loading={resolveWrong.isPending && resolveWrong.variables === item.questionId} onClick={() => resolveWrong.mutate(item.questionId)}>标记解决</Button></Space></div>)}</div> : <div className="dashboard-list-empty">暂无激活错题</div>}
      </SectionCard></Col>
    </Row>
    <SectionCard title="最近学习">{data.recentStudyItems.length ? <div className="dashboard-list">{data.recentStudyItems.map((item) => <div className="dashboard-list-item" key={`${item.questionId}-${item.lastStudiedAt}`}><Space wrap><Typography.Text>{item.title}</Typography.Text><MasteryBadge value={item.masteryLevel} /></Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button></div>)}</div> : <div className="dashboard-list-empty">提交一次练习后会显示在这里</div>}</SectionCard>
  </Space>;
}
