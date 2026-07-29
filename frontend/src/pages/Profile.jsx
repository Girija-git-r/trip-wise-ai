import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getCurrentUser } from '../services/authService';
import '../styles/Profile.css';

export default function Profile() {
  const { user, updateName } = useAuth();
  const { showToast } = useToast();

  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [createdAt, setCreatedAt] = useState(null);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getCurrentUser()
      .then((data) => {
        setName(data.name);
        setEmail(data.email);
        setCreatedAt(data.createdAt);
      })
      .finally(() => setLoading(false));
  }, []);

  function validate() {
    const next = {};
    if (!name.trim()) next.name = 'Name is required';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      await updateName(name);
      showToast('Profile updated successfully', 'success');
    } catch (err) {
      setServerError(err.message || 'Could not update your profile');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="empty-state"><div className="spinner spinner-dark" /></div>;
  }

  const initials = name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((n) => n[0].toUpperCase())
    .join('');

  return (
    <div className="container profile-page">
      <h1>Profile</h1>

      <div className="card profile-card fade-in">
        <div className="profile-avatar">{initials || '🙂'}</div>

        {serverError && <div className="alert alert-error">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="name">Full name</label>
            <input
              id="name"
              type="text"
              className={`form-input ${errors.name ? 'has-error' : ''}`}
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            {errors.name && <div className="form-error">{errors.name}</div>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email</label>
            <input id="email" type="email" className="form-input" value={email} disabled />
            <div className="form-hint">Managed by your Supabase account and can't be changed here.</div>
          </div>

          {createdAt && (
            <p className="form-hint">Member since {new Date(createdAt).toLocaleDateString()}</p>
          )}

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="spinner" /> : 'Save Changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
