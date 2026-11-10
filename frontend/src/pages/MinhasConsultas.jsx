import { useEffect, useMemo, useState } from 'react'
import { CalendarDays, Star } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { createAppointmentReview, getAllMyAppointments, getMyReviews } from '../services/appointmentApi.js'
import './minhas-consultas.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'
const filters = [
  { id: 'upcoming', label: 'Próximas' },
  { id: 'completed', label: 'Concluídas' },
  { id: 'all', label: 'Todas' },
]

const statusLabels = {
  SCHEDULED: 'Agendada',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
  NO_SHOW: 'Não compareceu',
}

function reviewKey(doctorId, appointmentStartAt) {
  return `${doctorId}|${appointmentStartAt}`
}

function formatDate(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    dateStyle: 'medium',
  }).format(new Date(instant))
}

function formatTime(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(instant))
}

export default function MinhasConsultas({ user, onLogout, onNavigate }) {
  const [appointments, setAppointments] = useState([])
  const [reviews, setReviews] = useState([])
  const [activeFilter, setActiveFilter] = useState('upcoming')
  const [reviewingId, setReviewingId] = useState(null)
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadData() {
      setIsLoading(true)
      setError('')
      try {
        const [appointmentData, reviewData] = await Promise.all([
          getAllMyAppointments(controller.signal),
          getMyReviews(controller.signal),
        ])
        setAppointments(appointmentData)
        setReviews(reviewData)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }

    loadData()
    return () => controller.abort()
  }, [])

  const reviewedKeys = useMemo(() => new Set(
    reviews.map((review) => reviewKey(review.doctorId, review.appointmentStartAt)),
  ), [reviews])

  const visibleAppointments = useMemo(() => {
    const now = Date.now()
    const filtered = appointments.filter((appointment) => {
      if (activeFilter === 'completed') return appointment.status === 'COMPLETED'
      if (activeFilter === 'upcoming') return appointment.status === 'SCHEDULED' && new Date(appointment.startAt).getTime() >= now
      return true
    })
    return filtered.sort((first, second) => new Date(second.startAt) - new Date(first.startAt))
  }, [appointments, activeFilter])

  function openReview(appointment) {
    setReviewingId(appointment.id)
    setRating(0)
    setComment('')
    setError('')
    setSuccess('')
  }

  function closeReview() {
    setReviewingId(null)
    setRating(0)
    setComment('')
    setError('')
  }

  async function submitReview(event, appointment) {
    event.preventDefault()
    setError('')
    if (!rating) {
      setError('Selecione uma nota de 1 a 5 estrelas.')
      return
    }

    setIsSubmitting(true)
    try {
      const review = await createAppointmentReview(appointment.id, {
        rating,
        comment: comment.trim() || null,
      })
      setReviews((current) => [
        {
          ...review,
          doctorId: appointment.doctorId,
          appointmentStartAt: appointment.startAt,
        },
        ...current,
      ])
      setReviewingId(null)
      setRating(0)
      setComment('')
      setSuccess(`Avaliação enviada para ${appointment.doctorName}.`)
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="patient-app appointments-page">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="appointments-content">
        <header className="appointments-heading">
          <div>
            <p>ÁREA DO PACIENTE</p>
            <h1>Minhas consultas</h1>
          </div>
          <span><CalendarDays size={16} aria-hidden="true" /> {appointments.length} no histórico</span>
        </header>

        <div className="appointments-filters" role="group" aria-label="Filtrar consultas">
          {filters.map((filter) => (
            <button
              className={activeFilter === filter.id ? 'is-active' : ''}
              type="button"
              key={filter.id}
              aria-pressed={activeFilter === filter.id}
              onClick={() => setActiveFilter(filter.id)}
            >
              {filter.label}
            </button>
          ))}
        </div>

        {error && <p className="appointments-error" role="alert">{error}</p>}
        {success && <p className="appointments-success" role="status">{success}</p>}
        {isLoading ? (
          <p className="appointments-empty">Carregando consultas...</p>
        ) : visibleAppointments.length === 0 ? (
          <p className="appointments-empty">
            {activeFilter === 'completed'
              ? 'Você ainda não tem consultas concluídas.'
              : activeFilter === 'upcoming'
                ? 'Você não tem consultas futuras agendadas.'
                : 'Nenhuma consulta encontrada.'}
          </p>
        ) : (
          <section className="appointments-list" aria-label="Consultas" aria-live="polite">
            {visibleAppointments.map((appointment) => {
              const isReviewed = reviewedKeys.has(reviewKey(appointment.doctorId, appointment.startAt))
              const isReviewing = reviewingId === appointment.id

              return (
                <article className="appointment-history-item" key={appointment.id}>
                  <div className="appointment-history-date">
                    <span>{formatDate(appointment.startAt)}</span>
                    <strong>{formatTime(appointment.startAt)}</strong>
                  </div>
                  <div className="appointment-history-main">
                    <h2>{appointment.doctorName}</h2>
                    <p>{appointment.specialtyName} · {appointment.type === 'RETORNO' ? 'Retorno' : 'Primeira consulta'}</p>
                    <span>{appointment.paymentType === 'CONVENIO'
                      ? appointment.healthPlan?.name || 'Convênio'
                      : 'Particular'}</span>
                  </div>
                  <div className="appointment-history-actions">
                    <span className={`appointment-history-status status-${appointment.status.toLowerCase()}`}>
                      {statusLabels[appointment.status] || appointment.status}
                    </span>
                    {appointment.status === 'COMPLETED' && (
                      isReviewed ? (
                        <span className="appointment-reviewed"><Star size={14} fill="currentColor" /> Avaliada</span>
                      ) : (
                        <button className="appointment-review-button" type="button" onClick={() => openReview(appointment)}>
                          Avaliar consulta
                        </button>
                      )
                    )}
                  </div>

                  {isReviewing && (
                    <form className="appointment-review-form" onSubmit={(event) => submitReview(event, appointment)}>
                      <h3>Avaliar consulta com {appointment.doctorName}</h3>
                      <div className="review-stars" role="radiogroup" aria-label="Nota de 1 a 5 estrelas">
                        {[1, 2, 3, 4, 5].map((value) => (
                          <button
                            className={value <= rating ? 'is-selected' : ''}
                            type="button"
                            key={value}
                            role="radio"
                            aria-checked={rating === value}
                            aria-label={`${value} ${value === 1 ? 'estrela' : 'estrelas'}`}
                            onClick={() => setRating(value)}
                          >
                            <Star size={24} fill={value <= rating ? 'currentColor' : 'none'} />
                          </button>
                        ))}
                      </div>
                      <label>
                        <span>Comentário (opcional)</span>
                        <textarea value={comment} onChange={(event) => setComment(event.target.value)}
                          maxLength={1000} rows={3} placeholder="Conte como foi sua experiência." />
                      </label>
                      {error && <p className="appointments-error" role="alert">{error}</p>}
                      <div className="appointment-review-actions">
                        <button type="button" className="review-cancel" onClick={closeReview} disabled={isSubmitting}>Cancelar</button>
                        <button type="submit" className="review-submit" disabled={isSubmitting}>
                          {isSubmitting ? 'Enviando...' : 'Enviar avaliação'}
                        </button>
                      </div>
                    </form>
                  )}
                </article>
              )
            })}
          </section>
        )}
      </main>
    </div>
  )
}