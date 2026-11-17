import { call } from './api.js';
// Badge : managers = prestations à valider ; collaborateurs = prestations rejetées à corriger.
async function refreshBadge() {
  try {
    const { user } = await chrome.storage.local.get('user');
    if (!user) return chrome.action.setBadgeText({ text: '' });
    const s = await call('/api/v1/work-logs/summary'), field = user.role === 'FIELD_WORKER';
    const n = field ? (s.REJECTED || 0) : ['COMPANY_ADMIN', 'PROJECT_MANAGER'].includes(user.role) ? (s.SUBMITTED || 0) : 0;
    await chrome.action.setBadgeBackgroundColor({ color: field ? '#DC2626' : '#2563EB' });
    await chrome.action.setBadgeText({ text: n ? String(n) : '' });
    await chrome.action.setTitle({ title: n ? `MizanFlow — ${n} ${field ? 'à corriger' : 'à valider'}` : 'MizanFlow' });
  } catch (e) { if (e.auth) chrome.action.setBadgeText({ text: '!' }); }
}
chrome.runtime.onInstalled.addListener(() => { chrome.alarms.create('badge', { periodInMinutes: 5 }); refreshBadge(); });
chrome.runtime.onStartup.addListener(refreshBadge);
chrome.alarms.onAlarm.addListener(a => { if (a.name === 'badge') refreshBadge(); });
chrome.runtime.onMessage.addListener(m => { if (m?.type === 'refresh-badge') refreshBadge(); });
