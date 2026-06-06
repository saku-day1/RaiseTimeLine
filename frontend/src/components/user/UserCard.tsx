import { Link } from 'react-router-dom';
import type { UserSummary } from '../../types/user';
import { useFollow } from '../../hooks/useFollow';

type Props = {
  user: UserSummary;
  onFollowChange?: (userId: number, following: boolean) => void;
};

export default function UserCard({ user, onFollowChange }: Props) {
  const { following, loading, toggle } = useFollow(user.id, user.following);

  const handleClick = async () => {
    await toggle();
    onFollowChange?.(user.id, !following);
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid #eee' }}>
      <Link to={`/users/${user.id}`} style={{ display: 'flex', alignItems: 'center', gap: '12px', textDecoration: 'none', color: 'inherit' }}>
        <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#ccc', overflow: 'hidden', flexShrink: 0 }}>
          {user.profileImageUrl && (
            <img src={user.profileImageUrl} alt={user.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          )}
        </div>
        <div>
          <div style={{ fontWeight: 'bold', fontSize: '14px' }}>{user.displayName}</div>
          <div style={{ color: '#555', fontSize: '13px' }}>@{user.username}</div>
        </div>
      </Link>
      <button
        onClick={handleClick}
        disabled={loading}
        style={{
          padding: '6px 16px',
          borderRadius: '20px',
          border: following ? '1px solid #ccc' : 'none',
          background: following ? '#fff' : '#1d9bf0',
          color: following ? '#333' : '#fff',
          cursor: 'pointer',
          fontSize: '13px',
          fontWeight: 'bold',
        }}
      >
        {following ? 'フォロー中' : 'フォロー'}
      </button>
    </div>
  );
}
