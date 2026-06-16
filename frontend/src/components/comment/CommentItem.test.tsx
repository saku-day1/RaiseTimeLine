import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CommentItem from './CommentItem'
import { renderWithProviders } from '../../test/utils'
import type { Comment } from '../../types/comment'

const buildComment = (userId = 2): Comment => ({
  id: 5,
  content: 'すごい投稿です！',
  createdAt: '2024-01-01T00:00:00',
  user: { id: userId, username: 'bob', profileImageUrl: null },
})

describe('CommentItem', () => {
  const onDelete = vi.fn()
  beforeEach(() => vi.clearAllMocks())

  it('コメント内容とユーザー名を表示する', () => {
    renderWithProviders(<CommentItem comment={buildComment()} onDelete={onDelete} />)
    expect(screen.getByText('すごい投稿です！')).toBeInTheDocument()
    expect(screen.getByText('bob')).toBeInTheDocument()
  })

  it('コメントの所有者には削除ボタンを表示する', () => {
    renderWithProviders(
      <CommentItem comment={buildComment(1)} onDelete={onDelete} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    expect(screen.getByTitle('削除')).toBeInTheDocument()
  })

  it('コメントの所有者でないユーザーには削除ボタンを表示しない', () => {
    renderWithProviders(
      <CommentItem comment={buildComment(2)} onDelete={onDelete} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    expect(screen.queryByTitle('削除')).not.toBeInTheDocument()
  })

  it('削除ボタンクリックでonDeleteを呼び出す', async () => {
    const user = userEvent.setup()
    renderWithProviders(
      <CommentItem comment={buildComment(1)} onDelete={onDelete} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    await user.click(screen.getByTitle('削除'))
    expect(onDelete).toHaveBeenCalledWith(5)
  })
})
