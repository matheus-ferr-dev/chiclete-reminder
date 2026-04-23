import { useState, useEffect, FormEvent } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { api } from '../lib/api'

type LoginRes = { token: string; type: string }

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const loc = useLocation() as { state?: { email?: string } }

  useEffect(() => {
    if (loc.state?.email) setEmail(loc.state.email)
  }, [loc.state])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const data = await api<LoginRes>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      })
      localStorage.setItem('chiclete_token', data.token)
      navigate('/lembretes', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha no login')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card narrow">
      <h1>Entrar</h1>
      <form onSubmit={onSubmit} className="form">
        <label>
          E-mail
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />
        </label>
        <label>
          Senha
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>
        {error && <p className="err">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <p className="hint">
        <Link to="/cadastro">Criar conta</Link>
      </p>
    </div>
  )
}
