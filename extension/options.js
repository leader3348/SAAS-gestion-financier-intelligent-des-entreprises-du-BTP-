import { apiBase } from './api.js';
const u = document.getElementById('u'), m = document.getElementById('m'); apiBase().then(v => { u.value = v; });
const say = (t, k) => { const d = document.createElement('div'); d.className = 'msg ' + k; d.textContent = t; m.replaceChildren(d); };
document.getElementById('s').addEventListener('click', async () => {
  let url; try { url = new URL(u.value.trim()); if (!['http:', 'https:'].includes(url.protocol)) throw 0; } catch { return say('URL invalide.', 'err'); }
  if (url.protocol === 'http:' && !['localhost', '127.0.0.1'].includes(url.hostname)) return say('HTTPS obligatoire hors localhost.', 'err');
  const ok = await chrome.permissions.request({ origins: [url.origin + '/*'] }); if (!ok) return say('Autorisation refusée.', 'err');
  await chrome.storage.local.set({ apiBase: url.origin }); say('Enregistré. Reconnectez-vous depuis l\'extension.', 'ok');
});
