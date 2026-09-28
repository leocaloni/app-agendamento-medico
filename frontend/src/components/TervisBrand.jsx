import { Heart } from 'lucide-react'

export default function TervisBrand({ className = '' }) {
  return (
    <div className={`tervis-brand ${className}`.trim()}>
      <span className="tervis-logo-mark" aria-hidden="true">
        <Heart size={29} strokeWidth={1.9} />
      </span>
      <span className="tervis-brand-name">Tervis</span>
    </div>
  )
}