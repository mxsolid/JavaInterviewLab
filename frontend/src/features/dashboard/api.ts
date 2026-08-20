import { request } from '../../api/client';
import type { CurrentPlan } from '../study/api';

export interface RecentStudyItem {
  questionId: number;
  title: string;
  starLevel: number;
  masteryLevel: string;
  lastStudiedAt: string;
}

export interface DashboardData {
  currentPlan: CurrentPlan | null;
  timeProgressDay: number | null;
  planDurationDays: number | null;
  todayPlanItemCount: number;
  todayReviewCount: number;
  totalQuestionCount: number;
  touchedQuestionCount: number;
  solidQuestionCount: number;
  masteredQuestionCount: number;
  fiveStarMasteryRate: number;
  activeWrongQuestionCount: number;
  favoriteQuestionCount: number;
  recentStudyItems: RecentStudyItem[];
}

export const dashboardApi = {
  get: () => request<DashboardData>('/api/dashboard'),
};
