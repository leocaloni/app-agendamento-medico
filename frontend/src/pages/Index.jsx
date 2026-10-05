import { useState } from 'react'
import Login from './Login.jsx'
import TelaUsuario from './TelaUsuario.jsx'

export default function Index() {
  const [mostrarTelaUsuario, setMostrarTelaUsuario] = useState(false)

  return mostrarTelaUsuario
    ? <TelaUsuario />
    : <Login onLogin={() => setMostrarTelaUsuario(true)} />
}