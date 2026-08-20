const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  traceId: string;
}

export class ApiRequestError extends Error {
  constructor(
    message: string,
    readonly code?: string,
    readonly status?: number,
    readonly traceId?: string,
  ) {
    super(message);
    this.name = 'ApiRequestError';
  }
}

/** 后续功能统一从此处发起请求，避免各页面散落服务地址和错误处理。 */
export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, init);
  } catch {
    throw new ApiRequestError('无法连接后端服务，请确认服务已启动。', 'NETWORK_ERROR');
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (contentType.includes('application/json')) {
    const body = await response.json() as ApiResponse<T>;
    if (response.ok && body.success) {
      return body.data;
    }
    throw new ApiRequestError(body.message || response.statusText, body.code, response.status, body.traceId);
  }

  const text = await response.text();
  throw new ApiRequestError(text || response.statusText || '请求失败', 'HTTP_ERROR', response.status);
}
