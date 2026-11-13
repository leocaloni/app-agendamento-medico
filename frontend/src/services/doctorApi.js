const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

async function request(path, { method = 'GET', body, signal } = {}) {
  const token = localStorage.getItem('authToken')
  const headers = {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(body ? { 'Content-Type': 'application/json' } : {}),
  }
  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, { method, headers, body, signal })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const responseBody = await response.json().catch(() => null)
  if (!response.ok) {
    const validationDetails = responseBody?.fields ? Object.values(responseBody.fields).join(' ') : ''
    throw new Error([responseBody?.message, validationDetails].filter(Boolean).join(' ') || 'Não foi possível concluir a solicitação.')
  }
  return responseBody
}

function get(path, signal) {
  return request(path, { signal })
}

export function getDoctorSearchOptions(signal) {
  return Promise.all([
    get('/api/specialties', signal),
    get('/api/health-plans', signal),
  ]).then(([specialties, healthPlans]) => ({ specialties, healthPlans }))
}

export function searchDoctors(filters, page = 0, signal) {
  const params = new URLSearchParams({
    page: String(page),
    size: '8',
    sort: filters.sort === 'name' ? 'name,asc' : 'rating,desc',
    allPlans: filters.healthPlanId ? 'false' : String(filters.allPlans),
  })

  if (filters.name.trim()) params.set('name', filters.name.trim())
  if (filters.specialtyId) params.set('specialtyId', filters.specialtyId)
  if (filters.healthPlanId) params.set('healthPlanId', filters.healthPlanId)
  if (filters.minRating) params.set('minRating', filters.minRating)

  return get(`/api/doctors?${params.toString()}`, signal)
}

export function getDoctor(doctorId, signal) {
  return get(`/api/doctors/${doctorId}`, signal)
}

export function getDoctorAvailability(doctorId, from, to, type = 'PRIMEIRA_CONSULTA', signal) {
  const params = new URLSearchParams({ from, to, type })
  return get(`/api/doctors/${doctorId}/availability?${params.toString()}`, signal)
}

export function createAppointment(appointment) {
  return request('/api/appointments', {
    method: 'POST',
    body: JSON.stringify(appointment),
  })
}

export function getMyDoctorProfile(signal) {
  return get('/api/doctors/me', signal)
}

export function updateDoctorProfile(profile) {
  return request('/api/doctors/me', {
    method: 'PUT',
    body: JSON.stringify(profile),
  })
}