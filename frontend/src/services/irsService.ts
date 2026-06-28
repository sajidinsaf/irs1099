import api from './api';

export interface ValidationResult {
  ready: boolean;
  errors: string[];
  warnings: string[];
  recordCount: number;
  formType: string;
  taxYear: number;
}

export interface SubmitResult {
  success: boolean;
  receiptId?: string;
  utid?: string;
  timestamp?: string;
  error?: string;
}

export interface StatusResult {
  success: boolean;
  status?: string;
  receiptId?: string;
  acceptedCount?: number;
  rejectedCount?: number;
  errorCode?: string;
  errorMessage?: string;
  error?: string;
}

export const irsService = {
  validate: (submissionId: number) =>
    api.get<ValidationResult>(`/irs/validate/${submissionId}`),
  previewXml: (submissionId: number) =>
    api.get<string>(`/irs/preview/${submissionId}`, {
      headers: { Accept: 'application/xml' },
      responseType: 'text' as const,
    }),
  submit: (submissionId: number) =>
    api.post<SubmitResult>(`/irs/submit/${submissionId}`),
  checkStatus: (submissionId: number) =>
    api.get<StatusResult>(`/irs/status/${submissionId}`),
};
