import api from './api';

export interface CreateSubmissionData {
  formType: string;
  taxYear: number;
  cfsfFiling?: boolean;
}

export interface FormRecordData {
  issuerEin: string;
  issuerName: string;
  issuerAddressLine1: string;
  issuerAddressLine2?: string;
  issuerCity: string;
  issuerState: string;
  issuerZipCode: string;
  issuerPhone?: string;
  recipientTin: string;
  recipientTinType: string;
  recipientFirstName?: string;
  recipientMiddleName?: string;
  recipientLastName?: string;
  recipientBusinessName?: string;
  recipientAddressLine1: string;
  recipientAddressLine2?: string;
  recipientCity: string;
  recipientState: string;
  recipientZipCode: string;
  recipientCountry?: string;
  recipientAccountNumber?: string;
  formDataJson: string;
}

export interface SubmissionResponse {
  id: number;
  formType: string;
  taxYear: number;
  transmissionType: string;
  status: string;
  utid: string | null;
  receiptId: string | null;
  cfsfFiling: boolean;
  recordCount: number;
  submittedAt: string | null;
  createdAt: string;
  irsErrors: string | null;
}

export interface FormRecordResponse {
  id: number;
  recordId: string;
  formType: string;
  issuerEinMasked: string;
  issuerName: string;
  issuerAddressLine1: string;
  issuerCity: string;
  issuerState: string;
  issuerZipCode: string;
  recipientTinMasked: string;
  recipientTinType: string;
  recipientFirstName: string;
  recipientLastName: string;
  recipientBusinessName: string;
  recipientAddressLine1: string;
  recipientCity: string;
  recipientState: string;
  recipientZipCode: string;
  formDataJson: string;
  status: string;
  createdAt: string;
}

// 1099-NEC specific financial data
export interface NecFormData {
  box1NonemployeeCompensation: string;
  box2PayerMadeDirectSales: boolean;
  box4FederalTaxWithheld: string;
  box5StateTaxWithheld: string;
  box6StatePayersNo: string;
  box7StateIncome: string;
  stateCode: string;
}

export const submissionService = {
  create: (data: CreateSubmissionData) =>
    api.post<SubmissionResponse>('/submissions', data),
  list: (page = 0, size = 20) =>
    api.get<{ content: SubmissionResponse[]; totalElements: number }>(`/submissions?page=${page}&size=${size}`),
  get: (id: number) =>
    api.get<SubmissionResponse>(`/submissions/${id}`),
  delete: (id: number) =>
    api.delete(`/submissions/${id}`),

  // Records
  addRecord: (submissionId: number, data: FormRecordData) =>
    api.post<FormRecordResponse>(`/submissions/${submissionId}/records`, data),
  getRecords: (submissionId: number) =>
    api.get<FormRecordResponse[]>(`/submissions/${submissionId}/records`),
  updateRecord: (submissionId: number, recordId: number, data: FormRecordData) =>
    api.put<FormRecordResponse>(`/submissions/${submissionId}/records/${recordId}`, data),
  deleteRecord: (submissionId: number, recordId: number) =>
    api.delete(`/submissions/${submissionId}/records/${recordId}`),
};
