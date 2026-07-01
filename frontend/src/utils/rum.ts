import { onLCP, onFCP, onCLS, onINP, onTTFB } from 'web-vitals';

// RUM data is sent to our own backend proxy at /observability/rum/collect
// Backend forwards to Verops server-side (no CORS issues)
const RUM_PROXY = import.meta.env.DEV ? '/api/observability/rum/collect' : '/observability/rum/collect';

interface WebVitalMetric {
  name: string;
  value: number;
  id: string;
  delta: number;
}

function sendToProxy(metric: WebVitalMetric) {
  const payload = {
    event_type: 'web_vitals',
    timestamp: new Date().toISOString(),
    metric_name: metric.name,
    metric_value: metric.value,
    metric_id: metric.id,
    metric_delta: metric.delta,
    page_url: window.location.href,
    session_id: getSessionId(),
  };

  fetch(RUM_PROXY, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
    keepalive: true,
  }).catch(() => {});
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
 * Initialize RUM web vitals tracking.
 * Sends data to our backend proxy which forwards to Verops.
 */
export function initRUM() {
  try {
    onLCP(sendToProxy);
    onFCP(sendToProxy);
    onCLS(sendToProxy);
    onINP(sendToProxy);
    onTTFB(sendToProxy);
  } catch {
    // Silently fail
  }
}
