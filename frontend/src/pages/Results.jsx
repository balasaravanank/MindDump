import { useState, useEffect, useMemo } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  Zap, ChevronLeft, ChevronDown, Brain, FileText,
  Briefcase, User, Users, Wrench, Palette, Heart, DollarSign, ClipboardList, Activity
} from 'lucide-react'
import toast from 'react-hot-toast'
import { dumpStorage } from '../storage'
import ExportBar from '../components/ExportBar'

const PRIORITY_MAP = {
  doFirst: { label: 'Do First', order: 0, color: 'urgent' },
  doNext:  { label: 'Do Next',  order: 1, color: 'week' },
  later:   { label: 'Later',    order: 2, color: 'someday' },
  capture: { label: 'Capture',  order: 3, color: 'ideas' },
}

const TYPE_ICONS = {
  work: Briefcase,
  personal: User,
  family: Users,
  maintenance: Wrench,
  creative: Palette,
  health: Heart,
  financial: DollarSign,
  administrative: ClipboardList,
}

function CognitiveLoadMeter({ cognitiveLoad }) {
  if (!cognitiveLoad || !cognitiveLoad.score) return null

  const { score, level } = cognitiveLoad
  const levelColors = {
    low: 'var(--green)',
    medium: 'var(--amber)',
    high: 'var(--accent)',
    overloaded: 'var(--red)',
  }
  const color = levelColors[level] || 'var(--accent)'

  return (
    <div className="cognitive-meter">
      <div className="cognitive-meter__header">
        <Activity size={14} />
        <span className="cognitive-meter__label">Cognitive Load</span>
        <span className="cognitive-meter__level" style={{ color }}>{level}</span>
      </div>
      <div className="cognitive-meter__track">
        <div
          className="cognitive-meter__fill"
          style={{ width: `${Math.min(score, 100)}%`, background: color }}
        />
      </div>
      <span className="cognitive-meter__score">{score}/100</span>
    </div>
  )
}

function Results() {
  const { id } = useParams()
  const [dump, setDump] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [completedItems, setCompletedItems] = useState(new Set())
  const [showRaw, setShowRaw] = useState(false)
  const [showDone, setShowDone] = useState(true)
  const [expandedTask, setExpandedTask] = useState(null)

  useEffect(() => {
    const found = dumpStorage.getById(id)
    if (found) {
      setDump(found)
      if (found.completedItems) {
        setCompletedItems(new Set(found.completedItems))
      }
    } else {
      setError('Could not load this dump.')
    }
    setLoading(false)
  }, [id])

  const allTasks = useMemo(() => {
    if (!dump) return []
    const tasks = []
    for (const [key, config] of Object.entries(PRIORITY_MAP)) {
      const items = dump[key] || []
      items.forEach((item, i) => {
        const taskText = typeof item === 'string' ? item : item.task
        tasks.push({
          id: `${key}-${i}`,
          text: taskText,
          reason: item.reason || '',
          urgencyScore: item.urgencyScore || 0,
          cognitiveType: item.cognitiveType || '',
          priority: key,
          ...config,
        })
      })
    }
    return tasks
  }, [dump])

  const pendingTasks = useMemo(() =>
    allTasks.filter(t => !completedItems.has(t.text)), [allTasks, completedItems])

  const doneTasks = useMemo(() =>
    allTasks.filter(t => completedItems.has(t.text)), [allTasks, completedItems])

  const sections = useMemo(() => {
    const groups = {}
    for (const task of pendingTasks) {
      if (!groups[task.priority]) {
        groups[task.priority] = { ...PRIORITY_MAP[task.priority], priority: task.priority, tasks: [] }
      }
      groups[task.priority].tasks.push(task)
    }
    return Object.values(groups).sort((a, b) => a.order - b.order)
  }, [pendingTasks])

  const progress = allTasks.length > 0
    ? Math.round((doneTasks.length / allTasks.length) * 100)
    : 0

  const circumference = 2 * Math.PI * 34
  const offset = circumference * (1 - progress / 100)

  const urgentPending = (dump?.doFirst || []).filter(t => {
    const text = typeof t === 'string' ? t : t.task
    return !completedItems.has(text)
  }).length

  const handleToggle = (task) => {
    const wasCompleted = completedItems.has(task.text)
    const nextCompleted = new Set(completedItems)

    if (nextCompleted.has(task.text)) {
      nextCompleted.delete(task.text)
    } else {
      nextCompleted.add(task.text)
    }

    setCompletedItems(nextCompleted)
    dumpStorage.updateDump(id, { completedItems: [...nextCompleted] })

    if (!wasCompleted) {
      toast.success('Done!', { icon: '✓', duration: 1200 })
    }
  }

  if (loading) {
    return (
      <div className="loading-view">
        <div className="loading-spinner" />
        <span className="loading-text">Loading your tasks...</span>
      </div>
    )
  }

  if (error || !dump) {
    return (
      <div className="results-view">
        <p style={{ color: 'var(--red)' }}>{error || 'Dump not found.'}</p>
        <Link to="/" className="back-link">
          <ChevronLeft size={16} />
          Back to dump
        </Link>
      </div>
    )
  }

  const TypeIcon = ({ type }) => {
    const Icon = TYPE_ICONS[type]
    return Icon ? <Icon size={12} /> : null
  }

  return (
    <div className="results-view">
      <Link to="/" className="back-link">
        <ChevronLeft size={16} />
        New dump
      </Link>

      {/* ── Progress Header ── */}
      <header className="focus-header">
        <div className="focus-header__info">
          <div className="focus-header__label">Your Focus</div>
          <h1 className="focus-header__title">
            {progress === 100 ? 'All done!' : `${pendingTasks.length} task${pendingTasks.length !== 1 ? 's' : ''} remaining`}
          </h1>
          <p className="focus-header__sub">
            {doneTasks.length} of {allTasks.length} completed
          </p>
        </div>
        <div className="progress-ring-wrap">
          <svg className="progress-ring" viewBox="0 0 80 80">
            <circle className="progress-ring__bg" cx="40" cy="40" r="34" />
            <circle
              className="progress-ring__fill"
              cx="40" cy="40" r="34"
              style={{ strokeDasharray: circumference, strokeDashoffset: offset }}
            />
          </svg>
          <span className="progress-ring__text">{progress}%</span>
        </div>
      </header>

      {/* ── Cognitive Load Meter ── */}
      <CognitiveLoadMeter cognitiveLoad={dump.cognitiveLoad} />

      {/* ── Focus Alert ── */}
      {urgentPending > 0 && (
        <div className="focus-alert">
          <Zap size={16} className="focus-alert__icon" />
          <span>{urgentPending} urgent task{urgentPending !== 1 ? 's' : ''} need your attention right now</span>
        </div>
      )}

      {/* ── Raw dump toggle ── */}
      <button className="raw-toggle" onClick={() => setShowRaw(!showRaw)}>
        <span className="raw-toggle__left">
          <FileText size={14} />
          Original brain dump
        </span>
        <ChevronDown size={16} className={showRaw ? 'raw-toggle__chevron--open' : ''} />
      </button>
      {showRaw && <div className="raw-content">{dump.rawText}</div>}

      {/* ── Desktop: Two-column layout ── */}
      <div className="results-body">
        {/* ── Priority Flow ── */}
        <div className="task-flow">
          {sections.map((section, si) => (
            <div key={section.priority} className="task-section" style={{ animationDelay: `${si * 0.06}s` }}>
              <div className={`task-section__head task-section__head--${section.color}`}>
                <span className="task-section__line" />
                <span className="task-section__label">{section.label}</span>
                <span className="task-section__count">{section.tasks.length}</span>
              </div>
              <div className="task-section__list">
                {section.tasks.map((task, ti) => (
                  <div key={task.id} className="task-card" style={{ animationDelay: `${(si * 0.06) + (ti * 0.04)}s` }}>
                    <button
                      className="task-item"
                      onClick={() => handleToggle(task)}
                    >
                      <span className="task-check" />
                      <span className="task-item__text">{task.text}</span>
                      {task.cognitiveType && (
                        <span className={`task-type task-type--${task.cognitiveType}`}>
                          <TypeIcon type={task.cognitiveType} />
                          {task.cognitiveType}
                        </span>
                      )}
                    </button>
                    {task.reason && (
                      <button
                        className="task-reason-toggle"
                        onClick={() => setExpandedTask(expandedTask === task.id ? null : task.id)}
                      >
                        {expandedTask === task.id ? 'Hide reason' : 'Why?'}
                      </button>
                    )}
                    {expandedTask === task.id && task.reason && (
                      <div className="task-reason">{task.reason}</div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}

          {pendingTasks.length === 0 && allTasks.length > 0 && (
            <div className="task-empty-state">
              <div className="task-empty-state__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="var(--green)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" width="48" height="48">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" />
                </svg>
              </div>
              <p>You crushed everything. Nice work.</p>
            </div>
          )}
        </div>

        {/* ── Sidebar: Insight + Done + Export ── */}
        <aside className="results-sidebar">
          {/* ── Insight ── */}
          {dump.insight && (
            <div className="insight-card">
              <div className="insight-card__label">
                <Brain size={14} /> Honest Insight
              </div>
              <p className="insight-card__text">{dump.insight}</p>
            </div>
          )}

          {/* ── Done Section ── */}
          {doneTasks.length > 0 && (
            <div className="done-section">
              <button className="done-section__toggle" onClick={() => setShowDone(!showDone)}>
                <span className="done-section__label">
                  Completed
                  <span className="done-section__count">{doneTasks.length}</span>
                </span>
                <ChevronDown size={14} className={showDone ? 'done-section__chevron--open' : ''} />
              </button>
              {showDone && (
                <div className="done-section__list">
                  {doneTasks.map((task) => (
                    <button
                      key={task.id}
                      className="task-item task-item--done"
                      onClick={() => handleToggle(task)}
                    >
                      <span className="task-check task-check--done" />
                      <span className="task-item__text">{task.text}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          <ExportBar dump={dump} />
        </aside>
      </div>
    </div>
  )
}

export default Results
