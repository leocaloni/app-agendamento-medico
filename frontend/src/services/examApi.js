const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

function getToken() {
  const token = localStorage.getItem('authToken')
  if (!token) throw new Error('Sua sessão expirou. Entre novamente.')
  return token
}

async function parseError(response, fallback) {
  const data = await response.json().catch(() => null)
  const validationDetails = data?.fields ? Object.values(data.fields).join(' ') : ''
  return new Error([data?.message, validationDetails].filter(Boolean).join(' ') || fallback)
}

export async function getMyExams(signal) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/exams/me`, {
      headers: { Authorization: `Bearer ${getToken()}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível carregar seus exames.')
  return response.json()
}

export async function getMyAppointments(signal) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/appointments/me`, {
      headers: { Authorization: `Bearer ${getToken()}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível carregar suas consultas.')
  return response.json()
}

export async function uploadExam({ title, examDate, laboratory, file }) {
  const body = new FormData()
  body.append('title', title)
  body.append('examDate', examDate)
  if (laboratory.trim()) body.append('laboratory', laboratory.trim())
  body.append('file', file)

  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/exams`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${getToken()}` },
      body,
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível enviar o exame.')
  return response.json()
}

export async function downloadExam(exam) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/exams/${exam.id}/download`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível baixar o exame.')

  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = exam.fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

export async function getAppointmentExams(appointmentId, signal) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/appointments/${appointmentId}/exams`, {
      headers: { Authorization: `Bearer ${getToken()}` },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível carregar os exames compartilhados.')
  return response.json()
}

export async function deleteExam(examId) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/exams/${examId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${getToken()}` },
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível excluir o exame.')
}

async function updateShare(examId, appointmentId, method) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/exams/${examId}/shares/${appointmentId}`, {
      method,
      headers: { Authorization: `Bearer ${getToken()}` },
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Confira se o backend está iniciado.')
  }
  if (!response.ok) throw await parseError(response, 'Não foi possível atualizar o compartilhamento.')
  return response.status === 204 ? null : response.json()
}

export function shareExam(examId, appointmentId) {
  return updateShare(examId, appointmentId, 'PUT')
}

export function unshareExam(examId, appointmentId) {
  return updateShare(examId, appointmentId, 'DELETE')
}
