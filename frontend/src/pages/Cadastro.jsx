import { useEffect, useState } from 'react'
import { CalendarClock, CalendarDays, Droplet, IdCard, LockKeyhole, Mail, Phone, Pill, ShieldAlert, ShieldPlus, Stethoscope, UserRound, Users } from 'lucide-react'
import LoginButton from '../components/LoginButton.jsx'
import LoginField from '../components/LoginField.jsx'
import TervisBrand from '../components/TervisBrand.jsx'
import { getHealthPlans } from '../services/userApi.js'
import './login.css'
import './cadastro.css'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const bloodTypes = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-', 'Não sei']

const appointmentFrequencies = [
  'Raramente',
  'Uma vez por ano',
  'Duas vezes por ano',
  'A cada três meses',
  'Mensalmente',
]

const initialForm = {
  fullName: '',
  cpf: '',
  email: '',
  password: '',
  phone: '',
  birthDate: '',
  healthPlanId: '',
  bloodType: '',
  allergies: '',
  familyHistory: '',
  currentMedications: '',
  appointmentFrequency: '',
}

export default function Cadastro({ onVoltar }) {
  const [form, setForm] = useState(initialForm)
  const [healthPlans, setHealthPlans] = useState([])
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCreated, setIsCreated] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function loadHealthPlans() {
      try {
        const plans = await getHealthPlans(controller.signal)
        setHealthPlans(plans)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setHealthPlans([])
      }
    }

    loadHealthPlans()
    return () => controller.abort()
  }, [])

  function updateField(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (error) setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')

    const payload = {
      fullName: form.fullName.trim(),
      cpf: form.cpf.replace(/\D/g, ''),
      email: form.email.trim().toLowerCase(),
      password: form.password,
      phone: form.phone.replace(/\D/g, '') || null,
      birthDate: form.birthDate,
      healthPlanId: form.healthPlanId || null,
      bloodType: form.bloodType || null,
      allergies: form.allergies.trim() || null,
      familyHistory: form.familyHistory.trim() || null,
      currentMedications: form.currentMedications.trim() || null,
      appointmentFrequency: form.appointmentFrequency || null,
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      const data = await response.json().catch(() => null)

      if (!response.ok) {
        const message = data?.message || data?.error || 'Não foi possível criar a conta.'
        throw new Error(message)
      }

      setIsCreated(true)
    } catch (submitError) {
      setError(submitError.message || 'Não foi possível criar a conta.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page cadastro-page">
      <div className="login-layout cadastro-layout">
        <section className="login-mobile-header" aria-label="Tervis">
          <TervisBrand />
          <p>Cuidar de você ficou mais simples</p>
        </section>

        <section className="login-promo cadastro-promo">
          <p className="login-promo-kicker">SAÚDE MAIS PERTO</p>
          <h1>Seu cuidado<br />começa aqui.</h1>
          <p className="login-promo-copy">
            Crie sua conta para encontrar médicos e acompanhar sua saúde em um só lugar.
          </p>
        </section>

        <section className="login-panel cadastro-panel" aria-labelledby="cadastro-heading">
          <div className="login-panel-content cadastro-content">
            <TervisBrand className="login-form-brand" />
            {isCreated ? (
              <div role="status" aria-live="polite">
                <h2 id="cadastro-heading">Conta criada com sucesso!</h2>
                <p className="cadastro-intro">
                  Seu cadastro foi concluído. Entre com seu e-mail e senha para acessar sua conta.
                </p>
                <LoginButton className="login-submit" type="button" onClick={onVoltar}>
                  Ir para login
                </LoginButton>
              </div>
            ) : (
              <>
                <h2 id="cadastro-heading">Crie sua conta</h2>
                <p className="cadastro-intro">Preencha seus dados para começar.</p>

                <form className="login-form cadastro-form" onSubmit={handleSubmit} noValidate>
              <LoginField
                id="cadastro-nome"
                name="fullName"
                label="Nome completo"
                type="text"
                placeholder="Seu nome completo"
                autoComplete="name"
                icon={UserRound}
                required
                maxLength={255}
                value={form.fullName}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-cpf"
                name="cpf"
                label="CPF"
                type="text"
                placeholder="11 dígitos, sem pontuação"
                autoComplete="off"
                icon={IdCard}
                required
                inputMode="numeric"
                pattern="[0-9]{11}"
                maxLength={11}
                value={form.cpf}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-email"
                name="email"
                label="E-mail"
                type="email"
                placeholder="seu@email.com"
                autoComplete="email"
                icon={Mail}
                required
                value={form.email}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-senha"
                name="password"
                label="Senha"
                type="password"
                placeholder="Mínimo de 8 caracteres"
                autoComplete="new-password"
                icon={LockKeyhole}
                required
                minLength={8}
                maxLength={72}
                value={form.password}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-telefone"
                name="phone"
                label="Telefone (opcional)"
                type="tel"
                placeholder="(11) 99999-9999"
                autoComplete="tel"
                icon={Phone}
                value={form.phone}
                onChange={updateField}
                maxLength={20}
              />
              <LoginField
                id="cadastro-data-nascimento"
                name="birthDate"
                label="Data de nascimento"
                type="date"
                placeholder="dd/mm/aaaa"
                autoComplete="bday"
                icon={CalendarDays}
                required
                max={new Date().toISOString().split('T')[0]}
                value={form.birthDate}
                onChange={updateField}
              />

              <div className="cadastro-section">
                <Stethoscope size={15} strokeWidth={1.8} aria-hidden="true" />
                <span>Informações médicas</span>
              </div>
              <p className="cadastro-section-hint">Opcional. Ajuda o médico a conhecer seu histórico antes da consulta.</p>

              <div className="login-field-group">
                <label className="login-label" htmlFor="cadastro-convenio">Convênio</label>
                <div className="login-input-wrap">
                  <ShieldPlus size={17} strokeWidth={1.8} aria-hidden="true" />
                  <select id="cadastro-convenio" name="healthPlanId" value={form.healthPlanId} onChange={updateField}>
                    <option value="">Particular</option>
                    {healthPlans.map((plan) => (
                      <option key={plan.id} value={plan.id}>{plan.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="login-field-group">
                <label className="login-label" htmlFor="cadastro-tipo-sanguineo">Tipo sanguíneo</label>
                <div className="login-input-wrap">
                  <Droplet size={17} strokeWidth={1.8} aria-hidden="true" />
                  <select id="cadastro-tipo-sanguineo" name="bloodType" value={form.bloodType} onChange={updateField}>
                    <option value="">Selecione</option>
                    {bloodTypes.map((type) => (
                      <option key={type} value={type}>{type}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="login-field-group">
                <label className="login-label" htmlFor="cadastro-frequencia">Frequência de consultas</label>
                <div className="login-input-wrap">
                  <CalendarClock size={17} strokeWidth={1.8} aria-hidden="true" />
                  <select id="cadastro-frequencia" name="appointmentFrequency" value={form.appointmentFrequency} onChange={updateField}>
                    <option value="">Selecione</option>
                    {appointmentFrequencies.map((frequency) => (
                      <option key={frequency} value={frequency}>{frequency}</option>
                    ))}
                  </select>
                </div>
              </div>

              <LoginField
                id="cadastro-alergias"
                name="allergies"
                label="Alergias"
                type="text"
                placeholder="Ex.: Dipirona, penicilina"
                autoComplete="off"
                icon={ShieldAlert}
                value={form.allergies}
                onChange={updateField}
                maxLength={500}
              />

              <div className="login-field-group">
                <label className="login-label" htmlFor="cadastro-historico">Histórico de doenças familiares</label>
                <div className="login-input-wrap login-input-wrap-area">
                  <Users size={17} strokeWidth={1.8} aria-hidden="true" />
                  <textarea id="cadastro-historico" name="familyHistory" rows={3} maxLength={1000}
                    placeholder="Ex.: Diabetes, hipertensão, doenças cardíacas"
                    value={form.familyHistory} onChange={updateField} />
                </div>
              </div>

              <div className="login-field-group">
                <label className="login-label" htmlFor="cadastro-medicacoes">Medicações atuais</label>
                <div className="login-input-wrap login-input-wrap-area">
                  <Pill size={17} strokeWidth={1.8} aria-hidden="true" />
                  <textarea id="cadastro-medicacoes" name="currentMedications" rows={3} maxLength={1000}
                    placeholder="Ex.: Losartana 50mg, uso diário"
                    value={form.currentMedications} onChange={updateField} />
                </div>
              </div>

              {error && (
                <p role="alert" style={{ margin: 0, color: '#c82828', fontSize: 12 }}>
                  {error}
                </p>
              )}

              <LoginButton className="login-submit" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Criando conta...' : 'Criar conta'}
              </LoginButton>
                </form>

                <p className="signup-prompt cadastro-login-prompt">
                  Já tem uma conta? <button className="login-text-button" type="button" onClick={onVoltar}>Voltar ao login</button>
                </p>
              </>
            )}
          </div>
        </section>
      </div>
    </main>
  )
}
