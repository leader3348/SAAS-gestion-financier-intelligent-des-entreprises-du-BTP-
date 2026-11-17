import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
type Lang = 'fr' | 'ar'
const dict = { fr: { dashboard: 'Tableau de bord', projects: 'Projets', workLogs: 'Travaux', customers: 'Clients', invoices: 'Factures', logout: 'Déconnexion', login: 'Connexion', email: 'Email', password: 'Mot de passe' },
  ar: { dashboard: 'لوحة القيادة', projects: 'المشاريع', workLogs: 'الأشغال', customers: 'الزبناء', invoices: 'الفواتير', logout: 'تسجيل الخروج', login: 'تسجيل الدخول', email: 'البريد الإلكتروني', password: 'كلمة المرور' } } as const
export type Key = keyof typeof dict.fr
const Ctx = createContext<{ lang: Lang; setLang: (l: Lang) => void; t: (k: Key) => string }>({ lang: 'fr', setLang: () => {}, t: k => dict.fr[k] })
function readLang(): Lang {
  if (typeof window === 'undefined') return 'fr'
  try { return window.localStorage.getItem('lang') === 'ar' ? 'ar' : 'fr' }
  catch { return 'fr' }
}
export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setL] = useState<Lang>(readLang)
  useEffect(() => {
    document.documentElement.lang = lang
    document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr'
  }, [lang])
  const setLang = (l: Lang) => {
    setL(l)
    try { if (typeof window !== 'undefined') window.localStorage.setItem('lang', l) } catch { /* The in-memory preference still applies. */ }
  }
  return <Ctx.Provider value={{ lang, setLang, t: k => dict[lang][k] }}>{children}</Ctx.Provider>
}
export const useI18n = () => useContext(Ctx)
