import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const res = await registerApi({ username, email, password });
      login(res.token, { userId: res.userId, username: res.username });
      navigate('/');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setError(msg ?? '登録に失敗しました');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '80px auto', padding: '32px', border: '1px solid #e5e7eb', borderRadius: '8px' }}>
      <h1 style={{ marginBottom: '24px' }}>新規登録</h1>
      {error && <p style={{ color: '#ef4444', marginBottom: '16px' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <input
          type="text"
          placeholder="ユーザー名（1〜50文字）"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          maxLength={50}
          required
          style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '4px' }}
        />
        <input
          type="email"
          placeholder="メールアドレス"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '4px' }}
        />
        <input
          type="password"
          placeholder="パスワード（8文字以上）"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
          required
          style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '4px' }}
        />
        <button type="submit" style={{ padding: '10px', backgroundColor: '#1d4ed8', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          登録する
        </button>
      </form>
      <p style={{ marginTop: '16px', textAlign: 'center' }}>
        すでにアカウントをお持ちの方は <Link to="/login">ログイン</Link>
      </p>
    </div>
  );
}
