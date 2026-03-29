import { useState, useEffect, useMemo } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authAPI } from '../api/endpoints'
import { FiEye, FiEyeOff, FiCheck } from 'react-icons/fi'

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
    if (!password) return { strength: '', color: 'bg-slate-200', text: '' }
    if (password.length < 8) return { strength: 'weak', color: 'bg-red-500', text: 'Weak' }
    
    const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/
    if (strongRegex.test(password)) {
      return { strength: 'strong', color: 'bg-green-500', text: 'Strong' }
    }
    
    // Check if it meets most requirements (at least 3 of 4)
    const hasUpper = /[A-Z]/.test(password)
    const hasLower = /[a-z]/.test(password)
    const hasNumber = /\d/.test(password)
    const hasSpecial = /[@$!%*?&#]/.test(password)
    const count = [hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length
    
    if (count >= 3) {
      return { strength: 'good', color: 'bg-yellow-500', text: 'Good' }
    }
    
    return { strength: 'weak', color: 'bg-red-500', text: 'Weak' }
  }

  const validateForm = () => {
    const newErrors = {}
    
    // Email validation regex (same as used in previous validation)
    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/
    // Password validation regex
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/

    // Name validation
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Full name is required'
    } else if (formData.fullName.trim().length < 2) {
      newErrors.fullName = 'Name must be at least 2 characters'
    } else if (formData.fullName.trim().length > 50) {
      newErrors.fullName = 'Name cannot exceed 50 characters'
    } else if (!/^[a-zA-Z\s]+$/.test(formData.fullName)) {
      newErrors.fullName = 'Name can only contain letters and spaces'
    }
    
    // Email validation
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Please enter a valid email'
    }
    
    // Password validation
    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (!passwordRegex.test(formData.password)) {
      newErrors.password = 'Password must be at least 8 characters with uppercase, lowercase, number, and special character (@$!%*?&#)'
    }
    
    // Confirm password validation
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
  const passwordsMatch =
    formData.password.length > 0 &&
    formData.confirmPassword.length > 0 &&
    formData.password === formData.confirmPassword
  const passwordsMismatch =
    formData.password.length > 0 &&
    formData.confirmPassword.length > 0 &&
    formData.password !== formData.confirmPassword
  const canSubmit = useMemo(() => {
    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/
    return (
      formData.fullName.trim().length >= 2 &&
      formData.fullName.trim().length <= 50 &&
      /^[a-zA-Z\s]+$/.test(formData.fullName) &&
      emailRegex.test(formData.email) &&
      passwordRegex.test(formData.password) &&
      formData.password === formData.confirmPassword &&
      !loading
    )
  }, [
    formData.fullName,
    formData.email,
    formData.password,
    formData.confirmPassword,
    loading
  ])

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
    <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center px-6 py-12 relative overflow-hidden animate-fade-in">
      <div className="hidden md:block fixed bottom-0 left-0 pointer-events-none opacity-20 transition-opacity duration-1000">
        <svg width="340" height="260" viewBox="0 0 340 260" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="20" y="158" width="190" height="58" rx="14" fill="#CFE4FF" />
          <rect x="32" y="140" width="170" height="20" rx="10" fill="#B9D8FF" />
          <rect x="40" y="70" width="120" height="70" rx="14" fill="#E9F2FF" />
          <rect x="52" y="86" width="96" height="8" rx="4" fill="#7CB5FF" />
          <rect x="52" y="100" width="64" height="8" rx="4" fill="#A7D0FF" />
          <rect x="170" y="80" width="90" height="64" rx="14" fill="#E2E8FF" />
          <rect x="180" y="96" width="70" height="8" rx="4" fill="#9DB6FF" />
          <rect x="180" y="110" width="54" height="8" rx="4" fill="#B4C7FF" />
          <rect x="210" y="40" width="70" height="36" rx="12" fill="#E9D5FF" />
          <rect x="216" y="50" width="44" height="8" rx="4" fill="#A855F7" />
          <rect x="18" y="44" width="70" height="40" rx="12" fill="#E0F2FE" />
          <path d="M28 70H76" stroke="#38BDF8" strokeWidth="4" strokeLinecap="round" />
          <circle cx="40" cy="62" r="5" fill="#38BDF8" />
          <circle cx="86" cy="64" r="18" fill="#FBD38D" />
          <rect x="68" y="82" width="36" height="40" rx="12" fill="#7DA9FF" />
          <rect x="60" y="94" width="18" height="18" rx="6" fill="#5EEAD4" />
          <circle cx="148" cy="86" r="16" fill="#C4B5FD" />
          <rect x="134" y="102" width="30" height="34" rx="10" fill="#60A5FA" />
          <rect x="268" y="110" width="54" height="20" rx="10" fill="#DBEAFE" />
          <rect x="276" y="116" width="30" height="8" rx="4" fill="#60A5FA" />
          <path d="M12 226H328" stroke="#BBD7FF" strokeWidth="6" strokeLinecap="round" />
        </svg>
      </div>
      <div className="hidden md:block fixed bottom-0 right-0 pointer-events-none opacity-20 transition-opacity duration-1000">
        <svg width="340" height="260" viewBox="0 0 340 260" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="170" y="150" width="130" height="66" rx="14" fill="#FFE4E6" />
          <rect x="180" y="162" width="46" height="40" rx="10" fill="#FDBA74" />
          <rect x="232" y="154" width="50" height="50" rx="10" fill="#93C5FD" />
          <rect x="242" y="166" width="30" height="8" rx="4" fill="#60A5FA" />
          <rect x="242" y="180" width="26" height="8" rx="4" fill="#38BDF8" />
          <rect x="70" y="132" width="60" height="70" rx="16" fill="#FDE68A" />
          <circle cx="100" cy="104" r="18" fill="#FCA5A5" />
          <rect x="92" y="122" width="20" height="20" rx="6" fill="#FB7185" />
          <rect x="250" y="96" width="60" height="38" rx="12" fill="#E0F2FE" />
          <rect x="258" y="106" width="38" height="8" rx="4" fill="#60A5FA" />
          <rect x="258" y="120" width="26" height="8" rx="4" fill="#38BDF8" />
          <rect x="120" y="86" width="84" height="46" rx="14" fill="#E9F2FF" />
          <rect x="130" y="100" width="64" height="8" rx="4" fill="#A7C5FF" />
          <rect x="130" y="114" width="40" height="8" rx="4" fill="#C7D7FF" />
          <path d="M150 134C170 112 196 110 220 96C244 82 266 84 298 68" stroke="#FB7185" strokeWidth="6" strokeLinecap="round" />
          <circle cx="298" cy="68" r="10" fill="#FB7185" />
          <rect x="184" y="72" width="70" height="20" rx="10" fill="#FECACA" />
          <rect x="40" y="210" width="260" height="6" rx="3" fill="#FBCFE8" />
        </svg>
      </div>
      <div className="w-full max-w-5xl">
        <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 overflow-hidden">
          <div className="grid md:grid-cols-[1.05fr_0.95fr] divide-x divide-slate-100">
            <div className="p-10 md:p-12">
              <div className="flex items-center gap-4 mb-8">
                <div className="h-12 w-12 rounded-xl bg-[#0052CC] flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-blue-100">
                  K
                </div>
                <div>
                  <h2 className="text-2xl font-semibold text-slate-900">Create your account</h2>
                  <p className="text-sm text-slate-500 mt-1">Build your first board in minutes.</p>
                </div>
              </div>

              {isInvited && invitedEmail && (
                <div className="mb-4 rounded-lg border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-700">
                  You&apos;ve been invited to a board! Create your account to accept.
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label htmlFor="fullName" className="block text-sm font-medium text-slate-700 mb-1.5">
                    Full name
                  </label>
                  <input
                    id="fullName"
                    name="fullName"
                    type="text"
                    autoComplete="name"
                    value={formData.fullName}
                    onChange={handleChange}
                    className={`
                      appearance-none relative block w-full px-3 py-3 h-11
                      border ${errors.fullName ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                      placeholder-slate-400 text-slate-900 rounded-lg
                      focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500
                    transition-all duration-300
                  `}
                    placeholder="Enter your full name"
                  />
                  {errors.fullName && <p className="mt-1 text-sm text-red-600">{errors.fullName}</p>}
                </div>

                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1.5">
                    Email
                  </label>
                  <div className="relative">
                    <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      value={formData.email}
                      onChange={handleChange}
                      readOnly={!!invitedEmail}
                      className={`
                        appearance-none relative block w-full px-3 py-3 h-11
                        border ${errors.email ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                        placeholder-slate-400 text-slate-900 rounded-lg
                        focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                        transition-all duration-200
                        ${invitedEmail ? 'bg-slate-50 cursor-not-allowed' : ''}
                      `}
                      style={invitedEmail ? { backgroundColor: '#F8FAFC', cursor: 'not-allowed' } : {}}
                      placeholder="you@company.com"
                    />
                    {invitedEmail && (
                      <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                        <FiCheck className="h-5 w-5 text-emerald-500" />
                      </div>
                    )}
                  </div>
                  {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
                </div>

                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-1.5">
                    Password
                  </label>
                  <div className="relative">
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      value={formData.password}
                      onChange={handleChange}
                      className={`
                        appearance-none relative block w-full px-3 pr-10 py-3 h-11
                        border ${errors.password ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                        placeholder-slate-400 text-slate-900 rounded-lg
                        focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                        transition-all duration-200
                      `}
                      placeholder="Create a password"
                    />
                    <button
                      type="button"
                      className="absolute inset-y-0 right-0 pr-3 flex items-center"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? (
                        <FiEyeOff className="h-5 w-5 text-slate-400 hover:text-slate-600" />
                      ) : (
                        <FiEye className="h-5 w-5 text-slate-400 hover:text-slate-600" />
                      )}
                    </button>
                  </div>
                  {formData.password && (
                    <div className="mt-2">
                      <div className="flex items-center space-x-2">
                        <div className="flex-1 bg-slate-200 rounded-full h-2">
                          <div
                            className={`h-2 rounded-full transition-all duration-300 ${passwordStrength.color}`}
                            style={{
                              width:
                                passwordStrength.strength === 'weak'
                                  ? '33%'
                                  : passwordStrength.strength === 'good'
                                    ? '66%'
                                    : '100%',
                            }}
                          ></div>
                        </div>
                        <span
                          className={`text-xs font-medium ${
                            passwordStrength.strength === 'weak'
                              ? 'text-red-500'
                              : passwordStrength.strength === 'medium'
                                ? 'text-yellow-500'
                                : 'text-green-500'
                          }`}
                        >
                          {passwordStrength.text}
                        </span>
                      </div>
                    </div>
                  )}
                  {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
                </div>

                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-slate-700 mb-1.5">
                    Confirm password
                  </label>
                  <div className="relative">
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      className={`
                        appearance-none relative block w-full px-3 pr-10 py-3 h-11
                        border ${errors.confirmPassword ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                        placeholder-slate-400 text-slate-900 rounded-lg
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
                        <FiEyeOff className="h-5 w-5 text-slate-400 hover:text-slate-600" />
                      ) : (
                        <FiEye className="h-5 w-5 text-slate-400 hover:text-slate-600" />
                      )}
                    </button>
                  </div>
                  {passwordsMatch && (
                    <div className="mt-1 flex items-center text-green-600">
                      <FiCheck className="w-3 h-3 mr-1" />
                      <span className="text-xs">Passwords match</span>
                    </div>
                  )}
                  {passwordsMismatch && (
                    <div className="mt-1 flex items-center text-red-600">
                      <span className="text-xs">Passwords don&apos;t match</span>
                    </div>
                  )}
                  {errors.confirmPassword && <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>}
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="
                    group relative w-full flex justify-center py-3 px-4 h-11
                    border border-transparent text-sm font-semibold rounded-lg text-white
                bg-[#0052CC] hover:bg-[#0047b3] active:bg-[#003d99]
                focus:outline-none focus:ring-4 focus:ring-blue-100 focus:ring-offset-0
                disabled:opacity-60 disabled:cursor-not-allowed
                transition-all duration-300 shadow-md shadow-blue-100 hover:-translate-y-0.5
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

                <div className="text-center pt-1">
                  <p className="text-sm text-slate-600">
                    Already have an account?{' '}
                    <Link
                      to={loginLink}
                      className="font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200"
                    >
                      Sign in
                    </Link>
                  </p>
                </div>
              </form>
            </div>

            <div className="bg-slate-50/50 p-10 md:p-12 flex flex-col justify-center">
              <h3 className="text-[11px] font-bold text-slate-400 uppercase tracking-[2px] mb-6">
                Why teams love Kanban
              </h3>
              <ul className="space-y-4 text-slate-700">
                {[
                  'Boards to organize every project',
                  'Drag-and-drop workflows',
                  'Labels for instant context',
                  'Real-time collaboration',
                  'Activity feed for visibility',
                  'Roles and permissions',
                ].map((item) => (
                  <li key={item} className="flex items-start gap-3 text-sm">
                    <div className="mt-0.5 h-5 w-5 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
                      <FiCheck className="h-3 w-3" />
                    </div>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
              <div className="mt-6 rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-600">
                Everything you need to move from idea to delivery, without the clutter.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
