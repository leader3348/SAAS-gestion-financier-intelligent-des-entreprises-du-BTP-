import { useState } from 'react'; import { useForm } from 'react-hook-form'; import { zodResolver } from '@hookform/resolvers/zod'; import { z } from 'zod'; import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, errMsg } from '../lib/api'; import { Empty, ErrorBox, Field, Modal, Spinner } from '../components/ui'
type C = { id: string; name: string; ice?: string; email?: string; phone?: string; city?: string }
const schema = z.object({ name: z.string().min(1, 'Nom requis'), ice: z.string().max(20).optional(), email: z.string().email('Email invalide').or(z.literal('')).optional(), phone: z.string().optional(), city: z.string().optional() })
export default function Customers() {
  const qc = useQueryClient(); const [q, setQ] = useState(''); const [page, setPage] = useState(0); const [open, setOpen] = useState(false); const [err, setErr] = useState('')
  const { data, isLoading, error } = useQuery({ queryKey: ['customers', q, page], queryFn: async () => (await api.get<{ content: C[]; totalPages: number }>('/customers', { params: { q: q || undefined, page } })).data })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<z.infer<typeof schema>>({ resolver: zodResolver(schema) })
  const create = useMutation({ mutationFn: (v: z.infer<typeof schema>) => api.post('/customers', { ...v, email: v.email || null }), onSuccess: () => { qc.invalidateQueries({ queryKey: ['customers'] }); setOpen(false); reset(); setErr('') }, onError: e => setErr(errMsg(e)) })
  return <div><div className="mb-4 flex flex-wrap items-center justify-between gap-3"><h1 className="text-2xl font-semibold">Clients</h1><div className="flex gap-2">
    <input className="input w-56" placeholder="Rechercher (nom, ICE, ville)" value={q} onChange={e => { setQ(e.target.value); setPage(0) }} /><button className="btn-primary" onClick={() => setOpen(true)}>Nouveau client</button></div></div>
    <div className="card overflow-x-auto p-0">{isLoading ? <Spinner /> : error ? <ErrorBox text="Chargement impossible" /> : !data?.content.length ? <Empty text="Aucun client. Ajoutez votre premier client." /> :
      <table className="w-full text-sm"><thead className="bg-slate-50 text-left text-xs uppercase text-slate-500"><tr><th className="p-3">Nom</th><th>ICE</th><th>Ville</th><th>Téléphone</th><th>Email</th></tr></thead>
        <tbody>{data.content.map(c => <tr key={c.id} className="border-t"><td className="p-3 font-medium">{c.name}</td><td>{c.ice}</td><td>{c.city}</td><td>{c.phone}</td><td>{c.email}</td></tr>)}</tbody></table>}</div>
    {data && data.totalPages > 1 && <div className="mt-3 flex justify-end gap-2"><button className="btn-ghost" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Précédent</button><button className="btn-ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(p => p + 1)}>Suivant</button></div>}
    {open && <Modal title="Nouveau client" onClose={() => setOpen(false)}><form noValidate className="space-y-3" onSubmit={handleSubmit(v => create.mutate(v))}>{err && <ErrorBox text={err} />}
      <Field label="Nom *" error={errors.name?.message}><input className="input" {...register('name')} /></Field><Field label="ICE"><input className="input" {...register('ice')} /></Field>
      <Field label="Ville"><input className="input" {...register('city')} /></Field><Field label="Téléphone"><input className="input" {...register('phone')} /></Field>
      <Field label="Email" error={errors.email?.message}><input className="input" {...register('email')} /></Field><button className="btn-primary w-full" disabled={create.isPending}>Enregistrer</button></form></Modal>}</div>
}
