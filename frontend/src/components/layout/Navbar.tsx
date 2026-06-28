import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { LogOut, LayoutDashboard, User, BookOpen } from 'lucide-react';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuthStore();

  return (
    <nav className="bg-navy-900 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="text-xl font-bold">
            IRS 1099 <span className="text-primary-400">Filing</span>
          </Link>

          <div className="hidden md:flex items-center space-x-8">
            <Link to="/" className="hover:text-primary-400 transition-colors">Home</Link>
            <a href="#features" className="hover:text-primary-400 transition-colors">Features</a>
            <a href="#pricing" className="hover:text-primary-400 transition-colors">Pricing</a>
            <a href="#faq" className="hover:text-primary-400 transition-colors">FAQ</a>
            <a href="https://docs.irs1099.visibleai.com" target="_blank" rel="noopener noreferrer" className="flex items-center gap-1 hover:text-primary-400 transition-colors">
              <BookOpen size={16} /> Docs
            </a>
          </div>

          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <Link to="/dashboard" className="flex items-center gap-2 hover:text-primary-400 transition-colors">
                  <LayoutDashboard size={18} />
                  Dashboard
                </Link>
                <div className="flex items-center gap-2 text-sm text-gray-300">
                  <User size={16} />
                  {user?.firstName}
                </div>
                <button onClick={logout} className="flex items-center gap-1 text-gray-400 hover:text-white transition-colors">
                  <LogOut size={16} />
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-primary-400 transition-colors">Sign In</Link>
                <Link to="/register" className="btn-primary text-sm py-2 px-4">Get Started</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
