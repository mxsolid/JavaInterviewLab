import type { AttemptResultType } from './types';

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: '简单', MEDIUM: '中等', HARD: '困难',
};
const FREQUENCY_LABELS: Record<string, string> = {
  LOW: '低频', MEDIUM: '中频', HIGH: '高频', VERY_HIGH: '极高频',
};
const MASTERY_LABELS: Record<string, string> = {
  UNKNOWN: '不会', SEEN: '有印象', BASIC: '基础掌握', SOLID: '较熟练', MASTERED: '熟练掌握',
};
const RESULT_LABELS: Record<AttemptResultType, string> = {
  NOT_ANSWERED: '不会', WRONG: '回答错误', PARTIAL: '部分正确', CORRECT: '回答正确',
};

export const difficultyLabel = (value: string) => DIFFICULTY_LABELS[value] ?? value;
export const frequencyLabel = (value: string) => FREQUENCY_LABELS[value] ?? value;
export const masteryLabel = (value: string, description?: string) => description || MASTERY_LABELS[value] || value;
export const attemptResultLabel = (value: AttemptResultType) => RESULT_LABELS[value];
