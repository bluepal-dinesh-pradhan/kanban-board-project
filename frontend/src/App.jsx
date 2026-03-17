import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import LandingPage from './pages/LandingPage'
import BoardListPage from './pages/BoardListPage'
import BoardPage from './pages/BoardPage'
import InvitePage from './pages/InvitePage'

// Private Route component
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    )
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />
}

// Public Route component (redirect if authenticated)
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  const location = useLocation()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    )
  }
  
  if (!isAuthenticated) {
    return children
  }

  const params = new URLSearchParams(location.search)
  let redirect = params.get('redirect')
  if (redirect) {
    try {
      redirect = decodeURIComponent(redirect)
    } catch {
      redirect = null
    }
  }

  if (redirect && redirect.startsWith('/')) {
    return <Navigate to={redirect} />
  }

  return <Navigate to="/boards" />
}

function App() {
  return (
    <Routes>
      <Route 
        path="/login" 
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } 
      />
      <Route 
        path="/register" 
        element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        } 
      />
      <Route 
        path="/" 
        element={
          <PublicRoute>
            <LandingPage />
          </PublicRoute>
        } 
      />
      <Route path="/invite" element={<InvitePage />} />
      <Route 
        path="/boards" 
        element={
          <PrivateRoute>
            <BoardListPage />
          </PrivateRoute>
        } 
      />
      <Route 
        path="/boards/:boardId" 
        element={
          <PrivateRoute>
            <BoardPage />
          </PrivateRoute>
        } 
      />
    </Routes>
  )
}

export default App
