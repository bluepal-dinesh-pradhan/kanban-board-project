import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiCheck } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const CreateBoardModal = ({ onClose }) => {
  const [formData, setFormData] = useState({
    title: '',
    background: '#0079BF'
  })
  const [errors, setErrors] = useState({})
  const [selectedTemplate, setSelectedTemplate] = useState('BLANK')

  const TEMPLATES = [
    { id: 'BLANK', name: 'Blank Board', desc: 'Start from scratch', icon: '📋', columns: 'No preset columns' },
    { id: 'SCRUM', name: 'Scrum Board', desc: 'Agile sprints', icon: '🏃', columns: 'Backlog, Sprint, In Progress, Review, Done' },
    { id: 'BUG_TRACKER', name: 'Bug Tracker', desc: 'Track & fix bugs', icon: '🐛', columns: 'New, Triaging, In Progress, Testing, Resolved, Closed' },
    { id: 'MARKETING', name: 'Marketing', desc: 'Campaign pipeline', icon: '📢', columns: 'Ideas, Planning, In Progress, Review, Published' },
    { id: 'PERSONAL', name: 'Personal', desc: 'Simple to-do', icon: '✅', columns: 'To Do, Doing, Done' },
    { id: 'DESIGN', name: 'Design', desc: 'Design workflow', icon: '🎨', columns: 'Research, Wireframes, Design, Feedback, Final' },
  ]

  const queryClient = useQueryClient()

  const backgroundOptions = [
    { color: '#0079BF', name: 'Blue', gradient: 'linear-gradient(135deg, #0079BF 0%, #5BA4CF 100%)' },
    { color: '#D29034', name: 'Orange', gradient: 'linear-gradient(135deg, #D29034 0%, #FFAB4A 100%)' },
    { color: '#519839', name: 'Green', gradient: 'linear-gradient(135deg, #519839 0%, #61BD4F 100%)' },
    { color: '#B04632', name: 'Red', gradient: 'linear-gradient(135deg, #B04632 0%, #EB5A46 100%)' },
    { color: '#89609E', name: 'Purple', gradient: 'linear-gradient(135deg, #89609E 0%, #CD8DE5 100%)' },
  ]

  const createMutation = useMutation({
    mutationFn: async (boardData) => {
      if (selectedTemplate === 'BLANK') {
        const response = await boardAPI.createBoard(boardData)
        return response.data.data
      } else {
        const response = await boardAPI.createFromTemplate({
          ...boardData,
          template: selectedTemplate
        })
        return response.data.data
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boards'] })
      toast.success('Board created successfully!', { id: 'board-created' })
      onClose()
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create board', { id: 'board-create-error' })
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
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-[0_20px_60px_rgba(0,0,0,0.2)] max-w-md w-full border border-gray-100 animate-scale-in">
        <div className="p-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-xl font-semibold text-gray-900 tracking-tight">Create new board</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-1.5 rounded-lg transition-colors duration-200"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="title" className="block text-sm font-semibold text-gray-700 mb-2">
                Board title
              </label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                className={`
                  block w-full px-4 py-3 text-sm border-2 ${
                    errors.title ? 'border-red-300 ring-4 ring-red-50' : 'border-gray-200 focus:border-blue-500 focus:ring-4 focus:ring-blue-100/50'
                  } rounded-xl outline-none transition-all duration-300 font-medium
                `}
                placeholder="Enter board title"
                autoFocus
              />
              {errors.title && <p className="mt-2 text-xs text-red-600 font-bold uppercase tracking-wider">{errors.title}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-3">
                Background
              </label>
              <div className="flex space-x-4">
                {backgroundOptions.map((option) => (
                  <button
                    key={option.color}
                    type="button"
                    onClick={() => setFormData(prev => ({ ...prev, background: option.color }))}
                    className={`
                      relative w-12 h-12 rounded-xl flex items-center justify-center transition-all duration-300 shadow-sm
                      ${formData.background === option.color 
                        ? 'ring-4 ring-gray-900 ring-offset-2 scale-110 shadow-lg' 
                        : 'hover:scale-105 hover:shadow-md'
                      }
                    `}
                    style={{ backgroundImage: option.gradient }}
                    title={option.name}
                  >
                    {formData.background === option.color && (
                      <div className="w-6 h-6 bg-white/30 backdrop-blur-md rounded-full flex items-center justify-center border border-white/40">
                        <FiCheck className="w-4 h-4 text-white" />
                      </div>
                    )}
                  </button>
                ))}
              </div>
            </div>

            {/* Template Selection */}
            <div className="space-y-3">
              <label className="text-sm font-semibold text-gray-700">
                Template
              </label>
              <div className="grid grid-cols-2 gap-2 max-h-[220px] overflow-y-auto pr-1 scrollbar-thin">
                {TEMPLATES.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    onClick={() => setSelectedTemplate(t.id)}
                    className={`text-left p-2.5 rounded-xl border-2 transition-all duration-200 ${
                      selectedTemplate === t.id
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 shadow-sm'
                        : 'border-gray-100 dark:border-gray-800 hover:border-gray-200 dark:hover:border-gray-700 bg-gray-50/50'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <span className="text-xl shrink-0">{t.icon}</span>
                      <div className="min-w-0">
                        <div className="text-xs font-bold text-gray-900 dark:text-gray-100 truncate">{t.name}</div>
                        <div className="text-[10px] text-gray-500 dark:text-gray-400 truncate">{t.desc}</div>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
              {selectedTemplate !== 'BLANK' && (
                <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-1 leading-relaxed">
                  <span className="font-bold text-gray-700 dark:text-gray-300">Preset Columns:</span> {TEMPLATES.find(t => t.id === selectedTemplate)?.columns}
                </p>
              )}
            </div>

            <div className="flex justify-end gap-3 pt-8 border-t border-gray-100 mt-10">
              <button
                type="button"
                onClick={onClose}
                className="px-6 py-2.5 text-sm font-bold text-gray-500 border-2 border-transparent rounded-xl hover:bg-gray-100 hover:text-gray-900 transition-all duration-200"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="px-8 py-2.5 text-sm font-bold text-white bg-[#0079BF] rounded-xl hover:bg-[#005A8E] disabled:opacity-50 shadow-lg shadow-blue-100 transition-all hover:-translate-y-0.5"
              >
                {createMutation.isPending ? 'Creating...' : 'Create board'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default CreateBoardModal
