import type { ReactNode } from 'react';
import { Card } from 'antd';

interface SectionCardProps {
  title?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
  className?: string;
  hoverable?: boolean;
}

export function SectionCard({ title, extra, children, className, hoverable = false }: SectionCardProps) {
  return <Card className={`section-card ${hoverable ? 'section-card-hoverable' : ''} ${className ?? ''}`} title={title} extra={extra}>{children}</Card>;
}
