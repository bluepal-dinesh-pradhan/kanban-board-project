import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { FiEye, FiEyeOff } from 'react-icons/fi'

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
      <div className="w-full max-w-[420px]">
        <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 p-10">
          <div className="flex flex-col items-center mb-6">
            <div className="h-10 w-10 rounded-lg bg-[#0052CC] flex items-center justify-center text-white font-semibold mb-3">
              K
            </div>
            <h2 className="text-2xl font-semibold text-slate-900">Log in</h2>
            <p className="text-sm text-slate-500 mt-1">Use your email and password to continue.</p>
          </div>

          {isInvited && (
            <div className="mb-4 rounded-lg border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-700">
              You&apos;ve been invited to a board! Log in to access it.
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
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
                  className={`
                    appearance-none relative block w-full px-3 py-3 h-11
                    border ${errors.email ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                    placeholder-slate-400 text-slate-900 rounded-lg
                    focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500
                    transition-all duration-300
                  `}
                  placeholder="you@company.com"
                />
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
                  autoComplete="current-password"
                  value={formData.password}
                  onChange={handleChange}
                  className={`
                    appearance-none relative block w-full px-3 pr-10 py-3 h-11
                    border ${errors.password ? 'border-red-300 ring-red-100' : 'border-slate-300'}
                    placeholder-slate-400 text-slate-900 rounded-lg
                    focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500
                    transition-all duration-300
                  `}
                  placeholder="Enter your password"
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
              {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="
                group relative w-full flex justify-center py-3 px-4 h-12
                border border-transparent text-sm font-semibold rounded-lg text-white
                bg-[#0052CC] hover:bg-[#0047b3] active:bg-[#003d99]
                focus:outline-none focus:ring-4 focus:ring-blue-100 focus:ring-offset-0
                disabled:opacity-60 disabled:cursor-not-allowed
                transition-all duration-300 shadow-md shadow-blue-100
              "
            >
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Logging in...
                </div>
              ) : (
                'Log in'
              )}
            </button>

            <div className="text-center pt-1">
              <p className="text-sm text-slate-600">
                Don&apos;t have an account?{' '}
                <Link
                  to={registerLink}
                  className="font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200"
                >
                  Create one
                </Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
