// Client API partagé (popup + service worker). Jeton d'accès en storage.session (effacé à la fermeture du navigateur), refresh en storage.local.
const DEFAULT_BASE = 'http://localhost:8080';
export const apiBase = async () => ((await chrome.storage.local.get('apiBase')).apiBase || DEFAULT_BASE).replace(/\/$/, '');
const authErr = () => Object.assign(new Error('Session expirée, reconnectez-vous'), { status: 401, auth: true });
async function raw(path, { method = 'GET', body, token } = {}) {
  const res = await fetch((await apiBase()) + path, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) }, body: body ? JSON.stringify(body) : undefined });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw Object.assign(new Error(data?.message || 'Erreur ' + res.status), { status: res.status });
  return data;
}
async function save(d) { await chrome.storage.session.set({ access: d.accessToken }); await chrome.storage.local.set({ refresh: d.refreshToken, user: { role: d.role, fullName: d.fullName } }); }
export async function login(email, password) { const d = await raw('/api/v1/auth/login', { method: 'POST', body: { email, password } }); await save(d); return d; }
export async function logout() {
  const { refresh } = await chrome.storage.local.get('refresh');
  if (refresh) await raw('/api/v1/auth/logout', { method: 'POST', body: { refreshToken: refresh } }).catch(() => {});
  await chrome.storage.session.clear(); await chrome.storage.local.remove(['refresh', 'user']);
}
// Verrou inter-contextes : la rotation du refresh token ne doit jamais être jouée deux fois en parallèle (sinon réutilisation détectée côté serveur).
const refreshAccess = stale => navigator.locks.request('mf-refresh', async () => {
  const { access } = await chrome.storage.session.get('access'); if (access && access !== stale) return access;
  const { refresh } = await chrome.storage.local.get('refresh'); if (!refresh) throw authErr();
  try { const d = await raw('/api/v1/auth/refresh', { method: 'POST', body: { refreshToken: refresh } }); await save(d); return d.accessToken; }
  catch (e) { if (e.status === 401) { await logout(); throw authErr(); } throw e; }
});
export async function call(path, opt = {}) {
  let tok = (await chrome.storage.session.get('access')).access || await refreshAccess(null);
  try { return await raw(path, { ...opt, token: tok }); }
  catch (e) { if (e.status !== 401) throw e; tok = await refreshAccess(tok); return raw(path, { ...opt, token: tok }); }
}
