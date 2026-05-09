import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Calendar, AlertCircle, Clock, Brain, Activity } from 'lucide-react'
import { dumpStorage } from '../storage'

function History() {
  const [dumps, setDumps] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setDumps(dumpStorage.getAll())
    setLoading(false)
  }, [])

  const formatDate = (dateStr) => {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const truncateText = (text, maxLen = 120) => {
    if (text.length <= maxLen) return text
    return text.substring(0, maxLen) + '...'
  }

  if (loading) {
    return (
      <div className="loading-view">
        <div className="loading-spinner" />
        <span className="loading-text">Loading history...</span>
      </div>
    )
  }

  return (
    <div className="history-view">
      <header className="history-header">
        <h1 className="history-header__title">Dump History</h1>
        <p className="history-header__count">
          {dumps.length} {dumps.length === 1 ? 'dump' : 'dumps'} total
        </p>
      </header>

      {dumps.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state__icon">
            <Brain size={48} strokeWidth={1} />
          </div>
          <p className="empty-state__text">
            No dumps yet. Your brain is either empty or too full.
          </p>
          <Link to="/" className="btn-primary">
            Make Your First Dump
          </Link>
        </div>
      ) : (
        <div className="history-list">
          {dumps.map((dump, index) => {
            const doFirstCount = (dump.doFirst || dump.urgent || []).length
            const doNextCount = (dump.doNext || dump.thisWeek || []).length
            const loadLevel = dump.cognitiveLoad?.level

            return (
              <Link
                key={dump.id}
                to={`/results/${dump.id}`}
                className="history-item"
                style={{ animationDelay: `${Math.min(index * 0.04, 0.24)}s` }}
              >
                <div className="history-item__num">
                  {String(index + 1).padStart(2, '0')}
                </div>
                <div className="history-item__body">
                  <div className="history-item__text">
                    {truncateText(dump.rawText)}
                  </div>
                  <div className="history-item__meta">
                    <span className="history-item__tag">
                      <Calendar size={12} /> {formatDate(dump.createdAt)}
                    </span>
                    {doFirstCount > 0 && (
                      <span className="history-item__tag history-item__tag--urgent">
                        <AlertCircle size={12} /> {doFirstCount} urgent
                      </span>
                    )}
                    {doNextCount > 0 && (
                      <span className="history-item__tag history-item__tag--week">
                        <Clock size={12} /> {doNextCount} next
                      </span>
                    )}
                    {loadLevel && (
                      <span className={`history-item__tag history-item__tag--load-${loadLevel}`}>
                        <Activity size={12} /> {loadLevel}
                      </span>
                    )}
                  </div>
                </div>
              </Link>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default History
