import { useState } from 'react'
import { LockKeyhole, Mail } from 'lucide-react'
import LoginButton from './LoginButton.jsx'
import LoginField from './LoginField.jsx'
import SocialLoginButton from './SocialLoginButton.jsx'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function LoginForm({ onSubmit, onRegister }) {
  const [credentials, setCredentials] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(event) {
    const { name, value } = event.target
    setCredentials((previous) => ({ ...previous, [name]: value }))
    if (error) setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: credentials.email.trim().toLowerCase(),
          password: credentials.password,
        }),
      })
      const data = await response.json().catch(() => null)

      if (!response.ok) {
        throw new Error(data?.message || 'E-mail ou senha inválidos.')
      }
      if (data?.user?.role !== 'PATIENT') {
        throw new Error('Esta área de acesso é exclusiva para pacientes.')
      }

      onSubmit?.(data)
    } catch (submitError) {
      setError(submitError.message || 'Não foi possível conectar ao servidor.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <LoginField
        id="email"
        name="email"
        label="E-mail"
        type="email"
        placeholder="seu@email.com"
        autoComplete="email"
        icon={Mail}
        required
        value={credentials.email}
        onChange={updateField}
      />
      <LoginField
        id="password"
        name="password"
        label="Senha"
        type="password"
        placeholder="Digite sua senha"
        autoComplete="current-password"
        icon={LockKeyhole}
        required
        value={credentials.password}
        onChange={updateField}
      />

      {error && <p className="login-error" role="alert">{error}</p>}

      <button className="login-text-button forgot-password" type="button">
        Esqueci minha senha
      </button>

      <LoginButton className="login-submit" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Entrando...' : 'Entrar'}
      </LoginButton>

      <div className="social-login-section">
        <div className="login-divider" aria-hidden="true">
          <span />
          <span>ou continue com</span>
          <span />
        </div>
        <div className="social-login-options">
          <SocialLoginButton provider="Google" mark="google" />
          <SocialLoginButton provider="Gov.br" mark="gov" />
        </div>
      </div>

      <p className="signup-prompt">
        Não tem conta? <button className="login-text-button" type="button" onClick={onRegister}>Cadastre-se</button>
      </p>
    </form>
  )
}