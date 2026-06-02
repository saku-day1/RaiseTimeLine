import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import type { Comment } from '../../types/comment';

interface Props {
  addComment: (content: string) => Promise<Comment>;
  onSuccess: () => void;
}

const MAX_LENGTH = 200;

export default function CommentForm({ addComment, onSuccess }: Props) {
  const { isAuthenticated } = useAuth();
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!isAuthenticated) {
    return (
      <div style={{ padding: '12px', backgroundColor: '#f9fafb', borderRadius: '4px', marginBottom: '16px', fontSize: '14px', color: '#6b7280' }}>
        コメントするには <Link to="/login">ログイン</Link> してください
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim() || submitting) return;
    setSubmitting(true);
    try {
      await addComment(content.trim());
      setContent('');
      onSuccess();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '16px' }}>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        maxLength={MAX_LENGTH}
        placeholder="コメントを入力（最大200文字）"
        rows={3}
        style={{ width: '100%', padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '4px', resize: 'vertical', fontSize: '14px', boxSizing: 'border-box' }}
      />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '4px' }}>
        <span style={{ color: content.length >= MAX_LENGTH ? '#ef4444' : '#9ca3af', fontSize: '12px' }}>
          {content.length} / {MAX_LENGTH}
        </span>
        <button
          type="submit"
          disabled={!content.trim() || submitting}
          style={{ padding: '6px 16px', backgroundColor: content.trim() ? '#1d4ed8' : '#9ca3af', color: 'white', border: 'none', borderRadius: '4px', cursor: content.trim() ? 'pointer' : 'not-allowed', fontSize: '14px' }}
        >
          コメントする
        </button>
      </div>
    </form>
  );
}
