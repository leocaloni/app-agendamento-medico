export default function LoginButton({ children, className = '', type = 'button' }) {
  return (
    <button className={`login-button ${className}`.trim()} type={type}>
      {children}
    </button>
  )
}