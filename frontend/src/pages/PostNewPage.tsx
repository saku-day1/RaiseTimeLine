import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost, uploadPostImage } from '../api/posts';

const MAX_LENGTH = 140;
const ACCEPTED_TYPES = 'image/jpeg,image/png,image/gif';

export default function PostNewPage() {
  const [content, setContent] = useState('');
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [imageError, setImageError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      setImageError('ファイルサイズは 5MB 以下にしてください');
      return;
    }

    setImageError('');
    setSelectedFile(file);

    const reader = new FileReader();
    reader.onload = (ev) => setPreviewUrl(ev.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleRemoveImage = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
    setImageError('');
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSubmitting(true);
    try {
      let imageUrl: string | undefined;
      if (selectedFile) {
        const result = await uploadPostImage(selectedFile);
        imageUrl = result.imageUrl;
      }
      await createPost(content, imageUrl);
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

        {previewUrl && (
          <div style={{ position: 'relative', marginTop: '8px', display: 'inline-block' }}>
            <img
              src={previewUrl}
              alt="プレビュー"
              style={{ maxWidth: '100%', maxHeight: '300px', borderRadius: '8px', display: 'block' }}
            />
            <button
              type="button"
              onClick={handleRemoveImage}
              style={{ position: 'absolute', top: '6px', right: '6px', background: 'rgba(0,0,0,0.6)', color: 'white', border: 'none', borderRadius: '50%', width: '24px', height: '24px', cursor: 'pointer', fontSize: '14px', lineHeight: 1 }}
            >
              ×
            </button>
          </div>
        )}

        {imageError && (
          <p style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{imageError}</p>
        )}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#1d4ed8', fontSize: '20px', padding: '4px' }}
              title="画像を追加"
            >
              🖼
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept={ACCEPTED_TYPES}
              onChange={handleFileChange}
              style={{ display: 'none' }}
            />
            <span style={{ color: content.length >= MAX_LENGTH ? '#ef4444' : '#9ca3af', fontSize: '14px' }}>
              {content.length} / {MAX_LENGTH}
            </span>
          </div>
          <button
            type="submit"
            disabled={!content.trim() || submitting}
            style={{ padding: '8px 24px', backgroundColor: content.trim() ? '#1d4ed8' : '#9ca3af', color: 'white', border: 'none', borderRadius: '4px', cursor: content.trim() ? 'pointer' : 'not-allowed' }}
          >
            {submitting ? '投稿中...' : '投稿する'}
          </button>
        </div>
      </form>
    </div>
  );
}
