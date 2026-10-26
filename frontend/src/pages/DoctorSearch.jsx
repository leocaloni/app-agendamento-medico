import { useEffect, useState } from 'react'
import { ArrowLeft, MapPin, Search, Star, Stethoscope } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { getDoctorSearchOptions, searchDoctors } from '../services/doctorApi.js'
import './doctor-search.css'

const initialFilters = {
  name: '',
  specialtyId: '',
  healthPlanId: '',
  minRating: '',
  sort: 'rating',
  allPlans: true,
}

function initials(fullName) {
  return fullName.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
}

export default function DoctorSearch({ user, onLogout, onNavigate }) {
  const [filters, setFilters] = useState(initialFilters)
  const [specialties, setSpecialties] = useState([])
  const [healthPlans, setHealthPlans] = useState([])
  const [results, setResults] = useState(null)
  const [page, setPage] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadDoctors(nextPage = 0, nextFilters = filters, signal) {
    setIsLoading(true)
    setError('')
    try {
      const data = await searchDoctors(nextFilters, nextPage, signal)
      setResults(data.doctors)
      setPage(nextPage)
    } catch (loadError) {
      if (loadError.name !== 'AbortError') setError(loadError.message)
    } finally {
      if (!signal?.aborted) setIsLoading(false)
    }
  }

  useEffect(() => {
    const controller = new AbortController()
    getDoctorSearchOptions(controller.signal)
      .then(({ specialties: specialtyOptions, healthPlans: planOptions }) => {
        setSpecialties(specialtyOptions)
        setHealthPlans(planOptions)
      })
      .catch((loadError) => {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      })
    loadDoctors(0, initialFilters, controller.signal)
    return () => controller.abort()
  }, [])

  function updateFilter(event) {
    const { name, value } = event.target
    setFilters((current) => ({
      ...current,
      [name]: value,
      ...(name === 'healthPlanId' ? { allPlans: !value } : {}),
    }))
  }

  function handleSearch(event) {
    event.preventDefault()
    loadDoctors(0, filters)
  }

  const doctors = results?.content || []

  return (
    <div className="patient-app doctor-search-app">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="doctor-search-page">
        <button className="doctor-search-back" type="button" onClick={() => onNavigate('inicio')}>
          <ArrowLeft size={16} aria-hidden="true" /> Início
        </button>

        <header className="doctor-search-heading">
          <div>
            <p>ENCONTRE SEU ESPECIALISTA</p>
            <h1>Buscar médicos</h1>
          </div>
          {results && <span className="doctor-search-count">{results.totalElements} encontrados</span>}
        </header>

        <form className="doctor-search-filters" onSubmit={handleSearch}>
          <label className="doctor-filter-name">
            <span>Nome do médico</span>
            <div className="doctor-filter-input doctor-name-input">
              <Search size={17} aria-hidden="true" />
              <input name="name" value={filters.name} onChange={updateFilter} placeholder="Ex.: Ana Ribeiro" />
            </div>
          </label>

          <label>
            <span>Especialidade</span>
            <select name="specialtyId" value={filters.specialtyId} onChange={updateFilter}>
              <option value="">Todas</option>
              {specialties.map((specialty) => (
                <option value={specialty.id} key={specialty.id}>{specialty.name}</option>
              ))}
            </select>
          </label>

          <label>
            <span>Convênio</span>
            <select name="healthPlanId" value={filters.healthPlanId} onChange={updateFilter}>
              <option value="">Todos</option>
              {healthPlans.map((plan) => (
                <option value={plan.id} key={plan.id}>{plan.name}</option>
              ))}
            </select>
          </label>

          <label>
            <span>Avaliação mínima</span>
            <select name="minRating" value={filters.minRating} onChange={updateFilter}>
              <option value="">Qualquer nota</option>
              <option value="3">3 ou mais</option>
              <option value="4">4 ou mais</option>
              <option value="4.5">4,5 ou mais</option>
            </select>
          </label>

          <label>
            <span>Ordenar por</span>
            <select name="sort" value={filters.sort} onChange={updateFilter}>
              <option value="rating">Melhor avaliação</option>
              <option value="name">Nome</option>
            </select>
          </label>

          <button className="doctor-search-submit" type="submit" disabled={isLoading}>
            <Search size={17} aria-hidden="true" /> Buscar
          </button>
        </form>

        {error && <p className="doctor-search-error" role="alert">{error}</p>}
        {isLoading && <p className="doctor-search-state">Buscando médicos...</p>}
        {!isLoading && !error && doctors.length === 0 && (
          <p className="doctor-search-state">Nenhum médico encontrado com esses filtros.</p>
        )}

        <section className="doctor-results" aria-label="Resultados da busca" aria-live="polite">
          {doctors.map((doctor) => (
            <article className="doctor-result" key={doctor.id}>
              <div className="doctor-result-avatar" aria-hidden="true">{initials(doctor.fullName)}</div>
              <div className="doctor-result-main">
                <div className="doctor-result-title">
                  <div>
                    <h2>{doctor.fullName}</h2>
                    <p>{doctor.specialties.join(' · ')} <span aria-hidden="true">·</span> CRM {doctor.crm}</p>
                  </div>
                  <div className="doctor-result-rating" aria-label={doctor.rating.total
                    ? `Nota ${Number(doctor.rating.average).toFixed(1)}, ${doctor.rating.total} avaliações`
                    : 'Ainda sem avaliações'}>
                    <Star size={16} fill="currentColor" aria-hidden="true" />
                    <strong>{doctor.rating.total ? Number(doctor.rating.average).toFixed(1) : 'Novo'}</strong>
                    {doctor.rating.total > 0 && <span>({doctor.rating.total})</span>}
                  </div>
                </div>

                {(doctor.city || doctor.state) && (
                  <p className="doctor-result-location">
                    <MapPin size={14} aria-hidden="true" />
                    {[doctor.city, doctor.state].filter(Boolean).join(', ')}
                  </p>
                )}

                <div className="doctor-result-details">
                  <div>
                    <span className="doctor-detail-label">Convênios</span>
                    <div className="doctor-result-tags">
                      {doctor.acceptedPlans.length ? doctor.acceptedPlans.map((plan) => (
                        <span className="doctor-result-tag" key={plan}>{plan}</span>
                      )) : <span className="doctor-result-particular">Particular</span>}
                    </div>
                  </div>
                  <span className="doctor-detail-note">
                    {doctor.rating.total ? `${doctor.rating.total} avaliações` : 'Ainda sem avaliações'}
                  </span>
                </div>
              </div>
            </article>
          ))}
        </section>

        {results && results.totalPages > 1 && (
          <nav className="doctor-pagination" aria-label="Paginação dos resultados">
            <button type="button" disabled={page === 0 || isLoading} onClick={() => loadDoctors(page - 1)}>
              Anterior
            </button>
            <span>Página {page + 1} de {results.totalPages}</span>
            <button type="button" disabled={page + 1 >= results.totalPages || isLoading}
              onClick={() => loadDoctors(page + 1)}>
              Próxima
            </button>
          </nav>
        )}

        {!isLoading && doctors.length === 0 && !error && (
          <div className="doctor-search-empty-icon" aria-hidden="true"><Stethoscope size={25} /></div>
        )}
      </main>
    </div>
  )
}