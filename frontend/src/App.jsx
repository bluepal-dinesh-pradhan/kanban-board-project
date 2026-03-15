import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import BoardListPage from './pages/BoardListPage'
import BoardPage from './pages/BoardPage'
import Navbar from './components/Navbar'

// Private Route component
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />
}

// Public Route component (redirect if authenticated)
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }
  
  return !isAuthenticated ? children : <Navigate to="/boards" />
}

function App() {
  const { isAuthenticated } = useAuth()

  return (
    <div className="min-h-screen bg-gray-50">
      {isAuthenticated && <Navbar />}
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
          path="/boards" 
          element={
            <PrivateRoute>
              <BoardListPage />
            </PrivateRoute>
          } 
        />
        <Route 
          path="/board/:boardId" 
          element={
            <PrivateRoute>
              <BoardPage />
            </PrivateRoute>
          } 
        />
        <Route 
          path="/" 
          element={<Navigate to={isAuthenticated ? "/boards" : "/login"} />} 
        />
      </Routes>
    </div>
  )
}

export default App