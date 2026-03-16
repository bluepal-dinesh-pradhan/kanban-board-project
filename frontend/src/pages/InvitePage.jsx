import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { invitationAPI } from '../api/endpoints'
import { useAuth } from '../context/AuthContext'

const InvitePage = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { isAuthenticated, loading: authLoading, user, logout } = useAuth()
  const token = searchParams.get('token')

  const [invitation, setInvitation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [accepting, setAccepting] = useState(false)
  const [error, setError] = useState('')

  const redirectParam = useMemo(() => {
    if (!token) return ''
    return encodeURIComponent(`/invite?token=${token}`)
  }, [token])

  useEffect(() => {
    let isMounted = true
    const loadInvitation = async () => {
      if (!token) {
        setError('Invitation link is missing or invalid.')
        setLoading(false)
        return
      }

      try {
        const response = await invitationAPI.getInvitation(token)
        if (isMounted) {
          setInvitation(response.data.data)
        }
      } catch (err) {
        if (isMounted) {
          setError(err.response?.data?.message || 'Failed to load invitation.')
        }
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadInvitation()
    return () => { isMounted = false }
  }, [token])

  useEffect(() => {
    const acceptIfReady = async () => {
      if (!token || !invitation || !isAuthenticated || accepting || error) return
      if (user?.email && invitation?.email && user.email.toLowerCase() !== invitation.email.toLowerCase()) {
        setError('This invitation was sent to a different email')
        return
      }
      setAccepting(true)
      try {
        const response = await invitationAPI.acceptInvitation(token)
        const boardId = response.data.data.boardId
        navigate(`/boards/${boardId}`, { replace: true })
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to accept invitation.')
        setAccepting(false)
      }
    }

    acceptIfReady()
  }, [token, invitation, isAuthenticated, accepting, navigate, user, error])

  if (loading || authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading invitation...</p>
        </div>
      </div>
    )
  }

  if (error) {
    const invitedEmail = invitation?.email
    const loginUrl = invitedEmail
      ? `/login?email=${encodeURIComponent(invitedEmail)}&inviteToken=${encodeURIComponent(token)}&redirect=${redirectParam}`
      : '/login'
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-8">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md text-center">
          <h1 className="text-2xl font-bold text-gray-800 mb-2">Invitation Error</h1>
          <p className="text-gray-600 mb-6">{error}</p>
          <div className="space-y-3">
            <button
              type="button"
              onClick={() => {
                logout()
                navigate(loginUrl, { replace: true })
              }}
              className="inline-block px-6 py-3 rounded-lg text-white brand-gradient w-full"
            >
              Sign in with invited email
            </button>
            <Link
              to="/login"
              className="inline-block px-6 py-3 rounded-lg text-blue-600 border border-blue-200 w-full"
            >
              Go to login
            </Link>
          </div>
        </div>
      </div>
    )
  }

  if (isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Accepting invitation...</p>
        </div>
      </div>
    )
  }

  const emailParam = invitation?.email ? `&email=${encodeURIComponent(invitation.email)}` : ''
  const loginUrl = `/login?inviteToken=${encodeURIComponent(token)}&redirect=${redirectParam}`
  const registerUrl = `/register?inviteToken=${encodeURIComponent(token)}${emailParam}&redirect=${redirectParam}`

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-8">
      <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full">
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">You're invited</h1>
          <p className="text-gray-600 mt-2">
            Join <span className="font-semibold">{invitation?.boardTitle}</span> as a{' '}
            <span className="font-semibold">{invitation?.role?.toLowerCase()}</span>.
          </p>
        </div>

        {invitation?.expiresAt && (
          <p className="text-xs text-gray-500 text-center mb-4">
            This invitation expires on {new Date(invitation.expiresAt).toLocaleString()}.
          </p>
        )}

        <div className="space-y-4">
          <Link
            to={invitation?.userExists ? loginUrl : registerUrl}
            className="block text-center px-6 py-3 rounded-lg text-white brand-gradient"
          >
            {invitation?.userExists ? 'Sign in to accept' : 'Create account to accept'}
          </Link>
          <div className="text-center text-sm text-gray-600">
            {invitation?.userExists ? (
              <>
                Don't have an account?{' '}
                <Link to={registerUrl} className="font-medium text-blue-600 hover:text-blue-500">
                  Sign up
                </Link>
              </>
            ) : (
              <>
                Already have an account?{' '}
                <Link to={loginUrl} className="font-medium text-blue-600 hover:text-blue-500">
                  Sign in
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default InvitePage
