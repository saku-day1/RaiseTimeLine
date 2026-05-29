import { useState, useEffect } from 'react';
import { getComments, createComment, deleteComment } from '../api/comments';
import type { Comment } from '../types/comment';

export function useComments(postId: number) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getComments(postId)
      .then(setComments)
      .finally(() => setLoading(false));
  }, [postId]);

  const addComment = async (content: string): Promise<Comment> => {
    const newComment = await createComment(postId, content);
    // 楽観的更新: リストの先頭に追加
    setComments((prev) => [newComment, ...prev]);
    return newComment;
  };

  const removeComment = async (commentId: number) => {
    // 楽観的更新: 先にリストから除去
    setComments((prev) => prev.filter((c) => c.id !== commentId));
    try {
      await deleteComment(commentId);
    } catch {
      // エラー時ロールバック: 再取得
      getComments(postId).then(setComments);
    }
  };

  return { comments, loading, addComment, removeComment };
}
