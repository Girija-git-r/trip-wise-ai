import api from './api';

// Registration, login, and profile edits go straight through Supabase
// (see AuthContext) — this just reads the backend's synced profile copy,
// which includes fields Supabase doesn't track itself, like createdAt.
export async function getCurrentUser() {
  const { data } = await api.get('/auth/me');
  return data;
}
