import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import api, { TOKEN_KEY } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(async () => {
    const res = await api.get('/users/me');
    setUser(res.data);
    return res.data;
  }, []);

  // Restore the session after a page refresh.
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setLoading(false);
      return;
    }
    refreshUser()
      .catch(() => {
        localStorage.removeItem(TOKEN_KEY);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, [refreshUser]);

  async function login(usernameOrEmail, password) {
    const res = await api.post('/auth/login', { usernameOrEmail, password });
    localStorage.setItem(TOKEN_KEY, res.data.token);
    await refreshUser(); // load the full profile (id, avatar, bio...), not just the token payload
    return res.data;
  }

  async function register(username, email, password) {
    const res = await api.post('/auth/register', { username, email, password });
    localStorage.setItem(TOKEN_KEY, res.data.token);
    await refreshUser();
    return res.data;
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }

  const value = { user, loading, login, register, logout, refreshUser, isAuthenticated: !!user };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}
