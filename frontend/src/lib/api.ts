import axios from "axios";
import { toast } from "react-hot-toast";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const API_VERSION_URL = `${API_BASE_URL}/api/v1`;

let accessTokenInMemory: string | null = null;

export const setAccessToken = (token: string | null) => {
  accessTokenInMemory = token;
};

export const getAccessToken = () => accessTokenInMemory;

export const api = axios.create({
  baseURL: API_VERSION_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  timeout: 15000,
});

api.interceptors.request.use(
  (config) => {
    if (accessTokenInMemory) {
      config.headers.Authorization = `Bearer ${accessTokenInMemory}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const status = error.response?.status;
    const isRefreshEndpoint = originalRequest?.url?.includes("/auth/refresh");

    if (status === 401 && !originalRequest._retry && !isRefreshEndpoint) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await axios.post(
          `${API_VERSION_URL}/auth/refresh`,
          {},
          { withCredentials: true },
        );
        const { accessToken } = res.data.data;

        setAccessToken(accessToken);
        api.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        processQueue(null, accessToken);
        isRefreshing = false;

        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        setAccessToken(null);
        localStorage.removeItem("isLoggedIn");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    if (status === 403) {
      console.warn("[403 Forbidden] 권한이 없는 요청입니다.");
      toast.error("해당 기능에 대한 접근 권한이 없습니다.", {
        id: "forbidden-403", // 중복 팝업 방지 ID
        duration: 3500, // 3.5초 후 자동 소멸
      });
      return Promise.reject(error);
    }

    return Promise.reject(error);
  },
);

export interface SseEvent {
  event: string;
  data: string;
  id: string;
  retry?: number;
}

export interface SseStreamOptions {
  onMessage: (event: SseEvent) => void;
  onComplete?: () => void;
  onError?: (error: Error) => void;
}

export const fetchSseStream = async <Req>(
  url: string,
  payload: Req,
  options: SseStreamOptions,
): Promise<void> => {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
  const token = getAccessToken();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "text/event-stream",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: "POST",
    headers,
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const reader = response.body?.getReader();
  const decoder = new TextDecoder("utf-8");
  if (!reader) {
    throw new Error("ReadableStream reader를 사용할 수 없습니다.");
  }

  let lineBuffer = "";

  let currentEventName = "";
  let currentDataBuffer = "";
  let currentId = "";
  let currentRetry: number | undefined = undefined;

  const dispatchEvent = () => {
    if (currentDataBuffer) {
      options.onMessage({
        event: currentEventName || "message",
        data: currentDataBuffer.replace(/\n$/, ""),
        id: currentId,
        retry: currentRetry,
      });

      currentDataBuffer = "";
      currentEventName = "";
    }
  };

  const processLine = (line: string) => {
    if (line.startsWith(":")) {
      return;
    }

    if (line === "") {
      dispatchEvent();
      return;
    }

    const colonIndex = line.indexOf(":");
    let field = line;
    let value = "";

    if (colonIndex !== -1) {
      field = line.substring(0, colonIndex);
      value = line.substring(colonIndex + 1);
      if (value.startsWith(" ")) {
        value = value.substring(1);
      }
    }

    switch (field) {
      case "data":
        currentDataBuffer += value + "\n";
        break;
      case "event":
        currentEventName = value;
        break;
      case "id":
        currentId = value;
        break;
      case "retry":
        const retryVal = parseInt(value, 10);
        if (!isNaN(retryVal)) {
          currentRetry = retryVal;
        }
        break;
      default:
        break;
    }
  };

  const findLineEnd = (buffer: string): number => {
    const index = buffer.indexOf("\n");
    if (index !== -1) {
      return index;
    }
    return buffer.indexOf("\r\n");
  };

  try {
    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        const remain = decoder.decode();
        if (remain) {
          lineBuffer += remain;
        }

        if (lineBuffer) {
          const lastLines = lineBuffer.split(/\r?\n/);
          lastLines.forEach(processLine);
        }
        dispatchEvent();
        options.onComplete?.();
        break;
      }

      const chunk = decoder.decode(value, { stream: true });
      lineBuffer += chunk;

      let lineEndIndex: number;
      while ((lineEndIndex = findLineEnd(lineBuffer)) !== -1) {
        const line = lineBuffer.substring(0, lineEndIndex);
        const cleanLine = line.endsWith("\r")
          ? line.substring(0, line.length - 1)
          : line;

        lineBuffer = lineBuffer.substring(
          lineEndIndex + (lineBuffer.charAt(lineEndIndex) === "\n" ? 1 : 2),
        );
        processLine(cleanLine);
      }
    }
  } catch (error) {
    const err = error instanceof Error ? error : new Error(String(error));
    options.onError?.(err);
    throw err;
  }
};
