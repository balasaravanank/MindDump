import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { dumpApi } from '../api'
import { dumpStorage } from '../storage'

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
      const ai = response.data

      // Save to localStorage (private per browser)
      const saved = dumpStorage.save({
        rawText: text.trim(),
        doFirst: ai.doFirst || [],
        doNext: ai.doNext || [],
        later: ai.later || [],
        capture: ai.capture || [],
        insight: ai.insight || '',
        cognitiveLoad: ai.cognitiveLoad || { score: 0, level: 'low' },
        completedItems: [],
      })

      toast.success('Mind organized.')
      navigate(`/results/${saved.id}`)
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
    <div className="dump-view">
      <div className="dump-inner">
        <header className="dump-hero">
          <h1 className="dump-hero__title">
            Dump your <span className="dump-hero__accent">mind</span>
          </h1>
          <p className="dump-hero__sub">
            No rules. No structure. Just type everything on your mind.
          </p>
        </header>

        <div className="dump-editor">
          <textarea
            id="dump-textarea"
            className="dump-editor__input"
            placeholder="i have a hackathon tomorrow, haven't slept, client owes me money, need to study for exam friday, also want to start a youtube channel but idk when..."
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
            autoFocus
          />

          <div className="dump-editor__bar">
            <span className="dump-editor__count">
              {text.length > 0 ? `${text.length} chars` : ''}
            </span>
            <button
              id="organize-btn"
              className={`btn-primary ${loading ? 'btn-primary--loading' : ''}`}
              onClick={handleSubmit}
              disabled={loading || !text.trim()}
            >
              {loading ? (
                <>
                  <span className="btn-spinner" />
                  Analyzing...
                </>
              ) : (
                'Organize my mind'
              )}
            </button>
          </div>

          {!loading && text.length === 0 && (
            <p className="dump-editor__hint">Ctrl + Enter to submit</p>
          )}
        </div>
      </div>
    </div>
  )
}

export default DumpBox
