import { useEffect, useState } from 'react'
import Cadastro from './Cadastro.jsx'
import AgendaMedico from './AgendaMedico.jsx'
import MeusHorarios from './MeusHorarios.jsx'
import DoctorSearch from './DoctorSearch.jsx'
import DoctorProfile from './DoctorProfile.jsx'
import Login from './Login.jsx'
import MinhasConsultas from './MinhasConsultas.jsx'
import MeuPerfil from './MeuPerfil.jsx'
import MeusExames from './MeusExames.jsx'
import Pacientes from './Pacientes.jsx'
import PerfilMedico from './PerfilMedico.jsx'
import TelaMedico from './TelaMedico.jsx'
import TelaUsuario from './TelaUsuario.jsx'
import { getCurrentUser } from '../services/userApi.js'

const PATIENT_PAGES = new Set(['usuario', 'buscar-medico', 'consultas', 'exames', 'meu-perfil', 'perfil-medico'])
const DOCTOR_PAGES = new Set(['medico', 'agenda-medica', 'meus-horarios', 'pacientes', 'medico-perfil'])

export default function Index() {
  const [pagina, setPaginaState] = useState(() => localStorage.getItem('authToken') ? 'restoring-session' : 'login')
  const [user, setUser] = useState(null)
  const [selectedDoctorId, setSelectedDoctorId] = useState(() => localStorage.getItem('selectedDoctorId'))

  function setPagina(nextPage) {
    setPaginaState(nextPage)
    if (nextPage !== 'restoring-session') localStorage.setItem('appPage', nextPage)
  }

  useEffect(() => {
    if (!localStorage.getItem('authToken')) return undefined

    const controller = new AbortController()
    getCurrentUser(controller.signal)
      .then((currentUser) => {
        setUser(currentUser)
        const savedPage = localStorage.getItem('appPage')
        const isDoctor = currentUser.role === 'DOCTOR'
        const validPage = isDoctor ? DOCTOR_PAGES.has(savedPage) : PATIENT_PAGES.has(savedPage)
        setPagina(validPage ? savedPage : (isDoctor ? 'medico' : 'usuario'))
      })
      .catch((error) => {
        if (error.name !== 'AbortError') {
          localStorage.removeItem('authToken')
          localStorage.removeItem('appPage')
          localStorage.removeItem('patientSection')
          setPagina('login')
        }
      })

    return () => controller.abort()
  }, [])

  function handleLogin(authResponse) {
    localStorage.setItem('authToken', authResponse.token)
    setUser(authResponse.user)
    localStorage.setItem('patientSection', 'inicio')
    setPagina(authResponse.user.role === 'DOCTOR' ? 'medico' : 'usuario')
  }

  function handleLogout() {
    localStorage.removeItem('authToken')
    localStorage.removeItem('appPage')
    localStorage.removeItem('selectedDoctorId')
    localStorage.removeItem('patientSection')
    setUser(null)
    setPagina('login')
  }

  function patientNavigate(item) {
    if (item === 'buscar') return setPagina('buscar-medico')
    if (item === 'consultas') return setPagina('consultas')
    if (item === 'exames') return setPagina('exames')
    if (item === 'avaliacoes') return setPagina('consultas')
    if (item === 'perfil') return setPagina('meu-perfil')
    return setPagina('usuario')
  }

  if (pagina === 'restoring-session') {
    return <main className="session-loading">Restaurando sua sessão...</main>
  }

  if (pagina === 'cadastro') {
    return <Cadastro onVoltar={() => setPagina('login')} />
  }

  if (pagina === 'usuario') {
    return <TelaUsuario user={user} onLogout={handleLogout} onNavigate={patientNavigate} />
  }

  if (pagina === 'consultas') {
    return <MinhasConsultas user={user} onLogout={handleLogout} onNavigate={patientNavigate} />
  }

  if (pagina === 'exames') {
    return <MeusExames user={user} onLogout={handleLogout} onNavigate={patientNavigate} />
  }

  if (pagina === 'meu-perfil') {
    return (
      <MeuPerfil
        user={user}
        onLogout={handleLogout}
        onNavigate={patientNavigate}
        onProfileUpdated={(updated) => setUser(updated)}
      />
    )
  }

  if (pagina === 'medico') {
    return (
      <TelaMedico
        user={user}
        onLogout={handleLogout}
        onNavigate={() => setPagina('agenda-medica')}
        onOpenHorarios={() => setPagina('meus-horarios')}
        onOpenPacientes={() => setPagina('pacientes')}
        onOpenPerfil={() => setPagina('medico-perfil')}
      />
    )
  }

  if (pagina === 'agenda-medica') {
    return <AgendaMedico user={user} onLogout={handleLogout} onBack={() => setPagina('medico')} />
  }

  if (pagina === 'meus-horarios') {
    return <MeusHorarios user={user} onLogout={handleLogout} onBack={() => setPagina('medico')} />
  }

  if (pagina === 'pacientes') {
    return <Pacientes user={user} onLogout={handleLogout} onBack={() => setPagina('medico')} />
  }

  if (pagina === 'medico-perfil') {
    return <PerfilMedico user={user} onLogout={handleLogout} onBack={() => setPagina('medico')} />
  }

  if (pagina === 'buscar-medico') {
    return (
      <DoctorSearch
        user={user}
        onLogout={handleLogout}
        onSelectDoctor={(doctorId) => {
          setSelectedDoctorId(doctorId)
          localStorage.setItem('selectedDoctorId', doctorId)
          setPagina('perfil-medico')
        }}
        onNavigate={patientNavigate}
      />
    )
  }

  if (pagina === 'perfil-medico' && selectedDoctorId) {
    return (
      <DoctorProfile
        doctorId={selectedDoctorId}
        user={user}
        onLogout={handleLogout}
        onBack={() => setPagina('buscar-medico')}
      />
    )
  }

  return (
    <Login
      onLogin={handleLogin}
      onRegister={() => setPagina('cadastro')}
    />
  )
}