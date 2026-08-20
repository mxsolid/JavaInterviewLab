import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { PageSkeleton } from '../components/states';
import { ModulePlaceholderPage } from '../features/shell/ModulePlaceholderPage';

const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const StudyPlanPage = lazy(() => import('../features/study/StudyPlanPage').then((module) => ({ default: module.StudyPlanPage })));
const QuestionBankPage = lazy(() => import('../features/content/QuestionBankPage').then((module) => ({ default: module.QuestionBankPage })));
const QuestionDetailPage = lazy(() => import('../features/content/QuestionDetailPage').then((module) => ({ default: module.QuestionDetailPage })));
const ReviewCenterPage = lazy(() => import('../features/review/ReviewCenterPage').then((module) => ({ default: module.ReviewCenterPage })));
const ContentManagerPage = lazy(() => import('../features/content/ContentManagerPage').then((module) => ({ default: module.ContentManagerPage })));

export function App() {
  return (
    <AppShell>
      <Suspense fallback={<PageSkeleton />}>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/study" element={<StudyPlanPage />} />
          <Route path="/knowledge" element={<ModulePlaceholderPage title="知识地图" description="知识域、专题和掌握度将在核心学习工作区阶段接入真实聚合 API。" />} />
          <Route path="/questions" element={<QuestionBankPage />} />
          <Route path="/questions/:id" element={<QuestionDetailPage />} />
          <Route path="/scenarios" element={<ModulePlaceholderPage title="场景训练" description="场景、Case 和方案矩阵将在场景工作区阶段接入数据库内容。" />} />
          <Route path="/review" element={<ReviewCenterPage />} />
          <Route path="/source" element={<ModulePlaceholderPage title="源码阅读" description="短源码片段和行级注释将在场景工作区阶段接入 Source API。" />} />
          <Route path="/lab" element={<ModulePlaceholderPage title="动画实验室" description="实验元数据和纯 TypeScript step engine 将在实验工作区阶段接入。" />} />
          <Route path="/interview" element={<ModulePlaceholderPage title="模拟面试" description="文本输入与规则评分将在模拟面试阶段接入，不依赖外部 LLM。" />} />
          <Route path="/ai" element={<ModulePlaceholderPage title="AI 专题" description="当前版本不接入外部模型，保留可扩展的专题入口。" />} />
          <Route path="/settings" element={<ContentManagerPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </AppShell>
  );
}
