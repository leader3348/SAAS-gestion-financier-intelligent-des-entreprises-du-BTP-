export const mad = (v: number | string) => new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(Number(v))
export const STATUS_LABELS: Record<string, string> = { DRAFT: 'Brouillon', SUBMITTED: 'Soumise', APPROVED: 'Approuvée', REJECTED: 'Rejetée', INVOICED: 'Facturée',
  ISSUED: 'Émise', PARTIALLY_PAID: 'Partiellement payée', PAID: 'Payée', OVERDUE: 'En retard', CANCELLED: 'Annulée', PLANNED: 'Planifié', IN_PROGRESS: 'En cours', ON_HOLD: 'En pause', COMPLETED: 'Terminé' }
export const STATUS_COLORS: Record<string, string> = { PAID: 'bg-green-100 text-green-700', APPROVED: 'bg-green-100 text-green-700', COMPLETED: 'bg-green-100 text-green-700', OVERDUE: 'bg-red-100 text-red-700', REJECTED: 'bg-red-100 text-red-700', CANCELLED: 'bg-red-100 text-red-700',
  PARTIALLY_PAID: 'bg-amber-100 text-amber-700', SUBMITTED: 'bg-amber-100 text-amber-700', ON_HOLD: 'bg-amber-100 text-amber-700' }
