import { FileText } from 'lucide-react'

import Navbar from '../components/Navbar.jsx'
import './meus-exames.css'

export default function MeusExames({ user, onLogout, onNavigate }) {
  return (
    <div className="patient-app exams-page">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="exams-content">
        <header className="exams-heading">
          <div>
            <p>ÁREA DO PACIENTE</p>
            <h1>Meu s exames</h1>
          </div>
          <span><FileText size={16} aria-hidden="true" /> Seus resultados em um só lugar</span>
        </header>

        <section className="exams-empty">
          <span className="exams-empty-icon"><FileText size={24} aria-hidden="true" /></span>
          <h2>Nenhum exame disponível</h2>
          <p>Quando seus exames forem anexados, eles aparecerão aqui para consulta e download.</p>
        </section>
      </main>
    </div>
  )
}
