import { useState, useMemo } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FiX, FiMail, FiUserPlus, FiStar, FiCopy, FiTrash2, FiAlertCircle } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Avatar from './common/Avatar'
import { timeAgo } from '../utils/timeAgo'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext'

const InviteModal = ({ boardId, onClose }) => {
  const [formData, setFormData] = useState({
    email: '',
    role: 'EDITOR'
  })
  const [errors, setErrors] = useState({})
  const [removeTarget, setRemoveTarget] = useState(null)
  const queryClient = useQueryClient()
  const { user } = useAuth()

  // Fetch board members and pending invitations
  const { data: membersData } = useQuery({
    queryKey: ['boardMembers', boardId],
    queryFn: async () => {
      const response = await boardAPI.getBoardMembers(boardId)
      return response.data.data
    }
  })

  const inviteMutation = useMutation({
    mutationFn: async (inviteData) => {
      const response = await boardAPI.inviteMember(boardId, inviteData)
      return response.data.data
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['boardMembers', boardId] })

      if (data.status === 'ADDED') {
        toast.success(data.message || 'Member added!', { id: 'invite-added' })
      } else if (data.status === 'INVITED') {
        if (data.emailSent) {
          toast.success(data.message || 'Invitation sent!', { id: 'invite-sent' })
        } else {
          toast(data.message || 'Invitation saved.', {
            id: 'invite-saved',
            icon: '⚠️',
            duration: 6000,
          })
        }
      } else if (data.status === 'RESENT') {
        if (data.emailSent) {
          toast.success(data.message || 'Invitation resent!', { id: 'invite-resent' })
        } else {
          toast(data.message || 'Invitation resent!', {
            id: 'invite-resent-warning',
            icon: '⚠️',
            duration: 6000,
          })
        }
      } else {
        toast.success(data.message || 'Invitation sent', { id: 'invite-generic' })
      }

      setFormData({ email: '', role: 'EDITOR' })
      setErrors({})
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || error.message || 'Failed to invite member', { id: 'invite-error' })
    }
  })

  const cancelInviteMutation = useMutation({
    mutationFn: async (invitationId) => {
      await boardAPI.cancelInvitation(boardId, invitationId)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boardMembers', boardId] })
      toast.success('Invitation cancelled', { id: 'invite-cancelled' })
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to cancel invitation', { id: 'invite-cancel-error' })
    }
  })

  const removeMemberMutation = useMutation({
    mutationFn: async (memberId) => {
      await boardAPI.removeMember(boardId, memberId)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boardMembers', boardId] })
      toast.success('Member removed', { id: 'member-removed' })
      setRemoveTarget(null)
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to remove member', { id: 'member-remove-error' })
    }
  })

  const validateForm = () => {
    const newErrors = {}

    const emailValue = formData.email.trim()
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!emailValue) {
      newErrors.email = 'Email is required'
    } else if (!emailRegex.test(emailValue)) {
      newErrors.email = 'Please enter a valid email address.'
    }
    
    if (!formData.role) {
      newErrors.role = 'Role is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleInvite = (payload) => {
    const nextData = payload || formData
    const nextEmail = nextData.email?.trim() || ''
    const nextRole = nextData.role || formData.role

    setFormData(prev => ({ ...prev, email: nextEmail, role: nextRole }))

    if (!nextEmail) {
      setErrors({ email: 'Email is required' })
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(nextEmail)) {
      setErrors({ email: 'Please enter a valid email address.' })
      return
    }

    inviteMutation.mutate({
      ...nextData,
      email: nextEmail,
      role: nextRole
    })
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validateForm()) return
    handleInvite()
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  const copyRegistrationLink = () => {
    const link = `${window.location.origin}/register`
    navigator.clipboard.writeText(link)
    toast.success('Registration link copied to clipboard!', { id: 'invite-copy-link' })
  }

  const getRoleBadge = (role) => {
    const styles = {
      OWNER: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      EDITOR: 'bg-blue-100 text-blue-800 border-blue-200', 
      VIEWER: 'bg-gray-100 text-gray-800 border-gray-200'
    }
    
    return (
      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${styles[role]}`}>
        {role === 'OWNER' && <FiStar className="w-3 h-3 mr-1" />}
        {role}
      </span>
    )
  }

  const canSendInvite = (() => {
    const emailValue = formData.email.trim()
    return emailValue.length > 0 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue)
  })()

  const currentUserRole = useMemo(() => {
    if (!membersData?.members || !user?.id) return null
    const currentMember = membersData.members.find((member) => member.user.id === user.id)
    return currentMember?.role || null
  }, [membersData, user])

  const canRemoveMembers = currentUserRole === 'OWNER'

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-2xl max-w-md w-full mx-4 max-h-[90vh] overflow-hidden">
        <div className="p-6">
          <div className="flex justify-between items-center mb-6">
            <div className="flex items-center">
              <FiUserPlus className="mr-2 h-5 w-5 text-gray-600" />
              <h2 className="text-xl font-semibold text-gray-900">Invite to board</h2>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors duration-200"
            >
              <FiX className="h-6 w-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4 mb-6">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                Email address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <FiMail className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  className={`block w-full pl-10 pr-3 py-2 border ${
                    errors.email ? 'border-red-300' : 'border-gray-300'
                  } rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500`}
                  placeholder="Enter email address"
                />
              </div>
              {errors.email && (
                <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                  <FiAlertCircle className="w-3.5 h-3.5" />
                  {errors.email}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-2">
                Role
              </label>
              <select
                id="role"
                name="role"
                value={formData.role}
                onChange={handleChange}
                className={`block w-full px-3 py-2 border ${
                  errors.role ? 'border-red-300' : 'border-gray-300'
                } rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500`}
              >
                <option value="EDITOR">Editor — Can edit and move cards</option>
                <option value="VIEWER">Viewer — Can only view</option>
              </select>
              {errors.role && <p className="mt-1 text-sm text-red-600">{errors.role}</p>}
            </div>

            <button
              type="submit"
              disabled={inviteMutation.isPending || !canSendInvite}
              className="w-full px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors duration-200"
            >
              {inviteMutation.isPending ? 'Sending...' : 'Send Invitation'}
            </button>
          </form>

          {/* Members and Invitations */}
          <div className="border-t border-gray-200 pt-6 max-h-80 overflow-y-auto">
            {/* Current Members */}
            {membersData?.members && membersData.members.length > 0 && (
              <div className="mb-6">
                <h3 className="text-sm font-medium text-gray-900 mb-3">Board Members</h3>
                <div className="space-y-3">
                  {membersData.members.map((member) => (
                    <div key={member.id} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <Avatar name={member.user.fullName} size="sm" />
                          <div>
                            <p className="text-sm font-medium text-gray-900">{member.user.fullName}</p>
                            <p className="text-xs text-gray-500">{member.user.email}</p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-2">
                          {getRoleBadge(member.role)}
                          {canRemoveMembers &&
                            member.role !== 'OWNER' &&
                            member.user.id !== user?.id && (
                              <button
                                onClick={() => setRemoveTarget(member)}
                                className="text-gray-400 hover:text-red-600 transition-colors duration-200"
                                title="Remove member"
                              >
                                <FiX className="w-4 h-4" />
                              </button>
                            )}
                        </div>
                      </div>

                      {removeTarget?.id === member.id && (
                        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700 flex items-center justify-between">
                          <span>
                            Remove <strong>{member.user.fullName}</strong> from this board?
                          </span>
                          <div className="flex items-center space-x-2">
                            <button
                              onClick={() => removeMemberMutation.mutate(member.id)}
                              className="px-3 py-1 rounded-md bg-red-600 text-white text-xs font-semibold hover:bg-red-700"
                              disabled={removeMemberMutation.isPending}
                            >
                              Remove
                            </button>
                            <button
                              onClick={() => setRemoveTarget(null)}
                              className="px-3 py-1 rounded-md border border-red-200 text-xs font-semibold text-red-700 hover:bg-red-100"
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Pending Invitations */}
            {membersData?.pendingInvitations && membersData.pendingInvitations.length > 0 && (
              <div>
                <h3 className="text-sm font-medium text-gray-900 mb-3">Pending Invitations</h3>
                <div className="space-y-3">
                  {membersData.pendingInvitations.map((invitation) => (
                    <div key={invitation.id} className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <Avatar name={invitation.email} size="sm" />
                        <div>
                          <p className="text-sm font-medium text-gray-900">{invitation.email}</p>
                          <p className="text-xs text-gray-500">Invited {timeAgo(invitation.createdAt)}</p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 border border-yellow-200">
                          Pending
                        </span>
                        <button
                          onClick={() => handleInvite({ email: invitation.email, role: invitation.role })}
                          disabled={inviteMutation.isPending}
                          className="text-blue-600 hover:text-blue-800 text-xs font-semibold"
                        >
                          Resend
                        </button>
                        <button
                          onClick={() => cancelInviteMutation.mutate(invitation.id)}
                          disabled={cancelInviteMutation.isPending}
                          className="text-gray-400 hover:text-red-600 transition-colors duration-200"
                          title="Cancel invitation"
                        >
                          <FiTrash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
                
                {/* Copy registration link button */}
                <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                  <p className="text-xs text-yellow-800 mb-2">
                    Email not configured? Share the registration link manually:
                  </p>
                  <button
                    onClick={copyRegistrationLink}
                    className="inline-flex items-center text-xs text-yellow-700 hover:text-yellow-900 font-medium"
                  >
                    <FiCopy className="w-3 h-3 mr-1" />
                    Copy registration link
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default InviteModal

