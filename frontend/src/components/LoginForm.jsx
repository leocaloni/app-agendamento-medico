import { LockKeyhole, Mail } from 'lucide-react'
import LoginButton from './LoginButton.jsx'
import LoginField from './LoginField.jsx'
import SocialLoginButton from './SocialLoginButton.jsx'

export default function LoginForm({ onSubmit }) {
  return (
    <form
      className="login-form"
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit?.()
      }}
    >
      <LoginField
        id="email"
        label="E-mail"
        type="email"
        placeholder="seu@email.com"
        autoComplete="email"
        icon={Mail}
      />
      <LoginField
        id="password"
        label="Senha"
        type="password"
        placeholder="Digite sua senha"
        autoComplete="current-password"
        icon={LockKeyhole}
      />

      <button className="login-text-button forgot-password" type="button">
        Esqueci minha senha
      </button>

      <LoginButton className="login-submit" type="submit">Entrar</LoginButton>

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
        Não tem conta? <button className="login-text-button" type="button">Cadastre-se</button>
      </p>
    </form>
  )
}