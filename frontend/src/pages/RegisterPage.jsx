import { useState, useEffect } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authAPI } from '../api/endpoints'
import { FiMail, FiLock, FiUser, FiEye, FiEyeOff, FiCheck } from 'react-icons/fi'

const RegisterPage = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const invitedEmail = searchParams.get('email')
  const inviteToken = searchParams.get('inviteToken')
  const redirect = searchParams.get('redirect')
  const invitedFlag = searchParams.get('invited') === 'true'
  const isInvited = invitedFlag || !!inviteToken
  
  const [formData, setFormData] = useState({
    fullName: '',
    email: invitedEmail || '',
    password: '',
    confirmPassword: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [checkingUser, setCheckingUser] = useState(false)
  const [errors, setErrors] = useState({})
  
  const { register } = useAuth()

  // Check if user exists when page loads with invitation parameters
  useEffect(() => {
    const checkUserExists = async () => {
      if (isInvited && invitedEmail) {
        setCheckingUser(true)
        try {
          const response = await authAPI.checkUserExists(invitedEmail)
          const userExists = response.data.data
          
          if (userExists) {
            // User already exists, redirect to login with invitation parameters
            const params = new URLSearchParams()
            params.set('email', invitedEmail)
            if (inviteToken) params.set('inviteToken', inviteToken)
            if (redirect) params.set('redirect', redirect)
            if (invitedFlag) params.set('invited', 'true')
            navigate(`/login?${params.toString()}`, { replace: true })
            return
          }
        } catch (error) {
          console.error('Error checking user existence:', error)
          // Continue with registration if check fails
        } finally {
          setCheckingUser(false)
        }
      }
    }

    checkUserExists()
  }, [isInvited, invitedEmail, inviteToken, redirect, invitedFlag, navigate])

  const loginLink = (() => {
    const params = new URLSearchParams()
    if (invitedEmail) params.set('email', invitedEmail)
    if (inviteToken) params.set('inviteToken', inviteToken)
    if (redirect) params.set('redirect', redirect)
    if (invitedFlag) params.set('invited', 'true')
    const qs = params.toString()
    return qs ? `/login?${qs}` : '/login'
  })()

  const getPasswordStrength = (password) => {
    if (password.length < 6) return { strength: 'weak', color: 'bg-red-500', text: 'Weak' }
    if (password.length < 8) return { strength: 'medium', color: 'bg-yellow-500', text: 'Medium' }
    if (password.match(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)) {
      return { strength: 'strong', color: 'bg-green-500', text: 'Strong' }
    }
    return { strength: 'medium', color: 'bg-yellow-500', text: 'Medium' }
  }

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Full name is required'
    }
    
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid'
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }
    
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password'
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!validateForm()) return
    
    setLoading(true)
    try {
      await register(formData.email, formData.password, formData.fullName)
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

  const passwordStrength = getPasswordStrength(formData.password)

  // Show loading spinner while checking user existence
  if (checkingUser) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Checking invitation...</p>
        </div>
      </div>
    )
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
            {isInvited && invitedEmail && (
              <div style={{background: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', color: '#1E40AF', fontSize: '14px'}}>
                You've been invited to a board! Create your account to accept.
              </div>
            )}

            <div className="text-center mb-8">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">Create your account</h2>
              <p className="text-sm text-gray-500">Join thousands of teams organizing their work</p>
            </div>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label htmlFor="fullName" className="block text-sm font-medium text-gray-700 mb-2">
                  Full name
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiUser className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="fullName"
                    name="fullName"
                    type="text"
                    autoComplete="name"
                    value={formData.fullName}
                    onChange={handleChange}
                    className={`
                      appearance-none relative block w-full pl-10 pr-3 py-3 h-12
                      border ${errors.fullName ? 'border-red-300 ring-red-100' : 'border-gray-300'} 
                      placeholder-gray-500 text-gray-900 rounded-lg 
                      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
                      transition-all duration-200
                    `}
                    placeholder="Enter your full name"
                  />
                </div>
                {errors.fullName && <p className="mt-1 text-sm text-red-600">{errors.fullName}</p>}
              </div>

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
                    readOnly={!!invitedEmail}
                    className={`
                      appearance-none relative block w-full pl-10 pr-3 py-3 h-12
                      border ${errors.email ? 'border-red-300 ring-red-100' : 'border-gray-300'} 
                      placeholder-gray-500 text-gray-900 rounded-lg 
                      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
                      transition-all duration-200
                      ${invitedEmail ? 'bg-gray-50 cursor-not-allowed' : ''}
                    `}
                    style={invitedEmail ? {backgroundColor: '#F3F4F6', cursor: 'not-allowed'} : {}}
                    placeholder="Enter your email"
                  />
                  {invitedEmail && (
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                      <FiCheck className="h-5 w-5 text-green-500" />
                    </div>
                  )}
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
                    autoComplete="new-password"
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
                {formData.password && (
                  <div className="mt-2">
                    <div className="flex items-center space-x-2">
                      <div className="flex-1 bg-gray-200 rounded-full h-2">
                        <div 
                          className={`h-2 rounded-full transition-all duration-300 ${passwordStrength.color}`}
                          style={{ width: passwordStrength.strength === 'weak' ? '33%' : passwordStrength.strength === 'medium' ? '66%' : '100%' }}
                        ></div>
                      </div>
                      <span className={`text-xs font-medium ${passwordStrength.strength === 'weak' ? 'text-red-500' : passwordStrength.strength === 'medium' ? 'text-yellow-500' : 'text-green-500'}`}>
                        {passwordStrength.text}
                      </span>
                    </div>
                  </div>
                )}
                {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
              </div>

              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  Confirm password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiLock className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={`
                      appearance-none relative block w-full pl-10 pr-10 py-3 h-12
                      border ${errors.confirmPassword ? 'border-red-300 ring-red-100' : 'border-gray-300'} 
                      placeholder-gray-500 text-gray-900 rounded-lg 
                      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
                      transition-all duration-200
                    `}
                    placeholder="Confirm your password"
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    {showConfirmPassword ? (
                      <FiEyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    ) : (
                      <FiEye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    )}
                  </button>
                </div>
                {formData.confirmPassword && formData.password && (
                  <div className="mt-1 flex items-center">
                    {formData.password === formData.confirmPassword ? (
                      <div className="flex items-center text-green-600">
                        <FiCheck className="w-3 h-3 mr-1" />
                        <span className="text-xs">Passwords match</span>
                      </div>
                    ) : (
                      <span className="text-xs text-red-600">Passwords don't match</span>
                    )}
                  </div>
                )}
                {errors.confirmPassword && <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>}
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
                    Creating account...
                  </div>
                ) : (
                  'Create account'
                )}
              </button>

              <div className="text-center">
                <p className="text-sm text-gray-600">
                  Already have an account?{' '}
                  <Link 
                    to={loginLink}
                    className="font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200"
                  >
                    Sign in
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

export default RegisterPage
