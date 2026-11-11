import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, Check, ChevronLeft, ChevronRight, Clock3, FileText, LogOut, UserX } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import {
  completeAppointment,
  getMyAppointments,
  markAppointmentNoShow,
  updateAppointmentNotes,
} from '../services/appointmentApi.js'
import './agenda-medico.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'
const WEEKDAYS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom']

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

function monthStartKey(dateKey) {
  return `${dateKey.slice(0, 7)}-01`
}

function moveMonth(monthKey, amount) {
  const [year, month] = monthKey.split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1 + amount, 1))
  return [date.getUTCFullYear(), String(date.getUTCMonth() + 1).padStart(2, '0'), '01'].join('-')
}

function monthRange(monthKey) {
  const [year, month] = monthKey.split('-').map(Number)
  const nextMonth = new Date(Date.UTC(year, month, 1))
  const firstKey = `${monthKey.slice(0, 7)}-01`
  const nextKey = [nextMonth.getUTCFullYear(), String(nextMonth.getUTCMonth() + 1).padStart(2, '0'), '01'].join('-')
  return { from: `${firstKey}T03:00:00Z`, to: `${nextKey}T03:00:00Z` }
}

function getCalendarCells(monthKey) {
  const [year, month] = monthKey.split('-').map(Number)
  const firstWeekday = (new Date(Date.UTC(year, month - 1, 1)).getUTCDay() + 6) % 7
  const daysInMonth = new Date(Date.UTC(year, month, 0)).getUTCDate()
  const cellCount = Math.ceil((firstWeekday + daysInMonth) / 7) * 7

  return Array.from({ length: cellCount }, (_, index) => {
    const day = index - firstWeekday + 1
    return day > 0 && day <= daysInMonth
      ? `${monthKey.slice(0, 7)}-${String(day).padStart(2, '0')}`
      : null
  })
}

function dateAtClinicNoon(dateKey) {
  const [year, month, day] = dateKey.split('-').map(Number)
  return new Date(Date.UTC(year, month - 1, day, 15))
}

function formatMonth(monthKey) {
  const [year, month] = monthKey.split('-').map(Number)
  return new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })
    .format(new Date(Date.UTC(year, month - 1, 15)))
}

function formatSelectedDate(dateKey) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  }).format(dateAtClinicNoon(dateKey))
}

function formatTime(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(instant))
}

const statusLabels = {
  SCHEDULED: 'Agendada',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
  NO_SHOW: 'Não compareceu',
}

export default function AgendaMedico({ user, onLogout, onBack }) {
  const todayKey = dateKeyInClinic(new Date())
  const [monthKey, setMonthKey] = useState(monthStartKey(todayKey))
  const [selectedDate, setSelectedDate] = useState(todayKey)
  const [appointments, setAppointments] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [actingId, setActingId] = useState(null)
  const [actionError, setActionError] = useState('')
  const [notesFor, setNotesFor] = useState(null)
  const [notesDraft, setNotesDraft] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    const { from, to } = monthRange(monthKey)

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
  }, [monthKey])

  const appointmentsByDate = useMemo(() => {
    const grouped = new Map()
    for (const appointment of appointments) {
      const dateKey = dateKeyInClinic(new Date(appointment.startAt))
      grouped.set(dateKey, [...(grouped.get(dateKey) || []), appointment])
    }
    return grouped
  }, [appointments])

  const calendarCells = getCalendarCells(monthKey)
  const selectedAppointments = appointmentsByDate.get(selectedDate) || []

  function changeMonth(amount) {
    const nextMonthKey = moveMonth(monthKey, amount)
    setMonthKey(nextMonthKey)
    setSelectedDate(nextMonthKey)
  }

  function applyUpdatedAppointment(updated) {
    setAppointments((current) => current.map((item) => (item.id === updated.id ? updated : item)))
  }

  async function runAction(appointmentId, action) {
    setActingId(appointmentId)
    setActionError('')
    try {
      const updated = await action()
      applyUpdatedAppointment(updated)
      return true
    } catch (runError) {
      setActionError(runError.message)
      return false
    } finally {
      setActingId(null)
    }
  }

  function openNotes(appointment) {
    setActionError('')
    setNotesFor(appointment.id)
    setNotesDraft(appointment.doctorNotes || '')
  }

  async function saveNotes(appointment) {
    const ok = await runAction(appointment.id, () => updateAppointmentNotes(appointment.id, notesDraft))
    if (ok) setNotesFor(null)
  }

  return (
    <main className="doctor-calendar-page">
      <header className="doctor-calendar-header">
        <TervisBrand />
        <div className="doctor-calendar-account">
          <span>{user?.firstName || user?.fullName || 'Médico'}</span>
          <button type="button" className="doctor-calendar-logout" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={16} />
          </button>
        </div>
      </header>

      <section className="doctor-calendar-content">
        <button className="doctor-calendar-back" type="button" onClick={onBack}>
          <ArrowLeft size={16} aria-hidden="true" /> Voltar ao início
        </button>

        <div className="doctor-calendar-title-row">
          <div>
            <p>ÁREA MÉDICA</p>
            <h1>Agenda geral</h1>
          </div>
          <div className="doctor-calendar-month-nav" aria-label="Navegação de mês">
            <button type="button" aria-label="Mês anterior" onClick={() => changeMonth(-1)}>
              <ChevronLeft size={18} />
            </button>
            <h2>{formatMonth(monthKey)}</h2>
            <button type="button" aria-label="Próximo mês" onClick={() => changeMonth(1)}>
              <ChevronRight size={18} />
            </button>
          </div>
        </div>

        {error && <p className="doctor-calendar-error" role="alert">{error}</p>}

        <div className="doctor-calendar-layout">
          <section className="doctor-calendar-grid-wrap" aria-label={`Calendário de ${formatMonth(monthKey)}`}>
            <div className="doctor-calendar-grid doctor-calendar-weekdays" aria-hidden="true">
              {WEEKDAYS.map((day) => <span key={day}>{day}</span>)}
            </div>
            <div className="doctor-calendar-grid">
              {calendarCells.map((dateKey, index) => {
                if (!dateKey) return <span className="doctor-calendar-empty-cell" aria-hidden="true" key={`empty-${index}`} />
                const dayAppointments = appointmentsByDate.get(dateKey) || []
                const isSelected = dateKey === selectedDate
                const isToday = dateKey === todayKey
                const dayNumber = Number(dateKey.slice(-2))

                return (
                  <button
                    className={`doctor-calendar-day${isSelected ? ' is-selected' : ''}${isToday ? ' is-today' : ''}${dayAppointments.length ? ' has-appointments' : ''}`}
                    type="button"
                    key={dateKey}
                    aria-label={`${dayNumber} ${formatMonth(monthKey)}${dayAppointments.length ? `, ${dayAppointments.length} consultas` : ''}`}
                    aria-pressed={isSelected}
                    onClick={() => setSelectedDate(dateKey)}
                  >
                    <span>{dayNumber}</span>
                    {dayAppointments.length > 0 && <small>{dayAppointments.length}</small>}
                  </button>
                )
              })}
            </div>
            <div className="doctor-calendar-legend">
              <span><i /> Dia com consulta</span>
              {isLoading && <span>Atualizando agenda...</span>}
            </div>
          </section>

          <section className="doctor-calendar-day-agenda" aria-live="polite">
            <header>
              <p>AGENDA DO DIA</p>
              <h2>{formatSelectedDate(selectedDate)}</h2>
              <span>{selectedAppointments.length} {selectedAppointments.length === 1 ? 'consulta' : 'consultas'}</span>
            </header>

            {actionError && <p className="doctor-appointment-action-error" role="alert">{actionError}</p>}

            {isLoading ? (
              <p className="doctor-calendar-day-empty">Carregando consultas...</p>
            ) : selectedAppointments.length ? (
              <div className="doctor-calendar-appointments">
                {selectedAppointments.map((appointment) => (
                  <article className="doctor-calendar-appointment" key={appointment.id}>
                    <time><Clock3 size={14} />{formatTime(appointment.startAt)}</time>
                    <strong>{appointment.patientName}</strong>
                    <span>{appointment.specialtyName} · {appointment.type === 'RETORNO' ? 'Retorno' : 'Primeira consulta'}</span>
                    <div>
                      <span className={`doctor-calendar-status status-${appointment.status.toLowerCase()}`}>
                        {statusLabels[appointment.status] || appointment.status}
                      </span>
                      <small>{appointment.paymentType === 'CONVENIO'
                        ? appointment.healthPlan?.name || 'Convênio'
                        : 'Particular'}</small>
                    </div>

                    {appointment.doctorNotes && notesFor !== appointment.id && (
                      <p className="doctor-appointment-note"><strong>Anotação:</strong> {appointment.doctorNotes}</p>
                    )}

                    <div className="doctor-appointment-actions">
                      {appointment.status === 'SCHEDULED' && (
                        <>
                          <button type="button" className="doctor-action-btn is-done" disabled={actingId === appointment.id}
                            onClick={() => runAction(appointment.id, () => completeAppointment(appointment.id))}>
                            <Check size={13} aria-hidden="true" /> Concluir
                          </button>
                          <button type="button" className="doctor-action-btn is-miss" disabled={actingId === appointment.id}
                            onClick={() => runAction(appointment.id, () => markAppointmentNoShow(appointment.id))}>
                            <UserX size={13} aria-hidden="true" /> Falta
                          </button>
                        </>
                      )}
                      <button type="button" className="doctor-action-btn is-notes" disabled={actingId === appointment.id}
                        onClick={() => (notesFor === appointment.id ? setNotesFor(null) : openNotes(appointment))}>
                        <FileText size={13} aria-hidden="true" /> Anotações
                      </button>
                    </div>

                    {notesFor === appointment.id && (
                      <div className="doctor-notes-box">
                        <textarea
                          value={notesDraft}
                          onChange={(event) => setNotesDraft(event.target.value)}
                          placeholder="Anotações clínicas da consulta..."
                        />
                        <div className="doctor-notes-actions">
                          <button type="button" className="doctor-action-btn is-notes" onClick={() => setNotesFor(null)}>
                            Cancelar
                          </button>
                          <button type="button" className="doctor-action-btn is-done" disabled={actingId === appointment.id}
                            onClick={() => saveNotes(appointment)}>
                            {actingId === appointment.id ? 'Salvando...' : 'Salvar anotação'}
                          </button>
                        </div>
                      </div>
                    )}
                  </article>
                ))}
              </div>
            ) : (
              <p className="doctor-calendar-day-empty">Nenhuma consulta neste dia.</p>
            )}
          </section>
        </div>
      </section>
    </main>
  )
}