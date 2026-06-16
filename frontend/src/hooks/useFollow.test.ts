import { renderHook, act } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useFollow } from './useFollow'

vi.mock('../api/users')

import * as usersApi from '../api/users'

describe('useFollow', () => {
  beforeEach(() => vi.clearAllMocks())

  it('初期状態がpropsを反映する', () => {
    const { result } = renderHook(() => useFollow(2, false))
    expect(result.current.following).toBe(false)
    expect(result.current.loading).toBe(false)
  })

  it('toggle: 未フォロー → フォロー (楽観的更新)', async () => {
    vi.mocked(usersApi.followUser).mockResolvedValue({ following: true, followerCount: 1, followingCount: 0 })
    const { result } = renderHook(() => useFollow(2, false))

    await act(async () => { await result.current.toggle() })

    expect(result.current.following).toBe(true)
    expect(usersApi.followUser).toHaveBeenCalledWith(2)
  })

  it('toggle: フォロー中 → アンフォロー', async () => {
    vi.mocked(usersApi.unfollowUser).mockResolvedValue({ following: false, followerCount: 0, followingCount: 0 })
    const { result } = renderHook(() => useFollow(2, true))

    await act(async () => { await result.current.toggle() })

    expect(result.current.following).toBe(false)
    expect(usersApi.unfollowUser).toHaveBeenCalledWith(2)
  })

  it('APIエラー時は元の状態にロールバックする', async () => {
    vi.mocked(usersApi.followUser).mockRejectedValue(new Error('error'))
    const { result } = renderHook(() => useFollow(2, false))

    await act(async () => { await result.current.toggle() })

    expect(result.current.following).toBe(false)
  })

  it('loading: リクエスト中はtrue、完了後はfalse', async () => {
    let resolve!: () => void
    vi.mocked(usersApi.followUser).mockReturnValue(
      new Promise<{ following: boolean; followerCount: number; followingCount: number }>((res) => { resolve = () => res({ following: true, followerCount: 1, followingCount: 0 }) })
    )
    const { result } = renderHook(() => useFollow(2, false))

    act(() => { result.current.toggle() })
    expect(result.current.loading).toBe(true)

    await act(async () => { resolve() })
    expect(result.current.loading).toBe(false)
  })
})
