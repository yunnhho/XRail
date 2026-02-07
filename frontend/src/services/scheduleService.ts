import apiClient from '../api/apiClient';

export interface Schedule {
    scheduleId: number;
    trainType: string;
    trainNumber: string;
    routeName: string;
    departureDate: string;
    departureTime: string;
    arrivalTime: string;
    price: number;
    isSoldOut: boolean;
}
export const searchSchedules = async (departureStationId: number, arrivalStationId: number, date: string) => {
    const response = await apiClient.get<ApiResponse<Schedule[]>>('/schedules', {
        params: { departureStationId, arrivalStationId, date }
    });
    return response.data.data || [];
};

interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
      code: string;
      message: string;
  } | null;
}
