import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PostCard from './PostCard'
import { renderWithProviders } from '../../test/utils'
import type { PostSummary } from '../../types/post'

vi.mock('../../hooks/useLike', () => ({
  useLike: () => ({ liked: false, count: 0, toggle: vi.fn() }),
}))
vi.mock('../../api/posts')
import * as postsApi from '../../api/posts'

const buildPost = (overrides: Partial<PostSummary> = {}): PostSummary => ({
  id: 1,
  content: 'テスト投稿',
  imageUrl: null,
  createdAt: '2024-01-01T00:00:00',
  userId: 10,
  username: 'alice',
  profileImageUrl: null,
  likeCount: 0,
  commentCount: 0,
  liked: false,
  ...overrides,
})

describe('PostCard', () => {
  beforeEach(() => vi.clearAllMocks())

  it('投稿内容とユーザー名を表示する', () => {
    renderWithProviders(<PostCard post={buildPost()} />)
    expect(screen.getByText('テスト投稿')).toBeInTheDocument()
    expect(screen.getByText('alice')).toBeInTheDocument()
  })

  it('imageUrlがある場合は画像を表示する', () => {
    renderWithProviders(<PostCard post={buildPost({ imageUrl: 'https://example.com/img.jpg' })} />)
    expect(screen.getByRole('img', { name: '投稿画像' })).toBeInTheDocument()
  })

  it('投稿者には削除ボタンを表示し、他のユーザーには非表示', () => {
    renderWithProviders(
      <PostCard post={buildPost({ userId: 1 })} />,
      { authUser: { userId: 1, username: 'alice' } }
    )
    expect(screen.getByTitle('削除')).toBeInTheDocument()
  })

  it('投稿者でないユーザーには削除ボタンを表示しない', () => {
    renderWithProviders(
      <PostCard post={buildPost({ userId: 10 })} />,
      { authUser: { userId: 99, username: 'bob' } }
    )
    expect(screen.queryByTitle('削除')).not.toBeInTheDocument()
  })

  it('削除確認でOKを選択するとdeletePostを呼び出す', async () => {
    const user = userEvent.setup()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.mocked(postsApi.deletePost).mockResolvedValue(undefined as never)
    const onDelete = vi.fn()

    renderWithProviders(
      <PostCard post={buildPost({ userId: 1 })} onDelete={onDelete} />,
      { authUser: { userId: 1, username: 'alice' } }
    )

    await user.click(screen.getByTitle('削除'))
    expect(postsApi.deletePost).toHaveBeenCalledWith(1)
  })

  it('削除確認でキャンセルするとdeletePostを呼び出さない', async () => {
    const user = userEvent.setup()
    vi.spyOn(window, 'confirm').mockReturnValue(false)

    renderWithProviders(
      <PostCard post={buildPost({ userId: 1 })} />,
      { authUser: { userId: 1, username: 'alice' } }
    )

    await user.click(screen.getByTitle('削除'))
    expect(postsApi.deletePost).not.toHaveBeenCalled()
  })
})
