import { useState } from 'react'
import { CalendarDays, FileText, Heart, House, Menu, Search, Star, X } from 'lucide-react'
import './navbar.css'

const items = [
  { id: 'inicio', label: 'Início', Icon: House },
  { id: 'buscar', label: 'Buscar médico', Icon: Search },
  { id: 'consultas', label: 'Minhas consultas', Icon: CalendarDays },
  { id: 'exames', label: 'Meus exames', Icon: FileText },
  { id: 'avaliacoes', label: 'Avaliações', Icon: Star },
]

export default function Navbar() {
  const [activeItem, setActiveItem] = useState('inicio')
  const [isOpen, setIsOpen] = useState(false)

  return (
    <aside className="app-navbar" aria-label="Navegação principal">
      <a className="navbar-brand" href="#inicio" aria-label="Tervis, início">
        <span className="navbar-brand-mark"><Heart size={19} strokeWidth={2.2} /></span>
        <span>Tervis</span>
      </a>

      <button
        className="navbar-toggle"
        type="button"
        aria-label={isOpen ? 'Fechar menu' : 'Abrir menu'}
        aria-expanded={isOpen}
        aria-controls="navbar-menu"
        onClick={() => setIsOpen((open) => !open)}
      >
        {isOpen ? <X size={22} /> : <Menu size={22} />}
      </button>

      <p className="navbar-caption">Menu principal</p>
      <button
        className={`navbar-overlay${isOpen ? ' is-open' : ''}`}
        type="button"
        aria-label="Fechar menu"
        aria-hidden={!isOpen}
        tabIndex={isOpen ? 0 : -1}
        onClick={() => setIsOpen(false)}
      />
      <nav className={`navbar-menu${isOpen ? ' is-open' : ''}`} id="navbar-menu">
        {items.map(({ id, label, Icon }) => (
          <button
            className={`navbar-item${activeItem === id ? ' is-active' : ''}`}
            type="button"
            key={id}
            aria-current={activeItem === id ? 'page' : undefined}
            onClick={() => {
              setActiveItem(id)
              setIsOpen(false)
            }}
          >
            <Icon className="navbar-item-icon" size={19} strokeWidth={1.9} />
            <span className="navbar-item-label">{label}</span>
          </button>
        ))}
      </nav>

    </aside>
  )
}