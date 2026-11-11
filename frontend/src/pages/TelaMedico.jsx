import { useEffect, useState } from 'react'
import { CalendarDays, Clock3, LogOut, Stethoscope, Video } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import { getMyAppointments } from '../services/appointmentApi.js'
import './tela-medico.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'

function clinicDateKey(date) {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: CLINIC_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date)
  const values = Object.fromEntries(parts.map(({ type, value }) => [type, value]))
  return `${values.year}-${values.month}-${values.day}`
}

function clinicDayRange() {
  const today = clinicDateKey(new Date())
  const [year, month, day] = today.split('-').map(Number)
  const tomorrow = new Date(Date.UTC(year, month - 1, day + 1))
  const nextDay = [tomorrow.getUTCFullYear(), String(tomorrow.getUTCMonth() + 1).padStart(2, '0'),
    String(tomorrow.getUTCDate()).padStart(2, '0')].join('-')

  return {
    from: `${today}T03:00:00Z`,
    to: `${nextDay}T03:00:00Z`,
  }
}

function formatTime(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(instant))
}

const appointmentTypeLabel = {
  PRIMEIRA_CONSULTA: 'Primeira consulta',
  RETORNO: 'Retorno',
}

const appointmentStatusLabel = {
  SCHEDULED: 'Agendada',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
  NO_SHOW: 'Não compareceu',
}

export default function TelaMedico({ user, onLogout, onNavigate, onOpenHorarios }) {
  const firstName = user?.firstName?.trim() || user?.fullName?.trim().split(/\s+/)[0]
  const [appointments, setAppointments] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    const { from, to } = clinicDayRange()

    async function loadAppointments() {
      setIsLoading(true)
      setError('')
      try {
        const data = await getMyAppointments(from, to, controller.signal)
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

  return (
    <main className="doctor-home-page">
      <header className="doctor-home-header">
        <TervisBrand />
        <div className="doctor-home-account">
          <span>{firstName || 'Médico'}</span>
          <button className="doctor-home-logout" type="button" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={16} aria-hidden="true" />
          </button>
        </div>
      </header>
      <section className="doctor-home-content">
        <header className="doctor-home-title">
          <div>
            <p>ÁREA MÉDICA</p>
            <h1>Bom dia{firstName ? `, ${firstName}` : ''}</h1>
          </div>
          <span className="doctor-home-date"><CalendarDays size={15} />
            {new Intl.DateTimeFormat('pt-BR', { timeZone: CLINIC_TIME_ZONE, dateStyle: 'full' }).format(new Date())}
          </span>
        </header>
        <div className="doctor-home-actions">
          <button className="doctor-open-calendar" type="button" onClick={onNavigate}>
            <CalendarDays size={16} aria-hidden="true" /> Ver agenda mensal
          </button>
          <button className="doctor-open-calendar" type="button" onClick={onOpenHorarios}>
            <Clock3 size={16} aria-hidden="true" /> Meus horários
          </button>
        </div>

        <section className="doctor-home-stats" aria-label="Resumo de hoje">
          <article className="doctor-stat-card stat-appointments">
            <span className="doctor-stat-icon"><CalendarDays size={18} /></span>
            <strong>{isLoading ? '…' : appointments.length}</strong>
            <span>Consultas hoje</span>
          </article>
          <article className="doctor-stat-card stat-online">
            <span className="doctor-stat-icon"><Video size={18} /></span>
            <strong aria-label="Não disponível">—</strong>
            <span>Online</span>
            <small>modalidade não registrada</small>
          </article>
          <article className="doctor-stat-card stat-pending">
            <span className="doctor-stat-icon"><Clock3 size={18} /></span>
            <strong aria-label="Não disponível">—</strong>
            <span>Pendências</span>
            <small>estado não registrado</small>
          </article>
        </section>

        {error && <p className="doctor-home-error" role="alert">{error}</p>}

        <section className="doctor-home-agenda" aria-labelledby="doctor-agenda-title">
          <div className="doctor-agenda-heading">
            <div>
              <p>HOJE</p>
              <h2 id="doctor-agenda-title">Agenda de hoje</h2>
            </div>
            {!isLoading && <span>{appointments.length} consultas</span>}
          </div>

          {isLoading ? (
            <p className="doctor-agenda-empty">Carregando agenda...</p>
          ) : appointments.length ? (
            <div className="doctor-agenda-list">
              {appointments.map((appointment) => (
                <article className="doctor-agenda-row" key={appointment.id}>
                  <time>{formatTime(appointment.startAt)}</time>
                  <div className="doctor-agenda-patient">
                    <strong>{appointment.patientName}</strong>
                    <span>{appointmentTypeLabel[appointment.type] || appointment.type} · {appointment.specialtyName}</span>
                  </div>
                  <div className="doctor-agenda-meta">
                    <span className={`appointment-status status-${appointment.status.toLowerCase()}`}>
                      {appointmentStatusLabel[appointment.status] || appointment.status}
                    </span>
                    <small>{appointment.paymentType === 'CONVENIO'
                      ? appointment.healthPlan?.name || 'Convênio'
                      : 'Particular'}</small>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <div className="doctor-agenda-empty">
              <Stethoscope size={21} aria-hidden="true" />
              <span>Você não tem consultas agendadas para hoje.</span>
            </div>
          )}
        </section>

        <p className="doctor-home-data-note">
          Online e pendências serão exibidos quando essas informações forem registradas pelo sistema.
        </p>
      </section>
    </main>
  )
}