import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FiX, FiMail, FiUserPlus, FiStar, FiCopy, FiTrash2 } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Avatar from './common/Avatar'
import { timeAgo } from '../utils/timeAgo'
import toast from 'react-hot-toast'

const InviteModal = ({ boardId, onClose }) => {
  const [formData, setFormData] = useState({
    email: '',
    role: 'EDITOR'
  })
  const [errors, setErrors] = useState({})
  const queryClient = useQueryClient()

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
      
      // Show appropriate toast based on response
      if (data.status === 'ADDED') {
        toast.success(data.message)
      } else if (data.status === 'INVITED' && data.emailSent) {
        toast.success(data.message)
      } else if (data.status === 'INVITED' && !data.emailSent) {
        toast(data.message, {
          icon: '⚠️',
          duration: 6000,
        })
      }
      
      // Reset form
      setFormData({ email: '', role: 'EDITOR' })
      setErrors({})
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to invite member')
    }
  })

  const cancelInviteMutation = useMutation({
    mutationFn: async (invitationId) => {
      await boardAPI.cancelInvitation(boardId, invitationId)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boardMembers', boardId] })
      toast.success('Invitation cancelled')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to cancel invitation')
    }
  })

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid'
    }
    
    if (!formData.role) {
      newErrors.role = 'Role is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    
    if (!validateForm()) return
    
    inviteMutation.mutate(formData)
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
    toast.success('Registration link copied to clipboard!')
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
              {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
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
              disabled={inviteMutation.isPending}
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
                    <div key={member.id} className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <Avatar name={member.user.fullName} size="sm" />
                        <div>
                          <p className="text-sm font-medium text-gray-900">{member.user.fullName}</p>
                          <p className="text-xs text-gray-500">{member.user.email}</p>
                        </div>
                      </div>
                      {getRoleBadge(member.role)}
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