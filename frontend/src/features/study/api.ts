import { request } from '../../api/client';

export interface StudyPlanSummary {
  id: number;
  code: string;
  name: string;
  durationDays: number;
  description?: string;
}

export interface StudyPlanItem {
  id: number;
  targetType: 'TOPIC' | 'QUESTION' | 'SCENARIO';
  targetId: number;
  targetTitle?: string;
  sortOrder: number;
}

export interface StudyPlanDay {
  id: number;
  dayNumber: number;
  title: string;
  description?: string;
  items: StudyPlanItem[];
}

export interface StudyPlanDetail extends StudyPlanSummary {
  days: StudyPlanDay[];
}

export interface CurrentPlan {
  planId: number;
  planCode: string;
  planName: string;
  durationDays: number;
  startedAt: string;
  timeProgressDay: number;
}

export interface TodayStudy {
  currentPlan: CurrentPlan;
  day: StudyPlanDay;
}

export const studyApi = {
  plans: () => request<StudyPlanSummary[]>('/api/study/plans'),
  plan: (planId: number) => request<StudyPlanDetail>(`/api/study/plans/${planId}`),
  activatePlan: (planId: number) => request<CurrentPlan>(`/api/study/plans/${planId}/activate`, { method: 'POST' }),
  currentPlan: () => request<CurrentPlan | null>('/api/study/current-plan'),
  today: () => request<TodayStudy | null>('/api/study/today'),
};
