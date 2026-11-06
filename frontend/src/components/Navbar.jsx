import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import api from '../services/api';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const [unread, setUnread] = useState(0);

  // Poll the unread count every 30s; the notifications page also fires a
  // "notifications-changed" event so the badge updates immediately after reading.
  useEffect(() => {
    if (!isAuthenticated) return undefined;
    let cancelled = false;
    async function poll() {
      try {
        const res = await api.get('/notifications/unread-count');
        if (!cancelled) setUnread(res.data.count);
      } catch {
        /* ignore - badge is best-effort */
      }
    }
    poll();
    const id = setInterval(poll, 30000);
    window.addEventListener('notifications-changed', poll);
    return () => {
      cancelled = true;
      clearInterval(id);
      window.removeEventListener('notifications-changed', poll);
    };
  }, [isAuthenticated]);

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="font-semibold">
          DevCollab
        </Link>
        {isAuthenticated ? (
          <nav className="flex items-center gap-4 text-sm">
            <Link to="/" className="text-gray-700 hover:text-gray-900">
              Workspaces
            </Link>
            <Link to="/notifications" className="relative text-gray-700 hover:text-gray-900">
              Notifications
              {unread > 0 && (
                <span className="ml-1 rounded-full bg-red-500 px-1.5 py-0.5 text-xs font-medium text-white">
                  {unread}
                </span>
              )}
            </Link>
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className="text-gray-700 hover:text-gray-900">
                Admin
              </Link>
            )}
            <Link to="/profile" className="text-gray-700 hover:text-gray-900">
              {user?.username}
            </Link>
            <button onClick={logout} className="text-gray-500 underline hover:text-gray-900">
              Log out
            </button>
          </nav>
        ) : (
          <nav className="flex items-center gap-3 text-sm">
            <Link to="/login">Log in</Link>
            <Link to="/register">Register</Link>
          </nav>
        )}
      </div>
    </header>
  );
}
