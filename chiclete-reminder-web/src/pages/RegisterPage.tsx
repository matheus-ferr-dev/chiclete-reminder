import { useState, FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../lib/api'

type UserRes = { id: number; name: string; email: string; role: string }

export default function RegisterPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await api<UserRes>('/users', {
        method: 'POST',
        body: JSON.stringify({ name, email, password }),
      })
      navigate('/login', { state: { email } })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao cadastrar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card narrow">
      <h1>Cadastro</h1>
      <form onSubmit={onSubmit} className="form">
        <label>
          Nome
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            autoComplete="name"
          />
        </label>
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
          Senha (mín. 8)
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
            autoComplete="new-password"
          />
        </label>
        {error && <p className="err">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Enviando…' : 'Criar conta'}
        </button>
      </form>
      <p className="hint">
        Já tem conta? <Link to="/login">Entrar</Link>
      </p>
    </div>
  )
}
