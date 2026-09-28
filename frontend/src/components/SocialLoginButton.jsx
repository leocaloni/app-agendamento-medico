export default function SocialLoginButton({ provider, mark }) {
  return (
    <button className="social-login-button" type="button" aria-label={`Continuar com ${provider}`}>
      <span className={`social-login-mark social-login-mark-${mark}`} aria-hidden="true">
        {mark === 'google' ? 'G' : 'gov.br'}
      </span>
      <span>{provider}</span>
    </button>
  )
}