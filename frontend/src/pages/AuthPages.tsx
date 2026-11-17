import { useState, type ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowRight, BadgeCheck, Building2, Check, LoaderCircle, LockKeyhole } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { ErrorBox, Field } from '../components/ui'
import { errMsg } from '../lib/api'
import { useAuth } from '../lib/auth'
import { useI18n } from '../lib/i18n'

function AuthLayout({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  const { lang, setLang } = useI18n()
  return <div className="min-h-screen bg-white lg:grid lg:grid-cols-[1.05fr_.95fr]">
    <aside className="hero-bg relative hidden min-h-screen overflow-hidden px-10 py-9 text-white lg:flex lg:flex-col lg:justify-between xl:px-16">
      <div aria-hidden="true" className="grid-bg absolute inset-0" />
      <div aria-hidden="true" className="absolute -bottom-36 -left-20 h-[30rem] w-[30rem] rounded-full bg-blue-500/15 blur-3xl" />
      <Link to="/" className="relative flex w-fit items-center gap-3" aria-label="MizanFlow — accueil">
        <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand shadow-glow"><Building2 size={20} /></span>
        <span className="text-xl font-bold tracking-tight">Mizan<span className="text-blue-300">Flow</span></span>
      </Link>
      <div className="relative max-w-xl py-12">
        <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/[.06] px-3 py-1.5 text-xs font-medium text-blue-100"><LockKeyhole size={14} /> Votre espace de travail</span>
        <h2 className="mt-6 text-4xl font-semibold leading-tight tracking-[-.04em] xl:text-5xl">Du chantier à l’encaissement, gardez le fil.</h2>
        <p className="mt-5 max-w-lg text-base leading-7 text-slate-300">Centralisez vos projets, les validations de prestations et le suivi de facturation dans un espace clair pour toute l’équipe.</p>
        <div className="mt-8 grid max-w-md gap-3">
          {['Suivi des prestations et validations', 'Factures et paiements au même endroit', 'Accès adaptés à chaque rôle'].map(text => <p key={text} className="flex items-center gap-2.5 text-sm text-slate-200"><span className="grid h-5 w-5 place-items-center rounded-full bg-emerald-400/15 text-emerald-300"><Check size={12} /></span>{text}</p>)}
        </div>
      </div>
      <div className="relative flex items-center justify-between border-t border-white/10 pt-5 text-xs text-slate-400"><span>MizanFlow · Gestion de chantier</span><span>Conçu pour les équipes BTP</span></div>
    </aside>

    <main className="flex min-h-screen flex-col px-5 py-6 sm:px-8 lg:px-12 xl:px-20">
      <header className="flex items-center justify-between lg:justify-end">
        <Link to="/" className="flex items-center gap-2.5 lg:hidden">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-navy text-white"><Building2 size={18} /></span>
          <span className="font-bold tracking-tight text-navy">Mizan<span className="text-brand">Flow</span></span>
        </Link>
        <button type="button" className="rounded-lg px-3 py-2 text-xs font-semibold text-slate-500 transition hover:bg-slate-100 hover:text-navy" onClick={() => setLang(lang === 'fr' ? 'ar' : 'fr')}>{lang === 'fr' ? 'العربية' : 'Français'}</button>
      </header>
      <div className="flex flex-1 items-center justify-center py-10">
        <div className="w-full max-w-md">
          <div className="mb-7 grid h-12 w-12 place-items-center rounded-2xl bg-blue-50 text-brand"><BadgeCheck size={23} /></div>
          <h1 className="text-2xl font-semibold tracking-tight text-navy sm:text-3xl">{title}</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p>
          <div className="mt-7">{children}</div>
          <p className="mt-8 text-center text-xs leading-5 text-slate-400">En continuant, vous acceptez nos <Link className="font-medium text-slate-600 underline underline-offset-2 hover:text-brand" to="/terms">conditions d’utilisation</Link> et notre <Link className="font-medium text-slate-600 underline underline-offset-2 hover:text-brand" to="/privacy">politique de confidentialité</Link>.</p>
        </div>
      </div>
      <footer className="text-center text-[11px] text-slate-400">MizanFlow · Gestion de chantier et facturation</footer>
    </main>
  </div>
}

const loginSchema = z.object({ email: z.string().email('Email invalide'), password: z.string().min(1, 'Mot de passe requis') })

export function Login() {
  const { login, sessionError } = useAuth()
  const navigate = useNavigate()
  const { t } = useI18n()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<z.infer<typeof loginSchema>>({ resolver: zodResolver(loginSchema) })
  const formError = error || sessionError

  return <AuthLayout title="Bon retour parmi nous" description="Connectez-vous pour retrouver les projets et le suivi de votre entreprise.">
    <form noValidate className="space-y-5" onSubmit={handleSubmit(async values => {
      try { await login(values.email, values.password); navigate('/app') } catch (reason) { setError(errMsg(reason)) }
    })}>
      {formError && <ErrorBox text={formError} />}
      <Field label={t('email')} error={errors.email?.message}><input className="input" type="email" autoComplete="username" placeholder="nom@entreprise.ma" {...register('email')} /></Field>
      <Field label={t('password')} error={errors.password?.message}><input className="input" type="password" autoComplete="current-password" placeholder="Votre mot de passe" {...register('password')} /></Field>
      <button className="btn-primary min-h-12 w-full" disabled={isSubmitting}>{isSubmitting ? <LoaderCircle size={17} className="animate-spin" /> : null}{isSubmitting ? 'Connexion…' : t('login')}<ArrowRight size={16} /></button>
      <p className="text-center text-sm text-slate-500">Pas encore de compte ? <Link className="font-semibold text-brand hover:text-brand-600" to="/register">Créer mon entreprise</Link></p>
    </form>
  </AuthLayout>
}

const registerSchema = z.object({
  companyName: z.string().min(2, 'Saisissez le nom de votre entreprise'),
  fullName: z.string().min(2, 'Saisissez votre nom complet'),
  email: z.string().email('Email invalide'),
  password: z.string().min(12, '12 caractères minimum'),
})

export function Register() {
  const { register: registerAccount } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<z.infer<typeof registerSchema>>({ resolver: zodResolver(registerSchema) })

  return <AuthLayout title="Créez votre espace" description="Configurez l’espace de votre entreprise et suivez votre activité depuis une interface claire.">
    <form noValidate className="space-y-4" onSubmit={handleSubmit(async values => {
      try {
        await registerAccount(values.companyName, values.fullName, values.email, values.password)
        navigate('/app')
      } catch (reason) { setError(errMsg(reason)) }
    })}>
      {error && <ErrorBox text={error} />}
      <Field label="Nom de l’entreprise" error={errors.companyName?.message}><input className="input" autoComplete="organization" placeholder="Ex. Atlas Construction" {...register('companyName')} /></Field>
      <Field label="Votre nom complet" error={errors.fullName?.message}><input className="input" autoComplete="name" placeholder="Ex. Salma El Amrani" {...register('fullName')} /></Field>
      <Field label="Email professionnel" error={errors.email?.message}><input className="input" type="email" autoComplete="email" placeholder="nom@entreprise.ma" {...register('email')} /></Field>
      <Field label="Mot de passe" error={errors.password?.message}><input className="input" type="password" autoComplete="new-password" placeholder="12 caractères minimum" {...register('password')} /></Field>
      <p className="text-xs leading-5 text-slate-500">Utilisez au moins 12 caractères. Vous pourrez configurer votre équipe après la création du compte.</p>
      <button className="btn-primary min-h-12 w-full" disabled={isSubmitting}>{isSubmitting ? <LoaderCircle size={17} className="animate-spin" /> : null}{isSubmitting ? 'Création en cours…' : 'Créer mon espace'}<ArrowRight size={16} /></button>
      <p className="text-center text-sm text-slate-500">Vous avez déjà un compte ? <Link className="font-semibold text-brand hover:text-brand-600" to="/login">Se connecter</Link></p>
    </form>
  </AuthLayout>
}
