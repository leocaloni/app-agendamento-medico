import { useEffect, useState } from 'react'
import { ArrowLeft, LogOut } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import { getMyDoctorProfile, updateDoctorSchedule } from '../services/doctorApi.js'
import './meus-horarios.css'

const WEEKDAYS = [
  ['MONDAY', 'Seg'],
  ['TUESDAY', 'Ter'],
  ['WEDNESDAY', 'Qua'],
  ['THURSDAY', 'Qui'],
  ['FRIDAY', 'Sex'],
  ['SATURDAY', 'Sáb'],
  ['SUNDAY', 'Dom'],
]

function toClockValue(value) {
  return value ? String(value).slice(0, 5) : ''
}

export default function MeusHorarios({ user, onLogout, onBack }) {
  const [form, setForm] = useState({
    workDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
    workStartTime: '08:00',
    workEndTime: '18:00',
    firstVisitDurationMin: 30,
    returnDurationMin: 20,
  })
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function loadProfile() {
      setIsLoading(true)
      setError('')
      try {
        const profile = await getMyDoctorProfile(controller.signal)
        setForm({
          workDays: [...(profile.workDays || [])],
          workStartTime: toClockValue(profile.workStartTime) || '08:00',
          workEndTime: toClockValue(profile.workEndTime) || '18:00',
          firstVisitDurationMin: profile.firstVisitDurationMin || 30,
          returnDurationMin: profile.returnDurationMin || 20,
        })
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }

    loadProfile()
    return () => controller.abort()
  }, [])

  function toggleDay(day) {
    setSuccess(false)
    setForm((current) => ({
      ...current,
      workDays: current.workDays.includes(day)
        ? current.workDays.filter((item) => item !== day)
        : [...current.workDays, day],
    }))
  }

  function updateField(field, value) {
    setSuccess(false)
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSuccess(false)

    if (!form.workDays.length) {
      setError('Selecione pelo menos um dia de atendimento.')
      return
    }

    setIsSaving(true)
    try {
      await updateDoctorSchedule({
        workStartTime: form.workStartTime,
        workEndTime: form.workEndTime,
        workDays: form.workDays,
        firstVisitDurationMin: Number(form.firstVisitDurationMin),
        returnDurationMin: Number(form.returnDurationMin),
      })
      setSuccess(true)
    } catch (saveError) {
      setError(saveError.message)
    } finally {
      setIsSaving(false)
    }
  }

  const busy = isLoading || isSaving

  return (
    <main className="doctor-schedule-page">
      <header className="doctor-schedule-header">
        <TervisBrand />
        <div className="doctor-schedule-account">
          <span>{user?.firstName || user?.fullName || 'Médico'}</span>
          <button type="button" className="doctor-schedule-logout" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={16} aria-hidden="true" />
          </button>
        </div>
      </header>

      <section className="doctor-schedule-content">
        <button className="doctor-schedule-back" type="button" onClick={onBack}>
          <ArrowLeft size={16} aria-hidden="true" /> Voltar ao início
        </button>

        <div className="doctor-schedule-title">
          <p>ÁREA MÉDICA</p>
          <h1>Meus horários</h1>
          <span>Defina os dias, o horário de atendimento e a duração das consultas. É isso que o paciente vê como disponibilidade.</span>
        </div>

        {error && <p className="doctor-schedule-error" role="alert">{error}</p>}

        <form className="doctor-schedule-card" onSubmit={handleSubmit}>
          <fieldset className="doctor-schedule-fieldset" disabled={busy}>
            <legend>Dias de atendimento</legend>
            <div className="doctor-schedule-days">
              {WEEKDAYS.map(([day, label]) => (
                <button
                  type="button"
                  key={day}
                  className={`doctor-schedule-day${form.workDays.includes(day) ? ' is-on' : ''}`}
                  aria-pressed={form.workDays.includes(day)}
                  onClick={() => toggleDay(day)}
                >
                  {label}
                </button>
              ))}
            </div>
          </fieldset>

          <div className="doctor-schedule-grid">
            <label className="doctor-schedule-input">
              <span>Início do expediente</span>
              <input type="time" value={form.workStartTime} disabled={busy} required
                onChange={(event) => updateField('workStartTime', event.target.value)} />
            </label>
            <label className="doctor-schedule-input">
              <span>Fim do expediente</span>
              <input type="time" value={form.workEndTime} disabled={busy} required
                onChange={(event) => updateField('workEndTime', event.target.value)} />
            </label>
            <label className="doctor-schedule-input">
              <span>Duração 1ª consulta (min)</span>
              <input type="number" min="10" max="180" value={form.firstVisitDurationMin} disabled={busy} required
                onChange={(event) => updateField('firstVisitDurationMin', event.target.value)} />
            </label>
            <label className="doctor-schedule-input">
              <span>Duração retorno (min)</span>
              <input type="number" min="10" max="180" value={form.returnDurationMin} disabled={busy} required
                onChange={(event) => updateField('returnDurationMin', event.target.value)} />
            </label>
          </div>

          {success && <p className="doctor-schedule-success" role="status">Horários atualizados com sucesso.</p>}

          <div className="doctor-schedule-footer">
            {isLoading && <span className="doctor-schedule-loading">Carregando seus horários...</span>}
            <button className="doctor-schedule-submit" type="submit" disabled={busy || !form.workDays.length}>
              {isSaving ? 'Salvando...' : 'Salvar horários'}
            </button>
          </div>
        </form>
      </section>
    </main>
  )
}
