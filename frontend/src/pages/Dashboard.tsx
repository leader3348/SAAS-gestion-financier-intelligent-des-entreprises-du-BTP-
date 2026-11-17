import { useQuery } from '@tanstack/react-query'
import { Activity, ArrowDownToLine, BriefcaseBusiness, CircleAlert, Coins, FileCheck2, Wallet } from 'lucide-react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { ErrorBox, Spinner } from '../components/ui'
import { api } from '../lib/api'
import { mad } from '../lib/format'

type DashboardData = {
  activeProjects: number
  totalInvoiced: number
  totalCollected: number
  totalOutstanding: number
  overdueAmount: number
  overdueInvoices: number
}

const money = (value: number) => mad(value)

export default function Dashboard() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: async () => (await api.get<DashboardData>('/dashboard')).data,
  })

  if (isLoading) return <Spinner />
  if (error || !data) return <ErrorBox text="Impossible de charger le tableau de bord. Vérifiez votre connexion et réessayez." />

  const metrics = [
    { label: 'Projets actifs', value: String(data.activeProjects), note: 'En cours de réalisation', icon: BriefcaseBusiness, tone: 'bg-blue-50 text-brand' },
    { label: 'Total facturé', value: money(data.totalInvoiced), note: 'Montant des factures émises', icon: FileCheck2, tone: 'bg-violet-50 text-violet-700' },
    { label: 'Total encaissé', value: money(data.totalCollected), note: 'Paiements enregistrés', icon: ArrowDownToLine, tone: 'bg-emerald-50 text-emerald-700' },
    { label: 'Reste à encaisser', value: money(data.totalOutstanding), note: 'Solde des factures', icon: Wallet, tone: 'bg-amber-50 text-amber-700' },
    { label: 'Montant échu', value: money(data.overdueAmount), note: 'Échéances dépassées', icon: CircleAlert, tone: 'bg-red-50 text-red-700' },
    { label: 'Factures en retard', value: String(data.overdueInvoices), note: 'À vérifier en priorité', icon: Activity, tone: 'bg-orange-50 text-orange-700' },
  ]
  const chart = [
    { name: 'Facturé', amount: Number(data.totalInvoiced) },
    { name: 'Encaissé', amount: Number(data.totalCollected) },
    { name: 'À encaisser', amount: Number(data.totalOutstanding) },
    { name: 'Échu', amount: Number(data.overdueAmount) },
  ]

  return <div className="space-y-7">
    <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
      <div>
        <p className="text-xs font-bold uppercase tracking-[.16em] text-brand">Votre activité</p>
        <h2 className="mt-2 text-2xl font-semibold tracking-tight text-navy sm:text-3xl">Vue d’ensemble</h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Retrouvez l’essentiel sur vos projets, vos factures et vos encaissements.</p>
      </div>
      <div className="flex items-center gap-2 self-start rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-500 shadow-sm sm:self-auto">
        <span className="h-2 w-2 rounded-full bg-emerald-500" /> Données actualisées à l’ouverture
      </div>
    </section>

    <section aria-label="Indicateurs clés" className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {metrics.map(({ label, value, note, icon: Icon, tone }) => <article key={label} className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0"><p className="text-sm font-medium text-slate-500">{label}</p><p className="mt-3 break-words text-xl font-semibold tracking-tight text-navy sm:text-2xl">{value}</p></div>
          <span className={`grid h-10 w-10 shrink-0 place-items-center rounded-xl ${tone}`}><Icon size={19} strokeWidth={1.9} /></span>
        </div>
        <p className="mt-3 text-xs text-slate-400">{note}</p>
      </article>)}
    </section>

    <section className="grid gap-5 xl:grid-cols-[1.55fr_.75fr]">
      <article className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div><h3 className="text-base font-semibold text-navy">Situation financière</h3><p className="mt-1 text-sm text-slate-500">Comparaison des montants suivis dans vos factures</p></div>
          <span className="inline-flex items-center gap-1.5 rounded-lg bg-slate-50 px-2.5 py-1.5 text-xs font-medium text-slate-500"><Coins size={14} /> MAD</span>
        </div>
        <div className="mt-5 h-72" role="img" aria-label="Graphique comparant les montants facturés, encaissés, à encaisser et échus">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chart} margin={{ top: 8, right: 8, left: 0, bottom: 4 }}>
              <CartesianGrid stroke="#eef2f7" strokeDasharray="4 4" vertical={false} />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12 }} />
              <YAxis axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 11 }} tickFormatter={value => new Intl.NumberFormat('fr-MA', { notation: 'compact', maximumFractionDigits: 1 }).format(Number(value))} />
              <Tooltip formatter={(value: number) => money(value)} cursor={{ fill: '#f8fafc' }} contentStyle={{ borderRadius: 12, borderColor: '#e2e8f0', boxShadow: '0 8px 24px rgba(15,23,42,.08)' }} />
              <Bar dataKey="amount" fill="#2563eb" radius={[7, 7, 0, 0]} maxBarSize={56} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </article>

      <aside className="flex flex-col rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-amber-50 text-amber-700"><CircleAlert size={19} /></span><div><h3 className="text-base font-semibold text-navy">À surveiller</h3><p className="mt-0.5 text-xs text-slate-500">Vos échéances de paiement</p></div></div>
        <div className="mt-6 rounded-2xl bg-amber-50/80 p-4">
          <p className="text-xs font-medium text-amber-800">Montant échu</p>
          <p className="mt-2 break-words text-2xl font-semibold tracking-tight text-amber-950">{money(data.overdueAmount)}</p>
        </div>
        <div className="mt-4 flex items-center justify-between border-b border-slate-100 py-3 text-sm"><span className="text-slate-500">Factures en retard</span><span className="font-semibold text-navy">{data.overdueInvoices}</span></div>
        <div className="mt-auto pt-5 text-xs leading-5 text-slate-500">Rapprochez les paiements reçus avec les factures concernées pour garder votre suivi à jour.</div>
      </aside>
    </section>
  </div>
}
