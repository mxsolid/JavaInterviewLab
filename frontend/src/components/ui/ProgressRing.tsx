import type { CSSProperties } from 'react';

interface ProgressRingProps {
  percent: number;
  label: string;
  detail?: string;
  size?: 'small' | 'default';
}

export function ProgressRing({ percent, label, detail, size = 'default' }: ProgressRingProps) {
  const normalized = Math.min(100, Math.max(0, Math.round(percent)));
  const style = { '--jil-progress': normalized } as CSSProperties;
  return (
    <div className={`progress-ring progress-ring-${size}`} style={style} role="progressbar" aria-label={label} aria-valuemin={0} aria-valuemax={100} aria-valuenow={normalized}>
      <div className="progress-ring-content"><strong>{normalized}%</strong><span>{label}</span>{detail && <small>{detail}</small>}</div>
    </div>
  );
}
