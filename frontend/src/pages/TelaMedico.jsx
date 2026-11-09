import { LogOut } from 'lucide-react'
import TervisBrand from '../components/TervisBrand.jsx'
import './tela-medico.css'

export default function TelaMedico({ user, onLogout }) {
  const firstName = user?.firstName?.trim() || user?.fullName?.trim().split(/\s+/)[0]

  return (
    <main className="doctor-home-page">
      <header className="doctor-home-header">
        <TervisBrand />
        <button className="doctor-home-logout" type="button" onClick={onLogout}>
          <LogOut size={16} aria-hidden="true" />
          <span>Sair da conta</span>
        </button>
      </header>
      <section className="doctor-home-content">
        <p>ÁREA MÉDICA</p>
        <h1>Bem-vindo{firstName ? `, ${firstName}` : ''}</h1>
        <span>Seu acesso como médico foi realizado.</span>
      </section>
    </main>
  )
}