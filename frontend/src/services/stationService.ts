import apiClient from '../api/apiClient';

export interface Station {
    id: number;
    name: string;
    idx: number; // Client-side mapped index
}

export const getStations = async () => {
    const response = await apiClient.get<ApiResponse<Station[]>>('/stations');
    const data = response.data.data || [];
    // Sort by ID to ensure order matches Seoul -> Busan roughly
    data.sort((a, b) => a.id - b.id);
    
    // Add idx
    return data.map((s, i) => ({
        ...s,
        idx: i
    }));
};

interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
      code: string;
      message: string;
  } | null;
}
