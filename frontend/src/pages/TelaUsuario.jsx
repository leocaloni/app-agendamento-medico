import { CalendarDays, FileText, Search, Star } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import './tela-usuario.css'

const shortcuts = [
  { label: 'Buscar médico', Icon: Search, tone: 'blue' },
  { label: 'Meus exames', Icon: FileText, tone: 'mint' },
  { label: 'Histórico', Icon: CalendarDays, tone: 'coral' },
  { label: 'Avaliações', Icon: Star, tone: 'rose' },
]

export default function TelaUsuario() {
  return (
    <div className="patient-app">
      <Navbar />
      <main className="patient-home">
        <header className="patient-home-heading">
          <p>ÁREA DO PACIENTE</p>
          <h1>Como podemos ajudar?</h1>
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