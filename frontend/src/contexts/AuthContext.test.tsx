import { describe, it, expect } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider, useAuth } from './AuthContext'
import type { AuthUser } from '../types/auth'

function TestConsumer() {
  const { user, isAuthenticated, login, logout } = useAuth()
  return (
    <div>
      <span data-testid="username">{user?.username ?? 'none'}</span>
      <span data-testid="authenticated">{String(isAuthenticated)}</span>
      <button onClick={() => login('tok', { userId: 1, username: 'alice' })}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  )
}

function renderConsumer(storageUser?: AuthUser) {
  if (storageUser) {
    localStorage.setItem('user', JSON.stringify(storageUser))
    localStorage.setItem('token', 'existing-token')
  }
  return render(
    <MemoryRouter>
      <AuthProvider><TestConsumer /></AuthProvider>
    </MemoryRouter>
  )
}

describe('AuthContext', () => {
  it('localStorageがなければuser=null、isAuthenticated=false', () => {
    renderConsumer()
    expect(screen.getByTestId('username')).toHaveTextContent('none')
    expect(screen.getByTestId('authenticated')).toHaveTextContent('false')
  })

  it('localStorageにユーザーがあれば初期状態を復元する', () => {
    renderConsumer({ userId: 2, username: 'bob' })
    expect(screen.getByTestId('username')).toHaveTextContent('bob')
    expect(screen.getByTestId('authenticated')).toHaveTextContent('true')
  })

  it('login() でlocalStorageに保存して状態を更新する', async () => {
    const user = userEvent.setup()
    renderConsumer()
    await user.click(screen.getByRole('button', { name: 'login' }))
    expect(localStorage.getItem('token')).toBe('tok')
    expect(JSON.parse(localStorage.getItem('user')!).username).toBe('alice')
    expect(screen.getByTestId('username')).toHaveTextContent('alice')
  })

  it('logout() でlocalStorageを削除してuser=nullにする', async () => {
    const user = userEvent.setup()
    renderConsumer({ userId: 1, username: 'alice' })
    await user.click(screen.getByRole('button', { name: 'logout' }))
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
    expect(screen.getByTestId('username')).toHaveTextContent('none')
  })

  it('AuthProvider外でuseAuth()を呼び出すとエラーを投げる', () => {
    function BadConsumer() { useAuth(); return null }
    expect(() => render(<MemoryRouter><BadConsumer /></MemoryRouter>)).toThrow()
  })
})
