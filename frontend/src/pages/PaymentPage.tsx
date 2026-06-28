import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { paymentService, SubscriptionInfo } from '../services/paymentService';
import toast from 'react-hot-toast';
import { ArrowLeft, CheckCircle, CreditCard, Loader2, Crown, FileText, Zap } from 'lucide-react';

export default function PaymentPage() {
  const [subscription, setSubscription] = useState<SubscriptionInfo | null>(null);
  const [formCount, setFormCount] = useState(1);
  const [loadingPlan, setLoadingPlan] = useState<string | null>(null);
  const [checking, setChecking] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    paymentService.getSubscription()
      .then((res) => setSubscription(res.data))
      .catch(() => {})
      .finally(() => setChecking(false));
  }, []);

  const handleCheckout = async (type: 'per-form' | 'subscription') => {
    setLoadingPlan(type);
    try {
      let res;
      if (type === 'per-form') {
        res = await paymentService.createPerFormCheckout(formCount);
      } else {
        res = await paymentService.createSubscriptionCheckout();
      }
      // Redirect to Stripe Checkout
      window.location.href = res.data.url;
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to start checkout';
      toast.error(message);
    } finally {
      setLoadingPlan(null);
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
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <button onClick={() => navigate('/dashboard')} className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 mb-4">
        <ArrowLeft size={16} /> Back to Dashboard
      </button>

      <h1 className="text-3xl font-bold text-gray-900 mb-2">Billing & Payments</h1>
      <p className="text-gray-600 mb-8">Choose a plan or pay per form to file your 1099s.</p>

      {/* Active Subscription Banner */}
      {subscription?.active && (
        <div className="mb-8 p-5 bg-green-50 border border-green-200 rounded-xl">
          <div className="flex items-center gap-3">
            <Crown className="text-green-600" size={24} />
            <div>
              <h3 className="font-semibold text-green-900">{subscription.planType} Plan Active</h3>
              <p className="text-sm text-green-700">
                {subscription.formsUsed} / {subscription.formsIncluded} forms used
                &middot; Renews {subscription.endDate}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Pricing Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Pay Per Form */}
        <div className="card border-2 border-gray-200">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-blue-100 rounded-lg">
              <FileText className="text-primary-600" size={20} />
            </div>
            <h2 className="text-xl font-bold">Pay Per Form</h2>
          </div>

          <div className="mb-4">
            <span className="text-4xl font-extrabold">$2.99</span>
            <span className="text-gray-500"> / form</span>
          </div>

          <ul className="space-y-2 mb-6 text-sm">
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Any 1099 form type</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> AI validation included</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Email notifications</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Status tracking</li>
          </ul>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">Number of forms</label>
            <div className="flex items-center gap-3">
              <input
                type="number"
                min={1}
                max={100}
                value={formCount}
                onChange={(e) => setFormCount(Math.max(1, parseInt(e.target.value) || 1))}
                className="input-field w-24"
              />
              <span className="text-gray-600 font-semibold">
                = ${(formCount * 2.99).toFixed(2)}
              </span>
            </div>
          </div>

          <button
            onClick={() => handleCheckout('per-form')}
            disabled={loadingPlan !== null}
            className="btn-secondary w-full flex items-center justify-center gap-2"
          >
            {loadingPlan === 'per-form' && <Loader2 size={18} className="animate-spin" />}
            <CreditCard size={18} />
            Pay ${(formCount * 2.99).toFixed(2)}
          </button>
        </div>

        {/* Professional */}
        <div className="card border-2 border-primary-500 relative">
          <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary-600 text-white text-xs font-bold px-3 py-1 rounded-full">
            Best Value
          </div>

          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-primary-100 rounded-lg">
              <Zap className="text-primary-600" size={20} />
            </div>
            <h2 className="text-xl font-bold">Professional</h2>
          </div>

          <div className="mb-4">
            <span className="text-4xl font-extrabold">$149</span>
            <span className="text-gray-500"> / year</span>
          </div>

          <ul className="space-y-2 mb-6 text-sm">
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Up to 500 forms</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Bulk CSV/Excel upload</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Priority support</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> AI assistant</li>
            <li className="flex items-center gap-2"><CheckCircle size={16} className="text-green-500" /> Corrections & replacements</li>
          </ul>

          <button
            onClick={() => handleCheckout('subscription')}
            disabled={loadingPlan !== null || (subscription?.active ?? false)}
            className="btn-primary w-full flex items-center justify-center gap-2"
          >
            {loadingPlan === 'subscription' && <Loader2 size={18} className="animate-spin" />}
            {subscription?.active ? 'Already Subscribed' : 'Subscribe Now'}
          </button>

          <p className="text-xs text-center text-gray-500 mt-2">
            $0.30/form &middot; Save 90% vs pay-per-form
          </p>
        </div>
      </div>
    </div>
  );
}
