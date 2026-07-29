import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteTrip, getMyTrips, updateTripSaved } from '../services/tripService';
import { useToast } from '../context/ToastContext';
import ConfirmModal from '../components/ConfirmModal';
import '../styles/MyTrips.css';

export default function MyTrips() {
  const { showToast } = useToast();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [pendingDelete, setPendingDelete] = useState(null);

  useEffect(() => {
    loadTrips();
  }, []);

  function loadTrips() {
    setLoading(true);
    getMyTrips(false)
      .then(setTrips)
      .catch(() => setTrips([]))
      .finally(() => setLoading(false));
  }

  async function handleToggleSaved(trip) {
    const updated = await updateTripSaved(trip.id, !trip.saved);
    setTrips((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
    showToast(updated.saved ? 'Trip saved' : 'Trip unsaved', 'success');
  }

  async function handleConfirmDelete() {
    const trip = pendingDelete;
    setPendingDelete(null);
    await deleteTrip(trip.id);
    setTrips((prev) => prev.filter((t) => t.id !== trip.id));
    showToast(`Deleted trip to ${trip.destination}`, 'success');
  }

  const visibleTrips = filter === 'saved' ? trips.filter((t) => t.saved) : trips;

  return (
    <div className="container my-trips-page">
      <div className="my-trips-header fade-in">
        <h1>My Trips</h1>
        <Link to="/plan-trip" className="btn btn-primary">Plan a New Trip</Link>
      </div>

      <div className="my-trips-filters">
        <button className={`chip ${filter === 'all' ? 'selected' : ''}`} onClick={() => setFilter('all')}>
          All Trips
        </button>
        <button className={`chip ${filter === 'saved' ? 'selected' : ''}`} onClick={() => setFilter('saved')}>
          ★ Saved Only
        </button>
      </div>

      {loading ? (
        <div className="trip-grid">
          {[1, 2, 3].map((i) => <div key={i} className="skeleton skeleton-card" />)}
        </div>
      ) : visibleTrips.length === 0 ? (
        <div className="card empty-state fade-in">
          <div className="empty-state-icon">🧭</div>
          <p>No trips to show here yet.</p>
          <Link to="/plan-trip" className="btn btn-primary">Plan Your First Trip</Link>
        </div>
      ) : (
        <div className="trip-grid fade-in-stagger">
          {visibleTrips.map((trip) => (
            <div className="card my-trip-card" key={trip.id}>
              <Link to={`/trips/${trip.id}`} className="my-trip-card-body">
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
              <div className="my-trip-card-actions">
                <button className="btn btn-outline" onClick={() => handleToggleSaved(trip)}>
                  {trip.saved ? 'Unsave' : 'Save'}
                </button>
                <button className="btn btn-danger" onClick={() => setPendingDelete(trip)}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <ConfirmModal
        open={!!pendingDelete}
        title="Delete this trip?"
        message={pendingDelete ? `Your trip to ${pendingDelete.destination} will be permanently removed. This cannot be undone.` : ''}
        confirmLabel="Delete"
        danger
        onConfirm={handleConfirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </div>
  );
}
