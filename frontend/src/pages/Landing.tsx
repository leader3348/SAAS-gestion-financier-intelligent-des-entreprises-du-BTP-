import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight, BadgeCheck, BarChart3, Building2, Check, ChevronDown, ClipboardCheck,
  FileText, HardHat, ShieldCheck, WalletCards,
} from 'lucide-react'

const plans = [
  { name: 'Starter', monthly: 149, yearly: 1490, users: '3 utilisateurs', projects: '3 projets actifs', featured: false },
  { name: 'Pro', monthly: 299, yearly: 2990, users: '10 utilisateurs', projects: '15 projets actifs', featured: true },
]

const features = [
  { icon: HardHat, title: 'Le terrain, enfin structuré', text: 'Saisissez les prestations par projet, quantité et unité. Chaque étape reste visible par la bonne personne.' },
  { icon: ClipboardCheck, title: 'Des validations traçables', text: 'Soumettez, approuvez ou rejetez avec un motif. Gardez un historique clair des décisions.' },
  { icon: FileText, title: 'Facturez le travail validé', text: 'Créez vos factures depuis les prestations approuvées et suivez leur échéance.' },
  { icon: WalletCards, title: 'Suivez ce qui est encaissé', text: 'Enregistrez les paiements, visualisez le solde restant et repérez les retards.' },
  { icon: BarChart3, title: 'Pilotez avec vos chiffres', text: 'Consultez projets actifs, montants facturés, encaissements et créances depuis le tableau de bord.' },
  { icon: ShieldCheck, title: 'Gardez vos données séparées', text: 'Les rôles et les accès à l’entreprise sont contrôlés par le serveur.' },
]

const steps = [
  { number: '01', title: 'Le travail est saisi', text: 'L’équipe renseigne le chantier, la prestation, la quantité et le prix.' },
  { number: '02', title: 'La prestation est validée', text: 'Le responsable approuve ou demande une correction avec un motif.' },
  { number: '03', title: 'La facture est suivie', text: 'La prestation validée devient facturable et son encaissement est suivi.' },
]

const questions = [
  ['À qui s’adresse MizanFlow ?', 'Aux petites entreprises du BTP et aux équipes techniques qui veulent relier le suivi des travaux à la facturation.'],
  ['Mes données sont-elles séparées de celles des autres entreprises ?', 'Oui. L’entreprise est déterminée côté serveur et les accès sont contrôlés par rôle.'],
  ['Les factures sont-elles conformes ?', 'MizanFlow facilite le suivi et la préparation. Faites valider les modèles et les règles fiscales par votre comptable avant usage officiel.'],
  ['Comment régler l’abonnement ?', 'La version actuelle prévoit une demande avec référence de virement, validée manuellement par l’équipe MizanFlow.'],
] as const

export default function Landing() {
  const [yearly, setYearly] = useState(false)

  return <div className="overflow-hidden bg-white text-slate-900">
    <header className="sticky top-0 z-30 border-b border-slate-200/70 bg-white/90 backdrop-blur-xl">
      <div className="mx-auto flex h-[4.5rem] max-w-7xl items-center justify-between px-5 sm:px-8">
        <Link to="/" className="flex items-center gap-2.5" aria-label="MizanFlow — accueil">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-navy text-white shadow-sm"><Building2 size={19} /></span>
          <span className="text-lg font-bold tracking-tight text-navy">Mizan<span className="text-brand">Flow</span></span>
        </Link>
        <nav className="hidden items-center gap-8 text-sm font-medium text-slate-600 md:flex" aria-label="Navigation principale">
          <a className="transition hover:text-navy" href="#fonctionnalites">Fonctionnalités</a>
          <a className="transition hover:text-navy" href="#parcours">Comment ça marche</a>
          <a className="transition hover:text-navy" href="#tarifs">Tarifs</a>
        </nav>
        <div className="flex items-center gap-2 sm:gap-3">
          <Link className="hidden rounded-xl px-3 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-navy sm:inline-flex" to="/login">Connexion</Link>
          <Link to="/register" className="btn-primary !min-h-10 !rounded-xl !px-4">Essai gratuit <ArrowRight size={16} /></Link>
        </div>
      </div>
    </header>

    <main>
      <section className="hero-bg relative isolate overflow-hidden text-white">
        <div aria-hidden="true" className="grid-bg absolute inset-0" />
        <div aria-hidden="true" className="absolute -right-36 top-8 h-96 w-96 rounded-full bg-blue-500/20 blur-3xl" />
        <div className="relative mx-auto grid max-w-7xl items-center gap-14 px-5 py-16 sm:px-8 sm:py-20 lg:grid-cols-[1.02fr_.98fr] lg:gap-12 lg:py-24">
          <div className="animate-rise">
            <div className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/[.06] px-3.5 py-2 text-xs font-medium text-blue-100 shadow-sm">
              <span className="h-2 w-2 rounded-full bg-emerald-400 ring-4 ring-emerald-400/15" />
              Gestion de chantier pensée pour le Maroc
            </div>
            <h1 className="mt-7 max-w-2xl text-4xl font-semibold leading-[1.08] tracking-[-.045em] sm:text-5xl lg:text-[3.65rem]">
              Du travail réalisé à l’argent encaissé.
              <span className="mt-2 block text-blue-200">Sans perdre le fil.</span>
            </h1>
            <p className="mt-6 max-w-xl text-base leading-7 text-slate-300 sm:text-lg">
              Suivez les prestations, faites-les valider et facturez ce qui a réellement été réalisé. Une chaîne claire pour le chantier et le bureau.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Link to="/register" className="btn-primary btn-lg shadow-glow">
                Démarrer l’essai gratuit <ArrowRight size={18} />
              </Link>
              <a href="#fonctionnalites" className="btn btn-lg border border-white/15 bg-white/[.04] text-white hover:bg-white/10">
                Découvrir la plateforme <ChevronDown size={17} />
              </a>
            </div>
            <div className="mt-7 flex flex-wrap gap-x-5 gap-y-2 text-xs font-medium text-slate-300">
              <span className="inline-flex items-center gap-2"><Check size={14} className="text-emerald-300" /> Essai de 14 jours</span>
              <span className="inline-flex items-center gap-2"><Check size={14} className="text-emerald-300" /> Sans carte bancaire</span>
              <span className="inline-flex items-center gap-2"><Check size={14} className="text-emerald-300" /> Montants en MAD</span>
            </div>
          </div>

          <div className="relative mx-auto w-full max-w-[34rem] animate-rise [animation-delay:120ms]">
            <div aria-hidden="true" className="absolute -inset-5 rounded-[2rem] bg-blue-400/10 blur-2xl" />
            <div className="relative rounded-[1.6rem] border border-white/15 bg-white/[.08] p-2.5 shadow-2xl backdrop-blur-sm">
              <div className="overflow-hidden rounded-[1.15rem] bg-[#f7f9fc] text-slate-900">
                <div className="flex items-center justify-between border-b border-slate-200/80 bg-white px-5 py-4">
                  <div className="flex items-center gap-3">
                    <span className="grid h-9 w-9 place-items-center rounded-xl bg-blue-50 text-brand"><BarChart3 size={18} /></span>
                    <div><p className="text-sm font-semibold">Vue d’ensemble</p><p className="text-[11px] text-slate-500">Aperçu de démonstration</p></div>
                  </div>
                  <span className="rounded-full border border-emerald-100 bg-emerald-50 px-2.5 py-1 text-[10px] font-semibold text-emerald-700">À jour</span>
                </div>
                <div className="space-y-4 p-4 sm:p-5">
                  <div className="grid grid-cols-2 gap-3">
                    {[
                      { label: 'Projets actifs', value: '12', tone: 'bg-blue-50 text-brand', Icon: Building2 },
                      { label: 'À encaisser', value: '114 450 DH', tone: 'bg-amber-50 text-amber-700', Icon: WalletCards },
                    ].map(({ label, value, tone, Icon }) => {
                      return <div key={label} className="rounded-2xl border border-slate-200/80 bg-white p-3.5 sm:p-4">
                        <div className="flex items-center justify-between"><span className="text-[11px] font-medium text-slate-500">{label}</span><span className={`grid h-7 w-7 place-items-center rounded-lg ${tone}`}><Icon size={14} /></span></div>
                        <p className="mt-3 text-lg font-semibold tracking-tight sm:text-xl">{value}</p>
                      </div>
                    })}
                  </div>
                  <div className="rounded-2xl border border-slate-200/80 bg-white p-4">
                    <div className="flex items-center justify-between"><div><p className="text-sm font-semibold">Activité récente</p><p className="mt-0.5 text-xs text-slate-500">Suivi des prestations</p></div><span className="text-xs font-medium text-slate-400">Cette semaine</span></div>
                    <div className="mt-5 grid grid-cols-7 items-end gap-2" aria-hidden="true">
                      {[38, 58, 44, 75, 53, 88, 66].map((height, index) => <div key={index} className="flex h-24 items-end rounded-lg bg-slate-50 p-1"><div className={`w-full rounded-md ${index === 5 ? 'bg-brand' : 'bg-blue-200'}`} style={{ height: `${height}%` }} /></div>)}
                    </div>
                    <div className="mt-2 flex justify-between text-[10px] text-slate-400"><span>Lun.</span><span>Mar.</span><span>Mer.</span><span>Jeu.</span><span>Ven.</span><span>Sam.</span><span>Dim.</span></div>
                  </div>
                  <div className="flex items-center gap-3 rounded-2xl border border-blue-100 bg-blue-50/70 px-4 py-3">
                    <span className="grid h-8 w-8 shrink-0 place-items-center rounded-full bg-white text-brand"><BadgeCheck size={17} /></span>
                    <div className="min-w-0 flex-1"><p className="text-xs font-semibold">Prestation validée</p><p className="truncate text-[11px] text-slate-500">Résidence Al Amal · Lot peinture</p></div>
                    <span className="text-[11px] font-semibold text-emerald-700">Approuvée</span>
                  </div>
                </div>
              </div>
            </div>
            <p className="mt-3 text-center text-[11px] text-blue-100/70">Exemple illustratif — les données réelles proviennent de votre espace.</p>
          </div>
        </div>
      </section>

      <section id="fonctionnalites" className="scroll-mt-20 px-5 py-20 sm:px-8 lg:py-24">
        <div className="mx-auto max-w-7xl">
          <div className="mx-auto max-w-2xl text-center">
            <span className="text-xs font-bold uppercase tracking-[.18em] text-brand">Un seul flux de travail</span>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-navy sm:text-4xl">Du chantier à la gestion financière</h2>
            <p className="mt-4 leading-7 text-slate-600">MizanFlow relie les personnes, les prestations et les factures dans un espace partagé par votre entreprise.</p>
          </div>
          <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {features.map(({ icon: Icon, title, text }) => <article key={title} className="group rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-blue-200 hover:shadow-lg">
              <span className="grid h-11 w-11 place-items-center rounded-2xl bg-blue-50 text-brand transition group-hover:bg-brand group-hover:text-white"><Icon size={21} /></span>
              <h3 className="mt-5 text-base font-semibold tracking-tight text-navy">{title}</h3>
              <p className="mt-2 text-sm leading-6 text-slate-600">{text}</p>
            </article>)}
          </div>
        </div>
      </section>

      <section id="parcours" className="scroll-mt-20 border-y border-slate-200/70 bg-surface px-5 py-20 sm:px-8 lg:py-24">
        <div className="mx-auto grid max-w-7xl gap-10 lg:grid-cols-[.8fr_1.2fr] lg:items-center">
          <div>
            <span className="text-xs font-bold uppercase tracking-[.18em] text-brand">Simple à suivre</span>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-navy sm:text-4xl">Chaque étape a son responsable.</h2>
            <p className="mt-4 max-w-lg leading-7 text-slate-600">Les équipes terrain saisissent. Les responsables valident. Le bureau suit les factures et les encaissements.</p>
            <Link to="/register" className="btn-primary mt-7">Créer mon espace <ArrowRight size={16} /></Link>
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            {steps.map(step => <article key={step.number} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <span className="font-mono text-xs font-semibold text-brand">{step.number}</span>
              <h3 className="mt-5 text-sm font-semibold text-navy">{step.title}</h3>
              <p className="mt-2 text-sm leading-6 text-slate-600">{step.text}</p>
            </article>)}
          </div>
        </div>
      </section>

      <section id="tarifs" className="scroll-mt-20 px-5 py-20 sm:px-8 lg:py-24">
        <div className="mx-auto max-w-7xl">
          <div className="mx-auto max-w-2xl text-center">
            <span className="text-xs font-bold uppercase tracking-[.18em] text-brand">Tarification transparente</span>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-navy sm:text-4xl">Un plan adapté à votre équipe</h2>
            <p className="mt-4 text-slate-600">Commencez par un essai, puis choisissez selon le nombre d’utilisateurs et de projets actifs.</p>
          </div>
          <div className="mx-auto mt-7 flex w-fit items-center gap-1 rounded-full border border-slate-200 bg-surface p-1" role="group" aria-label="Période de facturation">
            <button type="button" aria-pressed={!yearly} onClick={() => setYearly(false)} className={`rounded-full px-4 py-2 text-sm font-semibold transition ${!yearly ? 'bg-white text-navy shadow-sm' : 'text-slate-500 hover:text-navy'}`}>Mensuel</button>
            <button type="button" aria-pressed={yearly} onClick={() => setYearly(true)} className={`rounded-full px-4 py-2 text-sm font-semibold transition ${yearly ? 'bg-white text-navy shadow-sm' : 'text-slate-500 hover:text-navy'}`}>Annuel <span className="text-emerald-600">· 2 mois offerts</span></button>
          </div>
          <div className="mx-auto mt-10 grid max-w-3xl gap-5 lg:grid-cols-2">
            {plans.map(plan => <article key={plan.name} className={`relative flex flex-col rounded-3xl border bg-white p-6 shadow-sm ${plan.featured ? 'border-brand ring-1 ring-brand/20 shadow-lg shadow-blue-900/10' : 'border-slate-200'}`}>
              {plan.featured && <span className="absolute -top-3 left-6 rounded-full bg-brand px-3 py-1 text-[11px] font-bold text-white">Équipe en croissance</span>}
              <h3 className="text-lg font-semibold text-navy">{plan.name}</h3>
              <p className="mt-1 text-sm text-slate-500">Pour votre activité au quotidien</p>
              <p className="mt-6 text-4xl font-semibold tracking-tight text-navy">{yearly ? plan.yearly : plan.monthly}<span className="ml-1 text-sm font-medium text-slate-500">DH / {yearly ? 'an' : 'mois'}</span></p>
              <div className="my-6 h-px bg-slate-100" />
              <ul className="mb-7 flex-1 space-y-3 text-sm text-slate-600">
                {[plan.users, plan.projects, 'Clients, projets et prestations', 'Factures et suivi des paiements'].map(item => <li key={item} className="flex items-start gap-2.5"><Check size={16} className="mt-0.5 shrink-0 text-emerald-600" />{item}</li>)}
              </ul>
              <Link to="/register" className={plan.featured ? 'btn-primary w-full' : 'btn-ghost w-full'}>Essayer 14 jours <ArrowRight size={16} /></Link>
            </article>)}
          </div>
          <p className="mx-auto mt-6 max-w-2xl text-center text-xs leading-5 text-slate-500">Les prix et quotas reflètent les plans actuellement configurés. L’activation d’un abonnement est vérifiée manuellement après réception du virement.</p>
        </div>
      </section>

      <section className="border-t border-slate-200/70 bg-surface px-5 py-20 sm:px-8">
        <div className="mx-auto max-w-3xl">
          <div className="text-center"><span className="text-xs font-bold uppercase tracking-[.18em] text-brand">Besoin de savoir</span><h2 className="mt-3 text-3xl font-semibold tracking-tight text-navy">Questions fréquentes</h2></div>
          <div className="mt-8 space-y-3">{questions.map(([question, answer]) => <details key={question} className="group rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm">
            <summary className="flex cursor-pointer list-none items-center justify-between gap-4 text-sm font-semibold text-navy">{question}<ChevronDown size={17} className="shrink-0 text-slate-400 transition group-open:rotate-180" /></summary>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-600">{answer}</p>
          </details>)}</div>
        </div>
      </section>

      <section className="px-5 py-16 sm:px-8">
        <div className="mx-auto flex max-w-7xl flex-col items-start justify-between gap-6 rounded-[2rem] bg-navy px-6 py-10 text-white shadow-xl shadow-navy/10 sm:px-10 lg:flex-row lg:items-center">
          <div><p className="text-sm font-semibold text-blue-200">Prêt à structurer le suivi de vos chantiers ?</p><h2 className="mt-2 text-2xl font-semibold tracking-tight sm:text-3xl">Essayez MizanFlow avec votre équipe.</h2></div>
          <Link to="/register" className="btn-primary btn-lg shrink-0">Démarrer gratuitement <ArrowRight size={17} /></Link>
        </div>
      </section>
    </main>

    <footer className="border-t border-slate-200 bg-white px-5 py-7 sm:px-8">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 text-xs text-slate-500 sm:flex-row">
        <Link to="/" className="font-bold text-navy">Mizan<span className="text-brand">Flow</span></Link>
        <p>Un suivi plus clair, du chantier à l’encaissement.</p>
        <div className="flex gap-4"><Link className="hover:text-navy" to="/terms">Conditions</Link><Link className="hover:text-navy" to="/privacy">Confidentialité</Link></div>
      </div>
    </footer>
  </div>
}
