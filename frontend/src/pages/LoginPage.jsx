import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { FiMail, FiLock, FiEye, FiEyeOff } from 'react-icons/fi'

const LoginPage = () => {
  const [searchParams] = useSearchParams()
  const invitedEmail = searchParams.get('email')
  const inviteToken = searchParams.get('inviteToken')
  const redirect = searchParams.get('redirect')
  const invitedFlag = searchParams.get('invited') === 'true'
  const isInvited = invitedFlag || !!inviteToken
  
  const [formData, setFormData] = useState({
    email: invitedEmail || '',
    password: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  
  const { login } = useAuth()

  const registerLink = (() => {
    const params = new URLSearchParams()
    if (invitedEmail) params.set('email', invitedEmail)
    if (inviteToken) params.set('inviteToken', inviteToken)
    if (redirect) params.set('redirect', redirect)
    if (invitedFlag) params.set('invited', 'true')
    const qs = params.toString()
    return qs ? `/register?${qs}` : '/register'
  })()

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid'
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!validateForm()) return
    
    setLoading(true)
    try {
      await login(formData.email, formData.password)
    } catch (error) {
      // Error is handled by the auth context and axios interceptor
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Brand Section */}
      <div className="hidden lg:flex lg:w-1/2 brand-gradient relative overflow-hidden">
        <div className="absolute inset-0 bg-black bg-opacity-10"></div>
        <div className="relative z-10 flex flex-col justify-center items-center text-white p-12">
          <div className="text-center">
            <h1 className="text-5xl font-bold mb-4 text-shadow">Kanban</h1>
            <p className="text-xl mb-8 text-shadow">Collaborate. Organize. Achieve.</p>
            
            {/* Floating Cards Animation */}
            <div className="relative w-64 h-48 mx-auto">
              <div className="absolute top-0 left-0 w-16 h-20 bg-white bg-opacity-20 rounded-lg transform rotate-12 animate-pulse"></div>
              <div className="absolute top-4 right-0 w-16 h-20 bg-white bg-opacity-30 rounded-lg transform -rotate-6 animate-pulse delay-300"></div>
              <div className="absolute bottom-0 left-8 w-16 h-20 bg-white bg-opacity-25 rounded-lg transform rotate-3 animate-pulse delay-700"></div>
              <div className="absolute bottom-4 right-8 w-16 h-20 bg-white bg-opacity-35 rounded-lg transform -rotate-12 animate-pulse delay-1000"></div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Form Section */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gray-50">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-2xl p-8">
            {/* Invitation Banner */}
            {isInvited && (
              <div style={{background: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', color: '#1E40AF', fontSize: '14px'}}>
                You've been invited to a board! Sign in to access it.
              </div>
            )}

            <div className="text-center mb-8">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">Sign in to Kanban</h2>
              <p className="text-sm text-gray-500">Enter your credentials to access your boards</p>
            </div>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                  Email address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiMail className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    value={formData.email}
                    onChange={handleChange}
                    className={`
                      appearance-none relative block w-full pl-10 pr-3 py-3 h-12
                      border ${errors.email ? 'border-red-300 ring-red-100' : 'border-gray-300'} 
                      placeholder-gray-500 text-gray-900 rounded-lg 
                      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
                      transition-all duration-200
                    `}
                    placeholder="Enter your email"
                  />
                </div>
                {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiLock className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    value={formData.password}
                    onChange={handleChange}
                    className={`
                      appearance-none relative block w-full pl-10 pr-10 py-3 h-12
                      border ${errors.password ? 'border-red-300 ring-red-100' : 'border-gray-300'} 
                      placeholder-gray-500 text-gray-900 rounded-lg 
                      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
                      transition-all duration-200
                    `}
                    placeholder="Enter your password"
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? (
                      <FiEyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    ) : (
                      <FiEye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    )}
                  </button>
                </div>
                {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
              </div>

              <button
                type="submit"
                disabled={loading}
                className="
                  group relative w-full flex justify-center py-3 px-4 h-12
                  border border-transparent text-sm font-medium rounded-lg text-white 
                  brand-gradient hover:shadow-lg
                  focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 
                  disabled:opacity-60 disabled:cursor-not-allowed
                  transition-all duration-200 transform hover:scale-105
                "
              >
                {loading ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Signing in...
                  </div>
                ) : (
                  'Sign in'
                )}
              </button>

              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-300" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white text-gray-500">OR</span>
                </div>
              </div>

              <div className="text-center">
                <p className="text-sm text-gray-600">
                  Don't have an account?{' '}
                  <Link 
                    to={registerLink}
                    className="font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200"
                  >
                    Sign up
                  </Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
