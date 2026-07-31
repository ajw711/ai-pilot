import { api } from "../../lib/api";
import { useEffect, useRef } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

export interface OpsNotificationPayload {
  type: string;
  trackingId: string;
  appName: string;
  status: string;
  message: string;
}

export const useOpsNotification = (
  onOpsResult: (payload: OpsNotificationPayload) => void
) => {
  const callbackRef = useRef(onOpsResult);

  useEffect(() => {
    callbackRef.current = onOpsResult;
  }, [onOpsResult]);

  useEffect(() => {
    let eventSource: EventSource | null = null;

    const establishSse = async () => {
      try {
        const res = await api.post("/sse/ticket");
        const { ticket } = res.data.data;

        eventSource = new EventSource(
          `${API_BASE_URL}/api/v1/ops/notifications?ticket=${ticket}`,
          { withCredentials: true }
        );

        eventSource.addEventListener("ops-result", (event) => {
          try {
            const payload = JSON.parse(event.data);
            callbackRef.current(payload);
          } catch (err) {
            console.error("실시간 Ops 알림 데이터 파싱 오류:", err);
          }
        });
      } catch (err) {
        console.error("SSE 연결 수립 실패:", err);
      }
    };

    establishSse();

    return () => {
      if (eventSource) {
        eventSource.close();
      }
    };
  }, []);
};
