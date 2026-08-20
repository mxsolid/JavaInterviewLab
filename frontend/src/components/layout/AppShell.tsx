import {
  AppstoreOutlined,
  BookOutlined,
  CodeOutlined,
  CompassOutlined,
  ExperimentOutlined,
  HomeOutlined,
  MessageOutlined,
  ReadOutlined,
  SearchOutlined,
  SettingOutlined,
  SyncOutlined,
  UndoOutlined,
} from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, Input, Select, Tooltip, message } from 'antd';
import type { ReactNode } from 'react';
import { useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { studyApi } from '../../features/study/api';
import { studyQueryKeys } from '../../features/study/queryKeys';

interface AppShellProps {
  children: ReactNode;
}

const navigationItems = [
  { path: '/', label: '首页 / 工作台', icon: <HomeOutlined /> },
  { path: '/knowledge', label: '知识地图', icon: <CompassOutlined /> },
  { path: '/questions', label: '题目学习', icon: <BookOutlined /> },
  { path: '/scenarios', label: '场景训练', icon: <AppstoreOutlined /> },
  { path: '/review', label: '复习中心', icon: <UndoOutlined /> },
  { path: '/source', label: '源码阅读', icon: <CodeOutlined /> },
  { path: '/lab', label: '动画实验室', icon: <ExperimentOutlined /> },
  { path: '/interview', label: '模拟面试', icon: <MessageOutlined /> },
  { path: '/ai', label: 'AI 专题', icon: <ReadOutlined /> },
  { path: '/settings', label: '管理 / 系统', icon: <SettingOutlined /> },
];

export function AppShell({ children }: AppShellProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [keyword, setKeyword] = useState('');
  const plans = useQuery({ queryKey: studyQueryKeys.plans, queryFn: studyApi.plans });
  const currentPlan = useQuery({ queryKey: studyQueryKeys.currentPlan, queryFn: studyApi.currentPlan });
  const activatePlan = useMutation({
    mutationFn: studyApi.activatePlan,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.currentPlan }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.today }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard }),
      ]);
      message.success('学习路线已切换');
    },
    onError: () => message.error('学习路线切换失败'),
  });

  const search = () => {
    const value = keyword.trim();
    if (!value) return;
    navigate(`/questions?keyword=${encodeURIComponent(value)}`);
  };

  return (
    <div className="app-shell">
      <aside className="app-sider">
        <NavLink className="brand" to="/" aria-label="Java Interview Lab 首页">
          <span className="brand-logo">J</span>
          <span className="brand-copy"><b>Java Interview Lab</b><small>V0.3 社招通关版</small></span>
        </NavLink>
        <nav className="app-nav" aria-label="主导航">
          {navigationItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => `app-nav-item ${isActive || isNestedRoute(location.pathname, item.path) ? 'active' : ''}`}
            >
              <span className="app-nav-icon">{item.icon}</span>
              <span className="app-nav-label">{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="app-sider-foot"><b>本地学习档案</b><span>学习行为写入 PostgreSQL</span></div>
      </aside>

      <main className="app-main">
        <header className="app-topbar">
          <Input
            className="global-search"
            prefix={<SearchOutlined />}
            value={keyword}
            allowClear
            aria-label="全局搜索"
            placeholder="搜索考点、源码、场景（HashMap / 幂等 / B+ 树）"
            onChange={(event) => setKeyword(event.target.value)}
            onPressEnter={search}
          />
          <Select
            className="plan-switcher"
            aria-label="当前学习路线"
            loading={plans.isLoading || currentPlan.isLoading || activatePlan.isPending}
            disabled={plans.isError || currentPlan.isError || !plans.data?.length}
            value={currentPlan.data?.planId}
            placeholder={plans.isError || currentPlan.isError ? '路线未连接' : '选择学习路线'}
            options={plans.data?.map((plan) => ({ value: plan.id, label: plan.name }))}
            onChange={(planId) => activatePlan.mutate(planId)}
          />
          <div className="topbar-actions">
            <Tooltip title="复习中心"><Button aria-label="复习中心" icon={<SyncOutlined />} onClick={() => navigate('/review')} /></Tooltip>
            <Tooltip title="管理系统"><Button aria-label="管理系统" icon={<SettingOutlined />} onClick={() => navigate('/settings')} /></Tooltip>
            <span className="profile-avatar" aria-label="本地学习档案">本地</span>
          </div>
        </header>
        <div className="app-content">{children}</div>
      </main>
    </div>
  );
}

function isNestedRoute(pathname: string, navigationPath: string) {
  return navigationPath !== '/' && pathname.startsWith(`${navigationPath}/`);
}
