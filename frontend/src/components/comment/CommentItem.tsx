import { useAuth } from '../../contexts/AuthContext';
import type { Comment } from '../../types/comment';

interface Props {
  comment: Comment;
  onDelete: (commentId: number) => void;
}

export default function CommentItem({ comment, onDelete }: Props) {
  const { user } = useAuth();
  const isOwner = user?.userId === comment.user.id;
  const date = new Date(comment.createdAt).toLocaleString('ja-JP', {
    month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit',
  });

  return (
    <div style={{ display: 'flex', gap: '12px', padding: '12px 0', borderBottom: '1px solid #f3f4f6' }}>
      <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#d1d5db', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '14px' }}>
        {comment.user.username[0].toUpperCase()}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <span style={{ fontWeight: 'bold', fontSize: '14px' }}>{comment.user.username}</span>
            <span style={{ color: '#9ca3af', fontSize: '12px', marginLeft: '8px' }}>{date}</span>
          </div>
          {isOwner && (
            <button
              onClick={() => onDelete(comment.id)}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#9ca3af', fontSize: '12px', padding: '2px 6px' }}
              title="削除"
            >
              🗑️
            </button>
          )}
        </div>
        <p style={{ margin: '4px 0 0', fontSize: '14px', lineHeight: '1.5', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
          {comment.content}
        </p>
      </div>
    </div>
  );
}
