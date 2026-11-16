import { useEffect, useMemo, useState } from 'react'
import { CalendarCheck, CalendarDays, ChevronRight, Clock3, FileText, Search, Star, Stethoscope, UserRound } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { getAllMyAppointments, getMyReviews } from '../services/appointmentApi.js'
import './tela-usuario.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'

const shortcuts = [
  { item: 'buscar', label: 'Buscar médico', Icon: Search, tone: 'blue' },
  { item: 'exames', label: 'Meus exames', Icon: FileText, tone: 'mint' },
  { item: 'consultas', label: 'Minhas consultas', Icon: CalendarDays, tone: 'coral' },
  { item: 'avaliacoes', label: 'Avaliações', Icon: Star, tone: 'rose' },
  { item: 'perfil', label: 'Meu perfil', Icon: UserRound, tone: 'blue' },
]

const appointmentTypeLabel = {
  PRIMEIRA_CONSULTA: 'Primeira consulta',
  RETORNO: 'Retorno',
}

function formatDay(instant) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: CLINIC_TIME_ZONE, day: '2-digit', month: 'long' }).format(new Date(instant))
}

function formatTime(instant) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: CLINIC_TIME_ZONE, hour: '2-digit', minute: '2-digit' }).format(new Date(instant))
}

function monthKey(instant) {
  return new Intl.DateTimeFormat('en-CA', { timeZone: CLINIC_TIME_ZONE, year: 'numeric', month: '2-digit' }).format(new Date(instant))
}

function reviewKey(doctorId, startAt) {
  return `${doctorId}|${startAt}`
}

export default function TelaUsuario({ user, onLogout, onNavigate }) {
  const firstName = user?.firstName?.trim() || user?.fullName?.trim().split(/\s+/)[0]
  const [appointments, setAppointments] = useState([])
  const [reviews, setReviews] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const controller = new AbortController()

    async function loadOverview() {
      setIsLoading(true)
      try {
        const [appointmentData, reviewData] = await Promise.all([
          getAllMyAppointments(controller.signal),
          getMyReviews(controller.signal),
        ])
        setAppointments(appointmentData)
        setReviews(reviewData)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') {
          setAppointments([])
          setReviews([])
        }
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }

    loadOverview()
    return () => controller.abort()
  }, [])

  const nextAppointment = useMemo(() => {
    const now = Date.now()
    return appointments
      .filter((appointment) => appointment.status === 'SCHEDULED' && new Date(appointment.startAt).getTime() >= now)
      .sort((first, second) => new Date(first.startAt) - new Date(second.startAt))[0] || null
  }, [appointments])

  const monthlyCount = useMemo(() => {
    const current = monthKey(Date.now())
    return appointments.filter((appointment) => monthKey(appointment.startAt) === current).length
  }, [appointments])

  const pendingReviews = useMemo(() => {
    const reviewed = new Set(reviews.map((review) => reviewKey(review.doctorId, review.appointmentStartAt)))
    return appointments.filter((appointment) => appointment.status === 'COMPLETED'
      && !reviewed.has(reviewKey(appointment.doctorId, appointment.startAt))).length
  }, [appointments, reviews])

  return (
    <div className="patient-app">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="patient-home">
        <header className="patient-home-heading">
          <h1>Como podemos ajudar{firstName ? `, ${firstName}` : ''}?</h1>
        </header>

        <nav className="patient-shortcuts" aria-label="Ações do paciente">
          {shortcuts.map(({ item, label, Icon, tone }) => (
            <button className="patient-shortcut" type="button" key={item} onClick={() => onNavigate?.(item)}>
              <span className={`patient-shortcut-icon tone-${tone}`}>
                <Icon size={21} strokeWidth={1.9} />
              </span>
              <span>{label}</span>
            </button>
          ))}
        </nav>

        <section className="patient-overview" aria-label="Resumo da sua saúde">
          <div className="patient-panel">
            <div className="patient-panel-head">
              <h2>Próxima consulta</h2>
              <button className="patient-panel-link" type="button" onClick={() => onNavigate?.('consultas')}>
                Ver todas <ChevronRight size={15} aria-hidden="true" />
              </button>
            </div>

            {isLoading ? (
              <p className="patient-panel-state">Carregando sua agenda...</p>
            ) : nextAppointment ? (
              <article className="patient-next-featured">
                <div className="patient-next-featured-top">
                  <div>
                    <strong>{nextAppointment.doctorName}</strong>
                    <span>{nextAppointment.specialtyName} · {appointmentTypeLabel[nextAppointment.type] || nextAppointment.type}</span>
                  </div>
                  <span className="patient-next-badge">Agendada</span>
                </div>
                <div className="patient-next-featured-meta">
                  <span><CalendarDays size={15} aria-hidden="true" /> {formatDay(nextAppointment.startAt)}</span>
                  <span><Clock3 size={15} aria-hidden="true" /> {formatTime(nextAppointment.startAt)}</span>
                </div>
              </article>
            ) : (
              <div className="patient-next-empty">
                <span>Você não tem consultas agendadas.</span>
                <button className="patient-next-cta" type="button" onClick={() => onNavigate?.('buscar')}>Buscar médico</button>
              </div>
            )}

            <div className="patient-panel-head patient-panel-head-spaced">
              <h2>Últimos exames</h2>
              <button className="patient-panel-link" type="button" onClick={() => onNavigate?.('exames')}>
                Ver todos <ChevronRight size={15} aria-hidden="true" />
              </button>
            </div>
            <div className="patient-exams-empty">
              <span className="patient-exams-empty-icon"><FileText size={19} aria-hidden="true" /></span>
              <p>Seus exames aparecerão aqui quando forem enviados.</p>
            </div>
          </div>

          <aside className="patient-panel">
            <div className="patient-panel-head">
              <h2>Resumo</h2>
            </div>
            <div className="patient-summary">
              <div className="patient-stat stat-blue">
                <CalendarCheck size={18} aria-hidden="true" />
                <strong>{isLoading ? '…' : monthlyCount}</strong>
                <span>Consultas este mês</span>
              </div>
              <div className="patient-stat stat-mint">
                <FileText size={18} aria-hidden="true" />
                <strong>—</strong>
                <span>Exames guardados</span>
              </div>
              <div className="patient-stat stat-amber">
                <Star size={18} aria-hidden="true" />
                <strong>{isLoading ? '…' : pendingReviews}</strong>
                <span>{pendingReviews === 1 ? 'Avaliação pendente' : 'Avaliações pendentes'}</span>
              </div>
            </div>
          </aside>
        </section>
      </main>
    </div>
  )
}
