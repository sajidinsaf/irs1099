import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { submissionService } from '../services/submissionService';
import toast from 'react-hot-toast';
import { ArrowLeft, FileText, Loader2 } from 'lucide-react';

const FORM_TYPES = [
  { value: '1099-NEC', label: '1099-NEC', description: 'Nonemployee Compensation', available: true },
  { value: '1099-MISC', label: '1099-MISC', description: 'Miscellaneous Information', available: false },
  { value: '1099-INT', label: '1099-INT', description: 'Interest Income', available: false },
  { value: '1099-DIV', label: '1099-DIV', description: 'Dividends and Distributions', available: false },
  { value: '1099-K', label: '1099-K', description: 'Payment Card Transactions', available: false },
  { value: '1099-R', label: '1099-R', description: 'Distributions From Pensions/IRAs', available: false },
];

export default function NewFilingPage() {
  const [selectedType, setSelectedType] = useState('');
  const [taxYear, setTaxYear] = useState(2025);
  const [creating, setCreating] = useState(false);
  const navigate = useNavigate();

  const handleCreate = async () => {
    if (!selectedType) {
      toast.error('Please select a form type');
      return;
    }

    setCreating(true);
    try {
      const res = await submissionService.create({
        formType: selectedType,
        taxYear,
      });
      toast.success('Submission created');
      navigate(`/filing/${res.data.id}`);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to create submission';
      toast.error(message);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <button onClick={() => navigate('/dashboard')} className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 mb-4">
        <ArrowLeft size={16} /> Back to Dashboard
      </button>

      <h1 className="text-3xl font-bold text-gray-900 mb-2">New Filing</h1>
      <p className="text-gray-600 mb-8">Select the form type and tax year to start a new submission.</p>

      {/* Tax Year */}
      <div className="card mb-6">
        <h2 className="text-lg font-semibold mb-3">Tax Year</h2>
        <select
          value={taxYear}
          onChange={(e) => setTaxYear(parseInt(e.target.value))}
          className="input-field w-40"
        >
          <option value={2025}>2025</option>
          <option value={2024}>2024</option>
          <option value={2023}>2023</option>
        </select>
      </div>

      {/* Form Type Selection */}
      <div className="card mb-8">
        <h2 className="text-lg font-semibold mb-4">Form Type</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {FORM_TYPES.map((form) => (
            <button
              key={form.value}
              type="button"
              disabled={!form.available}
              onClick={() => setSelectedType(form.value)}
              className={`p-4 rounded-lg border-2 text-left transition-all ${
                selectedType === form.value
                  ? 'border-primary-500 bg-primary-50'
                  : form.available
                    ? 'border-gray-200 hover:border-primary-300 hover:bg-gray-50'
                    : 'border-gray-100 bg-gray-50 opacity-50 cursor-not-allowed'
              }`}
            >
              <div className="flex items-center gap-3">
                <FileText size={20} className={selectedType === form.value ? 'text-primary-600' : 'text-gray-400'} />
                <div>
                  <p className="font-semibold">{form.label}</p>
                  <p className="text-sm text-gray-500">{form.description}</p>
                </div>
              </div>
              {!form.available && (
                <span className="text-xs text-gray-400 mt-1 block">Coming soon</span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Create Button */}
      <div className="flex justify-end">
        <button
          onClick={handleCreate}
          disabled={!selectedType || creating}
          className="btn-primary flex items-center gap-2"
        >
          {creating && <Loader2 size={18} className="animate-spin" />}
          Create Submission
        </button>
      </div>
    </div>
  );
}
