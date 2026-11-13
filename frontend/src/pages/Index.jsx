import { useState } from 'react'
import Cadastro from './Cadastro.jsx'
import AgendaMedico from './AgendaMedico.jsx'
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

export default function Index() {
  const [pagina, setPagina] = useState('login')
  const [user, setUser] = useState(null)
  const [selectedDoctorId, setSelectedDoctorId] = useState(null)

  function handleLogin(authResponse) {
    localStorage.setItem('authToken', authResponse.token)
    setUser(authResponse.user)
    setPagina(authResponse.user.role === 'DOCTOR' ? 'medico' : 'usuario')
  }

  function handleLogout() {
    localStorage.removeItem('authToken')
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
        onOpenPacientes={() => setPagina('pacientes')}
        onOpenPerfil={() => setPagina('medico-perfil')}
      />
    )
  }

  if (pagina === 'agenda-medica') {
    return <AgendaMedico user={user} onLogout={handleLogout} onBack={() => setPagina('medico')} />
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
