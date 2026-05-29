import client from './client';
import type { LikeStatus } from '../types/like';

export const likePost = (postId: number) =>
  client.post<LikeStatus>(`/api/posts/${postId}/likes`).then((r) => r.data);

export const unlikePost = (postId: number) =>
  client.delete<LikeStatus>(`/api/posts/${postId}/likes`).then((r) => r.data);

export const getLikeStatus = (postId: number) =>
  client.get<LikeStatus>(`/api/posts/${postId}/likes/me`).then((r) => r.data);
