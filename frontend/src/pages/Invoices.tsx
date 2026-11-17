import { useState } from 'react'; import { useForm } from 'react-hook-form'; import { zodResolver } from '@hookform/resolvers/zod'; import { z } from 'zod'; import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, errMsg } from '../lib/api'; import { mad } from '../lib/format'; import { Badge, Empty, ErrorBox, Field, Modal, Spinner } from '../components/ui'
type I = { id: string; number?: string; status: string; dueDate?: string; totalTtc: number; paid: number; balance: number }
const schema = z.object({ amount: z.coerce.number().gt(0, 'Montant > 0'), paidOn: z.string().min(1, 'Date requise'), method: z.enum(['TRANSFER', 'CASH', 'CHECK', 'CARD', 'OTHER']), reference: z.string().optional() })
export default function Invoices() {
  const qc = useQueryClient(); const [paying, setPaying] = useState<I | null>(null); const [err, setErr] = useState('')
  const { data, isLoading, error } = useQuery({ queryKey: ['invoices'], queryFn: async () => (await api.get<{ content: I[] }>('/invoices')).data })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<z.infer<typeof schema>>({ resolver: zodResolver(schema), defaultValues: { method: 'TRANSFER' } })
  const done = () => { qc.invalidateQueries({ queryKey: ['invoices'] }); qc.invalidateQueries({ queryKey: ['dashboard'] }); setPaying(null); reset(); setErr('') }
  const pay = useMutation({ mutationFn: (v: z.infer<typeof schema>) => api.post(`/invoices/${paying!.id}/payments`, v), onSuccess: done, onError: e => setErr(errMsg(e)) })
  const issue = useMutation({ mutationFn: (id: string) => api.post(`/invoices/${id}/issue`, { paymentDays: 30 }), onSuccess: done, onError: e => setErr(errMsg(e)) })
  return <div><h1 className="mb-4 text-2xl font-semibold">Factures</h1>{err && !paying && <div className="mb-3"><ErrorBox text={err} /></div>}
    <div className="card overflow-x-auto p-0">{isLoading ? <Spinner /> : error ? <ErrorBox text="Chargement impossible" /> : !data?.content.length ? <Empty text="Aucune facture. Elles sont créées à partir des prestations approuvées (API : POST /invoices/from-work-logs)." /> :
      <table className="w-full text-sm"><thead className="bg-slate-50 text-left text-xs uppercase text-slate-500"><tr><th className="p-3">N°</th><th>Échéance</th><th>Total TTC</th><th>Encaissé</th><th>Solde</th><th>Statut</th><th /></tr></thead>
        <tbody>{data.content.map(i => <tr key={i.id} className="border-t"><td className="p-3 font-mono text-xs">{i.number ?? '(brouillon)'}</td><td>{i.dueDate ?? '—'}</td><td>{mad(i.totalTtc)}</td><td>{mad(i.paid)}</td><td className="font-medium">{mad(i.balance)}</td><td><Badge s={i.status} /></td>
          <td className="p-3 text-right">{i.status === 'DRAFT' && <button className="btn-primary" onClick={() => issue.mutate(i.id)}>Émettre</button>}{['ISSUED', 'PARTIALLY_PAID', 'OVERDUE'].includes(i.status) && <button className="btn-ghost" onClick={() => setPaying(i)}>Enregistrer un paiement</button>}</td></tr>)}</tbody></table>}</div>
    <p className="mt-3 text-xs text-slate-500">Les documents générés doivent être validés par un professionnel (comptable / fiscalité) avant usage officiel.</p>
    {paying && <Modal title={`Paiement — ${paying.number}`} onClose={() => setPaying(null)}><form noValidate className="space-y-3" onSubmit={handleSubmit(v => pay.mutate(v))}>{err && <ErrorBox text={err} />}
      <p className="text-sm text-slate-600">Solde restant : <b>{mad(paying.balance)}</b>. N'enregistrez que des encaissements réellement reçus.</p>
      <Field label="Montant (MAD)" error={errors.amount?.message}><input className="input" type="number" step="0.01" {...register('amount')} /></Field><Field label="Date de paiement" error={errors.paidOn?.message}><input className="input" type="date" {...register('paidOn')} /></Field>
      <Field label="Mode"><select className="input" {...register('method')}><option value="TRANSFER">Virement</option><option value="CASH">Espèces</option><option value="CHECK">Chèque</option><option value="CARD">Carte</option><option value="OTHER">Autre</option></select></Field>
      <Field label="Référence"><input className="input" {...register('reference')} /></Field><button className="btn-primary w-full" disabled={pay.isPending}>Confirmer l'encaissement</button></form></Modal>}</div>
}
