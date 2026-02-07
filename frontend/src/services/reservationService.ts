import apiClient from '../api/apiClient';

export interface Seat {
  seatId: number;
  carriageNumber: number;
  seatNumber: string;
  isReserved: boolean;
}

export const getSeats = async (scheduleId: number, startStationId: number, endStationId: number) => {
  const response = await apiClient.get<ApiResponse<Seat[]>>(`/schedules/${scheduleId}/seats`, {
    params: { startStationId, endStationId }
  });
  return response.data.data;
};

export interface ReservationRequest {
  scheduleId: number;
  seatIds: number[]; // 변경: 다중 좌석
  startStationId: number;
  endStationId: number;
  startStationIdx: number;
  endStationIdx: number;
  price: number;
}

export const createReservation = async (request: ReservationRequest) => {
  const response = await apiClient.post<ApiResponse<number>>('/reservations', request);
  return response.data.data;
};

export const payReservation = async (reservationId: number): Promise<string | null> => {
    const response = await apiClient.post<ApiResponse<string>>(`/reservations/${reservationId}/pay`);
    return response.data.data;
};

export const cancelReservation = async (reservationId: number) => {
    await apiClient.delete<ApiResponse<void>>(`/reservations/${reservationId}`);
};

interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
      code: string;
      message: string;
  } | null;
}