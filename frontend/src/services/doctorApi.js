const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

async function get(path, signal) {
  const token = localStorage.getItem('authToken')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, { headers, signal })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(body?.message || 'Não foi possível carregar os dados.')
  }
  return body
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