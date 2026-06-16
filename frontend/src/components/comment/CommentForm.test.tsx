import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CommentForm from './CommentForm'
import { renderWithProviders } from '../../test/utils'
import type { Comment } from '../../types/comment'

const mockAddComment = vi.fn()
const mockOnSuccess = vi.fn()

const buildComment = (): Comment => ({
  id: 1,
  content: 'great post!',
  createdAt: '2024-01-01T00:00:00',
  user: { id: 1, username: 'alice', profileImageUrl: null },
})

describe('CommentForm', () => {
  beforeEach(() => vi.clearAllMocks())

  it('未認証ユーザーにはログインを促すメッセージを表示する', () => {
    renderWithProviders(<CommentForm addComment={mockAddComment} onSuccess={mockOnSuccess} />)
    expect(screen.getByText(/ログイン/)).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
  })

  it('認証済みユーザーにはテキストエリアを表示する', () => {
    renderWithProviders(
      <CommentForm addComment={mockAddComment} onSuccess={mockOnSuccess} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    expect(screen.getByRole('textbox')).toBeInTheDocument()
  })

  it('空入力でサブミットしてもaddCommentを呼び出さない', async () => {
    const user = userEvent.setup()
    renderWithProviders(
      <CommentForm addComment={mockAddComment} onSuccess={mockOnSuccess} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    await user.click(screen.getByRole('button', { name: 'コメントする' }))
    expect(mockAddComment).not.toHaveBeenCalled()
  })

  it('有効な内容でサブミットするとaddCommentを呼び出しテキストエリアをクリアする', async () => {
    const user = userEvent.setup()
    mockAddComment.mockResolvedValue(buildComment())
    renderWithProviders(
      <CommentForm addComment={mockAddComment} onSuccess={mockOnSuccess} />,
      { authUser: { userId: 1, username: 'alice' } }
    )

    await user.type(screen.getByRole('textbox'), 'great post!')
    await user.click(screen.getByRole('button', { name: 'コメントする' }))

    expect(mockAddComment).toHaveBeenCalledWith('great post!')
    expect(screen.getByRole('textbox')).toHaveValue('')
  })
})
