import { Routes, Route, NavLink, useLocation } from 'react-router-dom'
import DumpBox from './pages/DumpBox'
import Results from './pages/Results'
import History from './pages/History'

function App() {
  const location = useLocation()

  return (
    <div className="app-layout">
      <nav className="app-nav">
        <NavLink to="/" className="app-nav__logo">
          <span className="app-nav__logo-dot" />
          MindDump
        </NavLink>
        <div className="app-nav__links">
          <NavLink
            to="/"
            className={({ isActive }) =>
              `app-nav__link ${isActive && location.pathname === '/' ? 'app-nav__link--active' : ''}`
            }
            end
          >
            Dump
          </NavLink>
          <NavLink
            to="/history"
            className={({ isActive }) =>
              `app-nav__link ${isActive ? 'app-nav__link--active' : ''}`
            }
          >
            History
          </NavLink>
        </div>
      </nav>

      <main className="app-content">
        <Routes>
          <Route path="/" element={<DumpBox />} />
          <Route path="/results/:id" element={<Results />} />
          <Route path="/history" element={<History />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
