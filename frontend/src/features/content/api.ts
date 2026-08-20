import { request } from '../../api/client';
import type { components } from '../../api/generated/schema';

export type ContentStatus = 'ENABLED' | 'DISABLED';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type FrequencyLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
export interface Category { id: number; code: string; name: string; description?: string; sortOrder: number; status: ContentStatus; }
export interface Topic { id: number; categoryId: number; categoryName: string; code: string; name: string; description?: string; starLevel: number; sortOrder: number; status: ContentStatus; }
export interface Tag { id: number; code: string; name: string; }
export interface QuestionSummary { id: number; topicId: number; topicName: string; categoryId: number; categoryName: string; title: string; starLevel: number; difficulty: Difficulty; frequencyLevel: FrequencyLevel; status: ContentStatus; oneLiner?: string; version: number; }
export interface QuestionDetail extends QuestionSummary { questionType: string; originType: string; plainExplanation?: string; designReason?: string; commonMistakes?: string; scorePoints?: string; tags: Tag[]; answers: { answerType: string; content: string; sortOrder: number }[]; followUps: { id: number; title: string; referenceAnswer?: string; sortOrder: number }[]; }
export interface Page<T> { items: T[]; total: number; page: number; pageSize: number; }
export interface QuestionPayload { topicId: number; title: string; questionType?: string; starLevel: number; difficulty: Difficulty; frequencyLevel: FrequencyLevel; originType?: string; status?: ContentStatus; oneLiner?: string; plainExplanation?: string; designReason?: string; commonMistakes?: string; scorePoints?: string; version?: number; tagIds: number[]; answers: { answerType: string; content: string; sortOrder: number }[]; followUps: { title: string; referenceAnswer?: string; sortOrder: number }[]; }
export type SystemStatus = components['schemas']['SystemStatusResponse'];

export const contentApi = {
  categories: () => request<Category[]>('/api/categories'),
  createCategory: (body: Omit<Category, 'id'>) => request<Category>('/api/categories', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  updateCategory: (id: number, body: Omit<Category, 'id'>) => request<Category>(`/api/categories/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  topics: (categoryId?: number) => request<Topic[]>(`/api/topics${categoryId ? `?categoryId=${categoryId}` : ''}`),
  createTopic: (body: Omit<Topic, 'id' | 'categoryName'>) => request<Topic>('/api/topics', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  updateTopic: (id: number, body: Omit<Topic, 'id' | 'categoryName'>) => request<Topic>(`/api/topics/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  tags: () => request<Tag[]>('/api/tags'),
  createTag: (body: Omit<Tag, 'id'>) => request<Tag>('/api/tags', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  updateTag: (id: number, body: Omit<Tag, 'id'>) => request<Tag>(`/api/tags/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  questions: (params: Record<string, string | number | undefined>) => request<Page<QuestionSummary>>(`/api/questions?${new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined && value !== '').map(([key, value]) => [key, String(value)])).toString()}`),
  question: (id: number) => request<QuestionDetail>(`/api/questions/${id}`),
  createQuestion: (body: Omit<QuestionPayload, 'version'>) => request<QuestionDetail>('/api/questions', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  updateQuestion: (id: number, body: QuestionPayload & { version: number }) => request<QuestionDetail>(`/api/questions/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
  systemStatus: () => request<SystemStatus>('/api/v1/system/status'),
  importSeed: (file: File) => { const form = new FormData(); form.append('file', file); return request<{ seedPack: string; created: number; skipped: number }>('/api/system/seeds/import', { method: 'POST', body: form }); },
};
