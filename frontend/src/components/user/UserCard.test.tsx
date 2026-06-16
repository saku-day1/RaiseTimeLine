import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import UserCard from './UserCard'
import { renderWithProviders } from '../../test/utils'
import type { UserSummary } from '../../types/user'

vi.mock('../../hooks/useFollow')
import * as useFollowModule from '../../hooks/useFollow'

const mockToggle = vi.fn()

const buildUser = (following = false): UserSummary => ({
  id: 2,
  username: 'bob',
  displayName: 'Bob',
  profileImageUrl: null,
  following,
})

describe('UserCard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useFollowModule.useFollow).mockReturnValue({ following: false, loading: false, toggle: mockToggle })
  })

  it('displayNameとusernameを表示する', () => {
    renderWithProviders(<UserCard user={buildUser()} />)
    expect(screen.getByText('Bob')).toBeInTheDocument()
    expect(screen.getByText('@bob')).toBeInTheDocument()
  })

  it('未フォロー状態でフォローボタンを表示する', () => {
    renderWithProviders(<UserCard user={buildUser(false)} />)
    expect(screen.getByRole('button')).toHaveTextContent('フォロー')
  })

  it('フォロー中状態でフォロー中ボタンを表示する', () => {
    vi.mocked(useFollowModule.useFollow).mockReturnValue({ following: true, loading: false, toggle: mockToggle })
    renderWithProviders(<UserCard user={buildUser(true)} />)
    expect(screen.getByRole('button')).toHaveTextContent('フォロー中')
  })

  it('ロード中はボタンがdisabledになる', () => {
    vi.mocked(useFollowModule.useFollow).mockReturnValue({ following: false, loading: true, toggle: mockToggle })
    renderWithProviders(<UserCard user={buildUser()} />)
    expect(screen.getByRole('button')).toBeDisabled()
  })

  it('プロフィールリンクが/users/:idを指している', () => {
    renderWithProviders(<UserCard user={buildUser()} />)
    expect(screen.getByRole('link')).toHaveAttribute('href', '/users/2')
  })
})
