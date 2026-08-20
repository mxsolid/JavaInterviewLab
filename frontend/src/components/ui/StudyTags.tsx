import { Tag } from 'antd';
import { difficultyLabel, frequencyLabel, masteryLabel } from '../../features/study/labels';

export function StarRating({ value }: { value: number }) {
  return <span className="star-rating" aria-label={`${value} 星`}>{'★'.repeat(value)}{'☆'.repeat(Math.max(0, 5 - value))}</span>;
}

export function DifficultyTag({ value }: { value: string }) {
  return <Tag className="study-tag study-tag-blue">{difficultyLabel(value)}</Tag>;
}

export function FrequencyTag({ value }: { value: string }) {
  return <Tag className="study-tag study-tag-violet">{frequencyLabel(value)}</Tag>;
}

export function MasteryBadge({ value, description }: { value: string; description?: string }) {
  return <Tag className="study-tag study-tag-teal">{masteryLabel(value, description)}</Tag>;
}

export function ReviewDueTag({ overdue }: { overdue: boolean }) {
  return <Tag className={`study-tag ${overdue ? 'study-tag-warning' : 'study-tag-blue'}`}>{overdue ? '已逾期' : '今天复习'}</Tag>;
}
