import { render, screen } from '@testing-library/react'
import { I18nProvider, useI18n } from './i18n'

const values = new Map<string, string>()
const storage: Storage = {
  get length() { return values.size },
  clear: () => values.clear(),
  getItem: key => values.get(key) ?? null,
  key: index => Array.from(values.keys())[index] ?? null,
  removeItem: key => { values.delete(key) },
  setItem: (key, value) => { values.set(key, String(value)) },
}

function CurrentLanguage() {
  const { lang } = useI18n()
  return <span>{lang}</span>
}

describe('I18nProvider', () => {
  beforeEach(() => {
    Object.defineProperty(window, 'localStorage', { configurable: true, value: storage })
  })

  afterEach(() => {
    values.clear()
    Reflect.deleteProperty(window, 'localStorage')
    document.documentElement.lang = ''
    document.documentElement.dir = 'ltr'
  })

  it('applies the saved language direction on initial render', () => {
    window.localStorage.setItem('lang', 'ar')
    render(<I18nProvider><CurrentLanguage /></I18nProvider>)

    expect(screen.getByText('ar')).toBeInTheDocument()
    expect(document.documentElement.lang).toBe('ar')
    expect(document.documentElement.dir).toBe('rtl')
  })
})
