import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import RemindersPage from './pages/RemindersPage'
import { getToken } from './lib/api'
import type { ReactNode } from 'react'
import './App.css'

function PrivateRoute({ children }: { children: ReactNode }) {
  if (!getToken()) return <Navigate to="/login" replace />
  return <>{children}</>
}

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <header className="top">
          <a href="/" className="brand">
            Chiclete Reminder
          </a>
        </header>
        <main>
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/cadastro" element={<RegisterPage />} />
            <Route
              path="/lembretes"
              element={
                <PrivateRoute>
                  <RemindersPage />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App
