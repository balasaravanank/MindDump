import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { dumpApi } from '../api'

function History() {
  const [dumps, setDumps] = useState([])
  const [loading, setLoading] = useState(true)
  const [patternInsight, setPatternInsight] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [dumpsRes, patternRes] = await Promise.all([
          dumpApi.getAllDumps(),
          dumpApi.getPatternInsight(),
        ])
        setDumps(dumpsRes.data)
        if (patternRes.data.insight) {
          setPatternInsight(patternRes.data.insight)
        }
      } catch (err) {
        console.error('Failed to fetch history:', err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
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
      <div className="loading-state">
        <div className="loading-spinner" />
        <span className="loading-text">Loading history...</span>
      </div>
    )
  }

  return (
    <div className="history-page">
      <div className="history-header">
        <h1 className="history-header__title">Dump History</h1>
        <p className="history-header__count">
          {dumps.length} {dumps.length === 1 ? 'dump' : 'dumps'} total
        </p>
      </div>

      {patternInsight && (
        <div className="pattern-card">
          <div className="pattern-card__label">🔍 Pattern Detected</div>
          <p className="pattern-card__text">{patternInsight}</p>
          <p className="pattern-card__note">
            Based on analysis of your last {dumps.length} dumps
          </p>
        </div>
      )}

      {dumps.length === 0 ? (
        <div className="history-empty">
          <div className="history-empty__icon">🧠</div>
          <p className="history-empty__text">
            No dumps yet. Your brain is either empty or too full.
          </p>
          <div className="history-empty__cta">
            <Link to="/" className="dump-btn">
              Make Your First Dump →
            </Link>
          </div>
        </div>
      ) : (
        <div className="history-list">
          {dumps.map((dump, index) => (
            <Link
              key={dump.id}
              to={`/results/${dump.id}`}
              className="history-item"
              style={{ animationDelay: `${Math.min(index * 0.05, 0.3)}s` }}
            >
              <div className="history-item__index">
                {String(index + 1).padStart(2, '0')}
              </div>
              <div className="history-item__content">
                <div className="history-item__text">
                  {truncateText(dump.rawText)}
                </div>
                <div className="history-item__meta">
                  <span className="history-item__tag">
                    📅 {formatDate(dump.createdAt)}
                  </span>
                  {dump.urgent && dump.urgent.length > 0 && (
                    <span className="history-item__tag">
                      🔴 {dump.urgent.length} urgent
                    </span>
                  )}
                  {dump.thisWeek && dump.thisWeek.length > 0 && (
                    <span className="history-item__tag">
                      🟡 {dump.thisWeek.length} this week
                    </span>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default History
