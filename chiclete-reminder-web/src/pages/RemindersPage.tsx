import { useEffect, useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, type Reminder, type CreateReminder, getToken } from '../lib/api'

function toIsoLocal(d: string): string {
  if (!d) return ''
  if (d.length === 16) return `${d}:00`
  return d
}

export default function RemindersPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<Reminder[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [scheduledAt, setScheduledAt] = useState('2026-12-20T10:00')
  const [priority, setPriority] = useState<CreateReminder['priority']>('MEDIA')
  const [chewing, setChewing] = useState(false)
  const [intervalMinutes, setIntervalMinutes] = useState<number | ''>('')

  async function load() {
    setError(null)
    try {
      const data = await api<Reminder[]>('/reminders', { method: 'GET' })
      setItems(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!getToken()) {
      navigate('/login', { replace: true })
      return
    }
    void load()
  }, [navigate])

  function logout() {
    localStorage.removeItem('chiclete_token')
    navigate('/login', { replace: true })
  }

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setError(null)
    const body: CreateReminder = {
      title,
      description: description || undefined,
      scheduledAt: toIsoLocal(scheduledAt),
      priority,
      chewing,
      intervalMinutes: intervalMinutes === '' ? null : Number(intervalMinutes),
    }
    try {
      const created = await api<Reminder>('/reminders', {
        method: 'POST',
        body: JSON.stringify(body),
      })
      setItems((prev) => [created, ...prev])
      setTitle('')
      setDescription('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar')
    }
  }

  async function onComplete(id: number) {
    setError(null)
    try {
      const updated = await api<Reminder>(`/reminders/${id}/complete`, {
        method: 'PATCH',
      })
      setItems((prev) => prev.map((r) => (r.id === id ? updated : r)))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao concluir')
    }
  }

  return (
    <div className="card wide">
      <header className="row">
        <h1>Lembretes</h1>
        <button type="button" className="secondary" onClick={logout}>
          Sair
        </button>
      </header>

      <section>
        <h2>Novo lembrete</h2>
        <form onSubmit={onCreate} className="form grid">
          <label>
            Título
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </label>
          <label className="span2">
            Descrição
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </label>
          <label>
            Quando
            <input
              type="datetime-local"
              value={scheduledAt}
              onChange={(e) => setScheduledAt(e.target.value)}
              required
            />
          </label>
          <label>
            Prioridade
            <select
              value={priority}
              onChange={(e) =>
                setPriority(e.target.value as CreateReminder['priority'])
              }
            >
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </select>
          </label>
          <label>
            <input
              type="checkbox"
              checked={chewing}
              onChange={(e) => setChewing(e.target.checked)}
            />{' '}
            Modo chiclete
          </label>
          <label>
            Intervalo (min)
            <input
              type="number"
              min={1}
              value={intervalMinutes}
              onChange={(e) =>
                setIntervalMinutes(
                  e.target.value === '' ? '' : Number(e.target.value)
                )
              }
            />
          </label>
          <div className="span2">
            <button type="submit">Criar</button>
          </div>
        </form>
      </section>

      {error && <p className="err">{error}</p>}

      <section>
        <h2>Lista</h2>
        {loading ? (
          <p>Carregando…</p>
        ) : items.length === 0 ? (
          <p className="hint">Nenhum lembrete ainda.</p>
        ) : (
          <ul className="list">
            {items.map((r) => (
              <li key={r.id} className={r.completed ? 'done' : ''}>
                <div>
                  <strong>{r.title}</strong>
                  {r.description && <span> — {r.description}</span>}
                  <br />
                  <small>
                    {r.scheduledAt} · {r.priority}
                    {r.chewing ? ' · chiclete' : ''}
                    {r.completed ? ' · concluído' : ''}
                  </small>
                </div>
                {!r.completed && (
                  <button
                    type="button"
                    className="small"
                    onClick={() => void onComplete(r.id)}
                  >
                    Concluir
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
