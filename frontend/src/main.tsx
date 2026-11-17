import React from 'react'; import ReactDOM from 'react-dom/client'; import { BrowserRouter } from 'react-router-dom'; import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'; import { AuthProvider } from './lib/auth'; import { I18nProvider } from './lib/i18n'; import './index.css'
const qc = new QueryClient({ defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } } })
ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><QueryClientProvider client={qc}><BrowserRouter><I18nProvider><AuthProvider><App /></AuthProvider></I18nProvider></BrowserRouter></QueryClientProvider></React.StrictMode>)
