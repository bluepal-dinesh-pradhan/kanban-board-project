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
      
      toast.success('Login successful!')
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
      
      toast.success('Registration successful!')
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
    toast.success('Logged out successfully')
  }

  const value = {
    user,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!user,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}