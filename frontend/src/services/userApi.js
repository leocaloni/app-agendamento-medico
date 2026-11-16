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

export function getCurrentUser(signal) {
  return request('/api/auth/me', { signal })
}

export function getHealthPlans(signal) {
  return request('/api/health-plans', { signal })
}

export function updateProfile(profile) {
  return request('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify(profile),
  })
}
