import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8088/api', // Backend API URL
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // Token expired logic could go here (refresh token handling)
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      // For now, just redirect to login if 401
      // window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
