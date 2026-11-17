import { useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  Building2, CreditCard, FileText, FolderKanban, HardHat, LayoutDashboard,
  LogOut, Menu, ShieldCheck, UserCog, Users, X, type LucideIcon,
} from 'lucide-react'
import { api } from '../lib/api'
import { useAuth } from '../lib/auth'
import { useI18n, type Key } from '../lib/i18n'
import { ErrorBox } from '../components/ui'

type NavItem = { to: string; icon: LucideIcon; key?: Key; label?: string; roles: string[] }

const ALL_ROLES = ['COMPANY_ADMIN', 'PROJECT_MANAGER', 'ACCOUNTANT', 'FIELD_WORKER']
const NAV: NavItem[] = [
  { to: '/app', icon: LayoutDashboard, key: 'dashboard', roles: ['COMPANY_ADMIN', 'PROJECT_MANAGER', 'ACCOUNTANT'] },
  { to: '/app/projects', icon: FolderKanban, key: 'projects', roles: ALL_ROLES },
  { to: '/app/work-logs', icon: HardHat, key: 'workLogs', roles: ALL_ROLES },
  { to: '/app/customers', icon: Users, key: 'customers', roles: ['COMPANY_ADMIN', 'PROJECT_MANAGER', 'ACCOUNTANT'] },
  { to: '/app/invoices', icon: FileText, key: 'invoices', roles: ['COMPANY_ADMIN', 'ACCOUNTANT'] },
  { to: '/app/team', icon: UserCog, label: 'Équipe', roles: ['COMPANY_ADMIN'] },
  { to: '/app/billing', icon: CreditCard, label: 'Abonnement', roles: ['COMPANY_ADMIN'] },
  { to: '/app/admin', icon: ShieldCheck, label: 'Console SaaS', roles: ['SUPER_ADMIN'] },
]

type Billing = { subscription: { status: string; trialEndsAt?: string; usable: boolean } }

export default function Shell() {
  const { session, logout } = useAuth()
  const { t, lang, setLang } = useI18n()
  const navigate = useNavigate()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const [logoutError, setLogoutError] = useState('')
  const role = session?.role ?? ''
  const isCompanyAdmin = role === 'COMPANY_ADMIN'
  const items = NAV.filter(item => item.roles.includes(role))
  const currentPage = items.find(item => item.to === location.pathname)
  const billing = useQuery({
    queryKey: ['billing'],
    enabled: isCompanyAdmin,
    queryFn: async () => (await api.get<Billing>('/billing')).data,
  })
  const subscription = billing.data?.subscription
  const trialDays = subscription?.trialEndsAt
    ? Math.max(0, Math.ceil((new Date(subscription.trialEndsAt).getTime() - Date.now()) / 864e5))
    : 0

  const handleLogout = async () => {
    setLogoutError('')
    try {
      await logout()
      navigate('/login', { replace: true })
    } catch {
      setLogoutError('Impossible de fermer la session. Vérifiez votre connexion et réessayez.')
    }
  }

  return <div className="flex min-h-screen bg-surface">
    {menuOpen && <button type="button" className="fixed inset-0 z-40 cursor-default bg-navy-900/50 backdrop-blur-[2px] md:hidden" onClick={() => setMenuOpen(false)} aria-label="Fermer le menu" />}
    <aside className={`${menuOpen ? 'translate-x-0' : lang === 'ar' ? 'translate-x-full' : '-translate-x-full'} fixed inset-y-0 start-0 z-50 flex w-[17rem] flex-col bg-navy-900 px-4 py-5 text-white shadow-2xl transition-transform duration-300 md:static md:z-auto md:translate-x-0 md:shadow-none`}>
      <div className="mb-9 flex items-center justify-between px-2">
        <Link to="/app" className="flex items-center gap-3" aria-label="MizanFlow — accueil">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand text-white shadow-glow"><Building2 size={20} /></span>
          <span className="text-xl font-bold tracking-tight">Mizan<span className="text-blue-300">Flow</span></span>
        </Link>
        <button type="button" className="grid h-9 w-9 place-items-center rounded-lg text-slate-400 transition hover:bg-white/10 hover:text-white md:hidden" onClick={() => setMenuOpen(false)} aria-label="Fermer le menu"><X size={19} /></button>
      </div>

      <p className="mb-2 px-3 text-[10px] font-bold uppercase tracking-[.18em] text-slate-500">Espace de travail</p>
      <nav className="flex-1 space-y-1" aria-label="Navigation de l’espace de travail">
        {items.map(item => <NavLink key={item.to} to={item.to} end={item.to === '/app'} onClick={() => setMenuOpen(false)}
          className={({ isActive }) => `group flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm font-medium transition ${isActive ? 'bg-brand text-white shadow-glow' : 'text-slate-300 hover:bg-white/[.07] hover:text-white'}`}>
          <item.icon size={18} strokeWidth={1.9} /><span>{item.key ? t(item.key) : item.label}</span>
        </NavLink>)}
      </nav>

      <div className="mb-4 rounded-2xl border border-white/10 bg-white/[.045] p-3.5">
        <div className="flex items-center gap-3">
          <span className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-blue-400/15 text-sm font-bold text-blue-200">{session?.fullName?.trim().charAt(0).toUpperCase() || 'M'}</span>
          <div className="min-w-0"><p className="truncate text-sm font-semibold text-white">{session?.fullName}</p><p className="mt-0.5 truncate text-xs text-slate-400">{role.replace(/_/g, ' ')}</p></div>
        </div>
      </div>
      <p className="px-3 text-[10px] text-slate-500">MizanFlow · Gestion de chantier</p>
    </aside>

    <div className="flex min-w-0 flex-1 flex-col">
      {isCompanyAdmin && subscription && (subscription.status === 'TRIAL' || !subscription.usable) &&
        <div className={`flex flex-wrap items-center justify-center gap-x-1.5 gap-y-1 px-4 py-2.5 text-center text-xs font-medium sm:text-sm ${subscription.usable ? 'bg-blue-50 text-brand-700' : 'bg-red-50 text-danger'}`}>
          {subscription.usable ? `Votre essai se termine dans ${trialDays} jour(s).` : 'Abonnement expiré : la création de ressources est bloquée.'}
          <NavLink to="/app/billing" className="font-bold underline underline-offset-2">Voir les offres</NavLink>
        </div>}
      <header className="sticky top-0 z-30 flex min-h-[4.25rem] items-center justify-between border-b border-slate-200/80 bg-white/90 px-4 backdrop-blur-xl sm:px-6">
        <div className="flex min-w-0 items-center gap-3">
          <button type="button" className="grid h-10 w-10 shrink-0 place-items-center rounded-xl border border-slate-200 bg-white text-slate-600 shadow-sm transition hover:bg-slate-50 md:hidden" onClick={() => setMenuOpen(true)} aria-label="Ouvrir le menu"><Menu size={19} /></button>
          <div className="min-w-0">
            <p className="hidden text-[10px] font-semibold uppercase tracking-[.16em] text-slate-400 sm:block">Espace entreprise</p>
            <h1 className="truncate text-sm font-semibold text-navy sm:mt-0.5">{currentPage?.key ? t(currentPage.key) : currentPage?.label ?? 'MizanFlow'}</h1>
          </div>
        </div>
        <div className="flex shrink-0 items-center gap-2">
          <button type="button" className="btn-ghost !min-h-9 !px-3 text-xs" onClick={() => setLang(lang === 'fr' ? 'ar' : 'fr')} aria-label={lang === 'fr' ? 'Afficher en arabe' : 'Afficher en français'}>
            {lang === 'fr' ? 'العربية' : 'Français'}
          </button>
          <button type="button" className="btn-ghost !min-h-9 !px-3 text-xs" onClick={handleLogout}><LogOut size={15} /><span className="hidden sm:inline">{t('logout')}</span></button>
        </div>
      </header>
      <main className="mx-auto w-full max-w-[1600px] flex-1 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
        {logoutError && <div className="mb-5"><ErrorBox text={logoutError} /></div>}
        <Outlet />
      </main>
      <footer className="border-t border-slate-200/70 bg-white/60 px-6 py-3 text-center text-[11px] text-slate-400">MizanFlow · Du chantier à l’encaissement</footer>
    </div>
  </div>
}
