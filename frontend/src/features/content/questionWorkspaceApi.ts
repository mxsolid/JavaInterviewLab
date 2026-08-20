import { request } from '../../api/client';
import type { components } from '../../api/generated/schema';

export type QuestionWorkspace = components['schemas']['QuestionWorkspaceResponse'];
export type QuestionLearning = components['schemas']['QuestionLearningResponse'];
export type AnswerView = components['schemas']['AnswerViewResponse'];

export const questionWorkspaceApi = {
  question: (questionId: number) => request<QuestionWorkspace>(`/api/v1/questions/${questionId}`),
  learning: (questionId: number) => request<QuestionLearning>(`/api/v1/questions/${questionId}/learning`),
  answerView: (questionId: number, clientViewId: string) => request<AnswerView>(`/api/v1/questions/${questionId}/answer-view`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ clientViewId }),
  }),
};
