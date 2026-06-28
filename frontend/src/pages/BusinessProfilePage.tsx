import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { profileService, BusinessProfileData } from '../services/profileService';
import toast from 'react-hot-toast';
import { Loader2, Building2, MapPin, Shield } from 'lucide-react';

const US_STATES = [
  'AL','AK','AZ','AR','CA','CO','CT','DE','FL','GA','HI','ID','IL','IN','IA',
  'KS','KY','LA','ME','MD','MA','MI','MN','MS','MO','MT','NE','NV','NH','NJ',
  'NM','NY','NC','ND','OH','OK','OR','PA','RI','SC','SD','TN','TX','UT','VT',
  'VA','WA','WV','WI','WY','DC','PR','VI','GU','AS','MP',
];

const BUSINESS_TYPES = [
  { value: 'SOLE_PROPRIETORSHIP', label: 'Sole Proprietorship' },
  { value: 'PARTNERSHIP', label: 'Partnership' },
  { value: 'CORPORATION', label: 'Corporation' },
  { value: 'S_CORPORATION', label: 'S Corporation' },
  { value: 'LLC', label: 'LLC' },
  { value: 'TRUST', label: 'Trust' },
  { value: 'ESTATE', label: 'Estate' },
  { value: 'NON_PROFIT', label: 'Non-Profit' },
  { value: 'GOVERNMENT', label: 'Government' },
];

const profileSchema = z.object({
  businessName: z.string().min(1, 'Business name is required'),
  doingBusinessAs: z.string().optional(),
  ein: z.string().regex(/^\d{2}-\d{7}$/, 'EIN must be in format XX-XXXXXXX'),
  businessType: z.string().optional(),
  businessPhone: z.string().optional(),
  addressLine1: z.string().min(1, 'Street address is required'),
  addressLine2: z.string().optional(),
  city: z.string().min(1, 'City is required'),
  state: z.string().length(2, 'Select a state'),
  zipCode: z.string().regex(/^\d{5}(-\d{4})?$/, 'ZIP must be XXXXX or XXXXX-XXXX'),
  country: z.string().optional(),
  useDifferentMailing: z.boolean().optional(),
  mailingAddressLine1: z.string().optional(),
  mailingAddressLine2: z.string().optional(),
  mailingCity: z.string().optional(),
  mailingState: z.string().optional(),
  mailingZipCode: z.string().optional(),
  tcc: z.string().optional(),
  irisClientId: z.string().optional(),
});

type ProfileForm = z.infer<typeof profileSchema>;

export default function BusinessProfilePage() {
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(true);
  const [isEdit, setIsEdit] = useState(false);
  const [showMailing, setShowMailing] = useState(false);
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors }, setValue, watch } = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { country: 'US' },
  });

  const useDifferentMailing = watch('useDifferentMailing');

  useEffect(() => {
    profileService.hasProfile()
      .then((res) => {
        if (res.data.exists) {
          setIsEdit(true);
          return profileService.getProfile();
        }
        return null;
      })
      .then((res) => {
        if (res?.data) {
          const p = res.data;
          setValue('businessName', p.businessName);
          setValue('doingBusinessAs', p.doingBusinessAs || '');
          setValue('businessType', p.businessType || '');
          setValue('businessPhone', p.businessPhone || '');
          setValue('addressLine1', p.addressLine1);
          setValue('addressLine2', p.addressLine2 || '');
          setValue('city', p.city);
          setValue('state', p.state);
          setValue('zipCode', p.zipCode);
          setValue('country', p.country || 'US');
          setValue('tcc', p.tcc || '');

          if (p.mailingAddressLine1) {
            setShowMailing(true);
            setValue('useDifferentMailing', true);
            setValue('mailingAddressLine1', p.mailingAddressLine1 || '');
            setValue('mailingAddressLine2', p.mailingAddressLine2 || '');
            setValue('mailingCity', p.mailingCity || '');
            setValue('mailingState', p.mailingState || '');
            setValue('mailingZipCode', p.mailingZipCode || '');
          }
        }
      })
      .catch(() => { /* No profile yet, that's fine */ })
      .finally(() => setChecking(false));
  }, [setValue]);

  const onSubmit = async (data: ProfileForm) => {
    setLoading(true);
    try {
      const payload: BusinessProfileData = {
        businessName: data.businessName,
        doingBusinessAs: data.doingBusinessAs,
        ein: data.ein,
        businessType: data.businessType,
        businessPhone: data.businessPhone,
        addressLine1: data.addressLine1,
        addressLine2: data.addressLine2,
        city: data.city,
        state: data.state,
        zipCode: data.zipCode,
        country: data.country || 'US',
        mailingAddressLine1: data.useDifferentMailing ? data.mailingAddressLine1 : undefined,
        mailingAddressLine2: data.useDifferentMailing ? data.mailingAddressLine2 : undefined,
        mailingCity: data.useDifferentMailing ? data.mailingCity : undefined,
        mailingState: data.useDifferentMailing ? data.mailingState : undefined,
        mailingZipCode: data.useDifferentMailing ? data.mailingZipCode : undefined,
        tcc: data.tcc,
        irisClientId: data.irisClientId,
      };

      if (isEdit) {
        await profileService.updateProfile(payload);
        toast.success('Business profile updated');
      } else {
        await profileService.createProfile(payload);
        toast.success('Business profile created');
      }
      navigate('/dashboard');
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to save profile';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  if (checking) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 size={32} className="animate-spin text-primary-600" />
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {isEdit ? 'Edit Business Profile' : 'Set Up Your Business Profile'}
        </h1>
        <p className="text-gray-600 mt-1">
          {isEdit
            ? 'Update your business information for IRS filings.'
            : 'Complete your business profile to start filing 1099s. This information appears on every form you submit.'}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        {/* Business Information */}
        <div className="card">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-2 bg-primary-100 rounded-lg">
              <Building2 className="text-primary-600" size={20} />
            </div>
            <h2 className="text-xl font-semibold">Business Information</h2>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Legal Business Name *</label>
              <input {...register('businessName')} className="input-field" placeholder="Acme Corporation" />
              {errors.businessName && <p className="text-red-500 text-sm mt-1">{errors.businessName.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Doing Business As (DBA)</label>
              <input {...register('doingBusinessAs')} className="input-field" placeholder="Optional trade name" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  EIN (Employer Identification Number) *
                </label>
                <input {...register('ein')} className="input-field" placeholder="XX-XXXXXXX"
                  maxLength={10} />
                {errors.ein && <p className="text-red-500 text-sm mt-1">{errors.ein.message}</p>}
                {isEdit && <p className="text-xs text-gray-500 mt-1">Re-enter your EIN to update</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Business Type</label>
                <select {...register('businessType')} className="input-field">
                  <option value="">Select type...</option>
                  {BUSINESS_TYPES.map((t) => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Business Phone</label>
              <input {...register('businessPhone')} type="tel" className="input-field" placeholder="(555) 123-4567" />
            </div>
          </div>
        </div>

        {/* Physical Address */}
        <div className="card">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-2 bg-primary-100 rounded-lg">
              <MapPin className="text-primary-600" size={20} />
            </div>
            <h2 className="text-xl font-semibold">Physical Address</h2>
          </div>
          <p className="text-sm text-gray-500 mb-4">Must be a physical location, not a PO Box (IRS requirement).</p>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Street Address *</label>
              <input {...register('addressLine1')} className="input-field" placeholder="123 Main Street" />
              {errors.addressLine1 && <p className="text-red-500 text-sm mt-1">{errors.addressLine1.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Suite / Unit</label>
              <input {...register('addressLine2')} className="input-field" placeholder="Suite 100" />
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">City *</label>
                <input {...register('city')} className="input-field" placeholder="New York" />
                {errors.city && <p className="text-red-500 text-sm mt-1">{errors.city.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">State *</label>
                <select {...register('state')} className="input-field">
                  <option value="">--</option>
                  {US_STATES.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
                {errors.state && <p className="text-red-500 text-sm mt-1">{errors.state.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">ZIP Code *</label>
                <input {...register('zipCode')} className="input-field" placeholder="10001" />
                {errors.zipCode && <p className="text-red-500 text-sm mt-1">{errors.zipCode.message}</p>}
              </div>
            </div>

            <div className="pt-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  {...register('useDifferentMailing')}
                  onChange={(e) => {
                    register('useDifferentMailing').onChange(e);
                    setShowMailing(e.target.checked);
                  }}
                  className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span className="text-sm text-gray-700">Mailing address is different from physical address</span>
              </label>
            </div>
          </div>

          {/* Mailing Address */}
          {showMailing && (
            <div className="mt-6 pt-6 border-t border-gray-200 space-y-4">
              <h3 className="font-medium text-gray-900">Mailing Address</h3>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Street Address</label>
                <input {...register('mailingAddressLine1')} className="input-field" placeholder="PO Box 123" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Suite / Unit</label>
                <input {...register('mailingAddressLine2')} className="input-field" />
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
                  <input {...register('mailingCity')} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
                  <select {...register('mailingState')} className="input-field">
                    <option value="">--</option>
                    {US_STATES.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">ZIP Code</label>
                  <input {...register('mailingZipCode')} className="input-field" />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* IRIS Credentials (Optional) */}
        <div className="card">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-2 bg-primary-100 rounded-lg">
              <Shield className="text-primary-600" size={20} />
            </div>
            <h2 className="text-xl font-semibold">IRIS Credentials</h2>
            <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">Optional</span>
          </div>
          <p className="text-sm text-gray-500 mb-4">
            If you already have an IRS IRIS Transmitter Control Code (TCC), enter it here.
            If not, we can help you apply for one.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Transmitter Control Code (TCC)</label>
              <input {...register('tcc')} className="input-field" placeholder="DXXXX" maxLength={5} />
              <p className="text-xs text-gray-500 mt-1">5-character code starting with 'D'</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">IRIS API Client ID</label>
              <input {...register('irisClientId')} className="input-field" placeholder="Your IRIS Client ID" />
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="btn-secondary"
          >
            {isEdit ? 'Cancel' : 'Skip for now'}
          </button>
          <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2">
            {loading && <Loader2 size={18} className="animate-spin" />}
            {isEdit ? 'Save Changes' : 'Complete Setup'}
          </button>
        </div>
      </form>
    </div>
  );
}
