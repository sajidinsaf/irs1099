import { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { useAuthStore } from '../../store/authStore';
import { Navigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Users, FileText, CreditCard, Shield, Bell, BarChart3,
  ChevronRight, Loader2, CheckCircle, XCircle, Clock,
  AlertTriangle, RefreshCw, Send, UserX, UserCheck, Key
} from 'lucide-react';

type Tab = 'overview' | 'users' | 'submissions' | 'payments' | 'audit' | 'notifications';

export default function AdminDashboard() {
  const user = useAuthStore((s) => s.user);
  const [activeTab, setActiveTab] = useState<Tab>('overview');
  const [stats, setStats] = useState<Record<string, unknown> | null>(null);
  const [users, setUsers] = useState<{ content: unknown[]; totalElements: number }>({ content: [], totalElements: 0 });
  const [submissions, setSubmissions] = useState<{ content: unknown[]; totalElements: number }>({ content: [], totalElements: 0 });
  const [payments, setPayments] = useState<{ content: unknown[]; totalElements: number }>({ content: [], totalElements: 0 });
  const [auditLog, setAuditLog] = useState<{ content: unknown[]; totalElements: number }>({ content: [], totalElements: 0 });
  const [notifications, setNotifications] = useState<{ content: unknown[]; totalElements: number }>({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  // Guard: only ADMIN role
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  useEffect(() => {
    loadData(activeTab);
  }, [activeTab, statusFilter]);

  const loadData = async (tab: Tab) => {
    setLoading(true);
    try {
      switch (tab) {
        case 'overview':
          const statsRes = await adminService.getStats();
          setStats(statsRes.data);
          break;
        case 'users':
          const usersRes = await adminService.getUsers();
          setUsers(usersRes.data);
          break;
        case 'submissions':
          const subsRes = await adminService.getSubmissions(0, 20, statusFilter || undefined);
          setSubmissions(subsRes.data);
          break;
        case 'payments':
          const paysRes = await adminService.getPayments();
          setPayments(paysRes.data);
          break;
        case 'audit':
          const auditRes = await adminService.getAuditLog();
          setAuditLog(auditRes.data);
          break;
        case 'notifications':
          const notifRes = await adminService.getNotifications();
          setNotifications(notifRes.data);
          break;
      }
    } catch {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userId: number, role: string) => {
    if (!confirm(`Change user role to ${role}?`)) return;
    try {
      await adminService.updateUserRole(userId, role);
      toast.success('Role updated');
      loadData('users');
    } catch { toast.error('Failed to update role'); }
  };

  const handleForceVerify = async (userId: number) => {
    try {
      await adminService.forceVerifyEmail(userId);
      toast.success('Email verified');
      loadData('users');
    } catch { toast.error('Failed'); }
  };

  const handleDeactivate = async (userId: number) => {
    if (!confirm('Deactivate this user?')) return;
    try {
      await adminService.deactivateUser(userId);
      toast.success('User deactivated');
      loadData('users');
    } catch { toast.error('Failed'); }
  };

  const handleStatusChange = async (submissionId: number, status: string) => {
    if (!confirm(`Change status to ${status}?`)) return;
    try {
      await adminService.updateSubmissionStatus(submissionId, status);
      toast.success('Status updated');
      loadData('submissions');
    } catch { toast.error('Failed'); }
  };

  const [notifSubject, setNotifSubject] = useState('');
  const [notifBody, setNotifBody] = useState('');

  const handleSendNotification = async () => {
    if (!notifSubject || !notifBody) { toast.error('Subject and body required'); return; }
    if (!confirm('Send notification to ALL users?')) return;
    try {
      await adminService.sendSystemNotification(notifSubject, notifBody);
      toast.success('System notification sent');
      setNotifSubject(''); setNotifBody('');
      loadData('notifications');
    } catch { toast.error('Failed'); }
  };

  const tabs: { id: Tab; label: string; icon: typeof Users }[] = [
    { id: 'overview', label: 'Overview', icon: BarChart3 },
    { id: 'users', label: 'Users', icon: Users },
    { id: 'submissions', label: 'Submissions', icon: FileText },
    { id: 'payments', label: 'Payments', icon: CreditCard },
    { id: 'audit', label: 'Audit Log', icon: Shield },
    { id: 'notifications', label: 'Notifications', icon: Bell },
  ];

  const any = (v: unknown): any => v as any; // eslint-disable-line

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Admin Panel</h1>
        <p className="text-gray-600 mt-1">Platform management and oversight</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-8 overflow-x-auto pb-2">
        {tabs.map((tab) => (
          <button key={tab.id} onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-colors ${
              activeTab === tab.id
                ? 'bg-primary-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}>
            <tab.icon size={16} /> {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 size={32} className="animate-spin text-primary-600" />
        </div>
      ) : (
        <>
          {/* Overview Tab */}
          {activeTab === 'overview' && stats && (
            <div>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <div className="card">
                  <p className="text-sm text-gray-600">Total Users</p>
                  <p className="text-3xl font-bold mt-1">{any(stats).totalUsers}</p>
                </div>
                <div className="card">
                  <p className="text-sm text-gray-600">Total Submissions</p>
                  <p className="text-3xl font-bold mt-1">{any(stats).totalSubmissions}</p>
                </div>
                <div className="card">
                  <p className="text-sm text-gray-600">Total Payments</p>
                  <p className="text-3xl font-bold mt-1">{any(stats).totalPayments}</p>
                </div>
                <div className="card">
                  <p className="text-sm text-gray-600">Active Subscriptions</p>
                  <p className="text-3xl font-bold mt-1">{any(stats).activeSubscriptions}</p>
                </div>
              </div>

              {any(stats).submissionsByStatus && (
                <div className="card">
                  <h2 className="text-lg font-semibold mb-4">Submissions by Status</h2>
                  <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                    {Object.entries(any(stats).submissionsByStatus).map(([status, count]: [string, any]) => (
                      <div key={status} className="text-center p-3 bg-gray-50 rounded-lg">
                        <p className="text-2xl font-bold">{count}</p>
                        <p className="text-xs text-gray-500 mt-1">{status.replace(/_/g, ' ')}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Users Tab */}
          {activeTab === 'users' && (
            <div className="card overflow-x-auto">
              <h2 className="text-lg font-semibold mb-4">Users ({users.totalElements})</h2>
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-gray-500">
                    <th className="pb-3 pr-4">Email</th>
                    <th className="pb-3 pr-4">Name</th>
                    <th className="pb-3 pr-4">Role</th>
                    <th className="pb-3 pr-4">Verified</th>
                    <th className="pb-3 pr-4">Created</th>
                    <th className="pb-3">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {any(users).content.map((u: any) => (
                    <tr key={u.id} className="border-b last:border-0">
                      <td className="py-3 pr-4 font-medium">{u.email}</td>
                      <td className="py-3 pr-4">{u.firstName} {u.lastName}</td>
                      <td className="py-3 pr-4">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          u.role === 'ADMIN' ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-800'
                        }`}>{u.role}</span>
                      </td>
                      <td className="py-3 pr-4">
                        {u.emailVerified
                          ? <CheckCircle size={16} className="text-green-500" />
                          : <XCircle size={16} className="text-red-400" />}
                      </td>
                      <td className="py-3 pr-4 text-gray-500">{new Date(u.createdAt).toLocaleDateString()}</td>
                      <td className="py-3">
                        <div className="flex gap-1">
                          {u.role === 'USER' ? (
                            <button onClick={() => handleRoleChange(u.id, 'ADMIN')}
                              className="p-1.5 text-purple-600 hover:bg-purple-50 rounded" title="Make Admin">
                              <Shield size={14} />
                            </button>
                          ) : (
                            <button onClick={() => handleRoleChange(u.id, 'USER')}
                              className="p-1.5 text-gray-600 hover:bg-gray-50 rounded" title="Remove Admin">
                              <UserX size={14} />
                            </button>
                          )}
                          {!u.emailVerified && (
                            <button onClick={() => handleForceVerify(u.id)}
                              className="p-1.5 text-green-600 hover:bg-green-50 rounded" title="Force Verify">
                              <UserCheck size={14} />
                            </button>
                          )}
                          <button onClick={() => handleDeactivate(u.id)}
                            className="p-1.5 text-red-600 hover:bg-red-50 rounded" title="Deactivate">
                            <UserX size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Submissions Tab */}
          {activeTab === 'submissions' && (
            <div>
              <div className="flex gap-2 mb-4">
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
                  className="input-field w-48">
                  <option value="">All Statuses</option>
                  {['DRAFT','SUBMITTED','PROCESSING','ACCEPTED','REJECTED','ACCEPTED_WITH_ERRORS','CANCELLED'].map(s => (
                    <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
                  ))}
                </select>
                <button onClick={() => loadData('submissions')} className="btn-secondary flex items-center gap-1 py-2 px-3">
                  <RefreshCw size={14} /> Refresh
                </button>
              </div>
              <div className="card overflow-x-auto">
                <h2 className="text-lg font-semibold mb-4">Submissions ({submissions.totalElements})</h2>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left text-gray-500">
                      <th className="pb-3 pr-4">ID</th>
                      <th className="pb-3 pr-4">Form</th>
                      <th className="pb-3 pr-4">Year</th>
                      <th className="pb-3 pr-4">Status</th>
                      <th className="pb-3 pr-4">Receipt ID</th>
                      <th className="pb-3 pr-4">Created</th>
                      <th className="pb-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {any(submissions).content.map((s: any) => (
                      <tr key={s.id} className="border-b last:border-0">
                        <td className="py-3 pr-4 font-mono text-xs">{s.id}</td>
                        <td className="py-3 pr-4 font-medium">{s.formType}</td>
                        <td className="py-3 pr-4">{s.taxYear}</td>
                        <td className="py-3 pr-4">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                            s.status === 'ACCEPTED' ? 'bg-green-100 text-green-800' :
                            s.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                            s.status === 'SUBMITTED' || s.status === 'PROCESSING' ? 'bg-blue-100 text-blue-800' :
                            'bg-gray-100 text-gray-800'
                          }`}>{s.status}</span>
                        </td>
                        <td className="py-3 pr-4 font-mono text-xs text-gray-500">{s.receiptId || '-'}</td>
                        <td className="py-3 pr-4 text-gray-500">{new Date(s.createdAt).toLocaleDateString()}</td>
                        <td className="py-3">
                          <select onChange={(e) => { if (e.target.value) handleStatusChange(s.id, e.target.value); e.target.value = ''; }}
                            className="text-xs border rounded px-1 py-0.5">
                            <option value="">Change...</option>
                            {['ACCEPTED','REJECTED','CANCELLED'].map(st => (
                              <option key={st} value={st}>{st}</option>
                            ))}
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Payments Tab */}
          {activeTab === 'payments' && (
            <div className="card overflow-x-auto">
              <h2 className="text-lg font-semibold mb-4">Payments ({payments.totalElements})</h2>
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-gray-500">
                    <th className="pb-3 pr-4">ID</th>
                    <th className="pb-3 pr-4">Amount</th>
                    <th className="pb-3 pr-4">Status</th>
                    <th className="pb-3 pr-4">Description</th>
                    <th className="pb-3 pr-4">Stripe ID</th>
                    <th className="pb-3">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {any(payments).content.map((p: any) => (
                    <tr key={p.id} className="border-b last:border-0">
                      <td className="py-3 pr-4 font-mono text-xs">{p.id}</td>
                      <td className="py-3 pr-4 font-semibold">${p.amount}</td>
                      <td className="py-3 pr-4">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          p.status === 'SUCCEEDED' ? 'bg-green-100 text-green-800' :
                          p.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>{p.status}</span>
                      </td>
                      <td className="py-3 pr-4 text-gray-600">{p.description || '-'}</td>
                      <td className="py-3 pr-4 font-mono text-xs text-gray-400">{p.stripePaymentIntentId || '-'}</td>
                      <td className="py-3 text-gray-500">{new Date(p.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                  {any(payments).content.length === 0 && (
                    <tr><td colSpan={6} className="py-8 text-center text-gray-400">No payments yet</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {/* Audit Log Tab */}
          {activeTab === 'audit' && (
            <div className="card overflow-x-auto">
              <h2 className="text-lg font-semibold mb-4">Audit Log ({auditLog.totalElements})</h2>
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-gray-500">
                    <th className="pb-3 pr-4">Time</th>
                    <th className="pb-3 pr-4">User ID</th>
                    <th className="pb-3 pr-4">Action</th>
                    <th className="pb-3 pr-4">Entity</th>
                    <th className="pb-3">Details</th>
                  </tr>
                </thead>
                <tbody>
                  {any(auditLog).content.map((log: any) => (
                    <tr key={log.id} className="border-b last:border-0">
                      <td className="py-3 pr-4 text-gray-500 text-xs">{new Date(log.timestamp).toLocaleString()}</td>
                      <td className="py-3 pr-4 font-mono text-xs">{log.userId || '-'}</td>
                      <td className="py-3 pr-4 font-medium">{log.action}</td>
                      <td className="py-3 pr-4 text-gray-600">{log.entityType} {log.entityId ? `#${log.entityId}` : ''}</td>
                      <td className="py-3 text-gray-500 text-xs">{log.details}</td>
                    </tr>
                  ))}
                  {any(auditLog).content.length === 0 && (
                    <tr><td colSpan={5} className="py-8 text-center text-gray-400">No audit log entries</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {/* Notifications Tab */}
          {activeTab === 'notifications' && (
            <div>
              {/* Send System Notification */}
              <div className="card mb-6">
                <h2 className="text-lg font-semibold mb-4">Send System Notification</h2>
                <div className="space-y-3">
                  <input value={notifSubject} onChange={(e) => setNotifSubject(e.target.value)}
                    className="input-field" placeholder="Subject" />
                  <textarea value={notifBody} onChange={(e) => setNotifBody(e.target.value)}
                    className="input-field" rows={3} placeholder="Message body" />
                  <button onClick={handleSendNotification} className="btn-primary flex items-center gap-2">
                    <Send size={16} /> Send to All Users
                  </button>
                </div>
              </div>

              {/* Recent Notifications */}
              <div className="card overflow-x-auto">
                <h2 className="text-lg font-semibold mb-4">Recent Notifications ({notifications.totalElements})</h2>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left text-gray-500">
                      <th className="pb-3 pr-4">Type</th>
                      <th className="pb-3 pr-4">Subject</th>
                      <th className="pb-3 pr-4">Email Sent</th>
                      <th className="pb-3">Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {any(notifications).content.map((n: any) => (
                      <tr key={n.id} className="border-b last:border-0">
                        <td className="py-3 pr-4">
                          <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            {n.type}
                          </span>
                        </td>
                        <td className="py-3 pr-4">{n.subject}</td>
                        <td className="py-3 pr-4">
                          {n.emailSent ? <CheckCircle size={14} className="text-green-500" /> : <XCircle size={14} className="text-gray-300" />}
                        </td>
                        <td className="py-3 text-gray-500">{new Date(n.createdAt).toLocaleString()}</td>
                      </tr>
                    ))}
                    {any(notifications).content.length === 0 && (
                      <tr><td colSpan={4} className="py-8 text-center text-gray-400">No notifications yet</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
