import api from './api';

export const adminService = {
  // Dashboard
  getStats: () => api.get('/admin/stats'),

  // Users
  getUsers: (page = 0, size = 20) => api.get(`/admin/users?page=${page}&size=${size}`),
  getUser: (userId: number) => api.get(`/admin/users/${userId}`),
  updateUserRole: (userId: number, role: string) => api.post(`/admin/users/${userId}/role`, { role }),
  forceVerifyEmail: (userId: number) => api.post(`/admin/users/${userId}/verify-email`),
  resetPassword: (userId: number, password: string) => api.post(`/admin/users/${userId}/reset-password`, { password }),
  deactivateUser: (userId: number) => api.post(`/admin/users/${userId}/deactivate`),

  // Submissions
  getSubmissions: (page = 0, size = 20, status?: string) =>
    api.get(`/admin/submissions?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
  updateSubmissionStatus: (submissionId: number, status: string) =>
    api.post(`/admin/submissions/${submissionId}/status`, { status }),

  // Payments
  getPayments: (page = 0, size = 20) => api.get(`/admin/payments?page=${page}&size=${size}`),

  // Subscriptions
  getSubscriptions: (page = 0, size = 20) => api.get(`/admin/subscriptions?page=${page}&size=${size}`),
  modifySubscription: (subscriptionId: number, changes: Record<string, unknown>) =>
    api.post(`/admin/subscriptions/${subscriptionId}/modify`, changes),

  // Audit Log
  getAuditLog: (page = 0, size = 50) => api.get(`/admin/audit-log?page=${page}&size=${size}`),

  // Notifications
  getNotifications: (page = 0, size = 20) => api.get(`/admin/notifications?page=${page}&size=${size}`),
  sendSystemNotification: (subject: string, body: string) =>
    api.post('/admin/notifications/system', { subject, body }),
};
