import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { dumpApi } from '../api'
import ExportBar from '../components/ExportBar'

const CATEGORIES = [
  { key: 'urgent', label: 'Urgent', icon: '🔴', className: 'category-card--urgent' },
  { key: 'thisWeek', label: 'This Week', icon: '🟡', className: 'category-card--week' },
  { key: 'someday', label: 'Someday', icon: '🟢', className: 'category-card--someday' },
  { key: 'ideas', label: 'Ideas', icon: '💡', className: 'category-card--ideas' },
]

function Results() {
  const { id } = useParams()
  const [dump, setDump] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchDump = async () => {
      try {
        const response = await dumpApi.getDumpById(id)
        setDump(response.data)
      } catch (err) {
        console.error('Failed to fetch dump:', err)
        setError('Could not load this dump.')
      } finally {
        setLoading(false)
      }
    }
    fetchDump()
  }, [id])

  if (loading) {
    return (
      <div className="loading-state">
        <div className="loading-spinner" />
        <span className="loading-text">Loading clarity...</span>
      </div>
    )
  }

  if (error || !dump) {
    return (
      <div className="results-page">
        <p style={{ color: 'var(--accent-red)' }}>{error || 'Dump not found.'}</p>
        <Link to="/" className="back-link">← Back to dump</Link>
      </div>
    )
  }

  return (
    <div className="results-page">
      <Link to="/" className="back-link">← New dump</Link>

      <div className="results-header">
        <div className="results-header__label">Your clarity report</div>
        <h1 className="results-header__title">Here's what's on your mind</h1>
        <div className="results-header__raw">{dump.rawText}</div>
      </div>

      <div className="results-grid">
        {CATEGORIES.map(({ key, label, icon, className }) => (
          <div key={key} className={`category-card ${className}`}>
            <div className="category-card__header">
              <div className="category-card__icon">{icon}</div>
              <h2 className="category-card__title">{label}</h2>
            </div>
            {dump[key] && dump[key].length > 0 ? (
              <ul className="category-card__items">
                {dump[key].map((item, i) => (
                  <li key={i} className="category-card__item">{item}</li>
                ))}
              </ul>
            ) : (
              <p className="category-card__empty">Nothing here — that's a good thing.</p>
            )}
          </div>
        ))}
      </div>

      {dump.insight && (
        <div className="insight-card">
          <div className="insight-card__label">
            <span>🧠</span> Honest Insight
          </div>
          <p className="insight-card__text">{dump.insight}</p>
        </div>
      )}

      <ExportBar dump={dump} />
    </div>
  )
}

export default Results
