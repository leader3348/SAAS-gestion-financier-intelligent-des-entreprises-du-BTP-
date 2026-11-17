import { useState } from 'react'; import { useForm } from 'react-hook-form'; import { zodResolver } from '@hookform/resolvers/zod'; import { z } from 'zod'; import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, errMsg } from '../lib/api'; import { useAuth } from '../lib/auth'; import { Badge, Empty, ErrorBox, Field, Modal, Spinner } from '../components/ui'
type W = { id: string; projectId: string; serviceType: string; quantity: number; unit: string; workDate: string; status: string; rejectionReason?: string }
const schema = z.object({ projectId: z.string().min(1, 'Projet requis'), workDate: z.string().min(1, 'Date requise'), serviceType: z.string().min(1, 'Type requis'), quantity: z.coerce.number().gt(0, 'Quantité > 0'), unit: z.enum(['M2', 'M3', 'ML', 'UNIT', 'HOUR', 'DAY']), unitPrice: z.coerce.number().min(0) })
export default function WorkLogs() {
  const { session } = useAuth(); const role = session!.role; const canApprove = ['COMPANY_ADMIN', 'PROJECT_MANAGER'].includes(role); const qc = useQueryClient(); const [open, setOpen] = useState(false); const [err, setErr] = useState('')
  const [rejecting, setRejecting] = useState<string | null>(null); const [reason, setReason] = useState('')
  const { data, isLoading, error } = useQuery({ queryKey: ['worklogs'], queryFn: async () => (await api.get<{ content: W[] }>('/work-logs')).data })
  const projects = useQuery({ queryKey: ['projects'], queryFn: async () => (await api.get<{ content: { id: string; name: string }[] }>('/projects')).data })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<z.infer<typeof schema>>({ resolver: zodResolver(schema), defaultValues: { unit: 'M2', unitPrice: 0 } })
  const inv = () => qc.invalidateQueries({ queryKey: ['worklogs'] })
  const create = useMutation({ mutationFn: (v: z.infer<typeof schema>) => api.post('/work-logs', v), onSuccess: () => { inv(); setOpen(false); reset(); setErr('') }, onError: e => setErr(errMsg(e)) })
  const act = useMutation({ mutationFn: ({ id, a, body }: { id: string; a: string; body?: object }) => api.post(`/work-logs/${id}/${a}`, body ?? {}), onSuccess: () => { inv(); setRejecting(null); setReason('') }, onError: e => setErr(errMsg(e)) })
  const pname = (id: string) => projects.data?.content.find(p => p.id === id)?.name ?? '—'
  return <div><div className="mb-4 flex items-center justify-between"><h1 className="text-2xl font-semibold">Travaux</h1>{role !== 'ACCOUNTANT' && <button className="btn-primary" onClick={() => setOpen(true)}>Nouvelle prestation</button>}</div>
    {err && !open && <div className="mb-3"><ErrorBox text={err} /></div>}
    <div className="card overflow-x-auto p-0">{isLoading ? <Spinner /> : error ? <ErrorBox text="Chargement impossible" /> : !data?.content.length ? <Empty text="Aucune prestation enregistrée." /> :
      <table className="w-full text-sm"><thead className="bg-slate-50 text-left text-xs uppercase text-slate-500"><tr><th className="p-3">Date</th><th>Projet</th><th>Prestation</th><th>Quantité</th><th>Statut</th><th /></tr></thead>
        <tbody>{data.content.map(w => <tr key={w.id} className="border-t"><td className="p-3">{w.workDate}</td><td>{pname(w.projectId)}</td><td className="font-medium">{w.serviceType}{w.rejectionReason && <div className="text-xs text-danger">Motif : {w.rejectionReason}</div>}</td>
          <td>{w.quantity} {w.unit}</td><td><Badge s={w.status} /></td><td className="space-x-2 whitespace-nowrap p-3 text-right">
            {['DRAFT', 'REJECTED'].includes(w.status) && role !== 'ACCOUNTANT' && <button className="btn-ghost" onClick={() => act.mutate({ id: w.id, a: 'submit' })}>Soumettre</button>}
            {w.status === 'SUBMITTED' && canApprove && <><button className="btn-primary" onClick={() => act.mutate({ id: w.id, a: 'approve' })}>Approuver</button><button className="btn-ghost" onClick={() => setRejecting(w.id)}>Rejeter</button></>}</td></tr>)}</tbody></table>}</div>
    {rejecting && <Modal title="Rejeter la prestation" onClose={() => setRejecting(null)}><Field label="Motif du rejet (obligatoire)"><textarea className="input" rows={3} value={reason} onChange={e => setReason(e.target.value)} /></Field>
      <button className="btn-danger mt-3 w-full" disabled={!reason.trim()} onClick={() => act.mutate({ id: rejecting, a: 'reject', body: { comment: reason } })}>Confirmer le rejet</button></Modal>}
    {open && <Modal title="Nouvelle prestation" onClose={() => setOpen(false)}><form noValidate className="space-y-3" onSubmit={handleSubmit(v => create.mutate(v))}>{err && <ErrorBox text={err} />}
      <Field label="Projet *" error={errors.projectId?.message}><select className="input" {...register('projectId')}><option value="">— Choisir —</option>{projects.data?.content.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select></Field>
      <Field label="Date *" error={errors.workDate?.message}><input className="input" type="date" {...register('workDate')} /></Field><Field label="Type de prestation *" error={errors.serviceType?.message}><input className="input" {...register('serviceType')} /></Field>
      <div className="grid grid-cols-2 gap-3"><Field label="Quantité *" error={errors.quantity?.message}><input className="input" type="number" step="0.001" {...register('quantity')} /></Field>
        <Field label="Unité"><select className="input" {...register('unit')}><option value="M2">m²</option><option value="M3">m³</option><option value="ML">ml</option><option value="UNIT">unité</option><option value="HOUR">heure</option><option value="DAY">jour</option></select></Field></div>
      <Field label="Prix unitaire HT (MAD)"><input className="input" type="number" step="0.01" {...register('unitPrice')} /></Field><button className="btn-primary w-full" disabled={create.isPending}>Enregistrer (brouillon)</button></form></Modal>}</div>
}
