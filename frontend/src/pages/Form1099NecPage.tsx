import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { submissionService, FormRecordData, FormRecordResponse, SubmissionResponse } from '../services/submissionService';
import toast from 'react-hot-toast';
import { Loader2, Plus, Trash2, FileText, ArrowLeft, Users } from 'lucide-react';

const US_STATES = [
  'AL','AK','AZ','AR','CA','CO','CT','DE','FL','GA','HI','ID','IL','IN','IA',
  'KS','KY','LA','ME','MD','MA','MI','MN','MS','MO','MT','NE','NV','NH','NJ',
  'NM','NY','NC','ND','OH','OK','OR','PA','RI','SC','SD','TN','TX','UT','VT',
  'VA','WA','WV','WI','WY','DC','PR','VI','GU','AS','MP',
];

const recordSchema = z.object({
  // Issuer (payer)
  issuerEin: z.string().regex(/^\d{2}-\d{7}$/, 'EIN must be XX-XXXXXXX'),
  issuerName: z.string().min(1, 'Required'),
  issuerAddressLine1: z.string().min(1, 'Required'),
  issuerAddressLine2: z.string().optional(),
  issuerCity: z.string().min(1, 'Required'),
  issuerState: z.string().length(2, 'Required'),
  issuerZipCode: z.string().regex(/^\d{5}(-\d{4})?$/, 'Invalid ZIP'),
  issuerPhone: z.string().optional(),

  // Recipient (payee)
  recipientTinType: z.enum(['SSN', 'EIN', 'ITIN', 'ATIN']),
  recipientTin: z.string().min(1, 'Required'),
  recipientFirstName: z.string().optional(),
  recipientLastName: z.string().optional(),
  recipientBusinessName: z.string().optional(),
  recipientAddressLine1: z.string().min(1, 'Required'),
  recipientAddressLine2: z.string().optional(),
  recipientCity: z.string().min(1, 'Required'),
  recipientState: z.string().length(2, 'Required'),
  recipientZipCode: z.string().regex(/^\d{5}(-\d{4})?$/, 'Invalid ZIP'),
  recipientAccountNumber: z.string().optional(),

  // 1099-NEC Boxes
  box1: z.string().min(1, 'Box 1 is required'),
  box2: z.boolean().optional(),
  box4: z.string().optional(),
  box5: z.string().optional(),
  box6: z.string().optional(),
  box7: z.string().optional(),
  stateCode: z.string().optional(),
});

type RecordForm = z.infer<typeof recordSchema>;

export default function Form1099NecPage() {
  const { submissionId } = useParams<{ submissionId: string }>();
  const navigate = useNavigate();
  const [submission, setSubmission] = useState<SubmissionResponse | null>(null);
  const [records, setRecords] = useState<FormRecordResponse[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  const { register, handleSubmit, formState: { errors }, reset, watch } = useForm<RecordForm>({
    resolver: zodResolver(recordSchema),
    defaultValues: {
      recipientTinType: 'SSN',
      box2: false,
    },
  });

  const tinType = watch('recipientTinType');

  useEffect(() => {
    if (!submissionId) return;
    const id = parseInt(submissionId);

    Promise.all([
      submissionService.get(id),
      submissionService.getRecords(id),
    ]).then(([subRes, recRes]) => {
      setSubmission(subRes.data);
      setRecords(recRes.data);
    }).catch(() => {
      toast.error('Failed to load submission');
      navigate('/dashboard');
    }).finally(() => setLoading(false));
  }, [submissionId, navigate]);

  const onSubmit = async (data: RecordForm) => {
    if (!submissionId) return;
    setSaving(true);

    try {
      const formDataJson = JSON.stringify({
        box1NonemployeeCompensation: data.box1,
        box2PayerMadeDirectSales: data.box2 || false,
        box4FederalTaxWithheld: data.box4 || '0',
        box5StateTaxWithheld: data.box5 || '0',
        box6StatePayersNo: data.box6 || '',
        box7StateIncome: data.box7 || '0',
        stateCode: data.stateCode || '',
      });

      const payload: FormRecordData = {
        issuerEin: data.issuerEin,
        issuerName: data.issuerName,
        issuerAddressLine1: data.issuerAddressLine1,
        issuerAddressLine2: data.issuerAddressLine2,
        issuerCity: data.issuerCity,
        issuerState: data.issuerState,
        issuerZipCode: data.issuerZipCode,
        issuerPhone: data.issuerPhone,
        recipientTin: data.recipientTin,
        recipientTinType: data.recipientTinType,
        recipientFirstName: data.recipientFirstName,
        recipientLastName: data.recipientLastName,
        recipientBusinessName: data.recipientBusinessName,
        recipientAddressLine1: data.recipientAddressLine1,
        recipientAddressLine2: data.recipientAddressLine2,
        recipientCity: data.recipientCity,
        recipientState: data.recipientState,
        recipientZipCode: data.recipientZipCode,
        recipientAccountNumber: data.recipientAccountNumber,
        formDataJson,
      };

      const res = await submissionService.addRecord(parseInt(submissionId), payload);
      setRecords([...records, res.data]);
      setShowForm(false);
      reset();
      toast.success('Record added');
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to add record';
      toast.error(message);
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteRecord = async (recordId: number) => {
    if (!submissionId || !confirm('Delete this record?')) return;
    try {
      await submissionService.deleteRecord(parseInt(submissionId), recordId);
      setRecords(records.filter(r => r.id !== recordId));
      toast.success('Record deleted');
    } catch {
      toast.error('Failed to delete record');
    }
  };

  const parseFormData = (json: string) => {
    try { return JSON.parse(json); } catch { return {}; }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 size={32} className="animate-spin text-primary-600" />
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <button onClick={() => navigate('/dashboard')} className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 mb-4">
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">1099-NEC</h1>
            <p className="text-gray-600 mt-1">
              Nonemployee Compensation &middot; Tax Year {submission?.taxYear} &middot;
              <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {submission?.status}
              </span>
            </p>
          </div>
          <div className="text-right text-sm text-gray-500">
            <p>{records.length} record{records.length !== 1 ? 's' : ''}</p>
          </div>
        </div>
      </div>

      {/* Records List */}
      {records.length > 0 && (
        <div className="mb-8 space-y-4">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Users size={20} /> Recipients
          </h2>
          {records.map((record) => {
            const fd = parseFormData(record.formDataJson);
            return (
              <div key={record.id} className="card flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <p className="font-semibold">
                      {record.recipientFirstName && record.recipientLastName
                        ? `${record.recipientFirstName} ${record.recipientLastName}`
                        : record.recipientBusinessName || 'Unnamed'}
                    </p>
                    <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">
                      {record.recipientTinType}: {record.recipientTinMasked}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    {record.recipientAddressLine1}, {record.recipientCity}, {record.recipientState} {record.recipientZipCode}
                  </p>
                  <p className="text-sm font-medium text-primary-600 mt-1">
                    Box 1: ${fd.box1NonemployeeCompensation || '0.00'}
                    {fd.box4FederalTaxWithheld && fd.box4FederalTaxWithheld !== '0' &&
                      ` | Box 4: $${fd.box4FederalTaxWithheld}`}
                  </p>
                </div>
                <button onClick={() => handleDeleteRecord(record.id)}
                  className="p-2 text-gray-400 hover:text-red-500 transition-colors">
                  <Trash2 size={18} />
                </button>
              </div>
            );
          })}
        </div>
      )}

      {/* Add Record Button */}
      {!showForm && (
        <button onClick={() => setShowForm(true)}
          className="w-full card border-2 border-dashed border-gray-300 hover:border-primary-400 hover:bg-primary-50 transition-colors flex items-center justify-center gap-2 py-8 text-gray-500 hover:text-primary-600">
          <Plus size={20} />
          <span className="font-semibold">Add Recipient</span>
        </button>
      )}

      {/* Record Form */}
      {showForm && (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Payer/Issuer Section */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4">Payer Information</h2>
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Payer's EIN *</label>
                  <input {...register('issuerEin')} className="input-field" placeholder="XX-XXXXXXX" maxLength={10} />
                  {errors.issuerEin && <p className="text-red-500 text-sm mt-1">{errors.issuerEin.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Payer's Name *</label>
                  <input {...register('issuerName')} className="input-field" placeholder="Company Name" />
                  {errors.issuerName && <p className="text-red-500 text-sm mt-1">{errors.issuerName.message}</p>}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Street Address *</label>
                <input {...register('issuerAddressLine1')} className="input-field" placeholder="123 Main St" />
                {errors.issuerAddressLine1 && <p className="text-red-500 text-sm mt-1">{errors.issuerAddressLine1.message}</p>}
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">City *</label>
                  <input {...register('issuerCity')} className="input-field" />
                  {errors.issuerCity && <p className="text-red-500 text-sm mt-1">{errors.issuerCity.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State *</label>
                  <select {...register('issuerState')} className="input-field">
                    <option value="">--</option>
                    {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">ZIP *</label>
                  <input {...register('issuerZipCode')} className="input-field" placeholder="10001" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input {...register('issuerPhone')} className="input-field" placeholder="(555) 123-4567" />
              </div>
            </div>
          </div>

          {/* Recipient Section */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4">Recipient Information</h2>
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">TIN Type *</label>
                  <select {...register('recipientTinType')} className="input-field">
                    <option value="SSN">SSN</option>
                    <option value="EIN">EIN</option>
                    <option value="ITIN">ITIN</option>
                    <option value="ATIN">ATIN</option>
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Recipient's {tinType} *
                  </label>
                  <input {...register('recipientTin')} className="input-field"
                    placeholder={tinType === 'EIN' ? 'XX-XXXXXXX' : 'XXX-XX-XXXX'} />
                  {errors.recipientTin && <p className="text-red-500 text-sm mt-1">{errors.recipientTin.message}</p>}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                  <input {...register('recipientFirstName')} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                  <input {...register('recipientLastName')} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Business Name</label>
                  <input {...register('recipientBusinessName')} className="input-field" />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Street Address *</label>
                <input {...register('recipientAddressLine1')} className="input-field" />
                {errors.recipientAddressLine1 && <p className="text-red-500 text-sm mt-1">{errors.recipientAddressLine1.message}</p>}
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">City *</label>
                  <input {...register('recipientCity')} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State *</label>
                  <select {...register('recipientState')} className="input-field">
                    <option value="">--</option>
                    {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">ZIP *</label>
                  <input {...register('recipientZipCode')} className="input-field" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Account Number</label>
                <input {...register('recipientAccountNumber')} className="input-field" placeholder="Optional" />
              </div>
            </div>
          </div>

          {/* 1099-NEC Boxes */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4">1099-NEC Amounts</h2>
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Box 1: Nonemployee Compensation *
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-3 text-gray-500">$</span>
                    <input {...register('box1')} className="input-field pl-7" placeholder="0.00" />
                  </div>
                  {errors.box1 && <p className="text-red-500 text-sm mt-1">{errors.box1.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Box 4: Federal Income Tax Withheld
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-3 text-gray-500">$</span>
                    <input {...register('box4')} className="input-field pl-7" placeholder="0.00" />
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <input type="checkbox" {...register('box2')} className="rounded border-gray-300 text-primary-600" />
                <label className="text-sm text-gray-700">
                  Box 2: Payer made direct sales totaling $5,000 or more
                </label>
              </div>

              <div className="border-t pt-4 mt-4">
                <p className="text-sm font-medium text-gray-700 mb-3">State Tax Information (optional)</p>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div>
                    <label className="block text-sm text-gray-600 mb-1">State</label>
                    <select {...register('stateCode')} className="input-field">
                      <option value="">--</option>
                      {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 mb-1">Box 5: State Tax Withheld</label>
                    <div className="relative">
                      <span className="absolute left-3 top-3 text-gray-500">$</span>
                      <input {...register('box5')} className="input-field pl-7" placeholder="0.00" />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 mb-1">Box 6: State/Payer's No.</label>
                    <input {...register('box6')} className="input-field" />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 mb-1">Box 7: State Income</label>
                    <div className="relative">
                      <span className="absolute left-3 top-3 text-gray-500">$</span>
                      <input {...register('box7')} className="input-field pl-7" placeholder="0.00" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-4">
            <button type="button" onClick={() => { setShowForm(false); reset(); }} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={saving} className="btn-primary flex items-center gap-2">
              {saving && <Loader2 size={18} className="animate-spin" />}
              Add Record
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
