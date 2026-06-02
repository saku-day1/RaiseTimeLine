export type UserSummary = {
  id: number;
  username: string;
  displayName: string;
  profileImageUrl: string | null;
  following: boolean;
};

export type UserProfile = UserSummary & {
  bio: string | null;
  followerCount: number;
  followingCount: number;
};

export type FollowStatus = {
  following: boolean;
  followerCount: number;
  followingCount: number;
};
