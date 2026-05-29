import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export default function Header() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header style={{ padding: '12px 24px', borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <Link to="/" style={{ fontWeight: 'bold', fontSize: '18px', textDecoration: 'none', color: '#1d4ed8' }}>
        RaiseTimeLine
      </Link>
      <nav style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
        {isAuthenticated ? (
          <>
            <span style={{ color: '#6b7280' }}>{user?.username}</span>
            <Link to="/posts/new">投稿する</Link>
            <button onClick={handleLogout} style={{ cursor: 'pointer', background: 'none', border: '1px solid #e5e7eb', borderRadius: '4px', padding: '4px 12px' }}>
              ログアウト
            </button>
          </>
        ) : (
          <>
            <Link to="/login">ログイン</Link>
            <Link to="/register">新規登録</Link>
          </>
        )}
      </nav>
    </header>
  );
}
