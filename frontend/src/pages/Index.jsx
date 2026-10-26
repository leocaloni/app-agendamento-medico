import { useState } from 'react'
import Cadastro from './Cadastro.jsx'
import Login from './Login.jsx'
import TelaUsuario from './TelaUsuario.jsx'

export default function Index() {
  const [pagina, setPagina] = useState('login')

  function handleLogin(authResponse) {
    localStorage.setItem('authToken', authResponse.token)
    setPagina('usuario')
  }

  if (pagina === 'cadastro') {
    return <Cadastro onVoltar={() => setPagina('login')} />
  }

  if (pagina === 'usuario') return <TelaUsuario />

  return (
    <Login
      onLogin={handleLogin}
      onRegister={() => setPagina('cadastro')}
    />
  )
}