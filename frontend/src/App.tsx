import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Header from './components/layout/Header';
import PrivateRoute from './components/layout/PrivateRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import TimelinePage from './pages/TimelinePage';
import PostDetailPage from './pages/PostDetailPage';
import PostNewPage from './pages/PostNewPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Header />
        <main style={{ maxWidth: '600px', margin: '0 auto', padding: '16px' }}>
          <Routes>
            <Route path="/" element={<TimelinePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/posts/:id" element={<PostDetailPage />} />
            <Route
              path="/posts/new"
              element={
                <PrivateRoute>
                  <PostNewPage />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
