import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const token = searchParams.get('token');

  useEffect(() => {
    if (token) {
      authService.verifyEmail(token)
        .then(() => setStatus('success'))
        .catch(() => setStatus('error'));
    } else {
      setStatus('error');
    }
  }, [token]);

  return (
    <div className="text-center">
      {status === 'loading' && (
        <>
          <Loader2 size={48} className="animate-spin text-primary-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold">Verifying your email...</h2>
        </>
      )}
      {status === 'success' && (
        <>
          <CheckCircle size={48} className="text-green-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold">Email Verified!</h2>
          <p className="text-gray-600 mt-2">Your email has been verified. You can now access all features.</p>
          <Link to="/dashboard" className="btn-primary inline-block mt-6">Go to Dashboard</Link>
        </>
      )}
      {status === 'error' && (
        <>
          <XCircle size={48} className="text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold">Verification Failed</h2>
          <p className="text-gray-600 mt-2">The verification link is invalid or has expired.</p>
          <Link to="/login" className="btn-primary inline-block mt-6">Back to Login</Link>
        </>
      )}
    </div>
  );
}
