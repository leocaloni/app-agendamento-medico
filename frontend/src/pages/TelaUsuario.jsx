import { CalendarDays, FileText, Search, Star } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import './tela-usuario.css'

const shortcuts = [
  { label: 'Buscar médico', Icon: Search, tone: 'blue' },
  { label: 'Meus exames', Icon: FileText, tone: 'mint' },
  { label: 'Histórico', Icon: CalendarDays, tone: 'coral' },
  { label: 'Avaliações', Icon: Star, tone: 'rose' },
]

export default function TelaUsuario({ user, onLogout }) {
  const firstName = user?.firstName?.trim() || user?.fullName?.trim().split(/\s+/)[0]

  return (
    <div className="patient-app">
      <Navbar user={user} onLogout={onLogout} />
      <main className="patient-home">
        <header className="patient-home-heading">
          <h1>Como podemos ajudar{firstName ? `, ${firstName}` : ''}?</h1>
        </header>

        <nav className="patient-shortcuts" aria-label="Ações do paciente">
          {shortcuts.map(({ label, Icon, tone }) => (
            <button className="patient-shortcut" type="button" key={label}>
              <span className={`patient-shortcut-icon tone-${tone}`}>
                <Icon size={21} strokeWidth={1.9} />
              </span>
              <span>{label}</span>
            </button>
          ))}
        </nav>
      </main>
    </div>
  )
}