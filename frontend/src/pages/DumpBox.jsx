import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { dumpApi } from '../api'

function DumpBox() {
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async () => {
    if (!text.trim()) {
      toast.error('Write something first. Let it all out.')
      return
    }

    setLoading(true)
    try {
      const response = await dumpApi.createDump(text.trim())
      const dumpId = response.data.id
      toast.success('Mind organized.')
      navigate(`/results/${dumpId}`)
    } catch (err) {
      console.error('Dump failed:', err)
      toast.error('Something went wrong. Try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      handleSubmit()
    }
  }

  return (
    <div className="dump-page">
      <header className="dump-page__header">
        <h1 className="dump-page__title">
          Dump your <span className="dump-page__title-accent">mind</span>
        </h1>
        <p className="dump-page__subtitle">
          No rules. No structure. Just type everything on your mind.
        </p>
      </header>

      <div className="dump-box-container">
        <textarea
          id="dump-textarea"
          className="dump-box"
          placeholder="i have a hackathon tomorrow, haven't slept, client owes me money, need to study for exam friday, also want to start a youtube channel but idk when, and i keep forgetting to call my mom..."
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading}
          autoFocus
        />

        <div className="dump-box-footer">
          <span className="dump-box-footer__count">
            {text.length > 0 ? `${text.length} characters` : 'Start typing...'}
          </span>
          <button
            id="organize-btn"
            className={`dump-btn ${loading ? 'dump-btn--loading' : ''}`}
            onClick={handleSubmit}
            disabled={loading || !text.trim()}
          >
            {loading ? 'Organizing...' : 'Organize My Mind →'}
          </button>
        </div>

        {!loading && (
          <div style={{
            textAlign: 'center',
            marginTop: '12px',
            fontSize: '0.6875rem',
            color: 'var(--text-muted)',
            letterSpacing: '0.05em'
          }}>
            Ctrl + Enter to submit
          </div>
        )}
      </div>
    </div>
  )
}

export default DumpBox
