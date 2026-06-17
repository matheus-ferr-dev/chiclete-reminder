const TOKEN_KEY = "chiclete_token";

let allReminders = [];
let allGroups = [];
let allGroupInvites = [];
let groupSentInvites = {};
let expandedGroups = new Set();
let unreadNotifications = [];
let pendingInviteToken = null;
let userProfile = null;
let chicleteStats = null;
let audioCtx = null;
let currentFilter = "all";
let searchQuery = "";
let currentView = "reminders";
let loaderCount = 0;
let pollTimer = null;
const toastDismissTimers = new Map();
const SPLASH_MIN_MS = 2600;
const SPLASH_EXIT_MS = 550;
const sheetHomes = new Map();
const NOTIFICATION_POLL_MS = 30000;

const PRIORITY_LABELS = {
  BAIXA: "Baixa",
  MEDIA: "Média",
  ALTA: "Alta",
  URGENTE: "Urgente",
};

const VIEW_TITLES = {
  reminders: "Lembretes",
  groups: "Grupos",
  profile: "Perfil",
};

const RECURRENCE_DAY_NAMES = {
  1: "Seg",
  2: "Ter",
  3: "Qua",
  4: "Qui",
  5: "Sex",
  6: "Sáb",
  7: "Dom",
};

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

function toastIcon(text, kind) {
  if (kind === "err") return "!";
  if (/breve/i.test(text)) return "⏳";
  if (/alerta|Chiclete|🫧/i.test(text)) return "🫧";
  if (/concluí|feito|🎉/i.test(text)) return "✓";
  if (/criado|guardado|adicionado|partilh|grupo/i.test(text)) return "★";
  if (/Adiado|adiado/i.test(text)) return "⏰";
  if (/ignor/i.test(text)) return "👀";
  if (/tranquilo|pendente/i.test(text)) return "😌";
  return "🫧";
}

function findVisibleToast(text, kind) {
  const container = $("toast-container");
  if (!container) return null;
  return (
    [...container.querySelectorAll(`.toast.${kind}`)].find((el) => el.dataset.toastMessage === text) || null
  );
}

function buildToastElement(text, kind) {
  const el = document.createElement("div");
  el.className = "toast " + kind;
  el.dataset.toastMessage = text;

  const icon = document.createElement("span");
  icon.className = "toast-icon";
  icon.setAttribute("aria-hidden", "true");
  icon.textContent = toastIcon(text, kind);

  const body = document.createElement("span");
  body.className = "toast-text";
  body.textContent = text;

  el.append(icon, body);
  return el;
}

function scheduleToastDismiss(el, key) {
  const prev = toastDismissTimers.get(key);
  if (prev) {
    clearTimeout(prev.hide);
    clearTimeout(prev.remove);
  }

  const hide = window.setTimeout(() => {
    el.classList.add("toast-out");
    el.style.transition = "opacity 0.28s ease, transform 0.28s ease";
    const remove = window.setTimeout(() => {
      el.remove();
      toastDismissTimers.delete(key);
    }, 250);
    toastDismissTimers.set(key, { hide, remove });
  }, 3200);

  toastDismissTimers.set(key, { hide, remove: null });
}

function toast(text, type) {
  const container = $("toast-container");
  if (!container || !text) return;

  const kind = type === "err" ? "err" : "ok";
  const key = `${kind}::${text}`;

  const existing = findVisibleToast(text, kind);
  if (existing) {
    existing.classList.remove("toast-out");
    existing.style.opacity = "";
    existing.style.transform = "";
    scheduleToastDismiss(existing, key);
    return;
  }

  const el = buildToastElement(text, kind);
  el.dataset.toastKey = key;
  container.appendChild(el);
  scheduleToastDismiss(el, key);
}

function showLoader() {
  loaderCount++;
  $("global-loader")?.classList.remove("hidden");
}

function hideLoader() {
  loaderCount = Math.max(0, loaderCount - 1);
  if (loaderCount === 0) $("global-loader")?.classList.add("hidden");
}

function setBtnLoading(btn, loading) {
  if (!btn) return;
  btn.disabled = loading;
  btn.classList.toggle("is-loading", loading);
}

function showListSkeleton(count) {
  const ul = $("reminder-list");
  $("empty-state").classList.add("hidden");
  ul.innerHTML = "";
  for (let i = 0; i < count; i++) {
    ul.innerHTML +=
      '<li class="skeleton-card"><div class="sk-line sk-title"></div><div class="sk-line sk-short"></div><div class="sk-line sk-short" style="width:30%"></div></li>';
  }
}

function showGroupSkeleton() {
  const ul = $("group-list");
  $("empty-groups").classList.add("hidden");
  ul.innerHTML =
    '<li class="skeleton-card"><div class="sk-line sk-title"></div><div class="sk-line sk-short"></div></li>';
}

function isMobile() {
  return window.innerWidth < 900;
}

function updateFabLabel() {
  const fab = $("fab-add");
  if (!fab) return;
  fab.setAttribute("aria-label", currentView === "groups" ? "Criar grupo" : "Criar lembrete");
}

function updateGroupsMobileCreate() {
  const btn = $("groups-mobile-create");
  if (!btn) return;
  const show = currentView === "groups" && isMobile() && allGroups.length > 0;
  btn.classList.toggle("hidden", !show);
}

function openMobileSheet(panelEl, title) {
  if (!isMobile() || !panelEl) return;
  const home = { parent: panelEl.parentElement, next: panelEl.nextElementSibling };
  sheetHomes.set(panelEl.id, home);

  $("sheet-root").innerHTML = `
    <div class="sheet-backdrop" id="sheet-bd"></div>
    <div class="sheet-panel">
      <div class="sheet-handle" aria-hidden="true"></div>
      <div class="sheet-header">
        <h2>${escapeHtml(title)}</h2>
        <button type="button" class="btn btn-ghost btn-icon" id="sheet-close" aria-label="Fechar">✕</button>
      </div>
      <div id="sheet-body"></div>
    </div>
  `;
  $("sheet-body").appendChild(panelEl);
  const close = () => closeMobileSheet(panelEl);
  $("sheet-bd").addEventListener("click", close);
  $("sheet-close").addEventListener("click", close);
  document.body.style.overflow = "hidden";
  window.setTimeout(() => {
    const firstInput = panelEl.querySelector("input, textarea, select");
    firstInput?.focus();
  }, 80);
}

function closeMobileSheet(panelEl) {
  if (!panelEl || !$("sheet-root").innerHTML) return;
  const home = sheetHomes.get(panelEl.id);
  if (home?.parent) {
    if (home.next) home.parent.insertBefore(panelEl, home.next);
    else home.parent.appendChild(panelEl);
  }
  sheetHomes.delete(panelEl.id);
  $("sheet-root").innerHTML = "";
  document.body.style.overflow = "";
}

function openCreateSheet() {
  const isGroup = currentView === "groups";
  if (currentView === "profile") {
    switchView("reminders");
  }

  const panel = isGroup ? $("side-create-group") : $("side-create-reminder");
  const title = isGroup ? "Novo grupo" : "Novo lembrete";
  if (!panel) return;

  if (isMobile()) {
    openMobileSheet(panel, title);
    if (!isGroup) refreshReminderGroupAssign();
    return;
  }

  const needsViewSwitch =
    (!isGroup && currentView !== "reminders") ||
    (isGroup && currentView !== "groups");

  if (!isGroup && currentView !== "reminders") {
    switchView("reminders");
  }
  if (isGroup && currentView !== "groups") {
    switchView("groups");
  }

  window.setTimeout(() => {
    panel.scrollIntoView({ behavior: "smooth", block: "nearest" });
    const firstInput = panel.querySelector("input, textarea, select");
    firstInput?.focus();
  }, needsViewSwitch ? 220 : 0);

  if (!isGroup) refreshReminderGroupAssign();
}

function myEmail() {
  return (userProfile?.email || emailFromToken() || "").toLowerCase();
}

function ownedGroups() {
  const me = myEmail();
  return allGroups.filter((g) => (g.ownerEmail || "").toLowerCase() === me);
}

function refreshReminderGroupAssign() {
  const wrap = $("nr-group-wrap");
  const groupSel = $("nr-group");
  const memberSel = $("nr-assignee");
  const assignWrap = $("nr-assignee-wrap");
  if (!wrap || !groupSel || !memberSel) return;
  const owned = ownedGroups();
  if (!owned.length) {
    wrap.classList.add("hidden");
    groupSel.innerHTML = '<option value="">— Sem grupo —</option>';
    memberSel.innerHTML = '<option value="">— Sem designação —</option>';
    assignWrap?.classList.add("hidden");
    return;
  }
  wrap.classList.remove("hidden");
  const prevGroup = groupSel.value;
  groupSel.innerHTML =
    '<option value="">— Sem grupo —</option>' +
    owned.map((g) => `<option value="${g.id}">${escapeHtml(g.name)}</option>`).join("");
  if (prevGroup && owned.some((g) => String(g.id) === prevGroup)) {
    groupSel.value = prevGroup;
  }
  populateAssigneeOptions(groupSel.value);
}

function populateAssigneeOptions(groupId) {
  const memberSel = $("nr-assignee");
  const assignWrap = $("nr-assignee-wrap");
  if (!memberSel) return;
  if (!groupId) {
    memberSel.innerHTML = '<option value="">— Sem designação —</option>';
    memberSel.disabled = false;
    assignWrap?.classList.add("hidden");
    return;
  }
  const group = allGroups.find((g) => String(g.id) === String(groupId));
  if (!group) {
    assignWrap?.classList.add("hidden");
    return;
  }
  assignWrap?.classList.remove("hidden");
  const me = myEmail();
  const others = (group.members || []).filter((m) => (m.email || "").toLowerCase() !== me);
  if (!others.length) {
    memberSel.innerHTML = '<option value="">Sem outros membros no grupo</option>';
    memberSel.disabled = true;
    return;
  }
  memberSel.disabled = false;
  memberSel.innerHTML =
    '<option value="">— Sem designação —</option>' +
    others.map((m) => `<option value="${attrVal(m.email)}">${escapeHtml(m.name || m.email)}</option>`).join("");
}

function greetingFromEmail(email) {
  const local = (email || "").split("@")[0];
  if (!local) return "Utilizador";
  return local.charAt(0).toUpperCase() + local.slice(1);
}

function updateUserDisplay(nameOrEmail) {
  const full = (nameOrEmail || "").trim() || greetingFromEmail(emailFromToken());
  const firstName = full.split(/\s+/)[0] || "Utilizador";
  const truncated = firstName.length > 14 ? firstName.slice(0, 12) + "…" : firstName;
  const greeting = $("user-greeting");
  if (greeting) greeting.textContent = "Olá, " + truncated;
  const avatar = $("user-avatar");
  if (avatar) avatar.textContent = firstName.charAt(0).toUpperCase();
}

function decodeJwtPayload(token) {
  try {
    const part = token.split(".")[1];
    if (!part) return {};
    const b64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const pad = b64.length % 4 === 0 ? "" : "=".repeat(4 - (b64.length % 4));
    return JSON.parse(atob(b64 + pad));
  } catch {
    return {};
  }
}

function emailFromToken() {
  return decodeJwtPayload(getToken() || "").sub || "";
}

function userIdFromToken() {
  const uid = decodeJwtPayload(getToken() || "").uid;
  return uid != null ? Number(uid) : null;
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
  const h = { "Content-Type": "application/json" };
  const t = getToken();
  if (t) h["Authorization"] = "Bearer " + t;
  return h;
}

async function parseError(res) {
  try {
    const j = await res.json();
    if (j.detail) return j.detail;
    if (j.message) return j.message;
  } catch (_) {}
  if (res.status === 403) return "Não tens permissão para esta operação";
  if (res.status === 400) return "Pedido inválido — verifica os dados e tenta novamente";
  return "Erro " + res.status;
}

function isGroupInviteNotification(n) {
  return n && (n.type === "GROUP_INVITE" || (n.groupInviteId != null && !n.reminderId));
}

function pickBannerNotification() {
  const reminder = unreadNotifications.find((n) => !isGroupInviteNotification(n));
  if (reminder) return reminder;
  return unreadNotifications.find(isGroupInviteNotification) || unreadNotifications[0];
}

function chicleteMarkIcon(extraClass = "") {
  const cls = extraClass ? ` chiclete-mark ${extraClass}` : " chiclete-mark";
  return `<span class="alert-icon${cls}" aria-hidden="true"><img src="/img/logo-icon.svg" alt="" width="32" height="32" decoding="async" /></span>`;
}

function formatAlertBannerMessage(top) {
  const title = (top.reminderTitle || "Lembrete").trim();
  const raw = (top.message || "").trim();
  if (!raw) return top.chewing ? '<span class="alert-mode-tag">Modo Chiclete ativo</span>' : "";
  let detail = raw;
  if (detail.startsWith(title)) detail = detail.slice(title.length).trim();
  detail = detail.replace(/^🫧\s*Chiclete\s*/i, "").replace(/\(ignorado \d+×\)\s*/i, "").trim();
  if (!detail || detail.toLowerCase() === "chiclete") {
    return top.chewing ? '<span class="alert-mode-tag">Modo Chiclete ativo</span>' : "";
  }
  if (detail === title) return "";
  return `<p>${escapeHtml(detail)}</p>`;
}

function validId(id) {
  if (id == null || id === "" || id === "null" || id === "undefined") return null;
  const n = Number(id);
  return Number.isFinite(n) && n > 0 ? n : null;
}

function buildInviteLink(token) {
  return `${window.location.origin}/?invite=${encodeURIComponent(token)}`;
}

function inviteShareMessage(inv) {
  const link = buildInviteLink(inv.inviteToken);
  return `Foste convidado para o grupo "${inv.groupName}" no Chiclete. Abre o link para aceitar: ${link}`;
}

async function copyInviteLink(inv) {
  const link = buildInviteLink(inv.inviteToken);
  try {
    await navigator.clipboard.writeText(link);
    toast("Link copiado.", "ok");
  } catch {
    toast(link, "info");
  }
}

function shareInviteWhatsApp(inv) {
  const text = encodeURIComponent(inviteShareMessage(inv));
  window.open(`https://wa.me/?text=${text}`, "_blank", "noopener");
}

function shareInviteEmail(inv) {
  const subject = encodeURIComponent(`Convite para o grupo ${inv.groupName}`);
  const body = encodeURIComponent(inviteShareMessage(inv));
  window.location.href = `mailto:${encodeURIComponent(inv.invitedEmail || "")}?subject=${subject}&body=${body}`;
}

async function api(path, options) {
  const res = await fetch(path, { ...options, headers: { ...authHeaders(), ...options?.headers } });
  if (res.status === 401) {
    clearToken();
    showAuth();
    showMsg($("msg-auth"), "Sessão expirada. Entra novamente.", "err");
    throw new Error("unauthorized");
  }
  return res;
}

function escapeHtml(s) {
  if (s == null) return "";
  const d = document.createElement("div");
  d.textContent = String(s);
  return d.innerHTML;
}

function splashDuration() {
  return window.matchMedia("(prefers-reduced-motion: reduce)").matches ? 900 : SPLASH_MIN_MS;
}

function runSplash() {
  const splash = $("splash-screen");
  if (!splash) return Promise.resolve();

  document.body.classList.add("splash-active");
  splash.classList.remove("hidden", "splash-exit");
  splash.setAttribute("aria-hidden", "false");

  return new Promise((resolve) => {
    window.setTimeout(() => {
      splash.classList.add("splash-exit");
      splash.setAttribute("aria-hidden", "true");
      window.setTimeout(() => {
        splash.classList.add("hidden");
        document.body.classList.remove("splash-active");
        resolve();
      }, SPLASH_EXIT_MS);
    }, splashDuration());
  });
}

function showAuth() {
  $("auth-root").classList.remove("hidden");
  $("app-root").classList.add("hidden");
}

function showApp(email) {
  $("auth-root").classList.add("hidden");
  $("app-root").classList.remove("hidden");
  const e = email || emailFromToken();
  updateUserDisplay(greetingFromEmail(e));
}

function switchView(view) {
  currentView = view;
  $("nav-reminders").classList.toggle("active", view === "reminders");
  $("nav-groups").classList.toggle("active", view === "groups");
  $("nav-profile").classList.toggle("active", view === "profile");

  const bottomNavItems = [
    ["bn-reminders", "reminders"],
    ["bn-groups", "groups"],
    ["bn-profile", "profile"],
  ];
  for (const [id, key] of bottomNavItems) {
    const btn = $(id);
    if (!btn) continue;
    const active = view === key;
    btn.classList.toggle("active", active);
    if (active) btn.setAttribute("aria-current", "page");
    else btn.removeAttribute("aria-current");
  }

  const titleEl = $("topbar-title");
  if (titleEl) titleEl.textContent = VIEW_TITLES[view] || "Chiclete";

  $("view-reminders").classList.toggle("hidden", view !== "reminders");
  $("view-groups").classList.toggle("hidden", view !== "groups");
  $("view-profile").classList.toggle("hidden", view !== "profile");
  if (view === "groups") loadGroups();
  if (view === "profile") {
    loadProfile();
    loadNotificationHistory();
    loadWhatsappSimulations();
  }
  if (view === "reminders") loadChicleteStats();
  updateFabLabel();
  updateGroupsMobileCreate();
}

function toggleIntervalField(chewingCheckbox, wrapId) {
  const wrap = $(wrapId);
  if (wrap) wrap.classList.toggle("hidden", !chewingCheckbox.checked);
}

function playChicleteAlert() {
  try {
    if (navigator.vibrate) navigator.vibrate([180, 90, 180]);
    audioCtx = audioCtx || new (window.AudioContext || window.webkitAudioContext)();
    const beep = (freq, delay) => {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.frequency.value = freq;
      osc.type = "sine";
      const t = audioCtx.currentTime + delay;
      gain.gain.setValueAtTime(0.14, t);
      gain.gain.exponentialRampToValueAtTime(0.01, t + 0.35);
      osc.start(t);
      osc.stop(t + 0.35);
    };
    beep(880, 0);
    beep(1100, 0.22);
  } catch (_) {}
}

const AUTH_HEADINGS = {
  login: { title: "Bem-vindo de volta", sub: "Entra para ver os teus lembretes." },
  register: { title: "Cria a tua conta", sub: "Começa a usar o Modo Chiclete hoje." },
  forgot: { title: "Redefinir senha", sub: "Escolhe uma nova senha para a tua conta." },
};

function updateAuthHeadings(mode) {
  const copy = AUTH_HEADINGS[mode] || AUTH_HEADINGS.login;
  const titleEl = $("auth-card-title");
  const subEl = $("auth-card-sub");
  if (titleEl) titleEl.textContent = copy.title;
  if (subEl) subEl.textContent = copy.sub;
}

function switchTab(which) {
  $("auth-tabs").classList.remove("hidden");
  $("tab-login").classList.toggle("active", which === "login");
  $("tab-register").classList.toggle("active", which === "register");
  $("panel-login").classList.toggle("active", which === "login");
  $("panel-register").classList.toggle("active", which === "register");
  $("panel-forgot").classList.remove("active");
  updateAuthHeadings(which);
  hideMsg($("msg-auth"));
}

function showForgot() {
  $("auth-tabs").classList.add("hidden");
  $("panel-login").classList.remove("active");
  $("panel-register").classList.remove("active");
  $("panel-forgot").classList.add("active");
  updateAuthHeadings("forgot");
  hideMsg($("msg-auth"));
  const email = $("login-email").value.trim();
  if (email) $("forgot-email").value = email;
}

let lastSocialToastAt = 0;

function handleSocialClick(e) {
  const btn = e.target.closest(".auth-social-btn");
  if (!btn) return;

  const now = Date.now();
  if (now - lastSocialToastAt < 2800) return;
  lastSocialToastAt = now;

  const names = { google: "Google", apple: "Apple", android: "Android" };
  const name = names[btn.dataset.social] || "Esta opção";
  toast(`${name} chega em breve! Por agora, entra com e-mail.`, "ok");
}

function backToLogin() {
  $("form-forgot").reset();
  switchTab("login");
}

function toggleLoginPasswordVisibility() {
  const password = $("login-password");
  const toggle = $("toggle-login-password");
  if (!password || !toggle) return;

  const shouldShow = password.type === "password";
  password.type = shouldShow ? "text" : "password";
  toggle.classList.toggle("is-visible", shouldShow);
  toggle.setAttribute("aria-pressed", String(shouldShow));
  toggle.setAttribute("aria-label", shouldShow ? "Ocultar senha" : "Mostrar senha");
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

function defaultDateTimeLocal() {
  const d = new Date();
  d.setMinutes(0, 0, 0);
  d.setHours(d.getHours() + 1);
  const pad = (n) => String(n).padStart(2, "0");
  return (
    d.getFullYear() +
    "-" +
    pad(d.getMonth() + 1) +
    "-" +
    pad(d.getDate()) +
    "T" +
    pad(d.getHours()) +
    ":00"
  );
}

function formatSchedule(iso) {
  if (!iso) return "";
  try {
    return new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
  } catch {
    return iso;
  }
}

function isOverdue(r) {
  if (r.completed || !r.scheduledAt) return false;
  return new Date(r.scheduledAt) < new Date();
}

function isOwner(r) {
  const uid = userIdFromToken();
  return uid != null && r.ownerId === uid;
}

function priorityClass(p) {
  const key = (p || "MEDIA").toUpperCase();
  return "badge-priority-" + key.toLowerCase();
}

function priorityLabel(p) {
  return PRIORITY_LABELS[(p || "MEDIA").toUpperCase()] || p;
}

function isRecurringReminder(r) {
  return Boolean(r?.recurrenceType && r.recurrenceType !== "NONE");
}

function isRecurringActive(r) {
  return isRecurringReminder(r) && r.recurringEnabled !== false;
}

function recurrenceLabel(r) {
  if (!isRecurringReminder(r)) return "";
  if (r.recurrenceType === "WEEKDAYS") return "Seg–Sex";
  const days = (r.recurrenceDays || "")
    .split(",")
    .map((d) => Number(d.trim()))
    .filter((d) => d >= 1 && d <= 7);
  return days.map((d) => RECURRENCE_DAY_NAMES[d] || d).join(", ");
}

function syncRecurrenceForm() {
  const repeat = $("nr-repeat")?.checked;
  const options = $("nr-repeat-options");
  const whenLabel = $("nr-when-label");
  if (options) options.classList.toggle("hidden", !repeat);
  if (whenLabel) whenLabel.textContent = repeat ? "Hora e primeira ocorrência" : "Data e hora";

  const type = document.querySelector('input[name="nr-recurrence-type"]:checked')?.value || "WEEKDAYS";
  const customDays = $("nr-custom-days");
  if (customDays) customDays.classList.toggle("hidden", type !== "CUSTOM");
}

function resetRecurrenceForm() {
  if (!$("nr-repeat")) return;
  $("nr-repeat").checked = false;
  const weekdays = document.querySelector('input[name="nr-recurrence-type"][value="WEEKDAYS"]');
  if (weekdays) weekdays.checked = true;
  document.querySelectorAll("#nr-custom-days .day-chip").forEach((chip, index) => {
    chip.classList.toggle("active", index < 5);
  });
  syncRecurrenceForm();
}

function readRecurrenceFromForm() {
  if (!$("nr-repeat")?.checked) {
    return { recurrenceType: "NONE", recurrenceDays: null, recurringEnabled: true };
  }

  const type = document.querySelector('input[name="nr-recurrence-type"]:checked')?.value || "WEEKDAYS";
  if (type === "CUSTOM") {
    const selected = [...document.querySelectorAll("#nr-custom-days .day-chip.active")].map(
      (chip) => chip.dataset.day
    );
    if (!selected.length) {
      throw new Error("Seleciona pelo menos um dia para a repetição personalizada.");
    }
    return {
      recurrenceType: "CUSTOM",
      recurrenceDays: selected.join(","),
      recurringEnabled: true,
    };
  }

  return { recurrenceType: "WEEKDAYS", recurrenceDays: null, recurringEnabled: true };
}

function updateStats(items) {
  $("stat-total").textContent = items.length;
  $("stat-pending").textContent = items.filter((r) => !r.completed).length;
  $("stat-chewing").textContent = items.filter((r) => r.chewing && !r.completed).length;
  $("stat-done").textContent = items.filter((r) => r.completed).length;
}

function syncFilterUI() {
  document.querySelectorAll(".stat-filter").forEach((btn) => {
    const active = btn.dataset.filter === currentFilter;
    btn.classList.toggle("active", active);
    btn.setAttribute("aria-selected", String(active));
  });
}

function setReminderFilter(filter) {
  currentFilter = filter;
  syncFilterUI();
  renderReminders();
}

function filteredReminders() {
  let items = [...allReminders];
  if (currentFilter === "pending") items = items.filter((r) => !r.completed);
  else if (currentFilter === "done") items = items.filter((r) => r.completed);
  else if (currentFilter === "chewing") items = items.filter((r) => r.chewing && !r.completed);

  if (searchQuery) {
    const q = searchQuery.toLowerCase();
    items = items.filter(
      (r) =>
        (r.title && r.title.toLowerCase().includes(q)) ||
        (r.description && r.description.toLowerCase().includes(q))
    );
  }

  items.sort((a, b) => {
    if (a.completed !== b.completed) return a.completed ? 1 : -1;
    return new Date(a.scheduledAt) - new Date(b.scheduledAt);
  });

  return items;
}

function renderReminders() {
  const ul = $("reminder-list");
  const empty = $("empty-state");
  const items = filteredReminders();

  updateStats(allReminders);
  syncFilterUI();
  $("list-count").textContent = items.length + (items.length === 1 ? " item" : " itens");

  ul.innerHTML = "";
  if (!items.length) {
    empty.classList.remove("hidden");
    $("empty-text").textContent =
      allReminders.length === 0
        ? "Ainda não tens lembretes. Toca no + para criar."
        : "Nenhum lembrete corresponde aos filtros.";
    return;
  }
  empty.classList.add("hidden");

  for (const r of items) {
    const li = document.createElement("li");
    const priorityKey = (r.priority || "MEDIA").toLowerCase();
    const overdue = isOverdue(r);
    li.className =
      "reminder-card" +
      (r.completed ? " completed" : "") +
      (overdue ? " overdue" : "") +
      " priority-" + priorityKey;
    li.dataset.id = r.id;

    const owner = isOwner(r);
    const shared = !owner;
    const badges = [];

    if (!overdue) {
      badges.push(`<span class="badge ${priorityClass(r.priority)}">${escapeHtml(priorityLabel(r.priority))}</span>`);
    }
    if (isRecurringReminder(r)) {
      const paused = r.recurringEnabled === false;
      const label = paused ? "Repetição pausada" : recurrenceLabel(r);
      badges.push(`<span class="badge badge-recurrence${paused ? " paused" : ""}">🔁 ${escapeHtml(label)}</span>`);
    }
    if (r.chewing) {
      const interval = r.intervalMinutes ? `${r.intervalMinutes}m` : "";
      badges.push(`<span class="badge badge-chiclete">🫧 Chiclete${interval ? ` · ${interval}` : ""}</span>`);
    }
    if (shared) badges.push('<span class="badge badge-shared">Partilhado</span>');
    if (overdue) badges.push('<span class="badge badge-overdue">Atrasado</span>');

    let primaryActions = "";
    let moreActions = "";

    if (!r.completed && r.chewing) {
      primaryActions += `<button type="button" class="btn btn-ghost btn-sm" data-action="snooze" data-id="${r.id}">Adiar</button>`;
      primaryActions += `<button type="button" class="btn btn-ghost btn-sm" data-action="ignore" data-id="${r.id}">Ignorar · ${r.ignoreCount || 0}×</button>`;
    }

    if (!r.completed && (!isRecurringReminder(r) || isRecurringActive(r))) {
      const completeLabel = isRecurringActive(r) ? "Concluir hoje" : "Concluir";
      moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="complete" data-id="${r.id}">${completeLabel}</button>`;
    }
    if (!r.completed) {
      moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="chewing" data-id="${r.id}">${r.chewing ? "Desativar Chiclete" : "Ativar Chiclete"}</button>`;
    }
    if (owner) {
      if (isRecurringReminder(r)) {
        if (r.recurringEnabled === false) {
          moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="resume-recurrence" data-id="${r.id}">Reativar repetição</button>`;
        } else {
          moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="pause-recurrence" data-id="${r.id}">Desativar repetição</button>`;
        }
      }
      moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="share" data-id="${r.id}">Partilhar</button>`;
      moreActions += `<button type="button" class="btn btn-secondary btn-sm" data-action="edit" data-id="${r.id}">Editar</button>`;
      moreActions += `<button type="button" class="btn btn-danger btn-sm" data-action="delete" data-id="${r.id}">Eliminar</button>`;
    }

    const sharedInfo =
      r.sharedWithEmails && r.sharedWithEmails.length
        ? `<div class="reminder-shared">Partilhado com ${r.sharedWithEmails.map(escapeHtml).join(", ")}</div>`
        : "";

    const scheduleClass = overdue ? "reminder-schedule overdue" : "reminder-schedule";
    const actionsHtml = primaryActions || moreActions
      ? `<div class="reminder-actions">
          ${primaryActions ? `<div class="reminder-actions-primary">${primaryActions}</div>` : ""}
          ${moreActions ? `<details class="reminder-more"><summary>Mais opções</summary><div class="reminder-actions-extra">${moreActions}</div></details>` : ""}
        </div>`
      : "";

    li.innerHTML = `
      <div class="reminder-card-header">
        <input type="checkbox" class="reminder-check" data-action="toggle" data-id="${r.id}" ${r.completed && !isRecurringActive(r) ? "checked disabled" : ""} aria-label="${r.completed && !isRecurringActive(r) ? "Concluído" : isRecurringActive(r) ? "Concluir hoje" : "Marcar como concluído"}" />
        <div class="reminder-title-wrap">
          <p class="reminder-title">${escapeHtml(r.title)}</p>
          ${r.description ? `<p class="reminder-desc">${escapeHtml(r.description)}</p>` : ""}
        </div>
      </div>
      <div class="reminder-meta">
        ${badges.length ? `<div class="reminder-badges">${badges.join("")}</div>` : ""}
        <time class="${scheduleClass}">${escapeHtml(formatSchedule(r.scheduledAt))}</time>
      </div>
      ${sharedInfo}
      ${actionsHtml}
    `;
    ul.appendChild(li);
  }
}

async function loadReminders() {
  showListSkeleton(3);
  try {
    const res = await api("/api/reminders");
    if (!res.ok) {
      toast(await parseError(res), "err");
      return;
    }
    allReminders = await res.json();
    renderReminders();
    loadChicleteStats();
  } catch (e) {
    if (e.message !== "unauthorized") toast("Erro ao carregar lembretes.", "err");
  }
}

async function setCompleted(id, completed) {
  const rid = validId(id);
  if (!rid) throw new Error("Lembrete inválido.");
  const res = await api("/api/reminders/" + rid + "/complete", {
    method: "PATCH",
    body: JSON.stringify({ completed }),
  });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function setRecurringEnabled(id, enabled) {
  const res = await api("/api/reminders/" + id + "/recurrence", {
    method: "PATCH",
    body: JSON.stringify({ enabled }),
  });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function toggleChewing(id, chewing) {
  const res = await api("/api/reminders/" + id + "/chewing", {
    method: "PATCH",
    body: JSON.stringify({ chewing }),
  });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function simulateIgnore(id) {
  const res = await api("/api/reminders/" + id + "/chewing/ignore", { method: "POST" });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function snoozeReminder(id) {
  const res = await api("/api/reminders/" + id + "/snooze", { method: "POST" });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function loadProfile() {
  try {
    const res = await api("/api/profile");
    if (!res.ok) throw new Error(await parseError(res));
    userProfile = await res.json();
    fillProfileForm(userProfile);
    updateUserDisplay(userProfile.name || userProfile.email);
  } catch (e) {
    if (e.message !== "unauthorized") toast("Erro ao carregar perfil.", "err");
  }
}

async function loadWhatsappSimulations() {
  const ul = $("whatsapp-simulations");
  if (!ul) return;
  try {
    const res = await api("/api/whatsapp/simulations");
    if (!res.ok) return;
    const items = await res.json();
    $("wa-sim-count").textContent = items.length + (items.length === 1 ? " mensagem" : " mensagens");
    if (!items.length) {
      ul.innerHTML = '<li class="wa-sim-empty">Nenhuma mensagem simulada ainda. Ativa WhatsApp no perfil e cria um lembrete Chiclete vencido.</li>';
      return;
    }
    ul.innerHTML = items
      .map(
        (s) => `
      <li class="wa-sim-item">
        <div class="wa-sim-header">
          <span class="wa-sim-phone">📱 ${escapeHtml(s.phone)}</span>
          <span class="wa-sim-time">${formatSchedule(s.createdAt)}</span>
        </div>
        <div class="wa-sim-bubble">${escapeHtml(s.message)}</div>
        <span class="wa-sim-reminder">Lembrete: ${escapeHtml(s.reminderTitle)}</span>
      </li>`
      )
      .join("");
  } catch (_) {}
}

async function loadNotificationHistory() {
  const ul = $("notification-history");
  if (!ul) return;
  try {
    const res = await api("/api/notifications/history");
    if (!res.ok) return;
    const items = await res.json();
    if (!items.length) {
      ul.innerHTML = '<li class="history-empty">Ainda sem alertas registados.</li>';
      return;
    }
    ul.innerHTML = items
      .map(
        (n) => `
      <li class="history-item ${n.read ? "read" : "unread"}">
        <span class="history-msg">${escapeHtml(n.message)}</span>
        <span class="history-meta">${formatSchedule(n.createdAt)} · ${escapeHtml(priorityLabel(n.priorityAtSend))}${n.read ? "" : " · novo"}</span>
      </li>`
      )
      .join("");
  } catch (_) {}
}

async function loadChicleteStats() {
  try {
    const res = await api("/api/stats/chiclete");
    if (!res.ok) return;
    chicleteStats = await res.json();
    renderChicleteDashboard();
  } catch (_) {}
}

function renderChicleteDashboard() {
  const dash = $("chiclete-dashboard");
  if (!dash || !chicleteStats) return;
  const hasData = chicleteStats.chewingCompleted > 0 || chicleteStats.totalIgnores > 0 || chicleteStats.chewingActive > 0;
  dash.classList.toggle("hidden", !hasData);
  if (!hasData) return;
  const max = Math.max(chicleteStats.chewingCompleted, chicleteStats.totalIgnores, 1);
  $("dash-val-done").textContent = chicleteStats.chewingCompleted;
  $("dash-val-ignore").textContent = chicleteStats.totalIgnores;
  $("dash-bar-done").style.width = (chicleteStats.chewingCompleted / max) * 100 + "%";
  $("dash-bar-ignore").style.width = (chicleteStats.totalIgnores / max) * 100 + "%";
  const alerts = chicleteStats.totalAlerts;
  $("dash-alerts-total").textContent = alerts + (alerts === 1 ? " alerta" : " alertas");

  const panel = $("dash-panel");
  const toggle = $("dash-toggle");
  if (window.matchMedia("(min-width: 900px)").matches) {
    panel?.classList.remove("hidden");
    toggle?.classList.add("is-open");
    toggle?.setAttribute("aria-expanded", "true");
  }
}

function toggleChicleteDashboard() {
  const panel = $("dash-panel");
  const toggle = $("dash-toggle");
  if (!panel || !toggle) return;
  const open = panel.classList.toggle("hidden");
  const isOpen = !open;
  toggle.setAttribute("aria-expanded", String(isOpen));
  toggle.classList.toggle("is-open", isOpen);
}

function fillProfileForm(p) {
  if (!p) return;
  $("pf-name").value = p.name || "";
  $("pf-email").value = p.email || "";
  $("pf-whatsapp").value = p.whatsapp || "";
  $("pf-notify-in-app").checked = p.notifyInApp !== false;
  $("pf-notify-email").checked = p.notifyEmail !== false;
  $("pf-notify-whatsapp").checked = p.notifyWhatsapp === true;
}

async function saveProfile(e) {
  e.preventDefault();
  if (!(await showConfirm("Guardar perfil", "Confirmas as alterações ao teu perfil e preferências de notificação?"))) return;
  const btn = $("btn-save-profile");
  setBtnLoading(btn, true);
  try {
    const res = await api("/api/profile", {
      method: "PUT",
      body: JSON.stringify({
        name: $("pf-name").value.trim(),
        whatsapp: $("pf-whatsapp").value.trim() || null,
        notifyInApp: $("pf-notify-in-app").checked,
        notifyEmail: $("pf-notify-email").checked,
        notifyWhatsapp: $("pf-notify-whatsapp").checked,
      }),
    });
    if (!res.ok) throw new Error(await parseError(res));
    userProfile = await res.json();
    updateUserDisplay(userProfile.name || userProfile.email);
    toast("Perfil atualizado — tudo pronto para os alertas.", "ok");
  } catch (err) {
    toast(err.message || "Falha ao guardar perfil.", "err");
  } finally {
    setBtnLoading(btn, false);
  }
}

async function loadNotifications() {
  try {
    const res = await api("/api/notifications");
    if (!res.ok) return;
    const incoming = await res.json();
    const prevIds = new Set(unreadNotifications.map((n) => n.id));
    unreadNotifications = incoming;
    renderAlertBanner();
    renderNotificationDrawer();
    updateAlertBadge();
    const fresh = incoming.filter((n) => !prevIds.has(n.id));
    if (fresh.length > 0 && document.visibilityState === "visible") {
      const inviteCount = fresh.filter(isGroupInviteNotification).length;
      const reminderCount = fresh.length - inviteCount;
      if (reminderCount > 0) playChicleteAlert();
      if (inviteCount > 0) {
        toast(`Convite${inviteCount > 1 ? "s" : ""} de grupo — vê no sino.`, "ok");
        if (currentView === "groups") loadGroups();
      }
      if (currentView === "profile") loadWhatsappSimulations();
    }
  } catch (e) {
    if (e.message !== "unauthorized") {
      /* silencioso no polling */
    }
  }
}

function updateAlertBadge() {
  const badge = $("alert-badge");
  const count = unreadNotifications.length;
  if (!badge) return;
  const chicleteCount = unreadNotifications.filter((n) => !isGroupInviteNotification(n)).length;
  badge.textContent = count > 9 ? "9+" : String(count);
  badge.classList.toggle("hidden", count === 0);
  badge.classList.toggle("alert-badge-chiclete", chicleteCount > 0);
  badge.classList.toggle("alert-badge-pulse", count > 0);
}

function renderNotificationDrawer() {
  const drawer = $("notification-drawer");
  const list = $("notification-drawer-list");
  if (!drawer || !list) return;

  const invites = unreadNotifications.filter(isGroupInviteNotification);
  if (!invites.length) {
    list.innerHTML = "";
    if (!drawer.classList.contains("hidden")) toggleNotificationDrawer(false);
    return;
  }

  list.innerHTML = `
    <li class="notif-drawer-section"><span class="notif-drawer-label">Convites de grupo</span></li>` +
    invites
      .map((n) => {
        const inviteId = validId(n.groupInviteId);
        const actions = inviteId
          ? `<button type="button" class="btn btn-primary btn-sm" data-alert-action="accept-invite" data-invite-id="${inviteId}">Aceitar</button>
             <button type="button" class="btn btn-ghost btn-sm" data-alert-action="reject-invite" data-invite-id="${inviteId}" data-notif-id="${n.id}">Recusar</button>`
          : `<button type="button" class="btn btn-ghost btn-sm" data-drawer-go-groups="1">Ver convites</button>`;
        return `
      <li class="notif-drawer-item notif-invite">
        <div class="notif-drawer-icon notif-drawer-icon-invite" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        </div>
        <div class="notif-drawer-body">
          <strong>${escapeHtml(n.groupName || "Grupo")}</strong>
          <p>${escapeHtml(n.message)}</p>
        </div>
        <div class="notif-drawer-actions">${actions}</div>
      </li>`;
      })
      .join("");
}

function renderAlertBanner() {
  const root = $("alert-banner-root");
  if (!root) return;
  if (!unreadNotifications.length) {
    root.innerHTML = "";
    root.classList.remove("has-alerts");
    return;
  }
  root.classList.add("has-alerts");
  const top = pickBannerNotification();
  if (isGroupInviteNotification(top)) {
    const inviteId = validId(top.groupInviteId);
    const actions = inviteId
      ? `<button type="button" class="btn btn-primary btn-sm" data-alert-action="accept-invite" data-invite-id="${inviteId}">Aceitar</button>
         <button type="button" class="btn btn-ghost btn-sm" data-alert-action="reject-invite" data-invite-id="${inviteId}" data-notif-id="${top.id}">Recusar</button>`
      : `<button type="button" class="btn btn-primary btn-sm" data-drawer-go-groups="1">Ver convites</button>`;
    root.innerHTML = `
    <div class="alert-banner alert-banner-invite alert-banner-featured">
      <div class="alert-banner-glow" aria-hidden="true"></div>
      <div class="alert-banner-accent" aria-hidden="true"></div>
      <div class="alert-banner-body">
        <span class="alert-icon alert-icon-invite" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        </span>
        <div class="alert-banner-text">
          <strong>Convite para ${escapeHtml(top.groupName || "grupo")}</strong>
          <p>${escapeHtml(top.message)}</p>
        </div>
      </div>
      <div class="alert-banner-actions">
        ${actions}
        ${unreadNotifications.length > 1 ? `<span class="alert-more">+${unreadNotifications.length - 1}</span>` : ""}
      </div>
    </div>
  `;
    return;
  }
  const priority = priorityLabel(top.priorityAtSend);
  const isHigh = ["ALTA", "URGENTE"].includes((top.priorityAtSend || "").toUpperCase());
  const chewingClass = top.chewing ? " alert-banner-chewing" : "";
  const featuredClass = isHigh || top.chewing ? " alert-banner-featured" : "";
  const topReminderId = validId(top.reminderId);
  const messageHtml = formatAlertBannerMessage(top);
  const waCompact = top.whatsappLink
    ? `<a href="${attrVal(top.whatsappLink)}" target="_blank" rel="noopener" class="btn btn-ghost btn-sm alert-wa-btn" title="WhatsApp">WA</a>`
    : "";
  const chicleteOnly = unreadNotifications.filter((n) => !isGroupInviteNotification(n)).length;
  const extraCount = Math.max(0, chicleteOnly - 1);
  root.innerHTML = `
    <div class="alert-banner priority-${(top.priorityAtSend || "MEDIA").toLowerCase()}${chewingClass}${featuredClass}">
      <div class="alert-banner-glow" aria-hidden="true"></div>
      <div class="alert-banner-accent" aria-hidden="true"></div>
      <div class="alert-banner-body">
        ${top.chewing ? chicleteMarkIcon("chiclete-mark-pulse") : chicleteMarkIcon()}
        <div class="alert-banner-text">
          <strong>${escapeHtml(top.reminderTitle || "Lembrete")}</strong>
          ${messageHtml}
          <span class="alert-priority-tag">${escapeHtml(priority)}</span>
        </div>
      </div>
      <div class="alert-banner-actions">
        ${topReminderId
          ? `<button type="button" class="btn btn-primary btn-sm" data-alert-action="complete" data-id="${top.id}" data-reminder="${topReminderId}">Concluir</button>`
          : `<button type="button" class="btn btn-ghost btn-sm" data-alert-action="dismiss" data-id="${top.id}">Dispensar</button>`}
        <div class="alert-quick-actions">
          <button type="button" class="btn btn-ghost btn-sm" data-alert-action="snooze" data-id="${top.id}">Adiar</button>
          <button type="button" class="btn btn-ghost btn-sm" data-alert-action="dismiss" data-id="${top.id}">Ignorar</button>
          ${waCompact}
        </div>
        ${extraCount > 0 ? `<span class="alert-more">+${extraCount}</span>` : ""}
      </div>
    </div>
  `;
}

async function handleAlertAction(e) {
  const btn = e.target.closest("[data-alert-action]");
  if (!btn) return;
  e.preventDefault();
  e.stopPropagation();
  const action = btn.dataset.alertAction;
  const notifId = validId(btn.dataset.id);
  const reminderId = validId(btn.dataset.reminder);
  const inviteId = validId(btn.dataset.inviteId);
  try {
    if (action === "accept-invite") {
      if (!inviteId) throw new Error("Convite inválido — abre a seção Grupos.");
      if (!(await showConfirm("Aceitar convite", "Entrar neste grupo e ver as tarefas partilhadas?"))) return;
      const res = await api("/api/groups/invites/" + inviteId + "/accept", { method: "POST" });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Convite aceite — já estás no grupo.", "ok");
      await Promise.all([loadGroups(), loadNotifications()]);
      return;
    }
    if (action === "reject-invite") {
      if (!inviteId) throw new Error("Convite inválido.");
      if (!(await showConfirm("Recusar convite", "Recusar este convite de grupo?", { confirmText: "Recusar", danger: true }))) return;
      const res = await api("/api/groups/invites/" + inviteId + "/reject", { method: "POST" });
      if (!res.ok) throw new Error(await parseError(res));
      if (btn.dataset.notifId) {
        await api("/api/notifications/" + btn.dataset.notifId + "/read", { method: "PATCH" });
      }
      toast("Convite recusado.", "ok");
      await loadNotifications();
      return;
    }
    if (action === "complete") {
      if (!reminderId) throw new Error("Lembrete inválido.");
      const updated = await setCompleted(reminderId, true);
      replaceReminder(updated);
      await api("/api/notifications/" + notifId + "/read", { method: "PATCH" });
      toast("Lembrete concluído — o Chiclete soltou!", "ok");
      loadChicleteStats();
    } else if (action === "snooze") {
      await api("/api/notifications/" + notifId + "/snooze", { method: "POST" });
      toast("Adiado 10 min — sem penalização.", "ok");
      await loadReminders();
    } else if (action === "dismiss") {
      await api("/api/notifications/" + notifId + "/dismiss", { method: "POST" });
      toast("Marcado como ignorado.", "ok");
      await loadReminders();
      loadChicleteStats();
    }
    await loadNotifications();
  } catch (err) {
    toast(err.message || "Operação falhou.", "err");
  }
}

function startNotificationPolling() {
  stopNotificationPolling();
  loadNotifications();
  pollTimer = setInterval(loadNotifications, NOTIFICATION_POLL_MS);
}

function stopNotificationPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
  unreadNotifications = [];
  renderAlertBanner();
  renderNotificationDrawer();
  updateAlertBadge();
}

async function deleteReminder(id) {
  const res = await api("/api/reminders/" + id, { method: "DELETE" });
  if (!res.ok) throw new Error(await parseError(res));
}

async function shareReminder(id, email) {
  const res = await api("/api/reminders/" + id + "/share", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

async function updateReminder(id, body) {
  const res = await api("/api/reminders/" + id, {
    method: "PUT",
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}

function replaceReminder(updated) {
  allReminders = allReminders.map((r) => (r.id === updated.id ? updated : r));
  renderReminders();
}

function removeReminder(id) {
  allReminders = allReminders.filter((r) => r.id !== id);
  renderReminders();
}

function showConfirm(title, message, options = {}) {
  const confirmText = options.confirmText || "Confirmar";
  const cancelText = options.cancelText || "Cancelar";
  const danger = options.danger || false;
  const btnClass = danger ? "btn btn-danger" : "btn btn-primary";
  return new Promise((resolve) => {
    const root = $("modal-root");
    const close = (result) => {
      root.innerHTML = "";
      resolve(result);
    };
    root.innerHTML = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal" role="dialog" aria-labelledby="confirm-title">
          <h3 id="confirm-title">${escapeHtml(title)}</h3>
          <p>${escapeHtml(message)}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn-ghost" id="modal-cancel">${escapeHtml(cancelText)}</button>
            <button type="button" class="${btnClass}" id="modal-confirm">${escapeHtml(confirmText)}</button>
          </div>
        </div>
      </div>
    `;
    $("modal-cancel").addEventListener("click", () => close(false));
    $("modal-backdrop").addEventListener("click", (e) => {
      if (e.target.id === "modal-backdrop") close(false);
    });
    $("modal-confirm").addEventListener("click", () => close(true));
  });
}

function openModal(title, subtitle, fields, onSubmit) {
  const root = $("modal-root");
  root.innerHTML = `
    <div class="modal-backdrop" id="modal-backdrop">
      <div class="modal" role="dialog">
        <h3>${escapeHtml(title)}</h3>
        ${subtitle ? `<p>${escapeHtml(subtitle)}</p>` : ""}
        <form id="modal-form">${fields}<div class="modal-actions">
          <button type="button" class="btn btn-ghost" id="modal-cancel">Cancelar</button>
          <button type="submit" class="btn btn-primary">Confirmar</button>
        </div></form>
      </div>
    </div>
  `;

  const close = () => { root.innerHTML = ""; };
  $("modal-cancel").addEventListener("click", close);
  $("modal-backdrop").addEventListener("click", (e) => {
    if (e.target.id === "modal-backdrop") close();
  });
  $("modal-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await onSubmit(new FormData(e.target), close);
    } catch (err) {
      toast(err.message || "Operação falhou.", "err");
    }
  });
}

function openShareModal(r) {
  openModal(
    "Partilhar lembrete",
    "Indica o e-mail de quem já tem conta no Chiclete.",
    `<label for="share-email">E-mail</label><input id="share-email" name="email" type="email" required placeholder="colega@email.com" />`,
    async (fd, close) => {
      const email = fd.get("email").trim();
      if (!(await showConfirm("Partilhar lembrete", `Queres partilhar «${r.title}» com ${email}?`))) return;
      const updated = await shareReminder(r.id, email);
      replaceReminder(updated);
      close();
      toast("Lembrete partilhado com sucesso.", "ok");
    }
  );
}

function attrVal(s) {
  return String(s ?? "")
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;");
}

function openEditModal(r) {
  const when = r.scheduledAt ? r.scheduledAt.slice(0, 16) : "";
  openModal(
    "Editar lembrete",
    null,
    `
      <label for="edit-title">Título</label>
      <input id="edit-title" name="title" type="text" required maxlength="255" value="${attrVal(r.title)}" />
      <label for="edit-desc">Descrição</label>
      <textarea id="edit-desc" name="description" maxlength="500">${escapeHtml(r.description || "")}</textarea>
      <label for="edit-when">Data e hora</label>
      <input id="edit-when" name="scheduledAt" type="datetime-local" required value="${attrVal(when)}" />
      <label for="edit-priority">Prioridade</label>
      <select id="edit-priority" name="priority">
        <option value="BAIXA" ${r.priority === "BAIXA" ? "selected" : ""}>Baixa</option>
        <option value="MEDIA" ${r.priority === "MEDIA" ? "selected" : ""}>Média</option>
        <option value="ALTA" ${r.priority === "ALTA" ? "selected" : ""}>Alta</option>
        <option value="URGENTE" ${r.priority === "URGENTE" ? "selected" : ""}>Urgente</option>
      </select>
      <div class="row-check">
        <input id="edit-chewing" name="chewing" type="checkbox" ${r.chewing ? "checked" : ""} />
        <label for="edit-chewing">Modo Chiclete</label>
      </div>
      <div id="edit-interval-wrap" class="${r.chewing ? "" : "hidden"}">
        <label for="edit-interval">Intervalo entre alertas</label>
        <select id="edit-interval" name="intervalMinutes">
          <option value="5" ${r.intervalMinutes === 5 ? "selected" : ""}>5 minutos</option>
          <option value="15" ${r.intervalMinutes === 15 ? "selected" : ""}>15 minutos</option>
          <option value="30" ${!r.intervalMinutes || r.intervalMinutes === 30 ? "selected" : ""}>30 minutos</option>
          <option value="60" ${r.intervalMinutes === 60 ? "selected" : ""}>60 minutos</option>
        </select>
      </div>
    `,
    async (fd, close) => {
      const chewing = fd.get("chewing") === "on";
      const interval = chewing ? Number(fd.get("intervalMinutes")) || 30 : null;
      const newTitle = fd.get("title").trim();
      if (!(await showConfirm("Guardar alterações", `Atualizar o lembrete «${newTitle}»?`))) return;
      const body = {
        title: newTitle,
        description: fd.get("description").trim() || null,
        scheduledAt: toIsoLocal(fd.get("scheduledAt")),
        chewing,
        intervalMinutes: interval,
        priority: fd.get("priority"),
        recurrenceType: r.recurrenceType || "NONE",
        recurrenceDays: r.recurrenceDays || null,
        recurringEnabled: r.recurringEnabled !== false,
      };
      const updated = await updateReminder(r.id, body);
      replaceReminder(updated);
      close();
      toast("Lembrete atualizado.", "ok");
    }
  );
  const editChewing = document.getElementById("edit-chewing");
  const editWrap = document.getElementById("edit-interval-wrap");
  if (editChewing && editWrap) {
    editChewing.addEventListener("change", () => editWrap.classList.toggle("hidden", !editChewing.checked));
  }
}

async function handleReminderAction(e) {
  const btn = e.target.closest("[data-action]");
  if (!btn) return;

  const action = btn.dataset.action;
  const id = Number(btn.dataset.id);
  const r = allReminders.find((x) => x.id === id);
  if (!r) return;

  try {
    if (action === "toggle") {
      if (r.completed && !isRecurringActive(r)) return;
      const recurring = isRecurringActive(r);
      const updated = await setCompleted(id, true);
      replaceReminder(updated);
      toast(
        recurring ? "Concluído para hoje. Próximo alerta agendado." : "Lembrete concluído.",
        "ok"
      );
    } else if (action === "complete") {
      const recurring = isRecurringActive(r);
      const updated = await setCompleted(id, true);
      replaceReminder(updated);
      toast(
        recurring ? "Concluído para hoje. Próximo alerta agendado." : "Lembrete concluído.",
        "ok"
      );
    } else if (action === "pause-recurrence") {
      const updated = await setRecurringEnabled(id, false);
      replaceReminder(updated);
      toast("Repetição desativada. O lembrete ficou em pausa.", "ok");
    } else if (action === "resume-recurrence") {
      const updated = await setRecurringEnabled(id, true);
      replaceReminder(updated);
      toast("Repetição reativada.", "ok");
    } else if (action === "chewing") {
      const updated = await toggleChewing(id, !r.chewing);
      replaceReminder(updated);
      toast(r.chewing ? "Modo Chiclete desativado." : "Modo Chiclete ativado.", "ok");
    } else if (action === "snooze") {
      const updated = await snoozeReminder(id);
      replaceReminder(updated);
      toast("Adiado 10 min — sem penalização.", "ok");
    } else if (action === "ignore") {
      const updated = await simulateIgnore(id);
      replaceReminder(updated);
      toast(`Ignorado ${updated.ignoreCount}× — prioridade: ${priorityLabel(updated.priority)}`, "ok");
      loadChicleteStats();
    } else if (action === "share") {
      openShareModal(r);
    } else if (action === "edit") {
      openEditModal(r);
    } else if (action === "delete") {
      if (!(await showConfirm("Eliminar lembrete", `Eliminar «${r.title}»? Esta ação não pode ser anulada.`, { confirmText: "Eliminar", danger: true }))) return;
      await deleteReminder(id);
      removeReminder(id);
      toast("Lembrete eliminado.", "ok");
    }
  } catch (err) {
    toast(err.message || "Operação falhou.", "err");
  }
}

function memberInitial(email) {
  const local = (email || "").split("@")[0];
  return (local.charAt(0) || "?").toUpperCase();
}

function renderGroupInvites() {
  const panel = $("group-invites-panel");
  const ul = $("group-invite-list");
  if (!panel || !ul) return;
  const countLabel = allGroupInvites.length === 1 ? "1 convite" : `${allGroupInvites.length} convites`;
  $("group-invite-count").textContent = countLabel;
  if (!allGroupInvites.length) {
    panel.classList.add("hidden");
    ul.innerHTML = "";
    return;
  }
  panel.classList.remove("hidden");
  ul.innerHTML = allGroupInvites
    .map(
      (inv) => `
    <li class="group-invite-card">
      <div class="group-invite-body">
        <strong>${escapeHtml(inv.groupName)}</strong>
        <p>Convite de ${escapeHtml(inv.invitedByName || inv.invitedByEmail)}</p>
        <span class="group-invite-meta">${formatSchedule(inv.createdAt)}</span>
      </div>
      <div class="group-invite-actions">
        <button type="button" class="btn btn-primary btn-sm" data-group-invite-action="accept" data-invite-id="${inv.id}">Aceitar</button>
        <button type="button" class="btn btn-ghost btn-sm" data-group-invite-action="reject" data-invite-id="${inv.id}">Recusar</button>
      </div>
    </li>`
    )
    .join("");
}

function renderSentInvites(groupId, invites) {
  if (!invites.length) {
    return '<p class="group-pending-empty">Nenhum convite pendente enviado.</p>';
  }
  return invites
    .map(
      (inv) => `
    <div class="group-pending-item">
      <div>
        <strong>${escapeHtml(inv.invitedEmail)}</strong>
        <span class="group-invite-meta">${formatSchedule(inv.createdAt)} · aguardando</span>
      </div>
      <div class="group-pending-actions">
        <button type="button" class="btn btn-ghost btn-sm" data-group-action="share-wa" data-token="${attrVal(inv.inviteToken)}" data-group-name="${attrVal(inv.groupName)}" data-email="${attrVal(inv.invitedEmail)}">WhatsApp</button>
        <button type="button" class="btn btn-ghost btn-sm" data-group-action="share-email" data-token="${attrVal(inv.inviteToken)}" data-group-name="${attrVal(inv.groupName)}" data-email="${attrVal(inv.invitedEmail)}">E-mail</button>
        <button type="button" class="btn btn-ghost btn-sm" data-group-action="copy-link" data-token="${attrVal(inv.inviteToken)}" data-group-name="${attrVal(inv.groupName)}">Copiar link</button>
        <button type="button" class="btn btn-ghost btn-sm" data-group-action="cancel-invite" data-group-id="${groupId}" data-invite-id="${inv.id}">Cancelar</button>
      </div>
    </div>`
    )
    .join("");
}

function renderGroups() {
  const ul = $("group-list");
  const empty = $("empty-groups");
  ul.innerHTML = "";
  const countLabel = allGroups.length === 1 ? "1 grupo" : `${allGroups.length} grupos`;
  $("group-count").textContent = countLabel;

  if (!allGroups.length) {
    empty.classList.remove("hidden");
    updateGroupsMobileCreate();
    renderGroupInvites();
    return;
  }
  empty.classList.add("hidden");
  updateGroupsMobileCreate();

  const myEmail = (userProfile?.email || emailFromToken() || "").toLowerCase();

  for (const g of allGroups) {
    const li = document.createElement("li");
    const isExpanded = expandedGroups.has(String(g.id));
    li.className = "group-card" + (isExpanded ? " group-card-expanded" : "");
    li.dataset.groupId = g.id;
    const memberRows = (g.members && g.members.length ? g.members : (g.memberEmails || []).map((e) => ({ email: e, name: e, admin: (g.ownerEmail || "").toLowerCase() === e.toLowerCase() })));
    const memberCount = memberRows.length;
    const countText = memberCount === 1 ? "1 membro" : `${memberCount} membros`;
    const isOwner = (g.ownerEmail || "").toLowerCase() === myEmail;
    const members = memberRows
      .map((m) => {
        const e = m.email;
        const isMe = e.toLowerCase() === myEmail;
        const adminBadge = m.admin ? '<span class="member-admin-badge">Admin</span>' : "";
        const label = isMe ? `${escapeHtml(e)} (tu)` : escapeHtml(e);
        const removeBtn = isOwner && !isMe
          ? `<button type="button" class="member-remove" data-group-action="remove-member" data-group-id="${g.id}" data-email="${attrVal(e)}" title="Remover membro">×</button>`
          : "";
        return `<span class="member-tag${isMe ? " member-tag-me" : ""}">
          <span class="member-avatar" aria-hidden="true">${memberInitial(e)}</span>${label}${adminBadge}${removeBtn}
        </span>`;
      })
      .join("");
    const sentInvites = groupSentInvites[g.id] || [];
    const managePanel = isExpanded
      ? `<div class="group-manage-panel">
          <h4>Membros ativos</h4>
          <div class="member-list">${members || '<span class="member-tag">Sem membros</span>'}</div>
          ${isOwner ? `<h4>Convites pendentes (${sentInvites.length})</h4>
          <div class="group-pending-list">${renderSentInvites(g.id, sentInvites)}</div>
          <div class="group-manage-actions">
            <button type="button" class="btn btn-ghost btn-sm" data-group-action="rename" data-group-id="${g.id}" data-name="${attrVal(g.name)}">Renomear</button>
            <button type="button" class="btn btn-ghost btn-sm" data-group-action="transfer" data-group-id="${g.id}">Transferir admin</button>
            <button type="button" class="btn btn-ghost btn-sm group-danger" data-group-action="delete" data-group-id="${g.id}">Apagar grupo</button>
          </div>` : `<div class="group-manage-actions">
            <button type="button" class="btn btn-ghost btn-sm" data-group-action="leave" data-group-id="${g.id}">Sair do grupo</button>
          </div>`}
        </div>`
      : "";
    li.innerHTML = `
      <div class="group-card-head">
        <span class="group-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </span>
        <div class="group-card-title">
          <h3>${escapeHtml(g.name)}</h3>
          <span class="group-member-count">${countText}${isOwner ? ' · <span class="member-admin-badge">Admin</span>' : ""}</span>
        </div>
        <button type="button" class="btn btn-ghost btn-sm group-toggle" data-group-action="toggle" data-group-id="${g.id}" aria-expanded="${isExpanded}">
          ${isExpanded ? "Fechar" : "Gerir"}
        </button>
      </div>
      ${!isExpanded ? `<div class="member-list member-list-compact">${members}</div>` : ""}
      <form class="group-add" data-group-id="${g.id}">
        <input type="email" name="email" required placeholder="E-mail do convidado" inputmode="email" autocomplete="email" />
        <button type="submit" class="btn btn-secondary btn-sm">Convidar</button>
      </form>
      ${managePanel}
    `;
    ul.appendChild(li);
  }
  renderGroupInvites();
}

async function loadGroupSentInvites(groupId) {
  const gid = validId(groupId);
  if (!gid) return [];
  try {
    const res = await api(`/api/groups/${gid}/invites/sent`);
    if (!res.ok) return [];
    const data = await res.json();
    groupSentInvites[gid] = data;
    return data;
  } catch {
    return [];
  }
}

async function loadGroups() {
  showGroupSkeleton();
  try {
    const [groupsRes, invitesRes] = await Promise.all([api("/api/groups"), api("/api/groups/invites")]);
    if (!groupsRes.ok) {
      toast(await parseError(groupsRes), "err");
      return;
    }
    allGroups = await groupsRes.json();
    allGroupInvites = invitesRes.ok ? await invitesRes.json() : [];
    renderGroups();
    refreshReminderGroupAssign();
  } catch (e) {
    if (e.message !== "unauthorized") toast("Erro ao carregar grupos.", "err");
  }
}

async function doLogin(e) {
  e.preventDefault();
  const msg = $("msg-auth");
  const btn = $("btn-login");
  hideMsg(msg);
  setBtnLoading(btn, true);
  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: $("login-email").value.trim(),
        password: $("login-password").value,
      }),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    const data = await res.json();
    setToken(data.token);
    showLoader();
    showApp($("login-email").value.trim() || emailFromToken());
    await Promise.all([loadReminders(), loadProfile(), loadGroups()]);
    startNotificationPolling();
    await tryAcceptPendingInviteToken();
  } catch {
    showMsg(msg, "Não foi possível conectar ao servidor.", "err");
  } finally {
    setBtnLoading(btn, false);
    hideLoader();
  }
}

async function doForgotPassword(e) {
  e.preventDefault();
  const msg = $("msg-auth");
  const btn = $("btn-forgot");
  hideMsg(msg);
  const email = $("forgot-email").value.trim();
  const password = $("forgot-password").value;
  if (password !== $("forgot-password2").value) {
    showMsg(msg, "As senhas não coincidem.", "err");
    return;
  }
  setBtnLoading(btn, true);
  try {
    const res = await fetch("/api/auth/forgot-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, newPassword: password }),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    $("login-email").value = email;
    $("login-password").value = "";
    $("form-forgot").reset();
    switchTab("login");
    showMsg(msg, "Senha redefinida. Entra com a nova senha.", "ok");
  } catch {
    showMsg(msg, "Não foi possível conectar ao servidor.", "err");
  } finally {
    setBtnLoading(btn, false);
  }
}

async function doRegister(e) {
  e.preventDefault();
  const msg = $("msg-auth");
  const btn = $("btn-register");
  hideMsg(msg);
  const email = $("reg-email").value.trim();
  const password = $("reg-password").value;
  if (password !== $("reg-password2").value) {
    showMsg(msg, "As senhas não coincidem.", "err");
    return;
  }
  setBtnLoading(btn, true);
  try {
    const body = {
      name: $("reg-name").value.trim(),
      email,
      password,
    };
    if (pendingInviteToken) body.inviteToken = pendingInviteToken;
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      showMsg(msg, await parseError(res), "err");
      return;
    }
    const data = await res.json();
    setToken(data.token);
    pendingInviteToken = null;
    const url = new URL(window.location.href);
    url.searchParams.delete("invite");
    window.history.replaceState({}, "", url.pathname + url.search);
    showLoader();
    showApp(email || emailFromToken());
    await Promise.all([loadReminders(), loadProfile(), loadGroups()]);
    startNotificationPolling();
  } catch {
    showMsg(msg, "Não foi possível conectar ao servidor.", "err");
  } finally {
    setBtnLoading(btn, false);
    hideLoader();
  }
}

async function doCreateReminder(e) {
  e.preventDefault();
  const btn = $("btn-create-reminder");
  const title = $("nr-title").value.trim();
  const scheduledAt = toIsoLocal($("nr-when").value);
  const chewing = $("nr-chewing").checked;
  if (!title || !scheduledAt) {
    toast("Preenche título e data/hora.", "err");
    return;
  }
  let recurrence;
  try {
    recurrence = readRecurrenceFromForm();
  } catch (err) {
    toast(err.message, "err");
    return;
  }

  const groupIdVal = $("nr-group")?.value || "";
  const assigneeEmail = ($("nr-assignee")?.value || "").trim();
  const group = groupIdVal ? allGroups.find((g) => String(g.id) === groupIdVal) : null;
  let confirmMsg = `Criar o lembrete «${title}»?`;
  if (assigneeEmail && group) {
    confirmMsg = `Criar «${title}» e designar a ${assigneeEmail} no grupo «${group.name}»?`;
  } else if (group) {
    confirmMsg = `Criar o lembrete «${title}» no grupo «${group.name}» (sem designação)?`;
  }
  if (!(await showConfirm("Criar lembrete", confirmMsg))) return;

  setBtnLoading(btn, true);
  try {
    const body = {
      title,
      description: $("nr-desc").value.trim() || null,
      scheduledAt,
      chewing,
      intervalMinutes: chewing ? (Number($("nr-interval").value) || 30) : null,
      priority: $("nr-priority").value,
      recurrenceType: recurrence.recurrenceType,
      recurrenceDays: recurrence.recurrenceDays,
      recurringEnabled: recurrence.recurringEnabled,
    };
    if (groupIdVal) body.groupId = Number(groupIdVal);
    if (assigneeEmail) body.assigneeEmail = assigneeEmail;

    const res = await api("/api/reminders", {
      method: "POST",
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(await parseError(res));
    $("nr-title").value = "";
    $("nr-desc").value = "";
    $("nr-when").value = defaultDateTimeLocal();
    $("nr-chewing").checked = false;
    toggleIntervalField($("nr-chewing"), "nr-interval-wrap");
    resetRecurrenceForm();
    if ($("nr-group")) $("nr-group").value = "";
    populateAssigneeOptions("");
    closeMobileSheet($("side-create-reminder"));
    toast(
      assigneeEmail ? "Lembrete criado e designado ao membro." : "Lembrete criado — o Chiclete já está de olho.",
      "ok"
    );
    await loadReminders();
  } catch (err) {
    if (err.message !== "unauthorized") toast(err.message || "Falha ao criar lembrete.", "err");
  } finally {
    setBtnLoading(btn, false);
  }
}

async function doCreateGroup(e) {
  e.preventDefault();
  const btn = $("btn-create-group");
  const name = $("group-name").value.trim();
  const inviteEmail = $("group-invite")?.value.trim() || "";
  if (!name) {
    toast("Dá um nome ao grupo.", "err");
    return;
  }
  const confirmMsg = inviteEmail
    ? `Criar o grupo «${name}» e enviar convite para ${inviteEmail}?`
    : `Criar o grupo «${name}»?`;
  if (!(await showConfirm("Criar grupo", confirmMsg))) return;
  setBtnLoading(btn, true);
  try {
    const res = await api("/api/groups", {
      method: "POST",
      body: JSON.stringify({ name }),
    });
    if (!res.ok) throw new Error(await parseError(res));
    const created = await res.json();

    if (inviteEmail) {
      const memberRes = await api("/api/groups/" + created.id + "/members", {
        method: "POST",
        body: JSON.stringify({ email: inviteEmail }),
      });
      if (!memberRes.ok) {
        $("group-name").value = "";
        $("group-invite").value = "";
        closeMobileSheet($("side-create-group"));
        toast("Grupo criado, mas o convite falhou: " + (await parseError(memberRes)), "err");
        await loadGroups();
        return;
      }
    }

    $("group-name").value = "";
    if ($("group-invite")) $("group-invite").value = "";
    closeMobileSheet($("side-create-group"));
    toast(
      inviteEmail ? "Grupo criado e convite enviado." : "Grupo criado — hora de partilhar lembretes.",
      "ok"
    );
    await loadGroups();
  } catch (err) {
    if (err.message !== "unauthorized") toast(err.message || "Falha ao criar grupo.", "err");
  } finally {
    setBtnLoading(btn, false);
  }
}

async function handleGroupAdd(e) {
  const form = e.target.closest(".group-add");
  if (!form) return;
  e.preventDefault();
  const groupId = form.dataset.groupId;
  const email = form.email.value.trim();
  if (!email) {
    toast("Introduz o e-mail do membro.", "err");
    return;
  }
  if (!(await showConfirm("Convidar membro", `Enviar convite para ${email}?`))) return;
  const submitBtn = form.querySelector('button[type="submit"]');
  setBtnLoading(submitBtn, true);
  try {
    const res = await api("/api/groups/" + groupId + "/members", {
      method: "POST",
      body: JSON.stringify({ email }),
    });
    if (!res.ok) throw new Error(await parseError(res));
    form.reset();
    toast("Convite enviado — aguarda a aceitação.", "ok");
    const gid = validId(groupId);
    if (gid && expandedGroups.has(String(gid))) await loadGroupSentInvites(gid);
    await loadGroups();
  } catch (err) {
    toast(err.message || "Falha ao adicionar membro.", "err");
  } finally {
    setBtnLoading(submitBtn, false);
  }
}

async function handleGroupInviteAction(e) {
  const btn = e.target.closest("[data-group-invite-action]");
  if (!btn) return;
  e.preventDefault();
  e.stopPropagation();
  const action = btn.dataset.groupInviteAction;
  const inviteId = validId(btn.dataset.inviteId);
  if (!inviteId) {
    toast("Convite inválido.", "err");
    return;
  }
  const isAccept = action === "accept";
  const ok = await showConfirm(
    isAccept ? "Aceitar convite" : "Recusar convite",
    isAccept ? "Entrar neste grupo?" : "Recusar este convite de grupo?",
    isAccept ? {} : { confirmText: "Recusar", danger: true }
  );
  if (!ok) return;
  setBtnLoading(btn, true);
  try {
    const endpoint = action === "accept" ? "accept" : "reject";
    const res = await api(`/api/groups/invites/${inviteId}/${endpoint}`, { method: "POST" });
    if (!res.ok) throw new Error(await parseError(res));
    toast(action === "accept" ? "Convite aceite — bem-vindo ao grupo." : "Convite recusado.", action === "accept" ? "ok" : "info");
    await Promise.all([loadGroups(), loadNotifications()]);
  } catch (err) {
    toast(err.message || "Falha ao responder ao convite.", "err");
  } finally {
    setBtnLoading(btn, false);
  }
}

async function handleGroupAction(e) {
  const btn = e.target.closest("[data-group-action]");
  if (!btn) return;
  const action = btn.dataset.groupAction;
  const groupId = validId(btn.dataset.groupId);
  if (!groupId && !["share-wa", "share-email", "copy-link"].includes(action)) return;
  try {
    if (action === "toggle") {
      const key = String(groupId);
      if (expandedGroups.has(key)) expandedGroups.delete(key);
      else {
        expandedGroups.add(key);
        await loadGroupSentInvites(groupId);
      }
      renderGroups();
      return;
    }
    if (action === "share-wa") {
      shareInviteWhatsApp({ inviteToken: btn.dataset.token, groupName: btn.dataset.groupName, invitedEmail: btn.dataset.email });
      return;
    }
    if (action === "share-email") {
      shareInviteEmail({ inviteToken: btn.dataset.token, groupName: btn.dataset.groupName, invitedEmail: btn.dataset.email });
      return;
    }
    if (action === "copy-link") {
      await copyInviteLink({ inviteToken: btn.dataset.token, groupName: btn.dataset.groupName });
      return;
    }
    if (action === "cancel-invite") {
      const inviteId = validId(btn.dataset.inviteId);
      if (!inviteId) return;
      if (!(await showConfirm("Cancelar convite", "Cancelar este convite pendente?", { confirmText: "Cancelar convite", danger: true }))) return;
      const res = await api(`/api/groups/${groupId}/invites/${inviteId}`, { method: "DELETE" });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Convite cancelado.", "ok");
      await loadGroupSentInvites(groupId);
      renderGroups();
      return;
    }
    if (action === "rename") {
      const nextName = window.prompt("Novo nome do grupo:", btn.dataset.name || "");
      if (!nextName || !nextName.trim()) return;
      if (!(await showConfirm("Renomear grupo", `Alterar o nome para «${nextName.trim()}»?`))) return;
      const res = await api("/api/groups/" + groupId, {
        method: "PATCH",
        body: JSON.stringify({ name: nextName.trim() }),
      });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Grupo renomeado.", "ok");
    } else if (action === "transfer") {
      const email = window.prompt("E-mail do novo admin (precisa ser membro):");
      if (!email || !email.trim()) return;
      if (!(await showConfirm("Transferir admin", `Tornar ${email.trim()} admin deste grupo?`))) return;
      const res = await api("/api/groups/" + groupId + "/transfer-owner", {
        method: "POST",
        body: JSON.stringify({ email: email.trim() }),
      });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Administração transferida.", "ok");
    } else if (action === "delete") {
      if (!(await showConfirm("Apagar grupo", "Apagar este grupo para todos os membros? Esta ação não pode ser anulada.", { confirmText: "Apagar", danger: true }))) return;
      const res = await api("/api/groups/" + groupId, { method: "DELETE" });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Grupo apagado.", "ok");
    } else if (action === "leave") {
      if (!(await showConfirm("Sair do grupo", "Tens a certeza que queres sair deste grupo?", { confirmText: "Sair", danger: true }))) return;
      const res = await api("/api/groups/" + groupId + "/leave", { method: "POST" });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Saíste do grupo.", "ok");
    } else if (action === "remove-member") {
      const email = btn.dataset.email;
      if (!(await showConfirm("Remover membro", `Remover ${email} deste grupo?`, { confirmText: "Remover", danger: true }))) return;
      const res = await api("/api/groups/" + groupId + "/members/" + encodeURIComponent(email), { method: "DELETE" });
      if (!res.ok) throw new Error(await parseError(res));
      toast("Membro removido.", "ok");
    }
    await loadGroups();
  } catch (err) {
    toast(err.message || "Falha na operação do grupo.", "err");
  }
}

async function tryAcceptPendingInviteToken() {
  if (!pendingInviteToken || !getToken()) return;
  try {
    const res = await api("/api/invites/token/" + encodeURIComponent(pendingInviteToken) + "/accept", { method: "POST" });
    if (res.ok) {
      toast("Convite aceite — bem-vindo ao grupo.", "ok");
      pendingInviteToken = null;
      const url = new URL(window.location.href);
      url.searchParams.delete("invite");
      window.history.replaceState({}, "", url.pathname + url.search);
      await loadGroups();
    }
  } catch (_) {}
}

async function loadInvitePreview() {
  const banner = $("invite-preview-banner");
  if (!pendingInviteToken || !banner) return;
  try {
    const res = await fetch("/api/invites/token/" + encodeURIComponent(pendingInviteToken));
    if (!res.ok) return;
    const preview = await res.json();
    banner.classList.remove("hidden");
    banner.innerHTML = `
      <strong>Convite para ${escapeHtml(preview.groupName)}</strong>
      <p>${escapeHtml(preview.invitedByName)} convidou-te para este grupo no Chiclete.</p>
      <p class="invite-preview-hint">${preview.requiresRegistration ? "Cria conta ou entra para aceitar." : "Entra para aceitar o convite."}</p>
    `;
    if (preview.requiresRegistration) switchTab("register");
  } catch (_) {}
}

function captureInviteFromUrl() {
  const token = new URLSearchParams(window.location.search).get("invite");
  if (token) pendingInviteToken = token.trim();
}

function flashAlertBanner() {
  const root = $("alert-banner-root");
  if (!root) return;
  root.classList.add("alert-banner-flash");
  window.setTimeout(() => root.classList.remove("alert-banner-flash"), 1400);
}

function focusActiveAlert() {
  toggleNotificationDrawer(false);
  const root = $("alert-banner-root");
  if (!root?.classList.contains("has-alerts")) {
    toast("Tudo tranquilo — nenhum alerta por agora.", "ok");
    return;
  }
  root.scrollIntoView({ behavior: "smooth", block: "nearest" });
  flashAlertBanner();
}

function toggleNotificationDrawer(force) {
  const drawer = $("notification-drawer");
  if (!drawer) return;
  const open = typeof force === "boolean" ? force : drawer.classList.contains("hidden");
  drawer.classList.toggle("hidden", !open);
  drawer.setAttribute("aria-hidden", open ? "false" : "true");
}

function doLogout() {
  showConfirm("Terminar sessão", "Tens a certeza que queres sair da conta?", { confirmText: "Sair", danger: true }).then((ok) => {
    if (!ok) return;
    clearToken();
  stopNotificationPolling();
  allReminders = [];
  allGroups = [];
  allGroupInvites = [];
  groupSentInvites = {};
  expandedGroups = new Set();
  userProfile = null;
  $("login-email").value = "";
  $("login-password").value = "";
  hideMsg($("msg-auth"));
  closeMobileSheet($("side-create-reminder"));
  closeMobileSheet($("side-create-group"));
  switchView("reminders");
  showAuth();
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  $("tab-login").addEventListener("click", () => switchTab("login"));
  $("tab-register").addEventListener("click", () => switchTab("register"));
  $("auth-root")?.addEventListener("click", handleSocialClick);
  $("link-forgot").addEventListener("click", showForgot);
  $("back-to-login").addEventListener("click", backToLogin);
  $("toggle-login-password")?.addEventListener("click", toggleLoginPasswordVisibility);
  $("form-login").addEventListener("submit", doLogin);
  $("form-forgot").addEventListener("submit", doForgotPassword);
  $("form-register").addEventListener("submit", doRegister);
  $("form-new").addEventListener("submit", doCreateReminder);
  $("form-group").addEventListener("submit", doCreateGroup);
  $("btn-logout").addEventListener("click", doLogout);

  $("nav-reminders").addEventListener("click", () => switchView("reminders"));
  $("nav-groups").addEventListener("click", () => switchView("groups"));
  $("nav-profile").addEventListener("click", () => switchView("profile"));
  $("bn-reminders").addEventListener("click", () => switchView("reminders"));
  $("bn-groups").addEventListener("click", () => switchView("groups"));
  $("bn-profile")?.addEventListener("click", () => switchView("profile"));
  $("form-profile").addEventListener("submit", saveProfile);
  $("alert-banner-root").addEventListener("click", handleAlertAction);
  $("notification-drawer")?.addEventListener("click", (e) => {
    if (e.target.closest("[data-drawer-go-groups]")) {
      switchView("groups");
      toggleNotificationDrawer(false);
      return;
    }
    handleAlertAction(e);
  });
  $("btn-notifications").addEventListener("click", () => {
    const invites = unreadNotifications.filter(isGroupInviteNotification);
    if (invites.length) {
      const drawer = $("notification-drawer");
      const open = drawer?.classList.contains("hidden");
      toggleNotificationDrawer(open);
      if (open) renderNotificationDrawer();
      return;
    }
    focusActiveAlert();
  });
  $("btn-close-notifications")?.addEventListener("click", () => toggleNotificationDrawer(false));
  $("fab-add").addEventListener("click", openCreateSheet);
  $("groups-mobile-create")?.addEventListener("click", openCreateSheet);
  $("dash-toggle")?.addEventListener("click", toggleChicleteDashboard);
  $("empty-create-btn").addEventListener("click", openCreateSheet);
  $("empty-group-btn").addEventListener("click", openCreateSheet);

  $("reminder-list").addEventListener("click", handleReminderAction);
  $("reminder-list").addEventListener("change", (e) => {
    if (e.target.matches(".reminder-check")) handleReminderAction(e);
  });

  $("group-list").addEventListener("submit", handleGroupAdd);
  $("group-list").addEventListener("click", handleGroupAction);
  $("group-invite-list")?.addEventListener("click", handleGroupInviteAction);

  $("search-input").addEventListener("input", (e) => {
    searchQuery = e.target.value.trim();
    renderReminders();
  });

  document.querySelectorAll(".stat-filter").forEach((btn) => {
    btn.addEventListener("click", () => setReminderFilter(btn.dataset.filter));
  });

  $("nr-when").value = defaultDateTimeLocal();
  $("nr-chewing").addEventListener("change", () => toggleIntervalField($("nr-chewing"), "nr-interval-wrap"));
  $("nr-repeat")?.addEventListener("change", syncRecurrenceForm);
  document.querySelectorAll('input[name="nr-recurrence-type"]').forEach((input) => {
    input.addEventListener("change", syncRecurrenceForm);
  });
  $("nr-custom-days")?.addEventListener("click", (e) => {
    const chip = e.target.closest(".day-chip");
    if (!chip) return;
    chip.classList.toggle("active");
  });
  $("nr-group")?.addEventListener("change", (e) => populateAssigneeOptions(e.target.value));

  await runSplash();

  window.addEventListener("resize", updateGroupsMobileCreate);

  captureInviteFromUrl();
  await loadInvitePreview();

  const t = getToken();
  if (t) {
    showLoader();
    showApp(emailFromToken());
    Promise.all([loadReminders(), loadProfile(), loadGroups()])
      .then(() => {
        startNotificationPolling();
        return tryAcceptPendingInviteToken();
      })
      .finally(hideLoader);
  } else {
    showAuth();
  }
});
