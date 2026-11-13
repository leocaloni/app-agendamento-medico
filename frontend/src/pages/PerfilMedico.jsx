import { useEffect, useState } from 'react'
import { ArrowLeft, LogOut, Stethoscope } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import { getDoctorSearchOptions, getMyDoctorProfile, updateDoctorProfile } from '../services/doctorApi.js'
import './perfil-medico.css'

export default function PerfilMedico({ user, onLogout, onBack }) {
  const [profile, setProfile] = useState(null)
  const [specialties, setSpecialties] = useState([])
  const [healthPlans, setHealthPlans] = useState([])
  const [form, setForm] = useState({
    bio: '',
    city: '',
    state: '',
    address: '',
    specialtyIds: [],
    acceptedPlanIds: [],
  })
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
        const [doctorProfile, options] = await Promise.all([
          getMyDoctorProfile(controller.signal),
          getDoctorSearchOptions(controller.signal),
        ])
        setProfile(doctorProfile)
        setSpecialties(options.specialties)
        setHealthPlans(options.healthPlans)
        setForm({
          bio: doctorProfile.bio || '',
          city: doctorProfile.city || '',
          state: doctorProfile.state || '',
          address: doctorProfile.address || '',
          specialtyIds: doctorProfile.specialties.map((specialty) => specialty.id),
          acceptedPlanIds: doctorProfile.acceptedPlans.map((plan) => plan.id),
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

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setSuccess('')
  }

  function toggleFromList(field, id) {
    setSuccess('')
    setForm((current) => {
      const selected = current[field]
      return {
        ...current,
        [field]: selected.includes(id) ? selected.filter((item) => item !== id) : [...selected, id],
      }
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (form.specialtyIds.length === 0) {
      setError('Selecione pelo menos uma especialidade.')
      return
    }

    setIsSubmitting(true)
    try {
      const updated = await updateDoctorProfile({
        bio: form.bio.trim() || null,
        city: form.city.trim() || null,
        state: form.state.trim() || null,
        address: form.address.trim() || null,
        specialtyIds: form.specialtyIds,
        acceptedPlanIds: form.acceptedPlanIds,
      })
      setProfile(updated)
      setSuccess('Perfil atualizado com sucesso.')
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="doctor-edit-page">
      <header className="doctor-edit-header">
        <TervisBrand />
        <div className="doctor-edit-account">
          <span>{user?.firstName || user?.fullName || 'Médico'}</span>
          <button type="button" className="doctor-edit-logout" onClick={onLogout} aria-label="Sair da conta">
            <LogOut size={16} aria-hidden="true" />
          </button>
        </div>
      </header>

      <section className="doctor-edit-content">
        <button className="doctor-edit-back" type="button" onClick={onBack}>
          <ArrowLeft size={16} aria-hidden="true" /> Voltar ao início
        </button>

        <div className="doctor-edit-title">
          <p>ÁREA MÉDICA</p>
          <h1>Meu perfil</h1>
          <span>Estes dados aparecem para os pacientes na busca.</span>
        </div>

        {error && <p className="doctor-edit-error" role="alert">{error}</p>}

        {isLoading ? (
          <p className="doctor-edit-state">Carregando seu perfil...</p>
        ) : !profile ? (
          <p className="doctor-edit-state">Não foi possível carregar o seu perfil.</p>
        ) : (
          <form className="doctor-edit-form" onSubmit={handleSubmit}>
            <div className="doctor-edit-identity">
              <span className="doctor-edit-identity-icon"><Stethoscope size={18} aria-hidden="true" /></span>
              <div>
                <strong>{profile.fullName}</strong>
                <span>CRM {profile.crm}</span>
              </div>
            </div>

            <label>
              <span>Sobre</span>
              <textarea value={form.bio} onChange={(event) => updateField('bio', event.target.value)}
                maxLength={2000} rows={4} placeholder="Conte sobre a sua experiência e áreas de atuação." />
            </label>

            <div className="doctor-edit-grid">
              <label>
                <span>Cidade</span>
                <input type="text" value={form.city} onChange={(event) => updateField('city', event.target.value)} maxLength={255} />
              </label>
              <label>
                <span>Estado</span>
                <input type="text" value={form.state} onChange={(event) => updateField('state', event.target.value)} maxLength={255} />
              </label>
            </div>

            <label>
              <span>Endereço de atendimento</span>
              <input type="text" value={form.address} onChange={(event) => updateField('address', event.target.value)} maxLength={255} />
            </label>

            <fieldset className="doctor-edit-fieldset">
              <legend>Especialidades</legend>
              <div className="doctor-edit-options">
                {specialties.map((specialty) => (
                  <button
                    type="button"
                    key={specialty.id}
                    className={`doctor-edit-chip${form.specialtyIds.includes(specialty.id) ? ' is-on' : ''}`}
                    aria-pressed={form.specialtyIds.includes(specialty.id)}
                    onClick={() => toggleFromList('specialtyIds', specialty.id)}
                  >
                    {specialty.name}
                  </button>
                ))}
              </div>
            </fieldset>

            <fieldset className="doctor-edit-fieldset">
              <legend>Convênios aceitos</legend>
              <div className="doctor-edit-options">
                {healthPlans.map((plan) => (
                  <button
                    type="button"
                    key={plan.id}
                    className={`doctor-edit-chip${form.acceptedPlanIds.includes(plan.id) ? ' is-on' : ''}`}
                    aria-pressed={form.acceptedPlanIds.includes(plan.id)}
                    onClick={() => toggleFromList('acceptedPlanIds', plan.id)}
                  >
                    {plan.name}
                  </button>
                ))}
              </div>
            </fieldset>

            {success && <p className="doctor-edit-success" role="status">{success}</p>}

            <button className="doctor-edit-submit" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Salvando...' : 'Salvar perfil'}
            </button>
          </form>
        )}
      </section>
    </main>
  )
}
