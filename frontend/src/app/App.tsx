import { BookOutlined, CompassOutlined, HomeOutlined, SettingOutlined } from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { ContentManagerPage } from '../features/content/ContentManagerPage';
import { QuestionBankPage } from '../features/content/QuestionBankPage';
import { QuestionDetailPage } from '../features/content/QuestionDetailPage';
import { StudyPlanPage } from '../features/study/StudyPlanPage';

const { Header, Content, Sider } = Layout;

const navigationItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页' },
  { key: '/study', icon: <CompassOutlined />, label: '开始学习' },
  { key: '/questions', icon: <BookOutlined />, label: '题库' },
  { key: '/settings', icon: <SettingOutlined />, label: '管理 / 设置' },
];

export function App() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <Layout className="app-shell">
      <Sider breakpoint="lg" collapsedWidth="0" theme="light" width={232}>
        <div className="brand">Java Interview Lab</div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
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
            <Route path="/settings" element={<ContentManagerPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}
