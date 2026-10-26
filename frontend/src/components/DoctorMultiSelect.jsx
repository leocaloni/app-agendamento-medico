import { useState } from 'react'
import { Search, X } from 'lucide-react'

function normalize(value) {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')
}

export default function DoctorMultiSelect({ id, label, options, value, onChange, placeholder }) {
  const [query, setQuery] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const selected = options.filter((option) => value.includes(option.id))
  const matches = options.filter((option) =>
    !value.includes(option.id) && normalize(option.name).includes(normalize(query)))

  return (
    <div className="admin-multi-select">
      <label className="admin-form-label" htmlFor={id}>{label}</label>
      <div className="admin-search-input">
        <Search size={17} aria-hidden="true" />
        <input
          id={id}
          type="search"
          role="combobox"
          aria-expanded={isOpen}
          aria-controls={`${id}-options`}
          aria-autocomplete="list"
          value={query}
          placeholder={placeholder}
          onFocus={() => setIsOpen(true)}
          onBlur={() => window.setTimeout(() => setIsOpen(false), 120)}
          onChange={(event) => {
            setQuery(event.target.value)
            setIsOpen(true)
          }}
        />
      </div>

      {selected.length > 0 && (
        <div className="admin-selected-options" aria-label={`${label} selecionadas`}>
          {selected.map((option) => (
            <span className="admin-selected-option" key={option.id}>
              {option.name}
              <button
                type="button"
                aria-label={`Remover ${option.name}`}
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => onChange(value.filter((idValue) => idValue !== option.id))}
              >
                <X size={14} aria-hidden="true" />
              </button>
            </span>
          ))}
        </div>
      )}

      {isOpen && (
        <div className="admin-search-options" id={`${id}-options`} role="listbox" aria-label={label}>
          {matches.length ? matches.map((option) => (
            <button
              className="admin-search-option"
              type="button"
              role="option"
              aria-selected="false"
              key={option.id}
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => {
                onChange([...value, option.id])
                setQuery('')
              }}
            >
              {option.name}
            </button>
          )) : <p className="admin-no-options">Nenhuma opção encontrada.</p>}
        </div>
      )}
    </div>
  )
}