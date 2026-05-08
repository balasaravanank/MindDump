import { useState, useEffect } from 'react'
import { Routes, Route, NavLink, useLocation } from 'react-router-dom'
import DumpBox from './pages/DumpBox'
import Results from './pages/Results'
import History from './pages/History'

function App() {
  const location = useLocation()
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('minddump-theme') || 'light'
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('minddump-theme', theme)
  }, [theme])

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light')
  const isDark = theme === 'dark'

  return (
    <div className="app">
      <header className="top-header">
        <NavLink to="/" className="logo">
          <span className="logo-dot" />
          MindDump
        </NavLink>

        <button
          className="theme-toggle"
          onClick={toggleTheme}
          aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
          title={isDark ? 'Light mode' : 'Dark mode'}
        >
          {isDark ? (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="5"/>
              <line x1="12" y1="1" x2="12" y2="3"/>
              <line x1="12" y1="21" x2="12" y2="23"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
              <line x1="1" y1="12" x2="3" y2="12"/>
              <line x1="21" y1="12" x2="23" y2="12"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
          )}
        </button>

        <nav className="desktop-nav">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `nav-pill ${isActive && location.pathname === '/' ? 'nav-pill--active' : ''}`
            }
          >
            Dump
          </NavLink>
          <NavLink
            to="/history"
            className={({ isActive }) =>
              `nav-pill ${isActive ? 'nav-pill--active' : ''}`
            }
          >
            History
          </NavLink>
        </nav>
      </header>

      <main className="main-content">
        <Routes>
          <Route path="/" element={<DumpBox />} />
          <Route path="/results/:id" element={<Results />} />
          <Route path="/history" element={<History />} />
        </Routes>
      </main>

      <nav className="bottom-tabs">
        <NavLink
          to="/"
          end
          className={({ isActive }) =>
            `tab ${isActive && location.pathname === '/' ? 'tab--active' : ''}`
          }
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />
          </svg>
          <span>Dump</span>
        </NavLink>
        <NavLink
          to="/history"
          className={({ isActive }) =>
            `tab ${isActive ? 'tab--active' : ''}`
          }
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          <span>History</span>
        </NavLink>
      </nav>
    </div>
  )
}

export default App
