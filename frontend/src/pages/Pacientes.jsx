import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, LogOut, Search, Users } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import { getAllMyAppointments } from '../services/appointmentApi.js'
import './pacientes.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'

function formatDate(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    dateStyle: 'medium',
  }).format(new Date(instant))
}

function initials(fullName) {
  return fullName?.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'P'
}

export default function Pacientes({ user, onLogout, onBack }) {
  const [appointments, setAppointments] = useState([])
  const [query, setQuery] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadAppointments() {
      setIsLoading(true)
      setError('')
      try {
        const data = await getAllMyAppointments(controller.signal)
        setAppointments(data)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }

    loadAppointments()
    return () => controller.abort()
  }, [])

  const patients = useMemo(() => {
    const grouped = new Map()
    for (const appointment of appointments) {
      const existing = grouped.get(appointment.patientId)
      if (existing) {
        existing.total += 1
        if (new Date(appointment.startAt) > new Date(existing.lastAt)) {
          existing.lastAt = appointment.startAt
          existing.lastSpecialty = appointment.specialtyName
        }
      } else {
        grouped.set(appointment.patientId, {
          id: appointment.patientId,
          name: appointment.patientName,
          total: 1,
          lastAt: appointment.startAt,
          lastSpecialty: appointment.specialtyName,
        })
      }
    }
    return [...grouped.values()].sort((first, second) => new Date(second.lastAt) - new Date(first.lastAt))
  }, [appointments])

  const visiblePatients = useMemo(() => {
    const term = query.trim().toLowerCase()
    if (!term) return patients
    return patients.filter((patient) => patient.name.toLowerCase().includes(term))
  }, [patients, query])

  return (
    <main className="doctor-patients-page">
      <header className="doctor-patients-header">
        <TervisBrand />
        <div className="doctor-patients-account">
          <span>{user?.firstName || user?.fullName || 'Médico'}</span>
          <button type="button" className="doctor-patients-logout" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={16} aria-hidden="true" />
          </button>
        </div>
      </header>

      <section className="doctor-patients-content">
        <button className="doctor-patients-back" type="button" onClick={onBack}>
          <ArrowLeft size={16} aria-hidden="true" /> Voltar ao início
        </button>

        <div className="doctor-patients-title-row">
          <div>
            <p>ÁREA MÉDICA</p>
            <h1>Meus pacientes</h1>
          </div>
          <span><Users size={15} aria-hidden="true" /> {patients.length} {patients.length === 1 ? 'paciente' : 'pacientes'}</span>
        </div>

        <label className="doctor-patients-search">
          <Search size={16} aria-hidden="true" />
          <input type="search" value={query} onChange={(event) => setQuery(event.target.value)}
            placeholder="Buscar paciente pelo nome" />
        </label>

        {error && <p className="doctor-patients-error" role="alert">{error}</p>}

        {isLoading ? (
          <p className="doctor-patients-empty">Carregando pacientes...</p>
        ) : visiblePatients.length === 0 ? (
          <p className="doctor-patients-empty">
            {patients.length === 0 ? 'Você ainda não tem pacientes atendidos.' : 'Nenhum paciente encontrado para essa busca.'}
          </p>
        ) : (
          <ul className="doctor-patients-list" aria-label="Lista de pacientes">
            {visiblePatients.map((patient) => (
              <li className="doctor-patient-item" key={patient.id}>
                <span className="doctor-patient-avatar" aria-hidden="true">{initials(patient.name)}</span>
                <div className="doctor-patient-main">
                  <strong>{patient.name}</strong>
                  <span>Última consulta: {formatDate(patient.lastAt)} · {patient.lastSpecialty}</span>
                </div>
                <span className="doctor-patient-count">{patient.total} {patient.total === 1 ? 'consulta' : 'consultas'}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  )
}
