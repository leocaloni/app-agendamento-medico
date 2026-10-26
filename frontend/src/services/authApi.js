const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

async function postAuthRequest(path, payload) {
  let response

  try {
    response = await fetch(`${API_BASE_URL}/api/auth/${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }

  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const validationDetails = body?.fields ? Object.values(body.fields).join(' ') : ''
    throw new Error([body?.message, validationDetails].filter(Boolean).join(' ') || 'Falha na autenticação.')
  }

  return body
}

export function login(credentials) {
  return postAuthRequest('login', credentials)
}

export function register(account) {
  return postAuthRequest('register', account)
}