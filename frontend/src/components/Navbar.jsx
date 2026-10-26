import { useState } from 'react'
import { CalendarDays, ChevronDown, FileText, Heart, House, LogOut, Menu, Search, Star, X } from 'lucide-react'
import './navbar.css'

const items = [
  { id: 'inicio', label: 'Início', Icon: House },
  { id: 'buscar', label: 'Buscar médico', Icon: Search },
  { id: 'consultas', label: 'Minhas consultas', Icon: CalendarDays },
  { id: 'exames', label: 'Meus exames', Icon: FileText },
  { id: 'avaliacoes', label: 'Avaliações', Icon: Star },
]

export default function Navbar({ user, onLogout }) {
  const [activeItem, setActiveItem] = useState('inicio')
  const [isOpen, setIsOpen] = useState(false)
  const [isProfileOpen, setIsProfileOpen] = useState(false)
  const fullName = user?.fullName || 'Paciente'
  const displayName = user?.firstName?.trim() || fullName.trim().split(/\s+/)[0] || 'Paciente'
  const initials = displayName[0]?.toUpperCase() || 'P'

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

      <div className="navbar-account">
        <button
          className="navbar-user"
          type="button"
          aria-expanded={isProfileOpen}
          aria-haspopup="menu"
          aria-controls="navbar-account-menu"
          onClick={() => setIsProfileOpen((open) => !open)}
        >
          <span className="navbar-user-avatar" aria-hidden="true">{initials}</span>
          <span className="navbar-user-info"><strong>{displayName}</strong></span>
          <ChevronDown className="navbar-user-chevron" size={15} aria-hidden="true" />
        </button>
        {isProfileOpen && (
          <div className="navbar-account-menu" id="navbar-account-menu" role="menu">
            <button
              className="navbar-logout"
              type="button"
              role="menuitem"
              onClick={onLogout}
            >
              <LogOut size={16} aria-hidden="true" />
              <span>Sair da conta</span>
            </button>
          </div>
        )}
      </div>
    </aside>
  )
}