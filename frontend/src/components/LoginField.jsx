export default function LoginField({
  id,
  label,
  type,
  placeholder,
  icon: Icon,
  autoComplete,
  required,
  minLength,
  maxLength,
  pattern,
  inputMode,
}) {
  function setValidationMessage(event) {
    const input = event.currentTarget
    const { validity } = input

    if (validity.valueMissing) {
      input.setCustomValidity('Preencha este campo.')
    } else if (validity.typeMismatch && input.type === 'email') {
      input.setCustomValidity('Digite um endereço de e-mail válido.')
    } else if (validity.patternMismatch && input.id === 'cadastro-cpf') {
      input.setCustomValidity('Informe os 11 números do CPF, sem pontuação.')
    } else if (validity.tooShort) {
      input.setCustomValidity(`Digite pelo menos ${input.minLength} caracteres.`)
    } else {
      input.setCustomValidity('')
    }
  }

  function clearValidationMessage(event) {
    event.currentTarget.setCustomValidity('')
  }

  return (
    <div className="login-field-group">
      <label className="login-label" htmlFor={id}>{label}</label>
      <div className="login-input-wrap">
        <Icon size={17} strokeWidth={1.8} aria-hidden="true" />
        <input
          id={id}
          name={id}
          type={type}
          placeholder={placeholder}
          autoComplete={autoComplete}
          required={required}
          minLength={minLength}
          maxLength={maxLength}
          pattern={pattern}
          inputMode={inputMode}
          onInvalid={setValidationMessage}
          onInput={clearValidationMessage}
        />
      </div>
    </div>
  )
}