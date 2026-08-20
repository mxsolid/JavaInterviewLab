import type { ReactNode } from 'react';
import { Card } from 'antd';

interface SectionCardProps {
  title?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
  className?: string;
}

export function SectionCard({ title, extra, children, className }: SectionCardProps) {
  return <Card className={`section-card ${className ?? ''}`} title={title} extra={extra}>{children}</Card>;
}
