import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { loginUser, registerUser } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('tripwise_user');
    const token = localStorage.getItem('tripwise_token');
    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  function persistSession(authResponse) {
    const { token, userId, name, email } = authResponse;
    const sessionUser = { id: userId, name, email };
    localStorage.setItem('tripwise_token', token);
    localStorage.setItem('tripwise_user', JSON.stringify(sessionUser));
    setUser(sessionUser);
    return sessionUser;
  }

  async function login(credentials) {
    const response = await loginUser(credentials);
    return persistSession(response);
  }

  async function register(details) {
    const response = await registerUser(details);
    return persistSession(response);
  }

  function logout() {
    localStorage.removeItem('tripwise_token');
    localStorage.removeItem('tripwise_user');
    setUser(null);
  }

  function updateStoredUser(updatedUser) {
    setUser((prev) => {
      const next = { ...prev, ...updatedUser };
      localStorage.setItem('tripwise_user', JSON.stringify(next));
      return next;
    });
  }

  const value = useMemo(
    () => ({ user, loading, login, register, logout, updateStoredUser, isAuthenticated: !!user }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
