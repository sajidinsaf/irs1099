import { Link } from 'react-router-dom';
import { Shield, Mail, Phone } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-navy-900 text-gray-400">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-white font-bold text-lg mb-4">
              IRS 1099 <span className="text-primary-400">Filing</span>
            </h3>
            <p className="text-sm">
              Secure, AI-powered electronic filing platform for IRS 1099 information returns via IRIS A2A.
            </p>
            <div className="flex items-center gap-2 mt-4 text-sm">
              <Shield size={16} className="text-green-400" />
              <span>IRS Pub 4557 Compliant</span>
            </div>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Product</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#features" className="hover:text-white transition-colors">Features</a></li>
              <li><a href="#pricing" className="hover:text-white transition-colors">Pricing</a></li>
              <li><Link to="/register" className="hover:text-white transition-colors">Get Started</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Support</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#faq" className="hover:text-white transition-colors">FAQ</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Documentation</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Contact Us</a></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Contact</h4>
            <ul className="space-y-2 text-sm">
              <li className="flex items-center gap-2"><Mail size={14} /> support@irs1099filing.com</li>
              <li className="flex items-center gap-2"><Phone size={14} /> 1-800-XXX-XXXX</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-sm">
          <p>&copy; {new Date().getFullYear()} IRS 1099 Filing Platform. All rights reserved.</p>
          <p className="mt-1 text-xs">Not affiliated with the Internal Revenue Service.</p>
        </div>
      </div>
    </footer>
  );
}
