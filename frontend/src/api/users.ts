import client from './client';
import type { UserSummary, UserProfile, FollowStatus } from '../types/user';

export const updateProfile = (data: { displayName: string; bio: string }) =>
  client.put<UserProfile>('/api/users/me', data).then((r) => r.data);

export const uploadProfileImage = (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return client.post<{ imageUrl: string }>('/api/users/me/image', form).then((r) => r.data);
};

export const uploadHeaderImage = (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return client.post<{ imageUrl: string }>('/api/users/me/header-image', form).then((r) => r.data);
};

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
