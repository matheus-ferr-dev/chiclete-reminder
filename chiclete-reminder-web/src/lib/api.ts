const baseUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function getToken(): string | null {
  return localStorage.getItem('chiclete_token');
}

function authHeaders(): Record<string, string> {
  const t = getToken();
  if (!t) return {};
  return { Authorization: `Bearer ${t}` };
}

export async function api<T>(
  path: string,
  init?: RequestInit
): Promise<T> {
  const res = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...init?.headers,
    },
  });
  if (!res.ok) {
    const text = await res.text();
    let msg = text || res.statusText;
    try {
      const j = JSON.parse(text) as { message?: string };
      if (j?.message) msg = j.message;
    } catch {
      // ignore
    }
    throw new Error(msg || `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export { baseUrl, getToken };

export type Reminder = {
  id: number;
  title: string;
  description: string | null;
  scheduledAt: string;
  priority: 'BAIXA' | 'MEDIA' | 'ALTA';
  chewing: boolean;
  intervalMinutes: number | null;
  ignoreCount: number;
  completed: boolean;
};

export type CreateReminder = {
  title: string;
  description?: string;
  scheduledAt: string;
  priority: 'BAIXA' | 'MEDIA' | 'ALTA';
  chewing: boolean;
  intervalMinutes?: number | null;
};
