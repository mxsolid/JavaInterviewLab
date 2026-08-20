import { BookOutlined, CompassOutlined, HomeOutlined, SettingOutlined, UndoOutlined } from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { ContentManagerPage } from '../features/content/ContentManagerPage';
import { QuestionBankPage } from '../features/content/QuestionBankPage';
import { QuestionDetailPage } from '../features/content/QuestionDetailPage';
import { StudyPlanPage } from '../features/study/StudyPlanPage';
import { ReviewCenterPage } from '../features/review/ReviewCenterPage';

const { Header, Content, Sider } = Layout;

const navigationItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页' },
  { key: '/study', icon: <CompassOutlined />, label: '开始学习' },
  { key: '/questions', icon: <BookOutlined />, label: '题库' },
  { key: '/review', icon: <UndoOutlined />, label: '复习中心' },
  { key: '/settings', icon: <SettingOutlined />, label: '管理 / 设置' },
];

export function App() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <Layout className="app-shell">
      <Sider className="app-sider" breakpoint="lg" collapsedWidth="0" theme="light" width={232}>
        <div className="brand">Java Interview Lab<small>Java 后端面试学习系统</small></div>
        <Menu
          mode="inline"
          selectedKeys={[resolveMenuKey(location.pathname)]}
          items={navigationItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header className="app-header">面向 Java 后端面试的学习系统</Header>
        <Content className="app-content">
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/study" element={<StudyPlanPage />} />
            <Route path="/questions" element={<QuestionBankPage />} />
            <Route path="/questions/:id" element={<QuestionDetailPage />} />
            <Route path="/review" element={<ReviewCenterPage />} />
            <Route path="/settings" element={<ContentManagerPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

function resolveMenuKey(pathname: string) {
  if (pathname.startsWith('/questions')) return '/questions';
  if (pathname.startsWith('/review')) return '/review';
  if (pathname.startsWith('/study')) return '/study';
  if (pathname.startsWith('/settings')) return '/settings';
  return '/';
}
