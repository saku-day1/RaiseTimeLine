import { useState, useEffect, useCallback } from 'react';
import { getPosts } from '../api/posts';
import type { PostSummary } from '../types/post';
import PostCard from '../components/post/PostCard';

export default function TimelinePage() {
  const [posts, setPosts] = useState<PostSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const fetchPosts = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const res = await getPosts(p);
      setPosts((prev) => p === 0 ? res.content : [...prev, ...res.content]);
      setHasMore(!res.last);
      setPage(p);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchPosts(0); }, [fetchPosts]);

  const handleLikeChange = (postId: number, liked: boolean, count: number) => {
    setPosts((prev) => prev.map((p) => p.id === postId ? { ...p, liked, likeCount: count } : p));
  };

  return (
    <div>
      <h2 style={{ marginBottom: '16px' }}>タイムライン</h2>
      {posts.map((post) => (
        <PostCard key={post.id} post={post} onLikeChange={handleLikeChange} />
      ))}
      {loading && <p style={{ textAlign: 'center', color: '#9ca3af' }}>読み込み中...</p>}
      {!loading && posts.length === 0 && (
        <p style={{ textAlign: 'center', color: '#9ca3af' }}>投稿がありません</p>
      )}
      {!loading && hasMore && (
        <button
          onClick={() => fetchPosts(page + 1)}
          style={{ display: 'block', margin: '16px auto', padding: '8px 24px', cursor: 'pointer', border: '1px solid #d1d5db', borderRadius: '4px' }}
        >
          さらに読み込む
        </button>
      )}
    </div>
  );
}
