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