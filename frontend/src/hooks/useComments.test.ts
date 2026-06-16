import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useComments } from './useComments'
import type { Comment } from '../types/comment'

vi.mock('../api/comments')

import * as commentsApi from '../api/comments'

const buildComment = (id: number): Comment => ({
  id,
  content: `comment ${id}`,
  createdAt: '2024-01-01T00:00:00',
  user: { id: 1, username: 'alice', profileImageUrl: null },
})

describe('useComments', () => {
  beforeEach(() => vi.clearAllMocks())

  it('マウント時にコメントを取得する', async () => {
    vi.mocked(commentsApi.getComments).mockResolvedValue([buildComment(1), buildComment(2)])
    const { result } = renderHook(() => useComments(10))

    expect(result.current.loading).toBe(true)
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.comments).toHaveLength(2)
  })

  it('addComment: 楽観的にリスト先頭へ追加する', async () => {
    vi.mocked(commentsApi.getComments).mockResolvedValue([buildComment(1)])
    const newComment = buildComment(99)
    vi.mocked(commentsApi.createComment).mockResolvedValue(newComment)

    const { result } = renderHook(() => useComments(10))
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => { await result.current.addComment('hello') })

    expect(result.current.comments[0].id).toBe(99)
    expect(commentsApi.createComment).toHaveBeenCalledWith(10, 'hello')
  })

  it('removeComment: 楽観的にリストから除去する', async () => {
    vi.mocked(commentsApi.getComments).mockResolvedValue([buildComment(1), buildComment(2)])
    vi.mocked(commentsApi.deleteComment).mockResolvedValue({ data: undefined } as never)

    const { result } = renderHook(() => useComments(10))
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => { await result.current.removeComment(1) })

    expect(result.current.comments.find((c) => c.id === 1)).toBeUndefined()
    expect(result.current.comments).toHaveLength(1)
  })

  it('removeComment APIエラー時: 再取得してロールバック', async () => {
    vi.mocked(commentsApi.getComments).mockResolvedValue([buildComment(1)])
    vi.mocked(commentsApi.deleteComment).mockRejectedValue(new Error('error'))

    const { result } = renderHook(() => useComments(10))
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => { await result.current.removeComment(1) })

    expect(commentsApi.getComments).toHaveBeenCalledTimes(2)
  })
})
