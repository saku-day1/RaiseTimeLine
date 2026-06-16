import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from '../contexts/AuthContext'
import type { AuthUser } from '../types/auth'
import type { ReactElement } from 'react'

export function renderWithProviders(
  ui: ReactElement,
  {
    initialEntries = ['/'],
    authUser = null,
  }: { initialEntries?: string[]; authUser?: AuthUser | null } = {}
) {
  if (authUser) {
    localStorage.setItem('user', JSON.stringify(authUser))
    localStorage.setItem('token', 'test-token')
  }
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthProvider>{ui}</AuthProvider>
    </MemoryRouter>
  )
}
