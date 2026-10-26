import { useState } from 'react'
import { CalendarDays, IdCard, LockKeyhole, Mail, Phone, UserRound } from 'lucide-react'
import LoginButton from '../components/LoginButton.jsx'
import LoginField from '../components/LoginField.jsx'
import TervisBrand from '../components/TervisBrand.jsx'
import './login.css'
import './cadastro.css'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const initialForm = {
  fullName: '',
  cpf: '',
  email: '',
  password: '',
  phone: '',
  birthDate: '',
}

export default function Cadastro({ onVoltar }) {
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCreated, setIsCreated] = useState(false)

  function updateField(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (error) setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')

    const payload = {
      fullName: form.fullName.trim(),
      cpf: form.cpf.replace(/\D/g, ''),
      email: form.email.trim().toLowerCase(),
      password: form.password,
      phone: form.phone.replace(/\D/g, '') || null,
      birthDate: form.birthDate,
      healthPlanId: null,
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      const data = await response.json().catch(() => null)

      if (!response.ok) {
        const message = data?.message || data?.error || 'Não foi possível criar a conta.'
        throw new Error(message)
      }

      setIsCreated(true)
    } catch (submitError) {
      setError(submitError.message || 'Não foi possível criar a conta.')
    } finally {
      setIsSubmitting(false)
    }
  }

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
            {isCreated ? (
              <div role="status" aria-live="polite">
                <h2 id="cadastro-heading">Conta criada com sucesso!</h2>
                <p className="cadastro-intro">
                  Seu cadastro foi concluído. Entre com seu e-mail e senha para acessar sua conta.
                </p>
                <LoginButton className="login-submit" type="button" onClick={onVoltar}>
                  Ir para login
                </LoginButton>
              </div>
            ) : (
              <>
                <h2 id="cadastro-heading">Crie sua conta</h2>
                <p className="cadastro-intro">Preencha seus dados para começar.</p>

                <form className="login-form cadastro-form" onSubmit={handleSubmit} noValidate>
              <LoginField
                id="cadastro-nome"
                name="fullName"
                label="Nome completo"
                type="text"
                placeholder="Seu nome completo"
                autoComplete="name"
                icon={UserRound}
                required
                maxLength={255}
                value={form.fullName}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-cpf"
                name="cpf"
                label="CPF"
                type="text"
                placeholder="11 dígitos, sem pontuação"
                autoComplete="off"
                icon={IdCard}
                required
                inputMode="numeric"
                pattern="[0-9]{11}"
                maxLength={11}
                value={form.cpf}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-email"
                name="email"
                label="E-mail"
                type="email"
                placeholder="seu@email.com"
                autoComplete="email"
                icon={Mail}
                required
                value={form.email}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-senha"
                name="password"
                label="Senha"
                type="password"
                placeholder="Mínimo de 8 caracteres"
                autoComplete="new-password"
                icon={LockKeyhole}
                required
                minLength={8}
                maxLength={72}
                value={form.password}
                onChange={updateField}
              />
              <LoginField
                id="cadastro-telefone"
                name="phone"
                label="Telefone (opcional)"
                type="tel"
                placeholder="(11) 99999-9999"
                autoComplete="tel"
                icon={Phone}
                value={form.phone}
                onChange={updateField}
                maxLength={20}
              />
              <LoginField
                id="cadastro-data-nascimento"
                name="birthDate"
                label="Data de nascimento"
                type="date"
                placeholder="dd/mm/aaaa"
                autoComplete="bday"
                icon={CalendarDays}
                required
                max={new Date().toISOString().split('T')[0]}
                value={form.birthDate}
                onChange={updateField}
              />

              {error && (
                <p role="alert" style={{ margin: 0, color: '#c82828', fontSize: 12 }}>
                  {error}
                </p>
              )}

              <LoginButton className="login-submit" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Criando conta...' : 'Criar conta'}
              </LoginButton>
                </form>

                <p className="signup-prompt cadastro-login-prompt">
                  Já tem uma conta? <button className="login-text-button" type="button" onClick={onVoltar}>Voltar ao login</button>
                </p>
              </>
            )}
          </div>
        </section>
      </div>
    </main>
  )
}