import { BookOutlined, CompassOutlined, HomeOutlined, SettingOutlined } from '@ant-design/icons';
import { Layout, Menu, Space, Typography } from 'antd';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { EmptyState } from '../components/states';
import { ContentManagerPage } from '../features/content/ContentManagerPage';
import { QuestionBankPage } from '../features/content/QuestionBankPage';
import { QuestionDetailPage } from '../features/content/QuestionDetailPage';

const { Header, Content, Sider } = Layout;

const navigationItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页' },
  { key: '/study', icon: <CompassOutlined />, label: '开始学习' },
  { key: '/questions', icon: <BookOutlined />, label: '题库' },
  { key: '/settings', icon: <SettingOutlined />, label: '管理 / 设置' },
];

function HomePage() {
  return (
    <Space direction="vertical" size={20} style={{ width: '100%' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>Java 面试学习</Typography.Title>
      <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
        从知识地图、题库与追问开始，形成可持续扩展的面试学习资料库。
      </Typography.Paragraph>
      <EmptyState description="可在管理页导入 V0.1 JSON 种子，或先手动创建分类、专题和题目。" />
    </Space>
  );
}

function PlaceholderPage() {
  return <EmptyState description="该功能将在对应任务中实现" />;
}

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
            <Route path="/" element={<HomePage />} />
            <Route path="/study" element={<PlaceholderPage />} />
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
