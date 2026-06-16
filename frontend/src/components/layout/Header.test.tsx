import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Header from './Header'
import { renderWithProviders } from '../../test/utils'

describe('Header', () => {
  beforeEach(() => vi.clearAllMocks())

  it('未認証: ログイン・新規登録リンクを表示する', () => {
    renderWithProviders(<Header />)
    expect(screen.getByRole('link', { name: 'ログイン' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '新規登録' })).toBeInTheDocument()
  })

  it('認証済み: ユーザー名・ログアウトボタンを表示する', () => {
    renderWithProviders(<Header />, { authUser: { userId: 1, username: 'alice' } })
    expect(screen.getByText('alice')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'ログアウト' })).toBeInTheDocument()
  })

  it('認証済み: ユーザー検索と投稿するリンクを表示する', () => {
    renderWithProviders(<Header />, { authUser: { userId: 1, username: 'alice' } })
    expect(screen.getByRole('link', { name: 'ユーザー検索' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '投稿する' })).toBeInTheDocument()
  })

  it('ログアウトクリック: logoutが呼ばれる', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Header />, { authUser: { userId: 1, username: 'alice' } })
    await user.click(screen.getByRole('button', { name: 'ログアウト' }))
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
  })
})
