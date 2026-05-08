import { Routes, Route, NavLink, useLocation } from 'react-router-dom'
import DumpBox from './pages/DumpBox'
import Results from './pages/Results'
import History from './pages/History'

function App() {
  const location = useLocation()

  return (
    <div className="app">
      <header className="top-header">
        <NavLink to="/" className="logo">
          <span className="logo-dot" />
          MindDump
        </NavLink>
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
