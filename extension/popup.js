import { call, login, logout } from './api.js';
const root = document.getElementById('app');
// Construction DOM sans innerHTML : les données serveur ne sont jamais interprétées comme du HTML (anti-XSS).
const h = (tag, props = {}, ...kids) => { const el = document.createElement(tag);
  for (const [k, v] of Object.entries(props)) { if (k.startsWith('on')) el.addEventListener(k.slice(2), v); else if (k === 'class') el.className = v; else el.setAttribute(k, v); }
  for (const c of kids.flat()) if (c != null) el.append(c instanceof Node ? c : document.createTextNode(String(c))); return el; };
const mad = v => new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(Number(v));
const ST = { DRAFT: 'Brouillon', SUBMITTED: 'Soumise', APPROVED: 'Approuvée', REJECTED: 'Rejetée', INVOICED: 'Facturée' };
const UNITS = { M2: 'm²', M3: 'm³', ML: 'ml', UNIT: 'unité', HOUR: 'heure', DAY: 'jour' };
const today = () => new Date().toLocaleDateString('sv-SE', { timeZone: 'Africa/Casablanca' });
const note = (t, k = 'err') => h('div', { class: 'msg ' + k, role: 'alert' }, t);
const badge = () => chrome.runtime.sendMessage({ type: 'refresh-badge' });
const MGR = ['COMPANY_ADMIN', 'PROJECT_MANAGER'], FIN = [...MGR, 'ACCOUNTANT'], LOG = [...MGR, 'FIELD_WORKER'];

function loginView(error) {
  const email = h('input', { type: 'email', required: '', autocomplete: 'username' }), pwd = h('input', { type: 'password', required: '', autocomplete: 'current-password' }), box = h('div');
  const form = h('form', { class: 'login', onsubmit: async e => { e.preventDefault(); box.replaceChildren();
    try { await login(email.value, pwd.value); badge(); boot(); } catch (err) { box.replaceChildren(note(err.message)); } } },
    h('b', {}, 'Connexion à MizanFlow'), box, h('label', {}, 'Email'), email, h('label', {}, 'Mot de passe'), pwd, h('button', { class: 'btn full' }, 'Se connecter'),
    h('p', {}, h('a', { href: '#', onclick: e => { e.preventDefault(); chrome.runtime.openOptionsPage(); } }, 'Paramètres du serveur')));
  root.replaceChildren(h('header', {}, h('b', {}, 'Mizan', h('span', {}, 'Flow'))), form); if (error) box.replaceChildren(note(error));
}

async function boot() {
  const { user } = await chrome.storage.local.get('user'); if (!user) return loginView();
  const tabs = [['summary', 'Résumé', FIN], ['review', 'À valider', MGR], ['log', 'Saisir', LOG], ['mine', 'Mes prestations', ['FIELD_WORKER']]].filter(t => t[2].includes(user.role));
  if (!tabs.length) return root.replaceChildren(h('header', {}, h('b', {}, 'Mizan', h('span', {}, 'Flow'))), h('div', { class: 'pane' }, h('div', { class: 'empty' }, 'Ce rôle n\'est pas pris en charge par l\'extension. Utilisez l\'application web.'), h('button', { class: 'btn ghost full', onclick: async () => { await logout(); boot(); } }, 'Déconnexion')));
  const pane = h('div', { class: 'pane' }), nav = h('nav'); let cur = tabs[0][0];
  const open = async id => { cur = id; [...nav.children].forEach((b, i) => b.className = tabs[i][0] === id ? 'on' : ''); pane.replaceChildren(h('div', { class: 'empty' }, 'Chargement…'));
    try { pane.replaceChildren(await VIEWS[id](user, () => open(id))); } catch (e) { if (e.auth) return loginView(e.message); pane.replaceChildren(note(e.message)); } };
  tabs.forEach(([id, label]) => nav.append(h('button', { onclick: () => open(id) }, label)));
  root.replaceChildren(h('header', {}, h('b', {}, 'Mizan', h('span', {}, 'Flow')), h('span', {}, h('small', {}, user.fullName + ' '), h('a', { href: '#', style: 'color:#93c5fd;font-size:11px', onclick: async e => { e.preventDefault(); await logout(); badge(); boot(); } }, 'Quitter'))), nav, pane);
  open(cur);
}
const projectsMap = async () => Object.fromEntries((await call('/api/v1/projects?size=100')).content.map(p => [p.id, p.name]));

const VIEWS = {
  async summary() { const d = await call('/api/v1/dashboard'), k = (l, v) => h('div', { class: 'kpi' }, h('small', {}, l), h('div', {}, v));
    return h('div', { class: 'kpis' }, k('Projets actifs', d.activeProjects), k('Total facturé', mad(d.totalInvoiced)), k('Total encaissé', mad(d.totalCollected)), k('Reste à encaisser', mad(d.totalOutstanding)), k('Montant échu', mad(d.overdueAmount)), k('Factures en retard', d.overdueInvoices)); },

  async review(_u, reload) { const [pm, page] = await Promise.all([projectsMap(), call('/api/v1/work-logs?size=100')]); const list = page.content.filter(w => w.status === 'SUBMITTED');
    if (!list.length) return h('div', { class: 'empty' }, 'Aucune prestation à valider 🎉'); const out = h('div'), err = h('div');
    const act = async (id, a, body) => { try { await call(`/api/v1/work-logs/${id}/${a}`, { method: 'POST', body: body || {} }); badge(); reload(); } catch (e) { err.replaceChildren(note(e.message)); } };
    list.forEach(w => { const acts = h('div', { class: 'acts' });
      const approve = h('button', { class: 'btn', onclick: () => act(w.id, 'approve') }, 'Approuver');
      const reject = h('button', { class: 'btn ghost', onclick: () => { const r = h('input', { placeholder: 'Motif du rejet (obligatoire)' });
        acts.replaceChildren(r, h('button', { class: 'btn danger', onclick: () => r.value.trim() ? act(w.id, 'reject', { comment: r.value.trim() }) : r.focus() }, 'Rejeter')); } }, 'Rejeter');
      acts.append(approve, reject); out.append(h('div', { class: 'item' }, h('div', { class: 'top' }, h('span', {}, w.serviceType), h('span', {}, `${w.quantity} ${UNITS[w.unit]}`)), h('small', {}, `${pm[w.projectId] || '—'} · ${w.workDate}`), acts)); });
    return h('div', {}, err, out); },

  async mine(_u, reload) { const [pm, page] = await Promise.all([projectsMap(), call('/api/v1/work-logs?size=30')]); if (!page.content.length) return h('div', { class: 'empty' }, 'Aucune prestation enregistrée.'); const err = h('div');
    const submit = async id => { try { await call(`/api/v1/work-logs/${id}/submit`, { method: 'POST' }); badge(); reload(); } catch (e) { err.replaceChildren(note(e.message)); } };
    return h('div', {}, err, page.content.map(w => h('div', { class: 'item' }, h('div', { class: 'top' }, h('span', {}, w.serviceType), h('span', { class: 'badge ' + w.status }, ST[w.status])),
      h('small', {}, `${pm[w.projectId] || '—'} · ${w.workDate} · ${w.quantity} ${UNITS[w.unit]}`), w.rejectionReason ? h('div', { class: 'msg err' }, 'Motif : ' + w.rejectionReason) : null,
      ['DRAFT', 'REJECTED'].includes(w.status) ? h('div', { class: 'acts' }, h('button', { class: 'btn', onclick: () => submit(w.id) }, 'Soumettre')) : null))); },

  async log() { const pm = await projectsMap(); if (!Object.keys(pm).length) return h('div', { class: 'empty' }, 'Aucun projet. Créez-en un dans l\'application web.');
    const f = { project: h('select', { required: '' }, h('option', { value: '' }, '— Projet —'), Object.entries(pm).map(([id, n]) => h('option', { value: id }, n))), date: h('input', { type: 'date', value: today(), required: '' }),
      service: h('input', { required: '', placeholder: 'ex. Coulage dalle' }), qty: h('input', { type: 'number', step: '0.001', min: '0.001', required: '' }), unit: h('select', {}, Object.entries(UNITS).map(([v, l]) => h('option', { value: v }, l))), price: h('input', { type: 'number', step: '0.01', min: '0', value: '0' }), now: h('input', { type: 'checkbox', checked: '' }) };
    const box = h('div'); return h('form', { onsubmit: async e => { e.preventDefault(); box.replaceChildren();
      try { const w = await call('/api/v1/work-logs', { method: 'POST', body: { projectId: f.project.value, workDate: f.date.value, serviceType: f.service.value.trim(), quantity: Number(f.qty.value), unit: f.unit.value, unitPrice: Number(f.price.value || 0) } });
        if (f.now.checked) await call(`/api/v1/work-logs/${w.id}/submit`, { method: 'POST' }); badge(); e.target.reset(); f.date.value = today(); box.replaceChildren(note(f.now.checked ? 'Prestation soumise pour validation.' : 'Brouillon enregistré.', 'ok')); }
      catch (err) { box.replaceChildren(note(err.message)); } } }, box, h('label', {}, 'Projet'), f.project, h('div', { class: 'row' }, h('div', {}, h('label', {}, 'Date'), f.date), h('div', {}, h('label', {}, 'Prestation'), f.service)),
      h('div', { class: 'row' }, h('div', {}, h('label', {}, 'Quantité'), f.qty), h('div', {}, h('label', {}, 'Unité'), f.unit)), h('label', {}, 'Prix unitaire HT (MAD)'), f.price,
      h('label', { class: 'check' }, f.now, 'Soumettre immédiatement pour validation'), h('button', { class: 'btn full' }, 'Enregistrer')); },
};
boot();
