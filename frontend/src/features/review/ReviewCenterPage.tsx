import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, List, Space, Tabs, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { MasteryBadge, ReviewDueTag, StarRating } from '../../components/ui/StudyTags';
import { studyApi } from '../study/api';
import { studyQueryKeys } from '../study/queryKeys';

function formatTime(value: string) {
  return new Date(value).toLocaleString('zh-CN', { dateStyle: 'medium', timeStyle: 'short' });
}

export function ReviewCenterPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const due = useQuery({ queryKey: studyQueryKeys.dueReviews, queryFn: studyApi.dueReviews });
  const wrong = useQuery({ queryKey: studyQueryKeys.wrongQuestions, queryFn: studyApi.wrongQuestions });
  const favorites = useQuery({ queryKey: studyQueryKeys.favorites, queryFn: studyApi.favorites });
  const resolveWrong = useMutation({
    mutationFn: studyApi.resolveWrongQuestion,
    onSuccess: async () => {
      await Promise.all([queryClient.invalidateQueries({ queryKey: studyQueryKeys.wrongQuestions }), queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard })]);
      message.success('错题已标记解决');
    },
    onError: () => message.error('错题状态更新失败'),
  });
  const removeFavorite = useMutation({
    mutationFn: studyApi.unfavoriteQuestion,
    onSuccess: async () => {
      await Promise.all([queryClient.invalidateQueries({ queryKey: studyQueryKeys.favorites }), queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard })]);
      message.success('已取消收藏');
    },
    onError: () => message.error('取消收藏失败'),
  });

  if (due.isLoading || wrong.isLoading || favorites.isLoading) return <LoadingState />;
  const dueContent = due.isError ? <ErrorState description="待复习加载失败" /> : <List dataSource={due.data ?? []} locale={{ emptyText: '暂无待复习内容' }} renderItem={(item) => <List.Item className="review-list-item" actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>开始复习</Button>]}><Space direction="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /><ReviewDueTag overdue={item.overdue} /></Space><Typography.Text type="secondary">应复习时间：{formatTime(item.dueAt)}</Typography.Text></Space></List.Item>} />;
  const wrongContent = wrong.isError ? <ErrorState description="错题本加载失败" /> : <List dataSource={wrong.data ?? []} locale={{ emptyText: '暂无激活错题' }} renderItem={(item) => <List.Item className="review-list-item" actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button>, <Button key="resolve" loading={resolveWrong.isPending && resolveWrong.variables === item.questionId} onClick={() => resolveWrong.mutate(item.questionId)}>标记解决</Button>]}><Space direction="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /><MasteryBadge value={item.masteryLevel} /></Space><Typography.Text type="secondary">错误 {item.wrongCount} 次 · 已练习 {item.attemptCount} 次</Typography.Text></Space></List.Item>} />;
  const favoriteContent = favorites.isError ? <ErrorState description="收藏加载失败" /> : <List dataSource={favorites.data ?? []} locale={{ emptyText: '暂无收藏题目' }} renderItem={(item) => <List.Item className="review-list-item" actions={[<Button key="open" type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button>, <Button key="remove" loading={removeFavorite.isPending && removeFavorite.variables === item.questionId} onClick={() => removeFavorite.mutate(item.questionId)}>取消收藏</Button>]}><Space direction="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /></Space><Typography.Text type="secondary">收藏于 {formatTime(item.createdAt)}</Typography.Text></Space></List.Item>} />;

  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="复习中心" description="待复习按逾期加今日口径展示；错题与收藏均来自当前学习档案。" />
    <SectionCard><Tabs items={[
      { key: 'due', label: due.isError ? '待复习（加载失败）' : `待复习 ${due.data?.length ?? 0}`, children: dueContent },
      { key: 'wrong', label: wrong.isError ? '错题本（加载失败）' : `错题本 ${wrong.data?.length ?? 0}`, children: wrongContent },
      { key: 'favorite', label: favorites.isError ? '收藏（加载失败）' : `收藏 ${favorites.data?.length ?? 0}`, children: favoriteContent },
    ]} /></SectionCard>
  </Space>;
}
