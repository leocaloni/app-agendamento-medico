import { IdCard, LockKeyhole, Mail, UserRound } from 'lucide-react'
import LoginButton from '../components/LoginButton.jsx'
import LoginField from '../components/LoginField.jsx'
import TervisBrand from '../components/TervisBrand.jsx'
import './login.css'
import './cadastro.css'

export default function Cadastro({ onVoltar }) {
  return (
    <main className="login-page cadastro-page">
      <div className="login-layout cadastro-layout">
        <section className="login-mobile-header" aria-label="Tervis">
          <TervisBrand />
          <p>Cuidar de você ficou mais simples</p>
        </section>

        <section className="login-promo cadastro-promo">
          <p className="login-promo-kicker">SAÚDE MAIS PERTO</p>
          <h1>Seu cuidado<br />começa aqui.</h1>
          <p className="login-promo-copy">
            Crie sua conta para encontrar médicos e acompanhar sua saúde em um só lugar.
          </p>
        </section>

        <section className="login-panel cadastro-panel" aria-labelledby="cadastro-heading">
          <div className="login-panel-content cadastro-content">
            <TervisBrand className="login-form-brand" />
            <h2 id="cadastro-heading">Crie sua conta</h2>
            <p className="cadastro-intro">Preencha seus dados para começar.</p>

            <form className="login-form cadastro-form" onSubmit={(event) => event.preventDefault()}>
              <LoginField
                id="cadastro-nome"
                label="Nome completo"
                type="text"
                placeholder="Seu nome completo"
                autoComplete="name"
                icon={UserRound}
                required
                maxLength={255}
              />
              <LoginField
                id="cadastro-cpf"
                label="CPF"
                type="text"
                placeholder="11 dígitos, sem pontuação"
                autoComplete="off"
                icon={IdCard}
                required
                inputMode="numeric"
                pattern="[0-9]{11}"
                maxLength={11}
              />
              <LoginField
                id="cadastro-email"
                label="E-mail"
                type="email"
                placeholder="seu@email.com"
                autoComplete="email"
                icon={Mail}
                required
              />
              <LoginField
                id="cadastro-senha"
                label="Senha"
                type="password"
                placeholder="Mínimo de 8 caracteres"
                autoComplete="new-password"
                icon={LockKeyhole}
                required
                minLength={8}
                maxLength={72}
              />
              <LoginButton className="login-submit" type="submit">Criar conta</LoginButton>
            </form>

            <p className="signup-prompt cadastro-login-prompt">
              Já tem uma conta? <button className="login-text-button" type="button" onClick={onVoltar}>Voltar ao login</button>
            </p>
          </div>
        </section>
      </div>
    </main>
  )
}