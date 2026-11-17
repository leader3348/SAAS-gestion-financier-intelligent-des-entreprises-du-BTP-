import { useState } from 'react'; import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'; import { api, errMsg } from '../lib/api'; import { Empty, ErrorBox, Field, Modal, Spinner } from '../components/ui'
type M = { id: string; email: string; fullName: string; role: string; active: boolean }
const ROLES: [string, string][] = [['COMPANY_ADMIN', 'Administrateur'], ['PROJECT_MANAGER', 'Chef de projet'], ['ACCOUNTANT', 'Comptable'], ['FIELD_WORKER', 'Collaborateur terrain'], ['CLIENT', 'Client (accès limité)']]
export default function Team() {
  const qc = useQueryClient(); const [open, setOpen] = useState(false); const [f, setF] = useState({ fullName: '', email: '', role: 'FIELD_WORKER' }); const [err, setErr] = useState(''); const [pwd, setPwd] = useState<string | null>(null)
  const { data, isLoading } = useQuery({ queryKey: ['team'], queryFn: async () => (await api.get<M[]>('/team')).data })
  const invite = useMutation({ mutationFn: () => api.post<{ temporaryPassword: string }>('/team', f), onSuccess: r => { qc.invalidateQueries({ queryKey: ['team'] }); qc.invalidateQueries({ queryKey: ['billing'] }); setPwd(r.data.temporaryPassword); setErr('') }, onError: e => setErr(errMsg(e)) })
  const off = useMutation({ mutationFn: (id: string) => api.post(`/team/${id}/deactivate`), onSuccess: () => qc.invalidateQueries({ queryKey: ['team'] }), onError: e => setErr(errMsg(e)) })
  const close = () => { setOpen(false); setPwd(null); setF({ fullName: '', email: '', role: 'FIELD_WORKER' }); setErr('') }
  return <div><div className="mb-4 flex items-center justify-between"><h1 className="text-2xl font-semibold">Équipe</h1><button className="btn-primary" onClick={() => setOpen(true)}>Ajouter un membre</button></div>
    {err && !open && <div className="mb-3"><ErrorBox text={err} /></div>}
    <div className="card overflow-x-auto p-0">{isLoading ? <Spinner /> : !data?.length ? <Empty text="Aucun membre." /> : <table className="w-full text-sm"><thead className="bg-slate-50 text-left text-xs uppercase text-slate-500"><tr><th className="p-3">Nom</th><th>Email</th><th>Rôle</th><th>Statut</th><th /></tr></thead>
      <tbody>{data.map(m => <tr key={m.id} className="border-t"><td className="p-3 font-medium">{m.fullName}</td><td>{m.email}</td><td>{ROLES.find(r => r[0] === m.role)?.[1] ?? m.role}</td><td>{m.active ? 'Actif' : 'Désactivé'}</td>
        <td className="p-3 text-right">{m.active && <button className="btn-ghost" onClick={() => { if (confirm(`Désactiver ${m.fullName} ? Ses sessions seront fermées.`)) off.mutate(m.id) }}>Désactiver</button>}</td></tr>)}</tbody></table>}</div>
    {open && <Modal title="Ajouter un membre" onClose={close}>{pwd ? <div className="space-y-3"><p className="text-sm">Compte créé. Transmettez ce mot de passe temporaire de façon sécurisée ; il <b>ne sera plus affiché</b> :</p><code className="block rounded-lg bg-slate-100 p-3 text-center text-lg">{pwd}</code>
      <p className="text-xs text-slate-500">L'envoi par email nécessitera un SMTP configuré. Le membre peut changer son mot de passe via l'API (PUT /account/password).</p><button className="btn-primary w-full" onClick={close}>Terminé</button></div> :
      <div className="space-y-3">{err && <ErrorBox text={err} />}<Field label="Nom complet"><input className="input" value={f.fullName} onChange={e => setF({ ...f, fullName: e.target.value })} /></Field><Field label="Email"><input className="input" type="email" value={f.email} onChange={e => setF({ ...f, email: e.target.value })} /></Field>
        <Field label="Rôle"><select className="input" value={f.role} onChange={e => setF({ ...f, role: e.target.value })}>{ROLES.map(([v, l]) => <option key={v} value={v}>{l}</option>)}</select></Field>
        <button className="btn-primary w-full" disabled={!f.fullName || !f.email || invite.isPending} onClick={() => invite.mutate()}>Créer le compte</button></div>}</Modal>}</div>
}
