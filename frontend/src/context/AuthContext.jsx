import { createContext, useContext, useState, useEffect } from 'react'
import { authAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    const userData = localStorage.getItem('user')
    
    if (token && userData) {
      try {
        setUser(JSON.parse(userData))
      } catch (error) {
        console.error('Error parsing user data:', error)
        logout()
      }
    } else {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
    setLoading(false)
  }, [])

  const login = async (email, password) => {
    try {
      const response = await authAPI.login({ email, password })
      const { accessToken, refreshToken, user: userData } = response.data.data
      
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(userData))
      setUser(userData)
      
      // Check if this was an invitation login
      const urlParams = new URLSearchParams(window.location.search)
      const isInvited = urlParams.get('invited') === 'true' || !!urlParams.get('inviteToken')
      
      if (isInvited) {
        toast.success('Welcome! You now have access to the board.', { id: 'auth-invite-login' })
      } else {
        toast.success('Login successful!', { id: 'auth-login' })
      }
      
      return response.data
    } catch (error) {
      throw error
    }
  }

  const register = async (email, password, fullName) => {
    try {
      const response = await authAPI.register({ email, password, fullName })
      const { accessToken, refreshToken, user: userData } = response.data.data
      
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(userData))
      setUser(userData)
      
      // Check if this was an invitation registration
      const urlParams = new URLSearchParams(window.location.search)
      const isInvited = urlParams.get('invited') === 'true' || !!urlParams.get('inviteToken')
      
      if (isInvited) {
        toast.success('Account created! You now have access to the board.', { id: 'auth-invite-register' })
      } else {
        toast.success('Registration successful!', { id: 'auth-register' })
      }
      
      return response.data
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    setUser(null)
    toast.success('Logged out successfully', { id: 'auth-logout' })
  }

  const value = {
    user,
    setUser,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!user && !!localStorage.getItem('accessToken'),
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
