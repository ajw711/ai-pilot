import axios from "axios";

// 백엔드 API 서버 주소 (스프링 부트 기본 포트 8080 및 공통 프리픽스 /api/v1 설정)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const API_VERSION_URL = `${API_BASE_URL}/api/v1`;

export const api = axios.create({
  baseURL: API_VERSION_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 15000, // 15초 타임아웃 (AI 연동 시 길어질 수 있음)
});

// API 요청 인터셉터 (인증 등 공통 처리)
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// API 응답 인터셉터 (에러 공통 처리)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("API Error:", error.response?.data || error.message);
    return Promise.reject(error);
  },
);
