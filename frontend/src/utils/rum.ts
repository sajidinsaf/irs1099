import { onLCP, onFCP, onCLS, onINP, onTTFB } from 'web-vitals';

const RUM_ENDPOINT = 'https://ingest.verops.io/rum/v2/beacon';
const RUM_APP_ID = 'rum_d1e8a8c104114024';
const RUM_API_KEY = 'rum_pk_JR8PVZsKd6JxxUekTZ7jftTWZfZHZhp21TYj1kxuVRc';

interface WebVitalMetric {
  name: string;
  value: number;
  id: string;
  delta: number;
}

function sendToVerops(metric: WebVitalMetric) {
  const payload = JSON.stringify({
    app_id: RUM_APP_ID,
    api_key: RUM_API_KEY,
    event_type: 'web_vitals',
    timestamp: new Date().toISOString(),
    metric_name: metric.name,
    metric_value: metric.value,
    metric_id: metric.id,
    metric_delta: metric.delta,
    page_url: window.location.href,
    user_agent: navigator.userAgent,
    session_id: getSessionId(),
  });

  // Use sendBeacon with text/plain to avoid CORS preflight
  if (navigator.sendBeacon) {
    const blob = new Blob([payload], { type: 'text/plain' });
    navigator.sendBeacon(RUM_ENDPOINT, blob);
  } else {
    // Fallback: fetch with no-cors mode (fire-and-forget)
    fetch(RUM_ENDPOINT, {
      method: 'POST',
      body: payload,
      mode: 'no-cors',
      keepalive: true,
    }).catch(() => {});
  }
}

function getSessionId(): string {
  let sessionId = sessionStorage.getItem('verops_session_id');
  if (!sessionId) {
    sessionId = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2);
    sessionStorage.setItem('verops_session_id', sessionId);
  }
  return sessionId;
}

/**
 * Initialize Verops RUM web vitals tracking.
 * Call once in main.tsx.
 */
export function initRUM() {
  try {
    onLCP(sendToVerops);
    onFCP(sendToVerops);
    onCLS(sendToVerops);
    onINP(sendToVerops);
    onTTFB(sendToVerops);
  } catch {
    // Silently fail
  }
}
