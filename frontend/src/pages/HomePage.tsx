import { Link } from 'react-router-dom';
import {
  Shield, FileText, Zap, Bot, CreditCard, Bell,
  CheckCircle, ArrowRight, Clock, Users
} from 'lucide-react';
import heroIllustration from '../assets/illustrations/hero-filing.svg';
import dashboardMockup from '../assets/illustrations/dashboard-mockup.svg';

const features = [
  {
    icon: FileText,
    title: 'All 1099 Forms Supported',
    description: 'File 1099-NEC, MISC, INT, DIV, K, R, and 30+ other information return types electronically.',
  },
  {
    icon: Shield,
    title: 'Bank-Level Security',
    description: 'AES-256 encryption for all TIN/SSN data. Fully compliant with IRS Publication 4557.',
  },
  {
    icon: Zap,
    title: 'Direct IRS Submission',
    description: 'Submit directly to the IRS IRIS A2A system. Real-time status tracking and acknowledgments.',
  },
  {
    icon: Bot,
    title: 'AI-Powered Assistance',
    description: 'Smart form filling, pre-submission validation, and plain-English error explanations.',
  },
  {
    icon: CreditCard,
    title: 'Simple Pricing',
    description: 'Pay per form or subscribe to a plan. No hidden fees. Volume discounts available.',
  },
  {
    icon: Bell,
    title: 'Real-Time Notifications',
    description: 'Get notified when submissions are accepted, rejected, or need corrections.',
  },
];

const steps = [
  { step: '1', title: 'Create Account', description: 'Register with your email and set up your business profile.' },
  { step: '2', title: 'Enter Form Data', description: 'Fill in your 1099 data manually or upload a CSV/Excel file.' },
  { step: '3', title: 'Review & Pay', description: 'AI validates your data, you review and complete payment.' },
  { step: '4', title: 'Submit to IRS', description: 'We submit your forms directly to the IRS. Track your filing status in real-time.' },
];

const pricing = [
  {
    name: 'Pay Per Form',
    price: '$2.99',
    unit: '/form',
    features: ['Any 1099 form type', 'AI validation', 'Email notifications', 'Status tracking'],
    cta: 'Get Started',
    popular: false,
  },
  {
    name: 'Professional',
    price: '$149',
    unit: '/year',
    features: ['Up to 500 forms', 'Bulk CSV/Excel upload', 'Priority support', 'AI assistant', 'Correction & replacement filing'],
    cta: 'Start Free Trial',
    popular: true,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    unit: '',
    features: ['Unlimited forms', 'API access', 'Dedicated support', 'Custom integrations', 'CF/SF state filing'],
    cta: 'Contact Sales',
    popular: false,
  },
];

export default function HomePage() {
  return (
    <div>
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-navy-900 via-navy-800 to-primary-900 text-white py-16 lg:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            {/* Left: Text content */}
            <div>
              <div className="inline-flex items-center gap-2 bg-white/10 rounded-full px-4 py-1.5 text-sm mb-6">
                <Clock size={14} />
                <span>Tax Year 2025 filing is open</span>
              </div>
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold leading-tight">
                File Your IRS 1099s
                <span className="block text-primary-400">Electronically with Ease</span>
              </h1>
              <p className="mt-6 text-lg sm:text-xl text-gray-300 max-w-xl">
                The secure, AI-powered platform for filing information returns directly to the IRS IRIS system.
                No complex XML. No manual uploads. Just simple, guided filing.
              </p>
              <div className="mt-10 flex flex-col sm:flex-row gap-4">
                <Link to="/register" className="btn-primary text-lg px-8 py-4 flex items-center justify-center gap-2">
                  Start Filing Now <ArrowRight size={20} />
                </Link>
                <a href="#features" className="border-2 border-white/50 text-white px-8 py-4 rounded-lg font-semibold text-lg hover:bg-white/10 transition-colors duration-200 text-center">
                  Learn More
                </a>
              </div>
              <div className="mt-8 flex flex-wrap gap-6 text-sm text-gray-400">
                <span className="flex items-center gap-1"><CheckCircle size={16} className="text-green-400" /> IRS Authorized</span>
                <span className="flex items-center gap-1"><Shield size={16} className="text-green-400" /> 256-bit Encryption</span>
                <span className="flex items-center gap-1"><Users size={16} className="text-green-400" /> 10,000+ Filers</span>
              </div>
            </div>

            {/* Right: Illustration */}
            <div className="hidden lg:block">
              <img src={heroIllustration} alt="1099 electronic filing illustration" className="w-full max-w-lg mx-auto drop-shadow-2xl" />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Everything You Need to File</h2>
            <p className="mt-4 text-lg text-gray-600">A complete platform for electronic 1099 filing</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature) => (
              <div key={feature.title} className="card hover:shadow-md transition-shadow">
                <feature.icon className="text-primary-600 mb-4" size={32} />
                <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">How It Works</h2>
            <p className="mt-4 text-lg text-gray-600">Four simple steps to file your 1099s</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {steps.map((step) => (
              <div key={step.step} className="text-center">
                <div className="w-14 h-14 bg-primary-600 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                  {step.step}
                </div>
                <h3 className="text-lg font-semibold mb-2">{step.title}</h3>
                <p className="text-gray-600 text-sm">{step.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Dashboard Preview */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900">Your Filing Dashboard</h2>
            <p className="mt-4 text-lg text-gray-600">Track every submission from draft to IRS acceptance</p>
          </div>
          <div className="max-w-5xl mx-auto">
            <div className="rounded-xl shadow-2xl overflow-hidden border border-gray-200">
              <img src={dashboardMockup} alt="IRS 1099 Filing Dashboard" className="w-full" />
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Simple, Transparent Pricing</h2>
            <p className="mt-4 text-lg text-gray-600">Choose the plan that fits your filing needs</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {pricing.map((plan) => (
              <div
                key={plan.name}
                className={`card relative ${plan.popular ? 'border-primary-500 border-2 shadow-lg' : ''}`}
              >
                {plan.popular && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary-600 text-white text-xs font-bold px-3 py-1 rounded-full">
                    Most Popular
                  </div>
                )}
                <h3 className="text-xl font-bold">{plan.name}</h3>
                <div className="mt-4">
                  <span className="text-4xl font-extrabold">{plan.price}</span>
                  <span className="text-gray-500">{plan.unit}</span>
                </div>
                <ul className="mt-6 space-y-3">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-center gap-2 text-sm">
                      <CheckCircle size={16} className="text-green-500 shrink-0" /> {f}
                    </li>
                  ))}
                </ul>
                <Link
                  to="/register"
                  className={`mt-8 block text-center py-3 rounded-lg font-semibold transition-colors ${
                    plan.popular ? 'btn-primary' : 'btn-secondary'
                  }`}
                >
                  {plan.cta}
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="py-20 bg-gray-50">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12">Frequently Asked Questions</h2>
          <div className="space-y-6">
            {[
              { q: 'Who needs to file 1099s electronically?', a: 'If you have 10 or more information returns to file in a calendar year, those returns must be filed electronically per IRS regulations.' },
              { q: 'What is IRIS?', a: 'IRIS (Information Returns Intake System) is the IRS system for electronic filing of information returns. Our platform submits directly to IRIS via the A2A (Application to Application) channel.' },
              { q: 'Is my data secure?', a: 'Yes. All sensitive data (TIN, SSN, EIN) is encrypted with AES-256-GCM. We follow IRS Publication 4557 guidelines for safeguarding taxpayer data.' },
              { q: 'When are 1099s due?', a: 'Most 1099 forms are due to the IRS by March 31 (electronic filing). Forms 1099-NEC are due January 31. Recipient copies are generally due by January 31.' },
              { q: 'Can I file corrections?', a: 'Yes. Our platform supports corrections and replacements for previously filed returns, as required by the IRS IRIS system.' },
            ].map(({ q, a }) => (
              <div key={q} className="card">
                <h3 className="font-semibold text-lg">{q}</h3>
                <p className="mt-2 text-gray-600">{a}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-primary-600 text-white">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold">Ready to File Your 1099s?</h2>
          <p className="mt-4 text-lg text-primary-100">
            Join thousands of businesses filing electronically. Get started in minutes.
          </p>
          <Link to="/register" className="mt-8 inline-flex items-center gap-2 bg-white text-primary-600 px-8 py-4 rounded-lg font-bold text-lg hover:bg-gray-100 transition-colors">
            Create Free Account <ArrowRight size={20} />
          </Link>
        </div>
      </section>
    </div>
  );
}
