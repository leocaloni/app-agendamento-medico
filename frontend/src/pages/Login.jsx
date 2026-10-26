import LoginForm from '../components/LoginForm.jsx'
import TervisBrand from '../components/TervisBrand.jsx'
import './login.css'

export default function Login({ onLogin, onRegister, onAdminAccess, onPatientAccess, mode = 'patient' }) {
  const isAdmin = mode === 'admin'

  return (
    <main className="login-page">
      <div className="login-layout">
        <section className="login-mobile-header" aria-label="Tervis">
          <TervisBrand />
          <p>Cuidar de você ficou mais simples</p>
        </section>

        <section className="login-promo">
          <p className="login-promo-kicker">SAÚDE MAIS PERTO</p>
          <h1>Cuidar de você<br />ficou mais simples.</h1>
          <p className="login-promo-copy">
            Encontre médicos, marque consultas, veja avaliações e acesse seu histórico e exames, tudo em um só lugar.
          </p>
        </section>

        <section className="login-panel" aria-labelledby="login-heading">
          <div className="login-panel-content">
            <TervisBrand className="login-form-brand" />
            <h2 id="login-heading">{isAdmin ? 'Acesso administrativo' : 'Entre na sua conta'}</h2><br />
            <LoginForm
              onSubmit={onLogin}
              onRegister={onRegister}
              expectedRole={isAdmin ? 'ADMIN' : 'PATIENT'}
              showRegister={!isAdmin}
            />
            <p className="signup-prompt login-admin-entry">
              {isAdmin ? (
                <button className="login-text-button" type="button" onClick={onPatientAccess}>
                  Voltar ao acesso de paciente
                </button>
              ) : (
                <button className="login-text-button" type="button" onClick={onAdminAccess}>
                  Acesso administrativo
                </button>
              )}
            </p>
            <p className="login-terms">Ao continuar, você concorda com nossos termos de uso e política de privacidade.</p>
          </div>
        </section>
      </div>
    </main>
  )
}