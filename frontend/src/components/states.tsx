import { Empty, Result, Spin } from 'antd';

export function LoadingState() {
  return <Spin size="large" aria-label="内容加载中" />;
}

export function EmptyState({ description = '暂无内容' }: { description?: string }) {
  return <Empty description={description} />;
}

export function ErrorState({ description = '内容加载失败' }: { description?: string }) {
  return <Result status="error" title={description} />;
}
