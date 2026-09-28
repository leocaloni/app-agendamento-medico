import LoginForm from '../components/LoginForm.jsx'
import TervisBrand from '../components/TervisBrand.jsx'
import './login.css'

export default function Login() {
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
            <h2 id="login-heading">Entre na sua conta</h2><br />
            <LoginForm />
            <p className="login-terms">Ao continuar, você concorda com nossos termos de uso e política de privacidade.</p>
          </div>
        </section>
      </div>
    </main>
  )
}