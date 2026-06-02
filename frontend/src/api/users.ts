import client from './client';
import type { UserSummary, UserProfile, FollowStatus } from '../types/user';

export const searchUsers = (q: string) =>
  client.get<UserSummary[]>('/api/users/search', { params: { q } }).then((r) => r.data);

export const getUserProfile = (id: number) =>
  client.get<UserProfile>(`/api/users/${id}`).then((r) => r.data);

export const getFollowers = (id: number) =>
  client.get<UserSummary[]>(`/api/users/${id}/followers`).then((r) => r.data);

export const getFollowing = (id: number) =>
  client.get<UserSummary[]>(`/api/users/${id}/following`).then((r) => r.data);

export const followUser = (id: number) =>
  client.post<FollowStatus>(`/api/users/${id}/follow`).then((r) => r.data);

export const unfollowUser = (id: number) =>
  client.delete<FollowStatus>(`/api/users/${id}/follow`).then((r) => r.data);
