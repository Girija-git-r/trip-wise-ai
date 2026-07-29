import api from './api';

export async function planTrip(payload) {
  const { data } = await api.post('/trips/plan', payload);
  return data;
}

export async function getMyTrips(savedOnly = false) {
  const { data } = await api.get('/trips', { params: { saved: savedOnly } });
  return data;
}

export async function getTripById(tripId) {
  const { data } = await api.get(`/trips/${tripId}`);
  return data;
}

export async function updateTripSaved(tripId, saved) {
  const { data } = await api.patch(`/trips/${tripId}/saved`, { saved });
  return data;
}

export async function deleteTrip(tripId) {
  await api.delete(`/trips/${tripId}`);
}

export async function togglePackingItem(tripId, itemId, checked) {
  const { data } = await api.patch(`/trips/${tripId}/packing-items/${itemId}`, { checked });
  return data;
}
