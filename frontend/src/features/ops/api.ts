import { fetchSseStream } from "../../lib/api";
import type { SseStreamOptions } from "../../lib/api";
import { useEffect, useRef } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

export const fetchPilotChatStream = async (
  message: string,
  options: SseStreamOptions
): Promise<void> => {
  return fetchSseStream(
    "/api/v1/pilot/chat/stream",
    { message },
    options
  );
};

export interface OpsNotificationPayload {
  type: string;
  trackingId: string;
  appName: string;
  status: string;
  message: string;
}

export const useOpsNotification = (
  userId: string,
  onOpsResult: (payload: OpsNotificationPayload) => void
) => {
  const callbackRef = useRef(onOpsResult);
  
  useEffect(() => {
    callbackRef.current = onOpsResult;
  }, [onOpsResult]);

  useEffect(() => {
    const eventSource = new EventSource(
      `${API_BASE_URL}/api/v1/ops/notifications?userId=${userId}`,
      { withCredentials: true }
    );

    eventSource.addEventListener("ops-result", (event) => {
      try {
        const payload = JSON.parse(event.data);
        callbackRef.current(payload);
      } catch (err) {
        console.error("실시간 Ops 알림 수신 실패:", err);
      }
    });

    return () => {
      eventSource.close();
    };
  }, [userId]);
};
