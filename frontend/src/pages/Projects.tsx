import { useState } from 'react'; import { useForm } from 'react-hook-form'; import { zodResolver } from '@hookform/resolvers/zod'; import { z } from 'zod'; import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, errMsg } from '../lib/api'; import { useAuth } from '../lib/auth'; import { mad } from '../lib/format'; import { Badge, Empty, ErrorBox, Field, Modal, Spinner } from '../components/ui'
type P = { id: string; reference: string; name: string; budget: number; status: string; plannedEndDate?: string }
const schema = z.object({ name: z.string().min(1, 'Nom requis'), customerId: z.string().min(1, 'Client requis'), budget: z.coerce.number().min(0, 'Budget invalide'), status: z.enum(['DRAFT', 'PLANNED', 'IN_PROGRESS']), siteAddress: z.string().optional() })
export default function Projects() {
  const { session } = useAuth(); const canEdit = ['COMPANY_ADMIN', 'PROJECT_MANAGER'].includes(session!.role); const qc = useQueryClient(); const [open, setOpen] = useState(false); const [err, setErr] = useState('')
  const { data, isLoading, error } = useQuery({ queryKey: ['projects'], queryFn: async () => (await api.get<{ content: P[] }>('/projects')).data })
  const customers = useQuery({ queryKey: ['customers-all'], enabled: open, queryFn: async () => (await api.get<{ content: { id: string; name: string }[] }>('/customers', { params: { size: 100 } })).data })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<z.infer<typeof schema>>({ resolver: zodResolver(schema), defaultValues: { status: 'PLANNED' } })
  const create = useMutation({ mutationFn: (v: z.infer<typeof schema>) => api.post('/projects', v), onSuccess: () => { qc.invalidateQueries({ queryKey: ['projects'] }); setOpen(false); reset(); setErr('') }, onError: e => setErr(errMsg(e)) })
  return <div><div className="mb-4 flex items-center justify-between"><h1 className="text-2xl font-semibold">Projets</h1>{canEdit && <button className="btn-primary" onClick={() => setOpen(true)}>Nouveau projet</button>}</div>
    <div className="card overflow-x-auto p-0">{isLoading ? <Spinner /> : error ? <ErrorBox text="Chargement impossible" /> : !data?.content.length ? <Empty text="Aucun projet pour le moment." /> :
      <table className="w-full text-sm"><thead className="bg-slate-50 text-left text-xs uppercase text-slate-500"><tr><th className="p-3">Réf.</th><th>Projet</th><th>Budget</th><th>Fin prévue</th><th>Statut</th></tr></thead>
        <tbody>{data.content.map(p => <tr key={p.id} className="border-t"><td className="p-3 font-mono text-xs">{p.reference}</td><td className="font-medium">{p.name}</td><td>{mad(p.budget)}</td><td>{p.plannedEndDate ?? '—'}</td><td><Badge s={p.status} /></td></tr>)}</tbody></table>}</div>
    {open && <Modal title="Nouveau projet" onClose={() => setOpen(false)}><form noValidate className="space-y-3" onSubmit={handleSubmit(v => create.mutate(v))}>{err && <ErrorBox text={err} />}
      <Field label="Nom du projet *" error={errors.name?.message}><input className="input" {...register('name')} /></Field>
      <Field label="Client *" error={errors.customerId?.message}><select className="input" {...register('customerId')}><option value="">— Choisir —</option>{customers.data?.content.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></Field>
      <Field label="Adresse du chantier"><input className="input" {...register('siteAddress')} /></Field><Field label="Budget prévisionnel (MAD)" error={errors.budget?.message}><input className="input" type="number" step="0.01" {...register('budget')} /></Field>
      <Field label="Statut"><select className="input" {...register('status')}><option value="DRAFT">Brouillon</option><option value="PLANNED">Planifié</option><option value="IN_PROGRESS">En cours</option></select></Field>
      <button className="btn-primary w-full" disabled={create.isPending}>Créer</button></form></Modal>}</div>
}
