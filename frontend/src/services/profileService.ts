import api from './api';

export interface BusinessProfileData {
  businessName: string;
  doingBusinessAs?: string;
  ein: string;
  businessType?: string;
  businessPhone?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode: string;
  country?: string;
  mailingAddressLine1?: string;
  mailingAddressLine2?: string;
  mailingCity?: string;
  mailingState?: string;
  mailingZipCode?: string;
  tcc?: string;
  irisClientId?: string;
}

export interface BusinessProfileResponse {
  id: number;
  businessName: string;
  doingBusinessAs: string | null;
  einMasked: string;
  businessType: string | null;
  businessPhone: string | null;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  mailingAddressLine1: string | null;
  mailingAddressLine2: string | null;
  mailingCity: string | null;
  mailingState: string | null;
  mailingZipCode: string | null;
  tcc: string | null;
  hasIrisClientId: boolean;
}

export const profileService = {
  getProfile: () => api.get<BusinessProfileResponse>('/profile'),
  hasProfile: () => api.get<{ exists: boolean }>('/profile/exists'),
  createProfile: (data: BusinessProfileData) => api.post<BusinessProfileResponse>('/profile', data),
  updateProfile: (data: BusinessProfileData) => api.put<BusinessProfileResponse>('/profile', data),
};
