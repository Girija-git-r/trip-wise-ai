import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { planTrip } from '../services/tripService';
import '../styles/PlanTrip.css';

const TRAVEL_TYPES = ['Leisure', 'Adventure', 'Business', 'Family', 'Solo', 'Romantic'];
const INTERESTS = [
  'Adventure', 'Culture', 'Food', 'Nature', 'Relaxation', 'Shopping', 'Nightlife', 'History',
];

export default function PlanTrip() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    destination: '',
    days: 5,
    budget: 50000,
    travelType: 'Leisure',
    interests: [],
    startDate: '',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function toggleInterest(interest) {
    const key = interest.toLowerCase();
    setForm((prev) => ({
      ...prev,
      interests: prev.interests.includes(key)
        ? prev.interests.filter((i) => i !== key)
        : [...prev.interests, key],
    }));
  }

  function validate() {
    const next = {};
    if (!form.destination.trim()) next.destination = 'Destination is required';
    if (!form.days || form.days < 1) next.days = 'Enter at least 1 day';
    if (!form.budget || form.budget <= 0) next.budget = 'Enter a valid budget';
    if (form.interests.length === 0) next.interests = 'Select at least one interest';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      const trip = await planTrip({
        destination: form.destination.trim(),
        days: Number(form.days),
        budget: Number(form.budget),
        travelType: form.travelType,
        interests: form.interests,
        startDate: form.startDate || null,
      });
      navigate(`/trips/${trip.id}`);
    } catch (err) {
      setServerError(err.response?.data?.message || 'Could not generate your itinerary. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container plan-trip-page">
      <div className="plan-trip-header fade-in">
        <h1>✨ Plan Your Trip</h1>
        <p>Tell us your preferences and we&apos;ll build a smart, day-wise itinerary and packing list.</p>
      </div>

      <div className="card plan-trip-card fade-in">
        {serverError && <div className="alert alert-error">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="destination">Destination</label>
            <input
              id="destination"
              type="text"
              className={`form-input ${errors.destination ? 'has-error' : ''}`}
              value={form.destination}
              onChange={(e) => setForm({ ...form, destination: e.target.value })}
              placeholder="e.g. Kyoto, Japan"
            />
            {errors.destination && <div className="form-error">{errors.destination}</div>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="days">Number of Days</label>
              <input
                id="days"
                type="number"
                min="1"
                max="30"
                className={`form-input ${errors.days ? 'has-error' : ''}`}
                value={form.days}
                onChange={(e) => setForm({ ...form, days: e.target.value })}
              />
              {errors.days && <div className="form-error">{errors.days}</div>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="budget">Budget (₹)</label>
              <input
                id="budget"
                type="number"
                min="1"
                className={`form-input ${errors.budget ? 'has-error' : ''}`}
                value={form.budget}
                onChange={(e) => setForm({ ...form, budget: e.target.value })}
                placeholder="e.g. 50000"
              />
              {errors.budget && <div className="form-error">{errors.budget}</div>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="travelType">Travel Type</label>
              <select
                id="travelType"
                className="form-select"
                value={form.travelType}
                onChange={(e) => setForm({ ...form, travelType: e.target.value })}
              >
                {TRAVEL_TYPES.map((type) => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="startDate">Start Date (optional)</label>
              <input
                id="startDate"
                type="date"
                className="form-input"
                value={form.startDate}
                onChange={(e) => setForm({ ...form, startDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Interests</label>
            <div className="chip-group">
              {INTERESTS.map((interest) => (
                <button
                  type="button"
                  key={interest}
                  className={`chip ${form.interests.includes(interest.toLowerCase()) ? 'selected' : ''}`}
                  onClick={() => toggleInterest(interest)}
                >
                  {interest}
                </button>
              ))}
            </div>
            {errors.interests && <div className="form-error">{errors.interests}</div>}
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
            {submitting ? (
              <><span className="spinner" /> Generating your itinerary…</>
            ) : (
              '✨ Generate My Itinerary'
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
