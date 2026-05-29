import { useState } from 'react';
import { likePost, unlikePost } from '../api/likes';

export function useLike(postId: number, initialLiked: boolean, initialCount: number) {
  const [liked, setLiked] = useState(initialLiked);
  const [count, setCount] = useState(initialCount);

  const toggle = async () => {
    const prevLiked = liked;
    const prevCount = count;

    // 楽観的更新
    setLiked(!prevLiked);
    setCount(prevLiked ? prevCount - 1 : prevCount + 1);

    try {
      const res = prevLiked ? await unlikePost(postId) : await likePost(postId);
      setLiked(res.liked);
      setCount(res.count);
      return res;
    } catch {
      // エラー時ロールバック
      setLiked(prevLiked);
      setCount(prevCount);
      return null;
    }
  };

  return { liked, count, toggle };
}
