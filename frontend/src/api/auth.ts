import client from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth';

export const register = (req: RegisterRequest) =>
  client.post<AuthResponse>('/api/auth/register', req).then((r) => r.data);

export const login = (req: LoginRequest) =>
  client.post<AuthResponse>('/api/auth/login', req).then((r) => r.data);
