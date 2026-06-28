import api from './api';

export interface CheckoutResponse {
  sessionId: string;
  url: string;
}

export interface SubscriptionInfo {
  active: boolean;
  planType?: string;
  formsIncluded?: number;
  formsUsed?: number;
  endDate?: string;
}

export const paymentService = {
  getConfig: () => api.get<{ publishableKey: string }>('/payments/config'),
  getPlans: () => api.get('/payments/plans'),
  createPerFormCheckout: (formCount: number) =>
    api.post<CheckoutResponse>('/payments/checkout/per-form', { formCount }),
  createSubscriptionCheckout: () =>
    api.post<CheckoutResponse>('/payments/checkout/subscription'),
  getPaymentHistory: (page = 0, size = 20) =>
    api.get(`/payments/history?page=${page}&size=${size}`),
  getSubscription: () =>
    api.get<SubscriptionInfo>('/payments/subscription'),
};
