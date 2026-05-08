import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App.jsx'
import './index.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
      <Toaster
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#FFFFFF',
            color: '#1A1A1F',
            border: '1px solid rgba(0,0,0,0.08)',
            borderRadius: '14px',
            fontFamily: "'Be Vietnam Pro', sans-serif",
            fontSize: '0.875rem',
            padding: '12px 16px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
          },
          success: {
            iconTheme: { primary: '#F97316', secondary: '#FFFFFF' },
          },
        }}
      />
    </BrowserRouter>
  </StrictMode>,
)
