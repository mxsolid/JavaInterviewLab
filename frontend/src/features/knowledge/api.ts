import { request } from '../../api/client';
import type { components } from '../../api/generated/schema';

export type KnowledgeMapData = components['schemas']['KnowledgeMapResponse'];
export type KnowledgeCategory = components['schemas']['KnowledgeCategoryResponse'];
export type KnowledgeTopic = components['schemas']['KnowledgeTopicResponse'];

export const knowledgeApi = {
  map: () => request<KnowledgeMapData>('/api/v1/knowledge-map'),
};
