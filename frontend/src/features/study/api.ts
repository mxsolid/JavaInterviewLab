import { request } from '../../api/client';
import type { NoteData, StudyProgress, SubmitAttemptRequest, SubmitAttemptResponse } from './types';

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

export interface WrongQuestion {
  questionId: number;
  title: string;
  starLevel: number;
  masteryLevel: string;
  attemptCount: number;
  wrongCount: number;
  lastStudiedAt: string;
}

export interface FavoriteQuestion {
  favoriteId: number;
  questionId: number;
  title: string;
  starLevel: number;
  createdAt: string;
}

export interface ReviewTask {
  id: number;
  questionId: number;
  title: string;
  starLevel: number;
  dueAt: string;
  status: string;
  overdue: boolean;
}

export const studyApi = {
  plans: () => request<StudyPlanSummary[]>('/api/study/plans'),
  plan: (planId: number) => request<StudyPlanDetail>(`/api/study/plans/${planId}`),
  activatePlan: (planId: number) => request<CurrentPlan>(`/api/study/plans/${planId}/activate`, { method: 'POST' }),
  currentPlan: () => request<CurrentPlan | null>('/api/study/current-plan'),
  today: () => request<TodayStudy | null>('/api/study/today'),
  wrongQuestions: () => request<WrongQuestion[]>('/api/study/wrong-questions'),
  resolveWrongQuestion: (questionId: number) => request<void>(`/api/study/questions/${questionId}/wrong-book/resolve`, { method: 'PUT' }),
  favorites: () => request<FavoriteQuestion[]>('/api/study/favorites'),
  favoriteQuestion: (questionId: number) => request<void>(`/api/study/favorites/questions/${questionId}`, { method: 'POST' }),
  unfavoriteQuestion: (questionId: number) => request<void>(`/api/study/favorites/questions/${questionId}`, { method: 'DELETE' }),
  todayReviews: () => request<ReviewTask[]>('/api/study/reviews/today'),
  dueReviews: () => request<ReviewTask[]>('/api/study/reviews/due'),
  getQuestionProgress: (questionId: number) => request<StudyProgress>(`/api/study/questions/${questionId}/progress`),
  submitAttempt: (body: SubmitAttemptRequest) => request<SubmitAttemptResponse>('/api/study/attempts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }),
  note: (targetType: 'QUESTION' | 'TOPIC', targetId: number) => request<NoteData | null>(`/api/study/notes?targetType=${targetType}&targetId=${targetId}`),
  createNote: (targetType: 'QUESTION' | 'TOPIC', targetId: number, content: string) => request<NoteData>('/api/study/notes', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ targetType, targetId, content }),
  }),
  updateNote: (id: number, content: string, version: number) => request<NoteData>(`/api/study/notes/${id}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ content, version }),
  }),
};
