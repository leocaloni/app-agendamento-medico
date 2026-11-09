import { useState } from 'react'
import Cadastro from './Cadastro.jsx'
import DoctorSearch from './DoctorSearch.jsx'
import DoctorProfile from './DoctorProfile.jsx'
import Login from './Login.jsx'
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

  if (pagina === 'cadastro') {
    return <Cadastro onVoltar={() => setPagina('login')} />
  }

  if (pagina === 'usuario') {
    return (
      <TelaUsuario
        user={user}
        onLogout={handleLogout}
        onNavigate={(item) => setPagina(item === 'buscar' ? 'buscar-medico' : 'usuario')}
      />
    )
  }

  if (pagina === 'medico') {
    return <TelaMedico user={user} onLogout={handleLogout} />
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
        onNavigate={(item) => setPagina(item === 'buscar' ? 'buscar-medico' : 'usuario')}
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