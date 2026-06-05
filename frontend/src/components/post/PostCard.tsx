import { Link } from 'react-router-dom';
import type { PostSummary } from '../../types/post';
import LikeButton from '../like/LikeButton';
import { useAuth } from '../../contexts/AuthContext';
import { deletePost } from '../../api/posts';

interface Props {
  post: PostSummary;
  onLikeChange?: (postId: number, liked: boolean, count: number) => void;
  onDelete?: (postId: number) => void;
}

export default function PostCard({ post, onLikeChange, onDelete }: Props) {
  const { user } = useAuth();
  const isOwner = user?.userId === post.userId;

  const date = new Date(post.createdAt).toLocaleString('ja-JP', {
    month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit',
  });

  const handleDelete = async () => {
    if (!confirm('この投稿を削除しますか？')) return;
    await deletePost(post.id);
    onDelete?.(post.id);
  };

  return (
    <article style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
      <div style={{ display: 'flex', gap: '12px' }}>
        <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#d1d5db', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
          {post.username[0].toUpperCase()}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '4px', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <Link to={`/users/${post.userId}`} style={{ fontWeight: 'bold', textDecoration: 'none', color: 'inherit' }}>{post.username}</Link>
              <span style={{ color: '#9ca3af', fontSize: '13px' }}>{date}</span>
            </div>
            {isOwner && (
              <button
                onClick={handleDelete}
                style={{ background: 'none', border: 'none', color: '#9ca3af', cursor: 'pointer', fontSize: '13px', padding: '2px 6px', borderRadius: '4px' }}
                title="削除"
              >
                🗑
              </button>
            )}
          </div>
          <Link to={`/posts/${post.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
            {post.content && (
              <p style={{ margin: '0 0 8px', lineHeight: '1.5', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{post.content}</p>
            )}
            {post.imageUrl && (
              <img
                src={post.imageUrl}
                alt="投稿画像"
                style={{ maxWidth: '100%', maxHeight: '400px', borderRadius: '12px', display: 'block', marginBottom: '12px', objectFit: 'cover' }}
              />
            )}
          </Link>
          <div style={{ display: 'flex', gap: '24px', color: '#6b7280', fontSize: '14px' }}>
            <LikeButton
              postId={post.id}
              initialLiked={post.liked}
              initialCount={post.likeCount}
              onLikeChange={onLikeChange}
            />
            <Link to={`/posts/${post.id}`} style={{ color: '#6b7280', textDecoration: 'none' }}>
              💬 {post.commentCount}
            </Link>
          </div>
        </div>
      </div>
    </article>
  );
}
