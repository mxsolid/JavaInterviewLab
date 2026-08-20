import { Empty, Result, Skeleton, Spin } from 'antd';

export function LoadingState() {
  return <div className="state-container"><Spin size="large" aria-label="内容加载中" /></div>;
}

export function PageSkeleton() {
  return <div className="page-skeleton" aria-label="页面加载中"><Skeleton active paragraph={{ rows: 8 }} /></div>;
}

export function EmptyState({ description = '暂无内容' }: { description?: string }) {
  return <div className="state-container"><Empty description={description} /></div>;
}

export function ErrorState({ description = '内容加载失败' }: { description?: string }) {
  return <div className="state-container"><Result status="error" title={description} /></div>;
}
