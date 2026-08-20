import type { ReactNode } from 'react';
import { Statistic } from 'antd';
import { SectionCard } from './SectionCard';

interface StatCardProps {
  title: string;
  value: number;
  suffix?: ReactNode;
  tone?: 'blue' | 'violet' | 'teal' | 'orange';
}

export function StatCard({ title, value, suffix, tone = 'blue' }: StatCardProps) {
  return <SectionCard className={`stat-card stat-card-${tone}`}><Statistic title={title} value={value} suffix={suffix} /></SectionCard>;
}
