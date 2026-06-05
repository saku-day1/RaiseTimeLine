import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getPost } from '../api/posts';
import type { PostSummary } from '../types/post';
import LikeButton from '../components/like/LikeButton';
import CommentList from '../components/comment/CommentList';
import CommentForm from '../components/comment/CommentForm';
import { useComments } from '../hooks/useComments';

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [post, setPost] = useState<PostSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const { comments, loading: commentsLoading, addComment, removeComment } = useComments(Number(id) || 0);

  useEffect(() => {
    if (!id) return;
    getPost(Number(id))
      .then(setPost)
      .finally(() => setLoading(false));
  }, [id]);

  const handleLikeChange = (_postId: number, liked: boolean, count: number) => {
    setPost((prev) => prev ? { ...prev, liked, likeCount: count } : prev);
  };

  const handleCommentAdded = () => {
    setPost((prev) => prev ? { ...prev, commentCount: prev.commentCount + 1 } : prev);
  };

  const handleCommentDeleted = () => {
    setPost((prev) => prev ? { ...prev, commentCount: Math.max(0, prev.commentCount - 1) } : prev);
  };

  if (loading) return <p>読み込み中...</p>;
  if (!post) return <p>投稿が見つかりません</p>;

  const date = new Date(post.createdAt).toLocaleString('ja-JP');

  return (
    <div>
      <button
        onClick={() => navigate(-1)}
        style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '14px', color: '#6b7280', background: 'none', border: 'none', cursor: 'pointer', padding: '12px 0', marginBottom: '4px' }}
      >
        ← タイムラインに戻る
      </button>
      <article style={{ padding: '16px', borderBottom: '1px solid #e5e7eb', marginBottom: '16px' }}>
        <div style={{ display: 'flex', gap: '12px' }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '50%', backgroundColor: '#d1d5db', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '20px' }}>
            {post.username[0].toUpperCase()}
          </div>
          <div style={{ flex: 1 }}>
            <Link to={`/users/${post.userId}`} style={{ fontWeight: 'bold', marginBottom: '4px', textDecoration: 'none', color: 'inherit' }}>{post.username}</Link>
            <div style={{ color: '#9ca3af', fontSize: '13px', marginBottom: '12px' }}>{date}</div>
            {post.content && (
              <p style={{ margin: '0 0 12px', lineHeight: '1.6', fontSize: '18px', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{post.content}</p>
            )}
            {post.imageUrl && (
              <img
                src={post.imageUrl}
                alt="投稿画像"
                style={{ maxWidth: '100%', borderRadius: '12px', display: 'block', marginBottom: '16px' }}
              />
            )}
            <div style={{ display: 'flex', gap: '24px', color: '#6b7280', fontSize: '15px', paddingTop: '12px', borderTop: '1px solid #f3f4f6' }}>
              <LikeButton
                postId={post.id}
                initialLiked={post.liked}
                initialCount={post.likeCount}
                onLikeChange={handleLikeChange}
              />
              <span>💬 {post.commentCount}</span>
            </div>
          </div>
        </div>
      </article>

      <CommentForm addComment={addComment} onSuccess={handleCommentAdded} />
      <CommentList comments={comments} loading={commentsLoading} removeComment={removeComment} onDeleted={handleCommentDeleted} />
    </div>
  );
}
