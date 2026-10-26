import { useState } from 'react'
import AdminDoctors from './AdminDoctors.jsx'
import Cadastro from './Cadastro.jsx'
import Login from './Login.jsx'
import TelaUsuario from './TelaUsuario.jsx'

export default function Index() {
  const [pagina, setPagina] = useState('login')
  const [user, setUser] = useState(null)

  function handleLogin(authResponse) {
    localStorage.setItem('authToken', authResponse.token)
    setUser(authResponse.user)
    setPagina(authResponse.user.role === 'ADMIN' ? 'admin-doctors' : 'usuario')
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
    return <TelaUsuario user={user} onLogout={handleLogout} />
  }

  if (pagina === 'admin-doctors') {
    return <AdminDoctors user={user} onLogout={handleLogout} onBack={handleLogout} />
  }

  if (pagina === 'login-admin') {
    return (
      <Login
        mode="admin"
        onLogin={handleLogin}
        onPatientAccess={() => setPagina('login')}
      />
    )
  }

  return (
    <Login
      onLogin={handleLogin}
      onRegister={() => setPagina('cadastro')}
      onAdminAccess={() => setPagina('login-admin')}
    />
  )
}