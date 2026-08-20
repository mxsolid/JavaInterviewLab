import { request } from '../../api/client';
import type { components } from '../../api/generated/schema';

export type ScenarioSummary = components['schemas']['ScenarioSummaryResponse'];
export type ScenarioDetail = components['schemas']['ScenarioDetailResponse'];
export type ScenarioMatrix = components['schemas']['ScenarioMatrixResponse'];
export type ScenarioAttempt = components['schemas']['ScenarioAttemptResponse'];
export type SourceSummary = components['schemas']['SourceSnippetSummaryResponse'];
export type SourceDetail = components['schemas']['SourceSnippetDetailResponse'];
export type LabDefinition = components['schemas']['LabDefinitionResponse'];
export type InterviewSession = components['schemas']['InterviewSessionResponse'];
export type InterviewTurn = components['schemas']['InterviewTurnResponse'];
export type InterviewFinish = components['schemas']['InterviewFinishResponse'];

export const scenarioApi = {
  list: () => request<ScenarioSummary[]>('/api/v1/scenarios'),
  detail: (id: number) => request<ScenarioDetail>(`/api/v1/scenarios/${id}`),
  matrix: (id: number) => request<ScenarioMatrix>(`/api/v1/scenarios/${id}/matrix`),
  submit: (body: components['schemas']['SubmitScenarioAttemptRequest']) => request<ScenarioAttempt>('/api/v1/scenario-attempts', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  }),
};

export const sourceApi = {
  list: () => request<SourceSummary[]>('/api/v1/source-snippets'),
  detail: (id: number) => request<SourceDetail>(`/api/v1/source-snippets/${id}`),
};

export const labApi = {
  list: () => request<LabDefinition[]>('/api/v1/labs'),
};

export const interviewApi = {
  create: (mode: 'RANDOM' | 'TOPIC', topicCode?: string) => request<InterviewSession>('/api/v1/interviews', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ mode, topicCode }),
  }),
  submit: (sessionId: number, clientTurnId: string, answerText: string) => request<InterviewTurn>(`/api/v1/interviews/${sessionId}/turns`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ clientTurnId, answerText }),
  }),
  finish: (sessionId: number) => request<InterviewFinish>(`/api/v1/interviews/${sessionId}/finish`, { method: 'POST' }),
};
