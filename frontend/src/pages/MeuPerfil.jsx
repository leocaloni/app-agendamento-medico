import { useEffect, useState } from 'react'
import { Stethoscope, UserRound } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { getCurrentUser, getHealthPlans, updateProfile } from '../services/userApi.js'
import './meu-perfil.css'

const bloodTypes = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-', 'Não sei']

const appointmentFrequencies = [
  'Raramente',
  'Uma vez por ano',
  'Duas vezes por ano',
  'A cada três meses',
  'Mensalmente',
]

export default function MeuPerfil({ user, onLogout, onNavigate, onProfileUpdated }) {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    birthDate: '',
    healthPlanId: '',

    bloodType : '',
    allergies: '',
    familyHistory: '',
    currentMedications: '',
    appointmentFrequency: '',
  })
  const [healthPlans, setHealthPlans] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadProfile() {
      setIsLoading(true)
      setError('')
      try {
        const [currentUser, plans] = await Promise.all([
          getCurrentUser(controller.signal),
          getHealthPlans(controller.signal),
        ])
        setForm({
          fullName: currentUser.fullName || '',
          email: currentUser.email || '',
          phone: currentUser.phone || '',
          birthDate: currentUser.birthDate || '',
          healthPlanId: currentUser.healthPlan?.id || '',
          bloodType: currentUser.bloodType || '',
          allergies: currentUser.allergies || '',
          familyHistory: currentUser.familyHistory || '',
          currentMedications: currentUser.currentMedications || '',
          appointmentFrequency: currentUser.appointmentFrequency || '',
        })
        setHealthPlans(plans)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }

    loadProfile()
    return () => controller.abort()
  }, [])

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setSuccess('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (!form.fullName.trim()) {
      setError('Informe o seu nome completo.')
      return
    }

    setIsSubmitting(true)
    try {
      const updated = await updateProfile({
        fullName: form.fullName.trim(),
        phone: form.phone.replace(/\D/g, '') || null,
        birthDate: form.birthDate || null,
        healthPlanId: form.healthPlanId || null,
        bloodType: form.bloodType || null,
        allergies: form.allergies.trim() || null,
        familyHistory: form.familyHistory.trim() || null,
        currentMedications: form.currentMedications.trim() || null,
        appointmentFrequency: form.appointmentFrequency || null,
      })
      onProfileUpdated?.(updated)
      setSuccess('Dados atualizados com sucesso.')
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setIsSubmitting(false)
    }
  }


  return (
    <div className="patient-app profile-page">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="profile-content">
        <header className="profile-heading">
          <div>
            <p>ÁREA DO PACIENTE</p>
            <h1>Meu perfil</h1>
          </div>
          <span><UserRound size={16} aria-hidden="true" /> Seus dados cadastrais</span>
        </header>

        {error && <p className="profile-error" role="alert">{error}</p>}
        {success && <p className="profile-success" role="status">{success}</p>}

        {isLoading ? (
          <p className="profile-state">Carregando seus dados...</p>
        ) : (
          <form className="profile-form" onSubmit={handleSubmit}>
            <div className="profile-grid">
              <section className="profile-card">
                <h2 className="profile-card-title"><UserRound size={15} aria-hidden="true" /> Dados pessoais</h2>
                <label>
                  <span>Nome completo</span>
                  <input type="text" value={form.fullName} onChange={(event) => updateField('fullName', event.target.value)}
                    maxLength={255} required />
                </label>
                <label>
                  <span>E-mail</span>
                  <input type="email" value={form.email} readOnly />
                </label>
                <label>
                  <span>Telefone</span>
                  <input type="tel" value={form.phone} onChange={(event) => updateField('phone', event.target.value)}
                    placeholder="(11) 90000-0000" />
                </label>
                <label>
                  <span>Data de nascimento</span>
                  <input type="date" value={form.birthDate} onChange={(event) => updateField('birthDate', event.target.value)} />
                </label>
              </section>

              <section className="profile-card">
                <h2 className="profile-card-title"><Stethoscope size={15} aria-hidden="true" /> Informações médicas</h2>
                <label>
                  <span>Convênio</span>
                  <select value={form.healthPlanId} onChange={(event) => updateField('healthPlanId', event.target.value)}>
                    <option value="">Particular</option>
                    {healthPlans.map((plan) => (
                      <option key={plan.id} value={plan.id}>{plan.name}</option>
                    ))}
                  </select>
                </label>
                <label>
                  <span>Tipo sanguíneo</span>
                  <select value={form.bloodType} onChange={(event) => updateField('bloodType', event.target.value)}>
                    <option value="">Selecione</option>
                    {bloodTypes.map((type) => (
                      <option key={type} value={type}>{type}</option>
                    ))}
                  </select>
                </label>
                <label>
                  <span>Frequência de consultas</span>
                  <select value={form.appointmentFrequency} onChange={(event) => updateField('appointmentFrequency', event.target.value)}>
                    <option value="">Selecione</option>
                    {appointmentFrequencies.map((frequency) => (
                      <option key={frequency} value={frequency}>{frequency}</option>
                    ))}
                  </select>
                </label>
                <label>
                  <span>Alergias</span>
                  <input type="text" value={form.allergies} onChange={(event) => updateField('allergies', event.target.value)}
                    placeholder="Ex.: Dipirona, penicilina" maxLength={500} />
                </label>
                <label>
                  <span>Histórico de doenças familiares</span>
                  <textarea value={form.familyHistory} onChange={(event) => updateField('familyHistory', event.target.value)}
                    rows={3} maxLength={1000} placeholder="Ex.: Diabetes, hipertensão, doenças cardíacas" />
                </label>
                <label>
                  <span>Medicações atuais</span>
                  <textarea value={form.currentMedications} onChange={(event) => updateField('currentMedications', event.target.value)}
                    rows={3} maxLength={1000} placeholder="Ex.: Losartana 50mg, uso diário" />
                </label>
              </section>
            </div>

            <button className="profile-submit" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Salvando...' : 'Salvar alterações'}
            </button>
          </form>
        )}
      </main>
    </div>
  )
}
