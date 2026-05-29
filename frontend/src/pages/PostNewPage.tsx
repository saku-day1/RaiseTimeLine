import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost } from '../api/posts';

const MAX_LENGTH = 140;

export default function PostNewPage() {
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSubmitting(true);
    try {
      await createPost(content);
      navigate('/');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: '16px' }}>新規投稿</h2>
      <form onSubmit={handleSubmit}>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={MAX_LENGTH}
          placeholder="いまどうしてる？（最大140文字）"
          rows={4}
          style={{ width: '100%', padding: '12px', border: '1px solid #d1d5db', borderRadius: '4px', resize: 'vertical', fontSize: '16px', boxSizing: 'border-box' }}
        />
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '8px' }}>
          <span style={{ color: content.length >= MAX_LENGTH ? '#ef4444' : '#9ca3af', fontSize: '14px' }}>
            {content.length} / {MAX_LENGTH}
          </span>
          <button
            type="submit"
            disabled={!content.trim() || submitting}
            style={{ padding: '8px 24px', backgroundColor: content.trim() ? '#1d4ed8' : '#9ca3af', color: 'white', border: 'none', borderRadius: '4px', cursor: content.trim() ? 'pointer' : 'not-allowed' }}
          >
            投稿する
          </button>
        </div>
      </form>
    </div>
  );
}
