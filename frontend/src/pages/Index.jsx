import { useState } from 'react'
import Cadastro from './Cadastro.jsx'
import Login from './Login.jsx'
import TelaUsuario from './TelaUsuario.jsx'

export default function Index() {
  const [pagina, setPagina] = useState('login')
  const [user, setUser] = useState(null)

  function handleLogin(authResponse) {
    localStorage.setItem('authToken', authResponse.token)
    setUser(authResponse.user)
    setPagina('usuario')
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

  return (
    <Login
      onLogin={handleLogin}
      onRegister={() => setPagina('cadastro')}
    />
  )
}