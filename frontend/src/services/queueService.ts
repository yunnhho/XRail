import apiClient from '../api/apiClient';

export interface QueueStatus {
  status: 'WAITING' | 'ACTIVE';
  rank: number;
  expectedWaitSeconds: number;
}

export const registerQueue = async () => {
  const response = await apiClient.post<ApiResponse<string>>('/queue/token');
  return response.data.data;
};

export const getQueueStatus = async () => {
  const response = await apiClient.get<ApiResponse<QueueStatus>>('/queue/status');
  return response.data.data;
};

// Common ApiResponse matching Java DTO
export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
      code: string;
      message: string;
  } | null;
}
