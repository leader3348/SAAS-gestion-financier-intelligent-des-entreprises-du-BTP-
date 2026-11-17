import { Link } from 'react-router-dom'
export function Legal({ kind }: { kind: 'terms' | 'privacy' }) {
  return <div className="mx-auto max-w-3xl p-6"><Link to="/" className="text-brand">← Accueil</Link>
    <h1 className="my-4 text-2xl font-semibold">{kind === 'terms' ? "Conditions d'utilisation" : 'Politique de confidentialité'}</h1>
    <div className="rounded-lg bg-amber-50 p-4 text-sm text-amber-800">Document modèle à faire valider par un juriste avant commercialisation. {kind === 'privacy' && 'Il doit être complété pour la conformité à la loi 09-08 et aux exigences de la CNDP (déclaration/autorisation, finalités, durées de conservation, droits d’accès et de rectification, contact DPO).'}</div>
    <p className="mt-4 text-sm text-slate-700">{kind === 'terms' ? "MizanFlow est fourni en tant que service SaaS de gestion de chantiers et de facturation. Les documents générés (devis, situations, factures) sont des aides à la gestion et ne garantissent pas à eux seuls la conformité aux obligations fiscales marocaines." : "Les données saisies par chaque entreprise sont isolées logiquement des autres entreprises. Seules les données nécessaires au service sont collectées. Les mots de passe sont stockés sous forme hachée."}</p></div>
}
