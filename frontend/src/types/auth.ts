export interface RegisterRequest {
  username: string;
  displayName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  displayName: string;
}

export interface AuthUser {
  userId: number;
  username: string;
}
