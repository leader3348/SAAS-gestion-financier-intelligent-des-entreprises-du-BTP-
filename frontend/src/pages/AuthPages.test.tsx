import { render, screen } from '@testing-library/react'; import userEvent from '@testing-library/user-event'; import { MemoryRouter } from 'react-router-dom'; import { Register } from './AuthPages'; import { AuthProvider } from '../lib/auth'; import { I18nProvider } from '../lib/i18n'
import axios from 'axios'
const wrap = () => render(<MemoryRouter><I18nProvider><AuthProvider><Register /></AuthProvider></I18nProvider></MemoryRouter>)
describe('Register form', () => {
  it('rejects a password shorter than 12 characters and an invalid email', async () => {
    vi.spyOn(axios, 'post').mockRejectedValue({ isAxiosError: true, response: { status: 401 } })
    wrap()
    const inputs = document.querySelectorAll('input'); await userEvent.type(inputs[0], 'Alpha BTP'); await userEvent.type(inputs[1], 'Karim'); await userEvent.type(inputs[2], 'pas-un-email'); await userEvent.type(inputs[3], 'trop-court')
    await userEvent.click(screen.getByRole('button', { name: /créer mon espace/i }))
    expect(await screen.findByText('12 caractères minimum')).toBeInTheDocument()
  })
})
