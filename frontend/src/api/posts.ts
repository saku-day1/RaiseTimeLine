import client from './client';
import type { PageResponse, PostSummary } from '../types/post';

export const getPosts = (page = 0, size = 20) =>
  client.get<PageResponse<PostSummary>>('/api/posts', { params: { page, size } }).then((r) => r.data);

export const getPost = (id: number) =>
  client.get<PostSummary>(`/api/posts/${id}`).then((r) => r.data);

export const createPost = (content: string, imageUrl?: string) =>
  client.post<PostSummary>('/api/posts', { content, imageUrl }).then((r) => r.data);

export const deletePost = (id: number) =>
  client.delete(`/api/posts/${id}`);
