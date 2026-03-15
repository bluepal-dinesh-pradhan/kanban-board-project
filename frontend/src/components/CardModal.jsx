import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiCalendar, FiTag, FiMessageSquare, FiSave } from 'react-icons/fi'
import { cardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const LABEL_COLORS = [
  '#ef4444', // red
  '#f97316', // orange
  '#eab308', // yellow
  '#22c55e', // green
  '#3b82f6', // blue
  '#8b5cf6', // purple
]

const CardModal = ({ cardId, onClose }) => {
  const [isEditing, setIsEditing] = useState(false)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    dueDate: '',
    labels: []
  })
  const [newComment, setNewComment] = useState('')
  const [newLabel, setNewLabel] = useState({ color: LABEL_COLORS[0], text: '' })
  const queryClient = useQueryClient()

  const { data: card } = useQuery({
    queryKey: ['card', cardId],
    queryFn: async () => {
      const response = await cardAPI.getCard(cardId)
      return response.data.data
    }
  })

  const { data: comments } = useQuery({
    queryKey: ['card', cardId, 'comments'],
    queryFn: async () => {
      const response = await cardAPI.getCardComments(cardId)
      return response.data.data
    }
  })

  useEffect(() => {
    if (card) {
      setFormData({
        title: card.title,
        description: card.description || '',
        dueDate: card.dueDate || '',
        labels: card.labels || []
      })
    }
  }, [card])

  const updateCardMutation = useMutation({
    mutationFn: async (cardData) => {
      const response = await cardAPI.updateCard(cardId, cardData)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId])
      queryClient.invalidateQueries(['board'])
      setIsEditing(false)
      toast.success('Card updated successfully!')
    }
  })

  const addCommentMutation = useMutation({
    mutationFn: async (content) => {
      const response = await cardAPI.addComment(cardId, { content })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'comments'])
      setNewComment('')
      toast.success('Comment added!')
    }
  })

  const handleSave = (e) => {
    e.preventDefault()
    updateCardMutation.mutate({
      title: formData.title.trim(),
      description: formData.description.trim() || null,
      dueDate: formData.dueDate || null,
      columnId: card.columnId,
      labels: formData.labels
    })
  }

  const handleAddComment = (e) => {
    e.preventDefault()
    if (newComment.trim()) {
      addCommentMutation.mutate(newComment.trim())
    }
  }

  const handleAddLabel = () => {
    if (newLabel.text.trim()) {
      setFormData(prev => ({
        ...prev,
        labels: [...prev.labels, { ...newLabel, text: newLabel.text.trim(), id: Date.now() }]
      }))
      setNewLabel({ color: LABEL_COLORS[0], text: '' })
    }
  }

  const handleRemoveLabel = (labelId) => {
    setFormData(prev => ({
      ...prev,
      labels: prev.labels.filter(label => label.id !== labelId)
    }))
  }

  if (!card) return null

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-start mb-4">
            {isEditing ? (
              <form onSubmit={handleSave} className="flex-1">
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  className="text-xl font-medium w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                  placeholder="Add a description..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mt-3"
                  rows={4}
                />
                <div className="flex items-center space-x-2 mt-3">
                  <FiCalendar className="h-4 w-4 text-gray-400" />
                  <input
                    type="date"
                    value={formData.dueDate}
                    onChange={(e) => setFormData(prev => ({ ...prev, dueDate: e.target.value }))}
                    className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                {/* Labels Section */}
                <div className="mt-4">
                  <div className="flex items-center space-x-2 mb-2">
                    <FiTag className="h-4 w-4 text-gray-400" />
                    <span className="text-sm font-medium text-gray-700">Labels</span>
                  </div>
                  
                  <div className="flex flex-wrap gap-2 mb-3">
                    {formData.labels.map((label) => (
                      <span
                        key={label.id}
                        className="inline-flex items-center px-2 py-1 text-xs rounded-full text-white cursor-pointer"
                        style={{ backgroundColor: label.color }}
                        onClick={() => handleRemoveLabel(label.id)}
                      >
                        {label.text}
                        <FiX className="ml-1 h-3 w-3" />
                      </span>
                    ))}
                  </div>

                  <div className="flex items-center space-x-2">
                    <select
                      value={newLabel.color}
                      onChange={(e) => setNewLabel(prev => ({ ...prev, color: e.target.value }))}
                      className="px-2 py-1 border border-gray-300 rounded text-xs"
                    >
                      {LABEL_COLORS.map(color => (
                        <option key={color} value={color} style={{ backgroundColor: color, color: 'white' }}>
                          {color}
                        </option>
                      ))}
                    </select>
                    <input
                      type="text"
                      value={newLabel.text}
                      onChange={(e) => setNewLabel(prev => ({ ...prev, text: e.target.value }))}
                      placeholder="Label text"
                      className="px-2 py-1 border border-gray-300 rounded text-xs flex-1"
                    />
                    <button
                      type="button"
                      onClick={handleAddLabel}
                      disabled={!newLabel.text.trim()}
                      className="px-2 py-1 bg-gray-200 hover:bg-gray-300 rounded text-xs disabled:opacity-50"
                    >
                      Add
                    </button>
                  </div>
                </div>

                <div className="flex space-x-2 mt-4">
                  <button
                    type="submit"
                    disabled={updateCardMutation.isPending}
                    className="inline-flex items-center px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-md disabled:opacity-50"
                  >
                    <FiSave className="mr-2 h-4 w-4" />
                    Save
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="text-gray-600 hover:text-gray-800 px-4 py-2"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <div className="flex-1">
                <h2 className="text-xl font-medium text-gray-900 mb-2">{card.title}</h2>
                {card.description && (
                  <p className="text-gray-700 mb-4 whitespace-pre-wrap">{card.description}</p>
                )}
                
                <div className="flex flex-wrap gap-2 mb-4">
                  {card.labels?.map((label) => (
                    <span
                      key={label.id}
                      className="inline-block px-2 py-1 text-xs rounded-full text-white"
                      style={{ backgroundColor: label.color }}
                    >
                      {label.text}
                    </span>
                  ))}
                </div>

                {card.dueDate && (
                  <div className="flex items-center text-sm text-gray-500 mb-4">
                    <FiCalendar className="mr-2 h-4 w-4" />
                    Due: {new Date(card.dueDate).toLocaleDateString()}
                  </div>
                )}
                
                <button
                  onClick={() => setIsEditing(true)}
                  className="text-blue-600 hover:text-blue-800 text-sm"
                >
                  Edit
                </button>
              </div>
            )}
            
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 ml-4"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          <div className="border-t pt-4">
            <div className="flex items-center space-x-2 mb-3">
              <FiMessageSquare className="h-4 w-4 text-gray-400" />
              <h3 className="font-medium text-gray-900">Comments</h3>
            </div>
            
            <form onSubmit={handleAddComment} className="mb-4">
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="Write a comment..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
              />
              <button
                type="submit"
                disabled={!newComment.trim() || addCommentMutation.isPending}
                className="mt-2 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm disabled:opacity-50"
              >
                Add Comment
              </button>
            </form>

            <div className="space-y-3">
              {comments?.map((comment) => (
                <div key={comment.id} className="bg-gray-50 p-3 rounded-md">
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-medium text-sm text-gray-900">
                      {comment.author.fullName}
                    </span>
                    <span className="text-xs text-gray-500">
                      {new Date(comment.createdAt).toLocaleString()}
                    </span>
                  </div>
                  <p className="text-gray-700 whitespace-pre-wrap">{comment.content}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CardModal