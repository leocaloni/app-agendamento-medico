export default function LoginButton({ children, className = '', type = 'button', disabled, onClick }) {
  return (
    <button
      className={`login-button ${className}`.trim()}
      type={type}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  )
}