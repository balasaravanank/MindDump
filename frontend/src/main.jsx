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
        position="bottom-right"
        toastOptions={{
          style: {
            background: '#141416',
            color: '#E8E4DF',
            border: '1px solid rgba(255,107,53,0.3)',
            fontFamily: 'Inter, sans-serif',
          },
        }}
      />
    </BrowserRouter>
  </StrictMode>,
)
