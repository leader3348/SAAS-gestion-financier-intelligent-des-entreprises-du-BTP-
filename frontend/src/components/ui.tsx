import type { ReactNode } from 'react'
import { X } from 'lucide-react'
import { STATUS_COLORS, STATUS_LABELS } from '../lib/format'
export const Badge = ({ s }: { s: string }) => <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[s] ?? 'bg-slate-100 text-slate-700'}`}>{STATUS_LABELS[s] ?? s}</span>
export const Empty = ({ text }: { text: string }) => <div role="status" className="px-5 py-14 text-center text-sm text-slate-500">{text}</div>
export const ErrorBox = ({ text }: { text: string }) => <div role="alert" className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-danger">{text}</div>
export const Spinner = () => <div role="status" aria-live="polite" className="flex min-h-40 items-center justify-center gap-3 text-sm font-medium text-slate-500"><span className="h-5 w-5 animate-spin rounded-full border-2 border-slate-200 border-t-brand" />Chargement…</div>
export function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  return <div className="fixed inset-0 z-50 flex items-center justify-center bg-navy-900/50 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" aria-label={title} onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}>
    <div className="max-h-[90vh] w-full max-w-lg overflow-auto rounded-2xl border border-white/60 bg-white p-6 shadow-2xl"><div className="mb-5 flex items-center justify-between"><h2 className="text-lg font-semibold tracking-tight">{title}</h2>
      <button type="button" onClick={onClose} className="btn-ghost !min-h-9 !w-9 !p-0" aria-label="Fermer"><X size={18} /></button></div>{children}</div></div>
}
export const Field = ({ label, error, children }: { label: string; error?: string; children: ReactNode }) => <div><label className="label">{label}{children}</label>{error && <p className="err">{error}</p>}</div>
