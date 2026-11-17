import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, refreshSession, setAccess, type Tokens } from './api'

type Session = { role: string; fullName: string } | null
type AuthContextValue = {
  session: Session
  loading: boolean
  sessionError: string | null
  login: (email: string, password: string) => Promise<void>
  register: (company: string, name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const Ctx = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session>(null)
  const [loading, setLoading] = useState(true)
  const [sessionError, setSessionError] = useState<string | null>(null)

  const apply = (tokens: Tokens) => {
    setAccess(tokens.accessToken)
    setSession({ role: tokens.role, fullName: tokens.fullName })
    setSessionError(null)
  }

  useEffect(() => {
    let active = true
    refreshSession()
      .then(tokens => { if (active && tokens) setSession({ role: tokens.role, fullName: tokens.fullName }) })
      .catch(() => { if (active) setSessionError('Impossible de vérifier votre session. Réessayez dans quelques instants.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  const login = async (email: string, password: string) => apply((await api.post<Tokens>('/auth/web/login', { email, password })).data)
  const register = async (company: string, name: string, email: string, password: string) => apply((await api.post<Tokens>('/auth/web/register', { companyName: company, fullName: name, email, password })).data)
  const logout = async () => {
    await api.post('/auth/web/logout')
    setAccess(null)
    setSession(null)
    setSessionError(null)
  }

  return <Ctx.Provider value={{ session, loading, sessionError, login, register, logout }}>{children}</Ctx.Provider>
}

export function useAuth() {
  const context = useContext(Ctx)
  if (!context) throw new Error('useAuth doit être utilisé dans AuthProvider')
  return context
}
