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
  const payload = {
    app_id: RUM_APP_ID,
    event_type: 'web_vitals',
    timestamp: new Date().toISOString(),
    metric_name: metric.name,
    metric_value: metric.value,
    metric_id: metric.id,
    metric_delta: metric.delta,
    page_url: window.location.href,
    user_agent: navigator.userAgent,
    session_id: getSessionId(),
  };

  // Use sendBeacon for reliability (survives page unload)
  if (navigator.sendBeacon) {
    const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
    navigator.sendBeacon(`${RUM_ENDPOINT}?apiKey=${RUM_API_KEY}`, blob);
  } else {
    fetch(`${RUM_ENDPOINT}?apiKey=${RUM_API_KEY}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      keepalive: true,
    }).catch(() => {
      // Silently fail - don't impact user experience
    });
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
