import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyTrips } from '../services/tripService';
import '../styles/Dashboard.css';

export default function Dashboard() {
  const { user } = useAuth();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyTrips(false)
      .then(setTrips)
      .catch(() => setTrips([]))
      .finally(() => setLoading(false));
  }, []);

  const savedCount = trips.filter((t) => t.saved).length;
  const recentTrips = trips.slice(0, 3);

  return (
    <div className="container dashboard-page">
      <div className="dashboard-hero card fade-in">
        <div>
          <h1>Welcome back, {user?.name?.split(' ')[0]} 👋</h1>
          <p>Ready to plan your next adventure? Let TripWise AI build your itinerary in seconds.</p>
        </div>
        <Link to="/plan-trip" className="btn btn-primary">Plan a New Trip</Link>
      </div>

      <div className="dashboard-stats fade-in-stagger">
        <div className="card stat-card">
          <span className="stat-icon">🧳</span>
          <span className="stat-value">{loading ? <span className="skeleton skeleton-line" style={{ width: 40, height: 28, margin: '0 auto' }} /> : trips.length}</span>
          <span className="stat-label">Total Trips Planned</span>
        </div>
        <div className="card stat-card">
          <span className="stat-icon">⭐</span>
          <span className="stat-value">{loading ? <span className="skeleton skeleton-line" style={{ width: 40, height: 28, margin: '0 auto' }} /> : savedCount}</span>
          <span className="stat-label">Saved Trips</span>
        </div>
        <div className="card stat-card">
          <span className="stat-icon">📅</span>
          <span className="stat-value">
            {loading ? <span className="skeleton skeleton-line" style={{ width: 40, height: 28, margin: '0 auto' }} /> : trips.reduce((sum, t) => sum + (t.days || 0), 0)}
          </span>
          <span className="stat-label">Total Days Planned</span>
        </div>
      </div>

      <div className="dashboard-section">
        <div className="dashboard-section-header">
          <h2>Recent Trips</h2>
          <Link to="/my-trips">View all →</Link>
        </div>

        {loading ? (
          <div className="trip-grid">
            {[1, 2, 3].map((i) => <div key={i} className="skeleton skeleton-card" />)}
          </div>
        ) : recentTrips.length === 0 ? (
          <div className="card empty-state fade-in">
            <div className="empty-state-icon">🗺️</div>
            <p>You haven&apos;t planned any trips yet.</p>
            <Link to="/plan-trip" className="btn btn-primary">Plan Your First Trip</Link>
          </div>
        ) : (
          <div className="trip-grid fade-in-stagger">
            {recentTrips.map((trip) => (
              <Link to={`/trips/${trip.id}`} key={trip.id} className="card trip-card">
                <div className="trip-card-header">
                  <h3>{trip.destination}</h3>
                  <div className="trip-card-badges">
                    {trip.aiGenerated && <span className="ai-badge">✨ AI</span>}
                    {trip.saved && <span className="badge badge-secondary">★ Saved</span>}
                  </div>
                </div>
                <div className="trip-card-meta">
                  <span>📆 {trip.days} days</span>
                  <span>💰 ₹{trip.budget}</span>
                  <span>🎒 {trip.travelType}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
