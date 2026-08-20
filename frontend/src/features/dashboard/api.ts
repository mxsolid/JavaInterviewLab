import { request } from '../../api/client';
import type { components } from '../../api/generated/schema';

export type WorkbenchData = components['schemas']['WorkbenchResponse'];

export const dashboardApi = {
  getWorkbench: () => request<WorkbenchData>('/api/v1/workbench'),
};
