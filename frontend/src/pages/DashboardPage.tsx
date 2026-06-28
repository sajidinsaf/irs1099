import { useAuthStore } from '../store/authStore';
import { FileText, Upload, Clock, CheckCircle, AlertCircle, CreditCard } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Welcome back, {user?.firstName}
        </h1>
        <p className="text-gray-600 mt-1">Manage your 1099 filings and submissions</p>
      </div>

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

        <button className="card hover:shadow-md transition-shadow text-left flex items-start gap-4">
          <div className="p-3 bg-purple-100 rounded-lg">
            <CreditCard className="text-purple-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-lg">Subscription</h3>
            <p className="text-sm text-gray-600">Manage your plan</p>
          </div>
        </button>
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
