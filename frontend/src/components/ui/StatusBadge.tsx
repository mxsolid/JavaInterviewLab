import type { ReactNode } from 'react';

export function StatusBadge({ children, tone = 'success' }: { children: ReactNode; tone?: 'success' | 'info' | 'warning' | 'danger' }) {
  return <span className={`status-badge status-badge-${tone}`}><i aria-hidden="true" />{children}</span>;
}
