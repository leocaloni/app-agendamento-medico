import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, CalendarDays, Check, Clock3, MapPin, Star, X } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { createAppointment, getDoctor, getDoctorAvailability, getDoctorReviews } from '../services/doctorApi.js'
import './doctor-profile.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'
const APPOINTMENT_TYPE = 'PRIMEIRA_CONSULTA'

function dateKeyInClinic(date) {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: CLINIC_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date)
  const values = Object.fromEntries(parts.map(({ type, value }) => [type, value]))
  return `${values.year}-${values.month}-${values.day}`
}

function addCalendarDays(dateKey, days) {
  const [year, month, day] = dateKey.split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1, day + days))
  return [date.getUTCFullYear(), String(date.getUTCMonth() + 1).padStart(2, '0'),
    String(date.getUTCDate()).padStart(2, '0')].join('-')
}

function clinicDateKey(instant) {
  return dateKeyInClinic(new Date(instant))
}

function formatDay(dateKey) {
  const [year, month, day] = dateKey.split('-').map(Number)
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    weekday: 'short',
    day: '2-digit',
    month: 'short',
  }).format(new Date(Date.UTC(year, month - 1, day, 15)))
}

function formatTime(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(instant))
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value || 0))
}

function initials(fullName) {
  return fullName?.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'M'
}

export default function DoctorProfile({ doctorId, user, onLogout, onBack }) {
  const [doctor, setDoctor] = useState(null)
  const [availability, setAvailability] = useState([])
  const [reviews, setReviews] = useState({ content: [], number: 0, totalPages: 0, totalElements: 0 })
  const [selectedDate, setSelectedDate] = useState('')
  const [selectedSlot, setSelectedSlot] = useState('')
  const [selectedSpecialtyId, setSelectedSpecialtyId] = useState('')
  const [paymentType, setPaymentType] = useState('PARTICULAR')
  const [healthPlanId, setHealthPlanId] = useState('')
  const [patientNotes, setPatientNotes] = useState('')
  const [bookingDraft, setBookingDraft] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [confirmation, setConfirmation] = useState(null)
  const [reviewsLoading, setReviewsLoading] = useState(true)
  const [reviewsError, setReviewsError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    const from = dateKeyInClinic(new Date())
    const to = addCalendarDays(from, 13)

    async function loadProfile() {
      setIsLoading(true)
      setError('')
      try {
        const [doctorData, slots, reviewData] = await Promise.all([
          getDoctor(doctorId, controller.signal),
          getDoctorAvailability(doctorId, from, to, APPOINTMENT_TYPE, controller.signal),
          getDoctorReviews(doctorId, 0, controller.signal),
        ])
        setDoctor(doctorData)
        setAvailability(slots)
        setReviews(reviewData)
        setSelectedDate(slots.length ? clinicDateKey(slots[0].startAt) : '')
        setSelectedSpecialtyId(doctorData.specialties[0]?.id || '')

        const patientPlanId = user?.healthPlan?.id
        const acceptedPatientPlan = doctorData.acceptedPlans.find((plan) => plan.id === patientPlanId)
        if (acceptedPatientPlan) {
          setPaymentType('CONVENIO')
          setHealthPlanId(acceptedPatientPlan.id)
        }
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
          setReviewsLoading(false)
        }
      }
    }

    loadProfile()
    return () => controller.abort()
  }, [doctorId, user?.healthPlan?.id])

  async function changeReviewsPage(page) {
    setReviewsLoading(true)
    setReviewsError('')
    try {
      const reviewData = await getDoctorReviews(doctorId, page)
      setReviews(reviewData)
    } catch (loadError) {
      setReviewsError(loadError.message)
    } finally {
      setReviewsLoading(false)
    }
  }

  const dates = useMemo(() => [...new Set(availability.map((slot) => clinicDateKey(slot.startAt)))], [availability])
  const slotsForDate = availability.filter((slot) => clinicDateKey(slot.startAt) === selectedDate)
  const selectedAppointmentSlot = availability.find((slot) => slot.startAt === selectedSlot)
  const ratingAverage = Number(doctor?.rating?.average)
  const hasRating = doctor?.rating?.total > 0 && Number.isFinite(ratingAverage)

  function choosePaymentType(value) {
    setPaymentType(value)
    setError('')
    if (value === 'PARTICULAR') {
      setHealthPlanId('')
    } else if (!healthPlanId && doctor?.acceptedPlans.length) {
      setHealthPlanId(doctor.acceptedPlans[0].id)
    }
  }

  function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (!selectedSlot) {
      setError('Selecione um horário disponível.')
      return
    }
    if (!selectedSpecialtyId) {
      setError('Selecione uma especialidade.')
      return
    }
    if (paymentType === 'CONVENIO' && !healthPlanId) {
      setError('Selecione o convênio que será utilizado.')
      return
    }

    setBookingDraft(true)
  }

  async function confirmBooking() {
    setIsSubmitting(true)
    setError('')
    try {
      const appointment = await createAppointment({
        doctorId,
        specialtyId: selectedSpecialtyId,
        type: APPOINTMENT_TYPE,
        paymentType,
        healthPlanId: paymentType === 'CONVENIO' ? healthPlanId : null,
        startAt: selectedSlot,
        patientNotes: patientNotes.trim() || null,
        parentAppointmentId: null,
      })
      setConfirmation(appointment)
      setBookingDraft(false)
    } catch (submitError) {
      setError(submitError.message)
      setBookingDraft(false)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="patient-app doctor-profile-app">
      <Navbar user={user} onLogout={onLogout} />
      <main className="doctor-profile-page">
        <button className="doctor-profile-back" type="button" onClick={onBack}>
          <ArrowLeft size={16} aria-hidden="true" /> Voltar à busca
        </button>

        {error && <p className="doctor-profile-error" role="alert">{error}</p>}
        {isLoading ? (
          <p className="doctor-profile-state">Carregando perfil e horários...</p>
        ) : !doctor ? (
          <p className="doctor-profile-state">Não foi possível carregar este perfil médico.</p>
        ) : confirmation ? (
          <section className="appointment-confirmation" role="status" aria-live="polite">
            <span className="appointment-confirmation-icon"><Check size={24} aria-hidden="true" /></span>
            <p>CONSULTA AGENDADA</p>
            <h1>Horário reservado com {doctor.fullName}</h1>
            <span>{formatDay(clinicDateKey(confirmation.startAt))} · {formatTime(confirmation.startAt)}</span>
            <small>A forma de pagamento foi registrada. O pagamento não foi processado por esta tela.</small>
            <button type="button" onClick={onBack}>Voltar à busca</button>
          </section>
        ) : (
          <>
            <header className="doctor-profile-heading">
              <span className="doctor-profile-avatar" aria-hidden="true">{initials(doctor.fullName)}</span>
              <div className="doctor-profile-identity">
                <h1>{doctor.fullName}</h1>
                <p>{doctor.specialties.map((specialty) => specialty.name).join(' · ')} · CRM {doctor.crm}</p>
                {(doctor.city || doctor.state) && (
                  <span><MapPin size={14} aria-hidden="true" /> {[doctor.city, doctor.state].filter(Boolean).join(', ')}</span>
                )}
              </div>
              <div className="doctor-profile-rating">
                <Star size={16} fill="currentColor" aria-hidden="true" />
                <strong>{hasRating ? ratingAverage.toFixed(1) : 'Novo'}</strong>
                {hasRating && <span>{doctor.rating.total} avaliações</span>}
              </div>
            </header>

            {doctor.bio && <p className="doctor-profile-bio">{doctor.bio}</p>}

            <div className="doctor-profile-layout">
              <section className="doctor-profile-section">
                <h2><CalendarDays size={17} aria-hidden="true" /> Horários disponíveis</h2>
                {dates.length ? (
                  <>
                    <div className="appointment-date-list" aria-label="Dias disponíveis">
                      {dates.map((date) => (
                        <button
                          className={`appointment-date${selectedDate === date ? ' is-selected' : ''}`}
                          type="button"
                          key={date}
                          aria-pressed={selectedDate === date}
                          onClick={() => {
                            setSelectedDate(date)
                            setSelectedSlot('')
                          }}
                        >
                          {formatDay(date)}
                        </button>
                      ))}
                    </div>
                    <div className="appointment-time-list" aria-label="Horários">
                      {slotsForDate.map((slot) => (
                        <button
                          className={`appointment-time${selectedSlot === slot.startAt ? ' is-selected' : ''}`}
                          type="button"
                          key={slot.startAt}
                          aria-pressed={selectedSlot === slot.startAt}
                          onClick={() => setSelectedSlot(slot.startAt)}
                        >
                          {formatTime(slot.startAt)}
                        </button>
                      ))}
                    </div>
                  </>
                ) : (
                  <p className="doctor-profile-state">Sem horários disponíveis nos próximos 14 dias.</p>
                )}
              </section>

              <form className="appointment-form" onSubmit={handleSubmit}>
                <h2>Solicitar consulta</h2>
                <label>
                  <span>Especialidade</span>
                  <select value={selectedSpecialtyId} onChange={(event) => setSelectedSpecialtyId(event.target.value)} required>
                    {doctor.specialties.map((specialty) => (
                      <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Forma de pagamento</span>
                  <select value={paymentType} onChange={(event) => choosePaymentType(event.target.value)}>
                    <option value="PARTICULAR">Particular</option>
                    {doctor.acceptedPlans.length > 0 && <option value="CONVENIO">Convênio</option>}
                  </select>
                </label>

                {paymentType === 'CONVENIO' && (
                  <label>
                    <span>Convênio</span>
                    <select value={healthPlanId} onChange={(event) => setHealthPlanId(event.target.value)} required>
                      {doctor.acceptedPlans.map((plan) => (
                        <option key={plan.id} value={plan.id}>{plan.name}</option>
                      ))}
                    </select>
                  </label>
                )}

                <label>
                  <span>Observação (opcional)</span>
                  <textarea value={patientNotes} onChange={(event) => setPatientNotes(event.target.value)}
                    maxLength={1000} rows={3} placeholder="Conte brevemente o motivo da consulta." />
                </label>

                {selectedAppointmentSlot && (
                  <p className="appointment-selected-summary">
                    <Clock3 size={15} aria-hidden="true" />
                    {formatDay(clinicDateKey(selectedAppointmentSlot.startAt))} · {formatTime(selectedAppointmentSlot.startAt)}
                  </p>
                )}

                <button className="appointment-submit" type="submit" disabled={isSubmitting || !dates.length}>
                  Confirmar horário
                </button>
                <small className="appointment-payment-note">O agendamento não realiza cobrança nesta etapa.</small>
              </form>
            </div>

            <section className="doctor-reviews-section" aria-labelledby="doctor-reviews-title">
              <div className="doctor-reviews-heading">
                <div>
                  <p>OPINIÕES DE PACIENTES</p>
                  <h2 id="doctor-reviews-title">Avaliações</h2>
                </div>
                {hasRating && (
                  <span className="doctor-reviews-summary">
                    <Star size={15} fill="currentColor" aria-hidden="true" />
                    <strong>{ratingAverage.toFixed(1)}</strong>
                    <small>{doctor.rating.total} avaliações</small>
                  </span>
                )}
              </div>

              {reviewsError && <p className="doctor-profile-error" role="alert">{reviewsError}</p>}
              {reviewsLoading ? (
                <p className="doctor-reviews-empty">Carregando avaliações...</p>
              ) : reviews.content.length === 0 ? (
                <p className="doctor-reviews-empty">Este médico ainda não recebeu avaliações.</p>
              ) : (
                <>
                  <div className="doctor-reviews-list">
                    {reviews.content.map((review) => (
                      <article className="doctor-review-item" key={review.id}>
                        <div className="doctor-review-top">
                          <strong>{review.authorFirstName}</strong>
                          <time dateTime={review.createdAt}>
                            {new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(review.createdAt))}
                          </time>
                        </div>
                        <div className="doctor-review-stars" aria-label={`${review.rating} de 5 estrelas`}>
                          {[1, 2, 3, 4, 5].map((value) => (
                            <Star key={value} size={14} fill={value <= review.rating ? 'currentColor' : 'none'} />
                          ))}
                        </div>
                        {review.comment && <p>{review.comment}</p>}
                      </article>
                    ))}
                  </div>
                  {reviews.totalPages > 1 && (
                    <div className="doctor-reviews-pagination">
                      <button type="button" disabled={reviews.number === 0 || reviewsLoading}
                        onClick={() => changeReviewsPage(reviews.number - 1)}>
                        Anteriores
                      </button>
                      <span>Página {reviews.number + 1} de {reviews.totalPages}</span>
                      <button type="button" disabled={reviews.number + 1 >= reviews.totalPages || reviewsLoading}
                        onClick={() => changeReviewsPage(reviews.number + 1)}>
                        Próximas
                      </button>
                    </div>
                  )}
                </>
              )}
            </section>
          </>
        )}
        {bookingDraft && selectedAppointmentSlot && (
          <div className="booking-modal-backdrop" role="presentation" onClick={() => !isSubmitting && setBookingDraft(false)}>
            <section className="booking-modal" role="dialog" aria-modal="true" aria-labelledby="booking-modal-title"
              onClick={(event) => event.stopPropagation()}>
              <header>
                <div>
                  <p>CONFIRME SUA CONSULTA</p>
                  <h2 id="booking-modal-title">Confira os detalhes</h2>
                </div>
                <button type="button" className="booking-modal-close" onClick={() => setBookingDraft(false)}
                  disabled={isSubmitting} aria-label="Fechar confirmação">
                  <X size={17} aria-hidden="true" />
                </button>
              </header>
              <div className="booking-modal-details">
                <p><strong>Médico</strong><span>{doctor.fullName}</span></p>
                <p><strong>Especialidade</strong><span>{doctor.specialties.find((specialty) => specialty.id === selectedSpecialtyId)?.name || 'Não informada'}</span></p>
                <p><strong>Data</strong><span>{formatDay(clinicDateKey(selectedAppointmentSlot.startAt))}</span></p>
                <p><strong>Horário</strong><span>{formatTime(selectedAppointmentSlot.startAt)}</span></p>
                <p><strong>Pagamento</strong><span>{paymentType === 'CONVENIO'
                  ? doctor.acceptedPlans.find((plan) => plan.id === healthPlanId)?.name || 'Convênio'
                  : `Particular · ${formatCurrency(doctor.consultationPrice)}`}</span></p>
              </div>
              {patientNotes.trim() && (
                <p className="booking-modal-note"><strong>Observação:</strong> {patientNotes.trim()}</p>
              )}
              <small>O pagamento não será processado agora.</small>
              <div className="booking-modal-actions">
                <button type="button" className="booking-modal-cancel" onClick={() => setBookingDraft(false)} disabled={isSubmitting}>
                  Voltar
                </button>
                <button type="button" className="booking-modal-confirm" onClick={confirmBooking} disabled={isSubmitting}>
                  {isSubmitting ? 'Agendando...' : 'Confirmar consulta'}
                </button>
              </div>
            </section>
          </div>
        )}
      </main>
    </div>
  )
}