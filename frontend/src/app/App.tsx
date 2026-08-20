import { BookOutlined, CompassOutlined, HomeOutlined, SettingOutlined } from '@ant-design/icons';
import { Layout, Menu, Space, Typography } from 'antd';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { EmptyState } from '../components/states';

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
        题库与学习数据将在后续任务接入 PostgreSQL。
      </Typography.Paragraph>
      <EmptyState description="题库内容尚未导入" />
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
            <Route path="/questions" element={<PlaceholderPage />} />
            <Route path="/settings" element={<PlaceholderPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}
