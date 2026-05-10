const TOKEN_KEY = "chiclete_token";

function $(id) {
  return document.getElementById(id);
}

function showMsg(el, text, type) {
  el.textContent = text;
  el.className = "msg show " + (type === "err" ? "err" : "ok");
}

function hideMsg(el) {
  el.className = "msg";
  el.textContent = "";
}

function decodeJwtPayload(token) {
  try {
    const part = token.split(".")[1];
    if (!part) return {};
    const b64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const pad = b64.length % 4 === 0 ? "" : "=".repeat(4 - (b64.length % 4));
    const json = atob(b64 + pad);
    return JSON.parse(json);
  } catch {
    return {};
  }
}

function emailFromToken() {
  const payload = decodeJwtPayload(getToken() || "");
  return payload.sub || "";
}

function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

function setToken(t) {
  sessionStorage.setItem(TOKEN_KEY, t);
}

function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY);
}

function authHeaders() {
  const t = getToken();
  const h = { "Content-Type": "application/json" };
  if (t) h["Authorization"] = "Bearer " + t;
  return h;
}

async function parseError(res) {
  try {
    const j = await res.json();
    if (j.detail) return j.detail;
    if (j.message) return j.message;
  } catch (_) {}
  return "Erro " + res.status;
}

function showAuth() {
  $("auth-root").classList.remove("hidden");
  $("app-root").classList.add("hidden");
}

function showApp(email) {
  $("auth-root").classList.add("hidden");
  $("app-root").classList.remove("hidden");
  $("user-email").textContent = email || "";
}

function switchTab(which) {
  $("tab-login").classList.toggle("active", which === "login");
  $("tab-register").classList.toggle("active", which === "register");
  $("panel-login").classList.toggle("active", which === "login");
  $("panel-register").classList.toggle("active", which === "register");
  hideMsg($("msg-auth"));
}

function toIsoLocal(dtLocal) {
  if (!dtLocal) return null;
  const d = new Date(dtLocal);
  if (Number.isNaN(d.getTime())) return null;
  const pad = (n) => String(n).padStart(2, "0");
  return (
    d.getFullYear() +
    "-" +
    pad(d.getMonth() + 1) +
    "-" +
    pad(d.getDate()) +
    "T" +
    pad(d.getHours()) +
    ":" +
    pad(d.getMinutes()) +
    ":00"
  );
}

async function doLogin(e) {
  e.preventDefault();
  const msg = $("msg-auth");
  hideMsg(msg);
  const email = $("login-email").value.trim();
  const password = $("login-password").value;
  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    const data = await res.json();
    setToken(data.token);
    showApp(email || emailFromToken());
    await loadReminders();
  } catch (err) {
    showMsg(msg, "Não foi possível conectar ao servidor.", "err");
  }
}

async function doRegister(e) {
  e.preventDefault();
  const msg = $("msg-auth");
  hideMsg(msg);
  const name = $("reg-name").value.trim();
  const email = $("reg-email").value.trim();
  const password = $("reg-password").value;
  try {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email, password }),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    const data = await res.json();
    setToken(data.token);
    showApp(email || emailFromToken());
    await loadReminders();
  } catch (err) {
    showMsg(msg, "Não foi possível conectar ao servidor.", "err");
  }
}

function formatSchedule(iso) {
  if (!iso) return "";
  try {
    const d = new Date(iso);
    return d.toLocaleString("pt-BR", {
      dateStyle: "short",
      timeStyle: "short",
    });
  } catch {
    return iso;
  }
}

async function loadReminders() {
  const ul = $("reminder-list");
  const empty = $("empty-state");
  ul.innerHTML = "";
  try {
    const res = await fetch("/api/reminders", { headers: authHeaders() });
    if (res.status === 401) {
      clearToken();
      showAuth();
      showMsg($("msg-auth"), "Sessão expirada. Entre novamente.", "err");
      return;
    }
    if (!res.ok) {
      empty.textContent = await parseError(res);
      empty.classList.remove("hidden");
      return;
    }
    const items = await res.json();
    if (!items.length) {
      empty.classList.remove("hidden");
      return;
    }
    empty.classList.add("hidden");
    for (const r of items) {
      const li = document.createElement("li");
      const chiclete = r.chewing ? " · Modo Chiclete" : "";
      const done = r.completed ? " (concluído)" : "";
      li.innerHTML =
        '<div class="title">' +
        escapeHtml(r.title) +
        done +
        '</div><div class="meta">' +
        escapeHtml(formatSchedule(r.scheduledAt)) +
        " · Prioridade: " +
        escapeHtml(r.priority) +
        chiclete +
        "</div>";
      ul.appendChild(li);
    }
  } catch {
    empty.textContent = "Erro ao carregar lembretes.";
    empty.classList.remove("hidden");
  }
}

function escapeHtml(s) {
  const d = document.createElement("div");
  d.textContent = s;
  return d.innerHTML;
}

async function doCreateReminder(e) {
  e.preventDefault();
  const msg = $("msg-app");
  hideMsg(msg);
  const title = $("nr-title").value.trim();
  const scheduledAt = toIsoLocal($("nr-when").value);
  const priority = $("nr-priority").value;
  const chewing = $("nr-chewing").checked;
  if (!title || !scheduledAt) {
    showMsg(msg, "Preencha título e data/hora.", "err");
    return;
  }
  const body = {
    title,
    description: null,
    scheduledAt,
    chewing,
    intervalMinutes: chewing ? 30 : null,
    priority,
  };
  try {
    const res = await fetch("/api/reminders", {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    $("nr-title").value = "";
    showMsg(msg, "Lembrete criado.", "ok");
    await loadReminders();
  } catch {
    showMsg(msg, "Falha ao criar lembrete.", "err");
  }
}

function doLogout() {
  clearToken();
  $("login-email").value = "";
  $("login-password").value = "";
  hideMsg($("msg-auth"));
  hideMsg($("msg-app"));
  showAuth();
}

document.addEventListener("DOMContentLoaded", () => {
  $("tab-login").addEventListener("click", () => switchTab("login"));
  $("tab-register").addEventListener("click", () => switchTab("register"));
  $("form-login").addEventListener("submit", doLogin);
  $("form-register").addEventListener("submit", doRegister);
  $("form-new").addEventListener("submit", doCreateReminder);
  $("btn-logout").addEventListener("click", doLogout);
  $("btn-refresh").addEventListener("click", loadReminders);

  const t = getToken();
  if (t) {
    showApp(emailFromToken());
    loadReminders();
  } else {
    showAuth();
  }
});
