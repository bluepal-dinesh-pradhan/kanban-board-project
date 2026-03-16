import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const CreateBoardModal = ({ onClose }) => {
  const [formData, setFormData] = useState({
    title: '',
    background: '#0079BF'
  })
  const [errors, setErrors] = useState({})
  const queryClient = useQueryClient()

  const backgroundOptions = [
    { color: '#0079BF', name: 'Blue' },
    { color: '#D29034', name: 'Orange' },
    { color: '#519839', name: 'Green' },
    { color: '#B04632', name: 'Red' },
    { color: '#89609E', name: 'Purple' },
  ]

  const createMutation = useMutation({
    mutationFn: async (boardData) => {
      const response = await boardAPI.createBoard(boardData)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boards'] })
      toast.success('Board created successfully!')
      onClose()
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create board')
    }
  })

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.title.trim()) {
      newErrors.title = 'Board title is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    
    if (!validateForm()) return
    
    createMutation.mutate({
      title: formData.title.trim(),
      background: formData.background
    })
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
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-2xl max-w-md w-full mx-4">
        <div className="p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-900">Create new board</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors duration-200"
            >
              <FiX className="h-6 w-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
                Board title
              </label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                className={`
                  block w-full px-3 py-2 border ${
                    errors.title ? 'border-red-300' : 'border-gray-300'
                  } rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                `}
                placeholder="Enter board title"
                autoFocus
              />
              {errors.title && <p className="mt-1 text-sm text-red-600">{errors.title}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Background color
              </label>
              <div className="flex space-x-3">
                {backgroundOptions.map((option) => (
                  <button
                    key={option.color}
                    type="button"
                    onClick={() => setFormData(prev => ({ ...prev, background: option.color }))}
                    className={`
                      w-12 h-12 rounded-lg border-4 transition-all duration-200
                      ${formData.background === option.color 
                        ? 'border-gray-800 scale-110' 
                        : 'border-gray-200 hover:border-gray-400'
                      }
                    `}
                    style={{ backgroundColor: option.color }}
                    title={option.name}
                  />
                ))}
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="px-6 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors duration-200"
              >
                {createMutation.isPending ? 'Creating...' : 'Create'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default CreateBoardModal