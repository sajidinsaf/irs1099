import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { profileService } from '../services/profileService';
import { FileText, Upload, Clock, CheckCircle, AlertCircle, CreditCard, Building2, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const [hasProfile, setHasProfile] = useState<boolean | null>(null);

  useEffect(() => {
    profileService.hasProfile()
      .then((res) => setHasProfile(res.data.exists))
      .catch(() => setHasProfile(false));
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Welcome back, {user?.firstName}
        </h1>
        <p className="text-gray-600 mt-1">Manage your 1099 filings and submissions</p>
      </div>

      {/* Profile completion banner */}
      {hasProfile === false && (
        <Link to="/profile"
          className="mb-8 flex items-center justify-between p-5 bg-amber-50 border border-amber-200 rounded-xl hover:bg-amber-100 transition-colors">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-amber-100 rounded-lg">
              <Building2 className="text-amber-600" size={24} />
            </div>
            <div>
              <h3 className="font-semibold text-amber-900">Complete your business profile</h3>
              <p className="text-sm text-amber-700">Add your business details and EIN to start filing 1099s</p>
            </div>
          </div>
          <ArrowRight className="text-amber-600" size={20} />
        </Link>
      )}

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <button className="card hover:shadow-md transition-shadow text-left flex items-start gap-4">
          <div className="p-3 bg-primary-100 rounded-lg">
            <FileText className="text-primary-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-lg">New Filing</h3>
            <p className="text-sm text-gray-600">Start a new 1099 submission</p>
          </div>
        </button>

        <button className="card hover:shadow-md transition-shadow text-left flex items-start gap-4">
          <div className="p-3 bg-green-100 rounded-lg">
            <Upload className="text-green-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-lg">Bulk Upload</h3>
            <p className="text-sm text-gray-600">Import from CSV or Excel</p>
          </div>
        </button>

        <Link to="/profile" className="card hover:shadow-md transition-shadow text-left flex items-start gap-4">
          <div className="p-3 bg-purple-100 rounded-lg">
            <Building2 className="text-purple-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-lg">Business Profile</h3>
            <p className="text-sm text-gray-600">{hasProfile ? 'Edit your profile' : 'Set up your business'}</p>
          </div>
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
          { label: 'Total Submissions', value: '0', icon: FileText, color: 'text-primary-600' },
          { label: 'Pending', value: '0', icon: Clock, color: 'text-yellow-600' },
          { label: 'Accepted', value: '0', icon: CheckCircle, color: 'text-green-600' },
          { label: 'Needs Attention', value: '0', icon: AlertCircle, color: 'text-red-600' },
        ].map((stat) => (
          <div key={stat.label} className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">{stat.label}</p>
                <p className="text-3xl font-bold mt-1">{stat.value}</p>
              </div>
              <stat.icon className={stat.color} size={28} />
            </div>
          </div>
        ))}
      </div>

      {/* Recent Submissions */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Recent Submissions</h2>
        <div className="text-center py-12 text-gray-500">
          <FileText size={48} className="mx-auto mb-4 text-gray-300" />
          <p className="text-lg font-medium">No submissions yet</p>
          <p className="text-sm mt-1">Start by creating a new 1099 filing</p>
          <button className="btn-primary mt-4">Create Your First Filing</button>
        </div>
      </div>
    </div>
  );
}
