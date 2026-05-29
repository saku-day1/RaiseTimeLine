import client from './client';
import type { Comment } from '../types/comment';

export const getComments = (postId: number) =>
  client.get<Comment[]>(`/api/posts/${postId}/comments`).then((r) => r.data);

export const createComment = (postId: number, content: string) =>
  client.post<Comment>(`/api/posts/${postId}/comments`, { content }).then((r) => r.data);

export const deleteComment = (commentId: number) =>
  client.delete(`/api/comments/${commentId}`);
