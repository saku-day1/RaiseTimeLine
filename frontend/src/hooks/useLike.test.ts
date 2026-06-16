import { renderHook, act } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useLike } from './useLike'

vi.mock('../api/likes')

import * as likesApi from '../api/likes'

describe('useLike', () => {
  beforeEach(() => vi.clearAllMocks())

  it('初期状態がpropsを反映する', () => {
    const { result } = renderHook(() => useLike(1, false, 3))
    expect(result.current.liked).toBe(false)
    expect(result.current.count).toBe(3)
  })

  it('未いいねからtoggle: 楽観的インクリメント後サーバー値で確定', async () => {
    vi.mocked(likesApi.likePost).mockResolvedValue({ liked: true, count: 5 })
    const { result } = renderHook(() => useLike(1, false, 4))

    await act(async () => { await result.current.toggle() })

    expect(result.current.liked).toBe(true)
    expect(result.current.count).toBe(5)
    expect(likesApi.likePost).toHaveBeenCalledWith(1)
  })

  it('いいね済みからtoggle: 楽観的デクリメント後サーバー値で確定', async () => {
    vi.mocked(likesApi.unlikePost).mockResolvedValue({ liked: false, count: 2 })
    const { result } = renderHook(() => useLike(1, true, 3))

    await act(async () => { await result.current.toggle() })

    expect(result.current.liked).toBe(false)
    expect(result.current.count).toBe(2)
    expect(likesApi.unlikePost).toHaveBeenCalledWith(1)
  })

  it('APIエラー時は元の状態にロールバックする', async () => {
    vi.mocked(likesApi.likePost).mockRejectedValue(new Error('Network error'))
    const { result } = renderHook(() => useLike(1, false, 3))

    await act(async () => { await result.current.toggle() })

    expect(result.current.liked).toBe(false)
    expect(result.current.count).toBe(3)
  })

  it('APIエラー時はnullを返す', async () => {
    vi.mocked(likesApi.likePost).mockRejectedValue(new Error('error'))
    const { result } = renderHook(() => useLike(1, false, 0))

    let ret: unknown
    await act(async () => { ret = await result.current.toggle() })

    expect(ret).toBeNull()
  })
})
