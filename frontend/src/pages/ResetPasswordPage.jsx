import { useState } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import { FiEye, FiEyeOff } from 'react-icons/fi'
import { authAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const [success, setSuccess] = useState(false)

  if (!token) {
    return (
      <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-[420px]">
          <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 p-10 text-center">
            <div className="h-12 w-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
              <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-2xl font-semibold text-slate-900 mb-2">Invalid Reset Link</h2>
            <p className="text-sm text-slate-500 mb-6">
              This password reset link is invalid or has expired.
            </p>
            <Link
              to="/forgot-password"
              className="text-sm font-semibold text-blue-600 hover:text-blue-500"
            >
              Request a new reset link
            </Link>
          </div>
        </div>
      </div>
    )
  }

  const validateForm = () => {
    const newErrors = {}
    const strengthRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/

    if (!formData.newPassword) {
      newErrors.newPassword = 'New password is required'
    } else if (!strengthRegex.test(formData.newPassword)) {
      newErrors.newPassword = 'Must be 8+ chars with uppercase, lowercase, number & special character'
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password'
    } else if (formData.newPassword !== formData.confirmPassword) {
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
      await authAPI.resetPassword({ token, newPassword: formData.newPassword })
      setSuccess(true)
      toast.success('Password reset successfully!')
      setTimeout(() => navigate('/login'), 3000)
    } catch (err) {
      const message = err.response?.data?.message || 'Failed to reset password. The link may have expired.'
      setErrors({ form: message })
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  if (success) {
    return (
      <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-[420px]">
          <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 p-10 text-center">
            <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
              <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-semibold text-slate-900 mb-2">Password Reset!</h2>
            <p className="text-sm text-slate-500 mb-6">
              Your password has been reset successfully. Redirecting to login...
            </p>
            <Link
              to="/login"
              className="text-sm font-semibold text-blue-600 hover:text-blue-500"
            >
              Go to login now
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center px-6 py-12">
      <div className="w-full max-w-[420px]">
        <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 p-10">
          <div className="flex flex-col items-center mb-6">
            <div className="h-10 w-10 rounded-lg bg-[#0052CC] flex items-center justify-center text-white font-semibold mb-3">
              K
            </div>
            <h2 className="text-2xl font-semibold text-slate-900">Set new password</h2>
            <p className="text-sm text-slate-500 mt-1 text-center">
              Your new password must be different from your previous password.
            </p>
          </div>

          {errors.form && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
              {errors.form}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-slate-700 mb-1.5">
                New Password
              </label>
              <div className="relative">
                <input
                  id="newPassword"
                  name="newPassword"
                  type={showPassword ? 'text' : 'password'}
                  value={formData.newPassword}
                  onChange={handleChange}
                  className={`appearance-none relative block w-full px-3 pr-10 py-3 h-11 border ${errors.newPassword ? 'border-red-300' : 'border-slate-300'} placeholder-slate-400 text-slate-900 rounded-lg focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500 transition-all duration-300`}
                  placeholder="Enter new password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <FiEyeOff className="h-5 w-5 text-slate-400" /> : <FiEye className="h-5 w-5 text-slate-400" />}
                </button>
              </div>
              {errors.newPassword && <p className="mt-1 text-sm text-red-600">{errors.newPassword}</p>}
              <p className="mt-1 text-xs text-slate-400">Min 8 chars: uppercase, lowercase, number & special char</p>
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-slate-700 mb-1.5">
                Confirm Password
              </label>
              <div className="relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirm ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className={`appearance-none relative block w-full px-3 pr-10 py-3 h-11 border ${errors.confirmPassword ? 'border-red-300' : 'border-slate-300'} placeholder-slate-400 text-slate-900 rounded-lg focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500 transition-all duration-300`}
                  placeholder="Confirm new password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowConfirm(!showConfirm)}
                >
                  {showConfirm ? <FiEyeOff className="h-5 w-5 text-slate-400" /> : <FiEye className="h-5 w-5 text-slate-400" />}
                </button>
              </div>
              {errors.confirmPassword && <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-4 h-12 border border-transparent text-sm font-semibold rounded-lg text-white bg-[#0052CC] hover:bg-[#0047b3] active:bg-[#003d99] focus:outline-none focus:ring-4 focus:ring-blue-100 disabled:opacity-60 disabled:cursor-not-allowed transition-all duration-300 shadow-md shadow-blue-100"
            >
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Resetting...
                </div>
              ) : (
                'Reset password'
              )}
            </button>

            <div className="text-center pt-1">
              <Link
                to="/login"
                className="text-sm font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200"
              >
                Back to login
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default ResetPasswordPage
