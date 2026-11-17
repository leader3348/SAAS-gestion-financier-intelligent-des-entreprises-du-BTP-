import axios from 'axios'

export type Tokens = { accessToken: string; expiresInSeconds: number; role: string; fullName: string }

let access: string | null = null
export const api = axios.create({ baseURL: '/api/v1', withCredentials: true })
export const setAccess = (token: string | null) => { access = token }

api.interceptors.request.use(config => {
  if (access) config.headers.Authorization = `Bearer ${access}`
  return config
})

let refreshing: Promise<string | null> | null = null

export async function refreshSession(): Promise<Tokens | null> {
  try {
    const { data } = await axios.post<Tokens>('/api/v1/auth/web/refresh', {}, { withCredentials: true })
    access = data.accessToken
    return data
  } catch (error) {
    access = null
    if (axios.isAxiosError(error) && error.response?.status === 401) return null
    throw error
  }
}

api.interceptors.response.use(response => response, async error => {
  const config = error.config
  if (error.response?.status === 401 && config && !config._retry && !config.url?.includes('/auth/')) {
    config._retry = true
    refreshing ??= refreshSession().then(tokens => tokens?.accessToken ?? null).finally(() => { refreshing = null })
    const token = await refreshing
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      return api(config)
    }
    window.location.href = '/login'
  }
  return Promise.reject(error)
})

export const errMsg = (error: unknown): string => {
  if (axios.isAxiosError<{ message?: string }>(error) && error.response?.data?.message) return error.response.data.message
  return 'Une erreur est survenue'
}
