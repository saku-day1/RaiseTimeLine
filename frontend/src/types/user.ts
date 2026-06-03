export type UserSummary = {
  id: number;
  username: string;
  displayName: string;
  profileImageUrl: string | null;
  following: boolean;
};

export type UserProfile = UserSummary & {
  headerImageUrl: string | null;
  bio: string | null;
  followerCount: number;
  followingCount: number;
  followedBack: boolean;
};

export type FollowStatus = {
  following: boolean;
  followerCount: number;
  followingCount: number;
};
