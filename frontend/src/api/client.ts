const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  traceId: string;
}

export class ApiRequestError extends Error {
  constructor(message: string, readonly traceId?: string) {
    super(message);
  }
}

/** 后续功能统一从此处发起请求，避免各页面散落服务地址和错误处理。 */
export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, init);
  const body = await response.json() as ApiResponse<T>;
  if (!response.ok || !body.success) {
    throw new ApiRequestError(body.message, body.traceId);
  }
  return body.data;
}
