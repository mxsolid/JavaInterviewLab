import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { PageSkeleton } from '../components/states';
import { ModulePlaceholderPage } from '../features/shell/ModulePlaceholderPage';

const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const StudyPlanPage = lazy(() => import('../features/study/StudyPlanPage').then((module) => ({ default: module.StudyPlanPage })));
const QuestionBankPage = lazy(() => import('../features/content/QuestionBankPage').then((module) => ({ default: module.QuestionBankPage })));
const QuestionDetailPage = lazy(() => import('../features/content/QuestionDetailPage').then((module) => ({ default: module.QuestionDetailPage })));
const KnowledgePage = lazy(() => import('../features/knowledge/KnowledgePage').then((module) => ({ default: module.KnowledgePage })));
const ReviewCenterPage = lazy(() => import('../features/review/ReviewCenterPage').then((module) => ({ default: module.ReviewCenterPage })));
const ContentManagerPage = lazy(() => import('../features/content/ContentManagerPage').then((module) => ({ default: module.ContentManagerPage })));
const ScenarioPage = lazy(() => import('../features/scenario/ScenarioPage').then((module) => ({ default: module.ScenarioPage })));
const SourcePage = lazy(() => import('../features/source/SourcePage').then((module) => ({ default: module.SourcePage })));
const LabPage = lazy(() => import('../features/lab/LabPage').then((module) => ({ default: module.LabPage })));
const InterviewPage = lazy(() => import('../features/interview/InterviewPage').then((module) => ({ default: module.InterviewPage })));

export function App() {
  return (
    <AppShell>
      <Suspense fallback={<PageSkeleton />}>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/study" element={<StudyPlanPage />} />
          <Route path="/knowledge" element={<KnowledgePage />} />
          <Route path="/questions" element={<QuestionBankPage />} />
          <Route path="/questions/:id" element={<QuestionDetailPage />} />
          <Route path="/scenarios" element={<ScenarioPage />} />
          <Route path="/review" element={<ReviewCenterPage />} />
          <Route path="/source" element={<SourcePage />} />
          <Route path="/lab" element={<LabPage />} />
          <Route path="/interview" element={<InterviewPage />} />
          <Route path="/ai" element={<ModulePlaceholderPage title="AI 专题" description="当前版本不接入外部模型，保留可扩展的专题入口。" />} />
          <Route path="/settings" element={<ContentManagerPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </AppShell>
  );
}
