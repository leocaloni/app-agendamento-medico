import { useEffect, useState } from 'react'
import { ArrowLeft, Check, HeartPulse, LogOut, Stethoscope } from 'lucide-react'
import DoctorMultiSelect from '../components/DoctorMultiSelect.jsx'
import { createDoctor, getDoctorFormOptions } from '../services/adminApi.js'
import './admin-doctors.css'

const weekdays = [
  ['MONDAY', 'Segunda'], ['TUESDAY', 'Terça'], ['WEDNESDAY', 'Quarta'],
  ['THURSDAY', 'Quinta'], ['FRIDAY', 'Sexta'], ['SATURDAY', 'Sábado'], ['SUNDAY', 'Domingo'],
]
const states = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
]
const initialForm = {
  fullName: '', email: '', password: '', cpf: '', phone: '', birthDate: '',
  crmNumber: '', crmUf: '', bio: '', city: '', state: '', address: '',
  workStartTime: '09:00', workEndTime: '17:00', firstVisitDurationMin: '30', returnDurationMin: '15',
}

export default function AdminDoctors({ user, onLogout, onBack }) {
  const [form, setForm] = useState(initialForm)
  const [specialties, setSpecialties] = useState([])
  const [healthPlans, setHealthPlans] = useState([])
  const [specialtyIds, setSpecialtyIds] = useState([])
  const [acceptedPlanIds, setAcceptedPlanIds] = useState([])
  const [workDays, setWorkDays] = useState([])
  const [privateOnly, setPrivateOnly] = useState(true)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [createdDoctor, setCreatedDoctor] = useState(null)

  useEffect(() => {
    let isMounted = true
    getDoctorFormOptions()
      .then((options) => {
        if (!isMounted) return
        setSpecialties(options.specialties)
        setHealthPlans(options.healthPlans)
      })
      .catch((loadError) => {
        if (isMounted) setError(loadError.message)
      })
      .finally(() => {
        if (isMounted) setIsLoading(false)
      })
    return () => { isMounted = false }
  }, [])

  function updateField(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    setError('')
  }

  function toggleDay(day) {
    setWorkDays((current) => current.includes(day)
      ? current.filter((value) => value !== day)
      : [...current, day])
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (!specialtyIds.length) {
      setError('Selecione pelo menos uma especialidade.')
      return
    }
    if (!workDays.length) {
      setError('Selecione pelo menos um dia de atendimento.')
      return
    }

    setIsSubmitting(true)
    const payload = {
      ...form,
      fullName: form.fullName.trim(),
      email: form.email.trim().toLowerCase(),
      cpf: form.cpf.replace(/\D/g, ''),
      phone: form.phone.replace(/\D/g, '') || null,
      crmNumber: form.crmNumber.trim(),
      crmUf: form.crmUf,
      bio: form.bio.trim() || null,
      city: form.city.trim() || null,
      state: form.state || null,
      address: form.address.trim() || null,
      workDays,
      firstVisitDurationMin: Number(form.firstVisitDurationMin),
      returnDurationMin: Number(form.returnDurationMin),
      specialtyIds,
      acceptedPlanIds: privateOnly ? [] : acceptedPlanIds,
    }

    try {
      const result = await createDoctor(payload)
      setCreatedDoctor(result.doctor)
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  function resetForm() {
    setForm(initialForm)
    setSpecialtyIds([])
    setAcceptedPlanIds([])
    setWorkDays([])
    setPrivateOnly(true)
    setCreatedDoctor(null)
    setError('')
  }

  return (
    <main className="admin-doctors-page">
      <header className="admin-doctors-header">
        <a className="admin-doctors-brand" href="#inicio" aria-label="Tervis">
          <span><HeartPulse size={19} /></span>Tervis
        </a>
        <div className="admin-doctors-account">
          <span>{user?.firstName || user?.fullName || 'Administrador'}</span>
          <button type="button" className="admin-doctors-logout" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={17} />
          </button>
        </div>
      </header>

      <section className="admin-doctors-content">
        <button className="admin-back-button" type="button" onClick={onBack}>
          <ArrowLeft size={16} /> Voltar ao painel
        </button>

        {createdDoctor ? (
          <div className="admin-doctor-success" role="status" aria-live="polite">
            <span className="admin-success-icon"><Check size={22} /></span>
            <p className="admin-section-kicker">CADASTRO CONCLUÍDO</p>
            <h1>{createdDoctor.user.fullName} foi cadastrado</h1>
            <p>O médico já pode acessar a plataforma com as credenciais informadas.</p>
            <button type="button" className="admin-primary-button" onClick={resetForm}>Cadastrar outro médico</button>
          </div>
        ) : (
          <>
            <div className="admin-doctors-title">
              <span className="admin-doctors-title-icon"><Stethoscope size={20} /></span>
              <div>
                <p className="admin-section-kicker">ADMINISTRAÇÃO</p>
                <h1>Cadastrar médico</h1>
                <p>Crie o acesso e configure o perfil profissional.</p>
              </div>
            </div>

            <form className="admin-doctor-form" onSubmit={handleSubmit}>
              <section className="admin-form-section">
                <h2>Dados pessoais e de acesso</h2>
                <div className="admin-form-grid">
                  <label className="admin-form-field admin-field-wide">
                    <span className="admin-form-label">Nome completo</span>
                    <input name="fullName" value={form.fullName} onChange={updateField} required maxLength={255} autoComplete="name" />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">E-mail</span>
                    <input name="email" type="email" value={form.email} onChange={updateField} required maxLength={255} autoComplete="email" />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Senha inicial</span>
                    <input name="password" type="password" value={form.password} onChange={updateField} required minLength={8} maxLength={72} autoComplete="new-password" />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">CPF (somente números)</span>
                    <input name="cpf" value={form.cpf} onChange={updateField} required inputMode="numeric" pattern="[0-9]{11}" maxLength={11} />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Data de nascimento</span>
                    <input name="birthDate" type="date" value={form.birthDate} onChange={updateField} max={new Date().toISOString().split('T')[0]} />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Telefone (opcional)</span>
                    <input name="phone" type="tel" value={form.phone} onChange={updateField} maxLength={20} autoComplete="tel" />
                  </label>
                </div>
              </section>

              <section className="admin-form-section">
                <h2>Perfil profissional</h2>
                <div className="admin-form-grid">
                  <label className="admin-form-field">
                    <span className="admin-form-label">Número do CRM</span>
                    <input name="crmNumber" value={form.crmNumber} onChange={updateField} required maxLength={20} />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">UF do CRM</span>
                    <select name="crmUf" value={form.crmUf} onChange={updateField} required>
                      <option value="">Selecione</option>
                      {states.map((state) => <option value={state} key={state}>{state}</option>)}
                    </select>
                  </label>
                  <div className="admin-field-wide">
                    <DoctorMultiSelect id="admin-doctor-specialties" label="Especialidades"
                      options={specialties} value={specialtyIds} onChange={setSpecialtyIds}
                      placeholder={isLoading ? 'Carregando especialidades...' : 'Pesquisar e selecionar especialidades'} />
                  </div>
                  <div className="admin-field-wide admin-plan-field">
                    <label className="admin-checkbox-row">
                      <input type="checkbox" checked={privateOnly} onChange={(event) => {
                        setPrivateOnly(event.target.checked)
                        if (event.target.checked) setAcceptedPlanIds([])
                      }} />
                      <span>Atende somente particular (não trabalha com convênios)</span>
                    </label>
                    {!privateOnly && (
                      <DoctorMultiSelect id="admin-doctor-plans" label="Convênios aceitos"
                        options={healthPlans} value={acceptedPlanIds} onChange={setAcceptedPlanIds}
                        placeholder={isLoading ? 'Carregando convênios...' : 'Pesquisar e selecionar convênios'} />
                    )}
                  </div>
                  <label className="admin-form-field admin-field-wide">
                    <span className="admin-form-label">Descrição profissional (opcional)</span>
                    <textarea name="bio" value={form.bio} onChange={updateField} maxLength={2000} rows={3} />
                  </label>
                </div>
              </section>

              <section className="admin-form-section">
                <h2>Local e horários de atendimento</h2>
                <div className="admin-form-grid">
                  <label className="admin-form-field">
                    <span className="admin-form-label">Cidade (opcional)</span>
                    <input name="city" value={form.city} onChange={updateField} maxLength={255} />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Estado (opcional)</span>
                    <select name="state" value={form.state} onChange={updateField}>
                      <option value="">Selecione</option>
                      {states.map((state) => <option value={state} key={state}>{state}</option>)}
                    </select>
                  </label>
                  <label className="admin-form-field admin-field-wide">
                    <span className="admin-form-label">Endereço de atendimento (opcional)</span>
                    <input name="address" value={form.address} onChange={updateField} maxLength={255} />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Início do expediente</span>
                    <input name="workStartTime" type="time" value={form.workStartTime} onChange={updateField} required />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Fim do expediente</span>
                    <input name="workEndTime" type="time" value={form.workEndTime} onChange={updateField} required />
                  </label>
                  <fieldset className="admin-days-field admin-field-wide">
                    <legend className="admin-form-label">Dias de atendimento</legend>
                    <div className="admin-days-list">
                      {weekdays.map(([value, label]) => (
                        <label className="admin-day-option" key={value}>
                          <input type="checkbox" checked={workDays.includes(value)} onChange={() => toggleDay(value)} />
                          <span>{label}</span>
                        </label>
                      ))}
                    </div>
                  </fieldset>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Primeira consulta (min)</span>
                    <input name="firstVisitDurationMin" type="number" min="1" step="1" value={form.firstVisitDurationMin} onChange={updateField} required />
                  </label>
                  <label className="admin-form-field">
                    <span className="admin-form-label">Retorno (min)</span>
                    <input name="returnDurationMin" type="number" min="1" step="1" value={form.returnDurationMin} onChange={updateField} required />
                  </label>
                </div>
              </section>

              {error && <p className="admin-form-error" role="alert">{error}</p>}
              <div className="admin-form-actions">
                <button className="admin-secondary-button" type="button" onClick={onBack}>Cancelar</button>
                <button className="admin-primary-button" type="submit" disabled={isLoading || isSubmitting}>
                  {isSubmitting ? 'Cadastrando...' : 'Cadastrar médico'}
                </button>
              </div>
            </form>
          </>
        )}
      </section>
    </main>
  )
}