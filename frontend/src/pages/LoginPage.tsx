import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const res = await loginApi({ email, password });
      login(res.token, { userId: res.userId, username: res.username });
      navigate('/');
    } catch {
      setError('メールアドレスまたはパスワードが正しくありません');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '80px auto', padding: '32px', border: '1px solid #e5e7eb', borderRadius: '8px' }}>
      <h1 style={{ marginBottom: '24px' }}>ログイン</h1>
      {error && <p style={{ color: '#ef4444', marginBottom: '16px' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
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
          placeholder="パスワード"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '4px' }}
        />
        <button type="submit" style={{ padding: '10px', backgroundColor: '#1d4ed8', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          ログイン
        </button>
      </form>
      <p style={{ marginTop: '16px', textAlign: 'center' }}>
        アカウントをお持ちでない方は <Link to="/register">新規登録</Link>
      </p>
    </div>
  );
}
