import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getUserProfile, getFollowers, getFollowing } from '../api/users';
import type { UserProfile, UserSummary } from '../types/user';
import { useAuth } from '../contexts/AuthContext';
import { useFollow } from '../hooks/useFollow';
import UserCard from '../components/user/UserCard';

type Tab = 'followers' | 'following';

export default function UserProfilePage() {
  const { id } = useParams<{ id: string }>();
  const { user: me } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [list, setList] = useState<UserSummary[]>([]);
  const [activeTab, setActiveTab] = useState<Tab>('followers');
  const [loading, setLoading] = useState(true);
  const [listLoading, setListLoading] = useState(false);

  const userId = Number(id);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    getUserProfile(userId)
      .then(setProfile)
      .finally(() => setLoading(false));
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    setListLoading(true);
    const fetch = activeTab === 'followers' ? getFollowers : getFollowing;
    fetch(userId)
      .then(setList)
      .finally(() => setListLoading(false));
  }, [userId, activeTab]);

  const handleFollowChange = (targetId: number, following: boolean) => {
    setList((prev) => prev.map((u) => (u.id === targetId ? { ...u, following } : u)));
    if (profile && targetId === userId) {
      setProfile((prev) =>
        prev ? { ...prev, followerCount: prev.followerCount + (following ? 1 : -1) } : prev
      );
    }
  };

  if (loading) return <p>読み込み中...</p>;
  if (!profile) return <p>ユーザーが見つかりません</p>;

  const isMe = me?.id === profile.id;

  return (
    <div>
      <ProfileHeader profile={profile} isMe={isMe} onFollowChange={(following) => {
        setProfile((prev) =>
          prev ? { ...prev, following, followerCount: prev.followerCount + (following ? 1 : -1) } : prev
        );
      }} />

      <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '16px' }}>
        {(['followers', 'following'] as Tab[]).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              flex: 1,
              padding: '12px',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === tab ? '2px solid #1d9bf0' : '2px solid transparent',
              fontWeight: activeTab === tab ? 'bold' : 'normal',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            {tab === 'followers' ? `フォロワー ${profile.followerCount}` : `フォロー中 ${profile.followingCount}`}
          </button>
        ))}
      </div>

      {listLoading ? (
        <p style={{ color: '#9ca3af', fontSize: '14px' }}>読み込み中...</p>
      ) : list.length === 0 ? (
        <p style={{ color: '#9ca3af', fontSize: '14px' }}>
          {activeTab === 'followers' ? 'フォロワーはまだいません' : 'フォロー中のユーザーはいません'}
        </p>
      ) : (
        list.map((u) => (
          <UserCard key={u.id} user={u} onFollowChange={handleFollowChange} />
        ))
      )}
    </div>
  );
}

function ProfileHeader({ profile, isMe, onFollowChange }: {
  profile: UserProfile;
  isMe: boolean;
  onFollowChange: (following: boolean) => void;
}) {
  const { following, loading, toggle } = useFollow(profile.id, profile.following);

  const handleClick = async () => {
    await toggle();
    onFollowChange(!following);
  };

  return (
    <div style={{ padding: '16px 0 24px', borderBottom: '1px solid #e5e7eb', marginBottom: '16px' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: '#d1d5db', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '24px', overflow: 'hidden' }}>
            {profile.profileImageUrl
              ? <img src={profile.profileImageUrl} alt={profile.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              : profile.username[0].toUpperCase()}
          </div>
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '18px' }}>{profile.displayName}</div>
            <div style={{ color: '#6b7280', fontSize: '14px' }}>@{profile.username}</div>
            {profile.bio && <div style={{ marginTop: '6px', fontSize: '14px' }}>{profile.bio}</div>}
          </div>
        </div>
        {!isMe && (
          <button
            onClick={handleClick}
            disabled={loading}
            style={{
              padding: '8px 20px',
              borderRadius: '20px',
              border: following ? '1px solid #ccc' : 'none',
              background: following ? '#fff' : '#1d9bf0',
              color: following ? '#333' : '#fff',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '14px',
              flexShrink: 0,
            }}
          >
            {following ? 'フォロー中' : 'フォロー'}
          </button>
        )}
      </div>
    </div>
  );
}
