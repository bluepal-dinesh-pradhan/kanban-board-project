import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authAPI } from '../api/endpoints'

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/
    if (!email) {
      setError('Email is required')
      return
    }
    if (!emailRegex.test(email)) {
      setError('Invalid email format')
      return
    }

    setLoading(true)
    try {
      await authAPI.forgotPassword(email)
      setSent(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (sent) {
    return (
      <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-[420px]">
          <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.08)] border border-slate-100 p-10">
            <div className="flex flex-col items-center mb-6">
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center mb-4">
                <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h2 className="text-2xl font-semibold text-slate-900">Check your email</h2>
              <p className="text-sm text-slate-500 mt-2 text-center">
                If an account exists for <strong>{email}</strong>, we've sent a password reset link. 
                The link will expire in 1 hour.
              </p>
            </div>

            <div className="space-y-3 mt-6">
              <button
                onClick={() => { setSent(false); setEmail(''); }}
                className="w-full py-3 px-4 h-12 border border-slate-300 text-sm font-semibold rounded-lg text-slate-700 hover:bg-slate-50 transition-all duration-300"
              >
                Try a different email
              </button>

              <div className="text-center">
                <Link
                  to="/login"
                  className="text-sm font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200"
                >
                  Back to login
                </Link>
              </div>
            </div>
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
            <h2 className="text-2xl font-semibold text-slate-900">Forgot password?</h2>
            <p className="text-sm text-slate-500 mt-1 text-center">
              Enter your email and we'll send you a link to reset your password.
            </p>
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1.5">
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setError(''); }}
                className="appearance-none relative block w-full px-3 py-3 h-11 border border-slate-300 placeholder-slate-400 text-slate-900 rounded-lg focus:outline-none focus:ring-[3px] focus:ring-blue-500/10 focus:border-blue-500 transition-all duration-300"
                placeholder="you@company.com"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-4 h-12 border border-transparent text-sm font-semibold rounded-lg text-white bg-[#0052CC] hover:bg-[#0047b3] active:bg-[#003d99] focus:outline-none focus:ring-4 focus:ring-blue-100 disabled:opacity-60 disabled:cursor-not-allowed transition-all duration-300 shadow-md shadow-blue-100"
            >
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Sending...
                </div>
              ) : (
                'Send reset link'
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

export default ForgotPasswordPage
