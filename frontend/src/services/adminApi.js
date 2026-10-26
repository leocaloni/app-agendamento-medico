const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

async function request(path, options = {}) {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Faça login como administrador para continuar.')

  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        Authorization: `Bearer ${token}`,
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const details = body?.fields ? Object.values(body.fields).join(' ') : ''
    throw new Error([body?.message, details].filter(Boolean).join(' ') || 'Não foi possível concluir a operação.')
  }
  return { body, location: response.headers.get('Location') }
}

export async function getDoctorFormOptions() {
  const [specialties, healthPlans] = await Promise.all([
    request('/api/specialties'),
    request('/api/health-plans'),
  ])
  return { specialties: specialties.body, healthPlans: healthPlans.body }
}

export async function createDoctor(doctor) {
  const { body, location } = await request('/api/admin/users/doctors', {
    method: 'POST',
    body: JSON.stringify(doctor),
  })
  return { doctor: body, location }
}