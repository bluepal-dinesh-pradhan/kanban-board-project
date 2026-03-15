import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiMail, FiUserPlus } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const InviteModal = ({ boardId, onClose }) => {
  const [formData, setFormData] = useState({
    email: '',
    role: 'EDITOR'
  })
  const [errors, setErrors] = useState({})
  const queryClient = useQueryClient()

  const inviteMutation = useMutation({
    mutationFn: async (inviteData) => {
      const response = await boardAPI.inviteMember(boardId, inviteData)
      return response.data.data
    },
    onSuccess: () => {
      toast.success('Member invited successfully!')
      onClose()
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to invite member')
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

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center">
              <FiUserPlus className="mr-2 h-5 w-5 text-gray-600" />
              <h2 className="text-lg font-medium text-gray-900">Invite Member</h2>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email address
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <FiMail className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  className={`block w-full pl-10 px-3 py-2 border ${
                    errors.email ? 'border-red-300' : 'border-gray-300'
                  } rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500`}
                  placeholder="Enter email address"
                />
              </div>
              {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
            </div>

            <div>
              <label htmlFor="role" className="block text-sm font-medium text-gray-700">
                Role
              </label>
              <select
                id="role"
                name="role"
                value={formData.role}
                onChange={handleChange}
                className={`mt-1 block w-full px-3 py-2 border ${
                  errors.role ? 'border-red-300' : 'border-gray-300'
                } rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500`}
              >
                <option value="EDITOR">Editor - Can create and edit cards</option>
                <option value="VIEWER">Viewer - Can only view the board</option>
              </select>
              {errors.role && <p className="mt-1 text-sm text-red-600">{errors.role}</p>}
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={inviteMutation.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {inviteMutation.isPending ? 'Inviting...' : 'Send Invite'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default InviteModal