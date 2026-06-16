import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import PrivateRoute from './PrivateRoute'
import { renderWithProviders } from '../../test/utils'

describe('PrivateRoute', () => {
  it('認証済み: childrenをレンダーする', () => {
    renderWithProviders(
      <PrivateRoute><span>protected content</span></PrivateRoute>,
      { authUser: { userId: 1, username: 'alice' } }
    )
    expect(screen.getByText('protected content')).toBeInTheDocument()
  })

  it('未認証: /loginへリダイレクトする', () => {
    renderWithProviders(
      <PrivateRoute><span>protected content</span></PrivateRoute>
    )
    expect(screen.queryByText('protected content')).not.toBeInTheDocument()
  })
})
