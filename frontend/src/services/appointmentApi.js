const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

export async function getMyAppointments(from, to, signal) {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Sua sessão expirou. Entre novamente.')

  const params = new URLSearchParams({ from, to })
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/appointments/me?${params.toString()}`, {
      headers: { Authorization: `Bearer ${token}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(data?.message || 'Não foi possível carregar a agenda médica.')
  }
  return data
}

export async function getAllMyAppointments(signal) {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Sua sessão expirou. Entre novamente.')

  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/appointments/me`, {
      headers: { Authorization: `Bearer ${token}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const data = await response.json().catch(() => null)
  if (!response.ok) throw new Error(data?.message || 'Não foi possível carregar suas consultas.')
  return data
}

export async function getMyReviews(signal) {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Sua sessão expirou. Entre novamente.')

  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/reviews/me`, {
      headers: { Authorization: `Bearer ${token}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const data = await response.json().catch(() => null)
  if (!response.ok) throw new Error(data?.message || 'Não foi possível carregar suas avaliações.')
  return data
}

export async function createAppointmentReview(appointmentId, review) {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Sua sessão expirou. Entre novamente.')

  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/appointments/${appointmentId}/review`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(review),
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const data = await response.json().catch(() => null)
  if (!response.ok) {
    const validationDetails = data?.fields ? Object.values(data.fields).join(' ') : ''
    throw new Error([data?.message, validationDetails].filter(Boolean).join(' ') || 'Não foi possível enviar a avaliação.')
  }
  return data
}