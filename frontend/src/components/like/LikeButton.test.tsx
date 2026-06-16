import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import LikeButton from './LikeButton'
import { renderWithProviders } from '../../test/utils'

vi.mock('../../hooks/useLike')
import * as useLikeModule from '../../hooks/useLike'

const mockToggle = vi.fn()

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(useLikeModule.useLike).mockReturnValue({ liked: false, count: 3, toggle: mockToggle })
})

describe('LikeButton', () => {
  it('未いいね状態: 空ハートとカウントを表示する', () => {
    renderWithProviders(<LikeButton postId={1} initialLiked={false} initialCount={3} />)
    expect(screen.getByRole('button')).toHaveTextContent('🤍')
    expect(screen.getByRole('button')).toHaveTextContent('3')
  })

  it('いいね済み状態: 塗りつぶしハートを表示する', () => {
    vi.mocked(useLikeModule.useLike).mockReturnValue({ liked: true, count: 5, toggle: mockToggle })
    renderWithProviders(<LikeButton postId={1} initialLiked={true} initialCount={5} />)
    expect(screen.getByRole('button')).toHaveTextContent('❤️')
  })

  it('認証済みユーザーがクリック: toggleを呼び出す', async () => {
    const user = userEvent.setup()
    mockToggle.mockResolvedValue({ liked: true, count: 4 })
    renderWithProviders(
      <LikeButton postId={1} initialLiked={false} initialCount={3} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    await user.click(screen.getByRole('button'))
    expect(mockToggle).toHaveBeenCalledOnce()
  })

  it('未認証ユーザーがクリック: /loginへナビゲートする', async () => {
    const user = userEvent.setup()
    renderWithProviders(<LikeButton postId={1} initialLiked={false} initialCount={0} />)
    await user.click(screen.getByRole('button'))
    expect(mockToggle).not.toHaveBeenCalled()
  })
})
