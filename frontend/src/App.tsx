import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './lib/auth'
import { Spinner } from './components/ui'

const Landing = lazy(() => import('./pages/Landing'))
const Shell = lazy(() => import('./pages/Shell'))
const Login = lazy(() => import('./pages/AuthPages').then(m => ({ default: m.Login })))
const Register = lazy(() => import('./pages/AuthPages').then(m => ({ default: m.Register })))
const Dashboard = lazy(() => import('./pages/Dashboard'))
const Customers = lazy(() => import('./pages/Customers'))
const Projects = lazy(() => import('./pages/Projects'))
const WorkLogs = lazy(() => import('./pages/WorkLogs'))
const Invoices = lazy(() => import('./pages/Invoices'))
const Legal = lazy(() => import('./pages/Legal').then(m => ({ default: m.Legal })))
const Billing = lazy(() => import('./pages/Billing'))
const Team = lazy(() => import('./pages/Team'))
const Admin = lazy(() => import('./pages/Admin'))

function Private({ children }: { children: JSX.Element }) { const { session, loading } = useAuth(); if (loading) return <Spinner />; return session ? children : <Navigate to="/login" replace /> }
function Home() { const { session } = useAuth(); return session!.role === 'SUPER_ADMIN' ? <Navigate to="/app/admin" replace /> : session!.role === 'FIELD_WORKER' ? <Navigate to="/app/work-logs" replace /> : <Dashboard /> }
export default function App() {
  return <Suspense fallback={<Spinner />}><Routes>
    <Route path="/" element={<Landing />} /><Route path="/login" element={<Login />} /><Route path="/register" element={<Register />} />
    <Route path="/terms" element={<Legal kind="terms" />} /><Route path="/privacy" element={<Legal kind="privacy" />} />
    <Route path="/app" element={<Private><Shell /></Private>}>
      <Route index element={<Home />} /><Route path="team" element={<Team />} /><Route path="billing" element={<Billing />} /><Route path="admin" element={<Admin />} /><Route path="projects" element={<Projects />} /><Route path="work-logs" element={<WorkLogs />} />
      <Route path="customers" element={<Customers />} /><Route path="invoices" element={<Invoices />} /></Route>
    <Route path="*" element={<Navigate to="/" replace />} /></Routes></Suspense>
}
