import { useState } from 'react';
import { followUser, unfollowUser } from '../api/users';

export function useFollow(userId: number, initialFollowing: boolean) {
  const [following, setFollowing] = useState(initialFollowing);
  const [loading, setLoading] = useState(false);

  const toggle = async () => {
    setLoading(true);
    const prevFollowing = following;
    setFollowing(!prevFollowing);

    try {
      if (prevFollowing) {
        await unfollowUser(userId);
      } else {
        await followUser(userId);
      }
    } catch {
      setFollowing(prevFollowing);
    } finally {
      setLoading(false);
    }
  };

  return { following, loading, toggle };
}
