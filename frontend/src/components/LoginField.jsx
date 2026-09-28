export default function LoginField({ id, label, type, placeholder, icon: Icon, autoComplete }) {
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
        />
      </div>
    </div>
  )
}