import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, Space, Tabs, Typography, message } from 'antd';
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
  const dueContent = due.isError ? <ErrorState description="待复习加载失败" /> : due.data?.length ? <div className="data-list">{due.data.map((item) => <div className="data-list-item" key={item.id}><Space orientation="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /><ReviewDueTag overdue={item.overdue} /></Space><Typography.Text type="secondary">应复习时间：{formatTime(item.dueAt)}</Typography.Text></Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>开始复习</Button></div>)}</div> : <div className="data-list-empty">暂无待复习内容</div>;
  const wrongContent = wrong.isError ? <ErrorState description="错题本加载失败" /> : wrong.data?.length ? <div className="data-list">{wrong.data.map((item) => <div className="data-list-item" key={item.questionId}><Space orientation="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /><MasteryBadge value={item.masteryLevel} /></Space><Typography.Text type="secondary">错误 {item.wrongCount} 次 · 已练习 {item.attemptCount} 次</Typography.Text></Space><Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button><Button loading={resolveWrong.isPending && resolveWrong.variables === item.questionId} onClick={() => resolveWrong.mutate(item.questionId)}>标记解决</Button></Space></div>)}</div> : <div className="data-list-empty">暂无激活错题</div>;
  const favoriteContent = favorites.isError ? <ErrorState description="收藏加载失败" /> : favorites.data?.length ? <div className="data-list">{favorites.data.map((item) => <div className="data-list-item" key={item.favoriteId}><Space orientation="vertical" size={4}><Space wrap><Typography.Text strong>{item.title}</Typography.Text><StarRating value={item.starLevel} /></Space><Typography.Text type="secondary">收藏于 {formatTime(item.createdAt)}</Typography.Text></Space><Space><Button type="link" onClick={() => navigate(`/questions/${item.questionId}`)}>查看</Button><Button loading={removeFavorite.isPending && removeFavorite.variables === item.questionId} onClick={() => removeFavorite.mutate(item.questionId)}>取消收藏</Button></Space></div>)}</div> : <div className="data-list-empty">暂无收藏题目</div>;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="复习中心" description="待复习按逾期加今日口径展示；错题与收藏均来自当前学习档案。" />
    <SectionCard><Tabs items={[
      { key: 'due', label: due.isError ? '待复习（加载失败）' : `待复习 ${due.data?.length ?? 0}`, children: dueContent },
      { key: 'wrong', label: wrong.isError ? '错题本（加载失败）' : `错题本 ${wrong.data?.length ?? 0}`, children: wrongContent },
      { key: 'favorite', label: favorites.isError ? '收藏（加载失败）' : `收藏 ${favorites.data?.length ?? 0}`, children: favoriteContent },
    ]} /></SectionCard>
  </Space>;
}
