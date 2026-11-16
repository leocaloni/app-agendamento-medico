import { useEffect, useState } from 'react'
import { Star } from 'lucide-react'
import Navbar from '../components/Navbar.jsx'
import { getMyReviews } from '../services/appointmentApi.js'
import './minhas-avaliacoes.css'

const CLINIC_TIME_ZONE = 'America/Sao_Paulo'

function formatDate(instant) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: CLINIC_TIME_ZONE,
    dateStyle: 'medium',
  }).format(new Date(instant))
}

export default function MinhasAvaliacoes({ user, onLogout, onNavigate }) {
  const [reviews, setReviews] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    getMyReviews(controller.signal)
      .then(setReviews)
      .catch((loadError) => {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsLoading(false)
      })
    return () => controller.abort()
  }, [])

  return (
    <div className="patient-app reviews-page">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="reviews-content">
        <header className="reviews-heading">
          <h1>Avaliações</h1>
          <span><Star size={15} aria-hidden="true" /> {reviews.length} registradas</span>
        </header>

        {error && <p className="reviews-message reviews-error" role="alert">{error}</p>}
        {isLoading ? (
          <p className="reviews-empty">Carregando avaliações...</p>
        ) : reviews.length === 0 ? (
          <p className="reviews-empty">Você ainda não enviou nenhuma avaliação.</p>
        ) : (
          <section className="reviews-list" aria-label="Minhas avaliações">
            {reviews.map((review) => (
              <article className="review-item" key={review.id}>
                <div className="review-item-heading">
                  <div>
                    <h2>{review.doctorName}</h2>
                    <span>Consulta em {formatDate(review.appointmentStartAt)}</span>
                  </div>
                  <time dateTime={review.createdAt}>Enviada em {formatDate(review.createdAt)}</time>
                </div>
                <div className="review-item-stars" aria-label={`${review.rating} de 5 estrelas`}>
                  {[1, 2, 3, 4, 5].map((value) => (
                    <Star key={value} size={16} fill={value <= review.rating ? 'currentColor' : 'none'} />
                  ))}
                </div>
                {review.comment && <p>{review.comment}</p>}
              </article>
            ))}
          </section>
        )}
      </main>
    </div>
  )
}
