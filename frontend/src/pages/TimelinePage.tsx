import { useState, useEffect, useCallback } from 'react';
import { getPosts } from '../api/posts';
import type { PostSummary } from '../types/post';
import PostCard from '../components/post/PostCard';
import { useAuth } from '../contexts/AuthContext';

type Tab = 'home' | 'all';

interface TabState {
  posts: PostSummary[];
  page: number;
  hasMore: boolean;
  initialized: boolean;
}

const initialTabState = (): TabState => ({
  posts: [],
  page: 0,
  hasMore: false,
  initialized: false,
});

export default function TimelinePage() {
  const { isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>(isAuthenticated ? 'home' : 'all');
  const [loading, setLoading] = useState(false);
  const [tabStates, setTabStates] = useState<Record<Tab, TabState>>({
    home: initialTabState(),
    all: initialTabState(),
  });

  const fetchPosts = useCallback(async (tab: Tab, p: number) => {
    setLoading(true);
    try {
      const res = await getPosts(p, 20, tab);
      setTabStates((prev) => ({
        ...prev,
        [tab]: {
          posts: p === 0 ? res.content : [...prev[tab].posts, ...res.content],
          page: p,
          hasMore: !res.last,
          initialized: true,
        },
      }));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!tabStates[activeTab].initialized) {
      fetchPosts(activeTab, 0);
    }
  }, [activeTab, tabStates, fetchPosts]);

  const handleLikeChange = (postId: number, liked: boolean, count: number) => {
    setTabStates((prev) => ({
      ...prev,
      [activeTab]: {
        ...prev[activeTab],
        posts: prev[activeTab].posts.map((p) =>
          p.id === postId ? { ...p, liked, likeCount: count } : p
        ),
      },
    }));
  };

  const handleDelete = (postId: number) => {
    setTabStates((prev) => ({
      ...prev,
      home: { ...prev.home, posts: prev.home.posts.filter((p) => p.id !== postId) },
      all: { ...prev.all, posts: prev.all.posts.filter((p) => p.id !== postId) },
    }));
  };

  const current = tabStates[activeTab];

  const tabStyle = (tab: Tab): React.CSSProperties => ({
    flex: 1,
    padding: '12px',
    background: 'none',
    border: 'none',
    borderBottom: activeTab === tab ? '2px solid #1d9bf0' : '2px solid transparent',
    fontWeight: activeTab === tab ? 'bold' : 'normal',
    color: activeTab === tab ? '#1d9bf0' : 'inherit',
    cursor: 'pointer',
    fontSize: '15px',
  });

  return (
    <div>
      <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '8px' }}>
        {isAuthenticated && (
          <button style={tabStyle('home')} onClick={() => setActiveTab('home')}>
            ホーム
          </button>
        )}
        <button style={tabStyle('all')} onClick={() => setActiveTab('all')}>
          すべて
        </button>
      </div>

      {current.posts.map((post) => (
        <PostCard key={post.id} post={post} onLikeChange={handleLikeChange} onDelete={handleDelete} />
      ))}

      {loading && <p style={{ textAlign: 'center', color: '#9ca3af' }}>読み込み中...</p>}

      {!loading && current.initialized && current.posts.length === 0 && (
        <p style={{ textAlign: 'center', color: '#9ca3af' }}>
          {activeTab === 'home'
            ? 'フォロー中のユーザーの投稿はありません。誰かをフォローしてみましょう！'
            : '投稿がありません'}
        </p>
      )}

      {!loading && current.hasMore && (
        <button
          onClick={() => fetchPosts(activeTab, current.page + 1)}
          style={{ display: 'block', margin: '16px auto', padding: '8px 24px', cursor: 'pointer', border: '1px solid #d1d5db', borderRadius: '4px' }}
        >
          さらに読み込む
        </button>
      )}
    </div>
  );
}
