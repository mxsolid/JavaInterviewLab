import type { ReactNode } from 'react';
import { Space, Typography } from 'antd';

interface PageHeaderProps {
  title: string;
  description?: string;
  extra?: ReactNode;
}

export function PageHeader({ title, description, extra }: PageHeaderProps) {
  return (
    <div className="page-header">
      <div>
        <Typography.Title level={2}>{title}</Typography.Title>
        {description && <Typography.Paragraph>{description}</Typography.Paragraph>}
      </div>
      {extra && <Space>{extra}</Space>}
    </div>
  );
}
