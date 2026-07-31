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
    const token = localStorage.getItem("access_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
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

  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
    },
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
        data: currentDataBuffer.trim(),
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
