import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiCalendar, FiTag, FiMessageSquare, FiSave, FiList, FiClock, FiAlignLeft } from 'react-icons/fi'
import { cardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'
import RichTextEditor from './RichTextEditor'

const LABEL_COLORS = [
  '#ef4444', // red
  '#f97316', // orange
  '#eab308', // yellow
  '#22c55e', // green
  '#3b82f6', // blue
  '#8b5cf6', // purple
]

const CardModal = ({ cardId, onClose, isViewer = false }) => {
  const [isEditing, setIsEditing] = useState(false)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    dueDate: '',
    labels: [],
    reminderType: 'ONE_DAY_BEFORE'
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
      const reminderType =
        card.reminders && card.reminders.length > 0
          ? card.reminders[0].reminderType
          : 'ONE_DAY_BEFORE'
      setFormData({
        title: card.title,
        description: card.description || '',
        dueDate: card.dueDate || '',
        labels: card.labels || [],
        reminderType
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
      toast.success('Card updated successfully!', { id: 'card-updated' })
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
      toast.success('Comment added!', { id: 'comment-added' })
    }
  })

  const handleSave = (e) => {
    if (isViewer) return
    e.preventDefault()
    const reminderType =
      formData.dueDate && formData.reminderType
        ? formData.reminderType
        : formData.dueDate
          ? 'ONE_DAY_BEFORE'
          : null

    updateCardMutation.mutate({
      title: formData.title.trim(),
      description: formData.description || null,
      dueDate: formData.dueDate || null,
      reminderType,
      columnId: card.columnId,
      labels: formData.labels
    })
  }

  const handleDescriptionSave = (html) => {
    if (isViewer) return
    setFormData(prev => ({ ...prev, description: html }))
    updateCardMutation.mutate({
      title: formData.title.trim(),
      description: html || null,
      dueDate: formData.dueDate || null,
      reminderType: formData.reminderType,
      columnId: card.columnId,
      labels: formData.labels
    })
  }

  const handleAddComment = (e) => {
    if (isViewer) return
    e.preventDefault()
    if (newComment.trim()) {
      addCommentMutation.mutate(newComment.trim())
    }
  }

  const handleAddLabel = () => {
    if (isViewer) return
    if (newLabel.text.trim()) {
      setFormData(prev => ({
        ...prev,
        labels: [...prev.labels, { ...newLabel, text: newLabel.text.trim(), id: Date.now() }]
      }))
      setNewLabel({ color: LABEL_COLORS[0], text: '' })
    }
  }

  const handleRemoveLabel = (labelId) => {
    if (isViewer) return
    setFormData(prev => ({
      ...prev,
      labels: prev.labels.filter(label => label.id !== labelId)
    }))
  }

  if (!card) return null

  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-start justify-center z-50 overflow-y-auto py-10 transition-opacity">
      <div className="bg-[#f4f5f7] rounded-xl shadow-2xl max-w-3xl w-full mx-4 border border-gray-200 animate-scale-in flex flex-col relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-500 hover:text-gray-800 hover:bg-gray-200 p-2 rounded-full transition-colors duration-200 z-10"
        >
          <FiX className="h-5 w-5" />
        </button>

        <div className="p-8 pb-4">
          <div className="flex items-start gap-3 mb-6">
            <FiList className="h-6 w-6 text-gray-600 mt-1 flex-shrink-0" />
            <div className="flex-1">
              {isEditing ? (
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  className="w-full text-2xl font-bold bg-white px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-4 focus:ring-blue-100 focus:border-blue-500 transition-all shadow-sm"
                  autoFocus
                />
              ) : (
                <div className="flex items-center gap-3">
                  <h2 className="text-2xl font-bold text-gray-900 leading-tight tracking-tight">{card.title}</h2>
                  {isViewer && (
                    <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-gray-200/80 text-gray-700">
                      View only
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="flex flex-col md:flex-row gap-8">
            <div className="flex-1 space-y-8">
              {/* Status Row (Labels & Due Date viewing mode) */}
              {!isEditing && (
                <div className="flex flex-wrap items-start gap-8 ml-9">
                  {card.labels?.length > 0 && (
                    <div>
                      <h3 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Labels</h3>
                      <div className="flex flex-wrap gap-2">
                        {card.labels.map((label) => (
                          <span
                            key={label.id}
                            className="inline-block px-3 py-1 text-sm font-semibold rounded-md text-white shadow-sm"
                            style={{ backgroundColor: label.color }}
                          >
                            {label.text}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {card.dueDate && (
                    <div>
                      <h3 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Due Date</h3>
                      <div className="flex items-center text-sm text-gray-800 bg-gray-200/60 px-3 py-1.5 rounded-md font-medium">
                        <FiClock className="mr-2 h-4 w-4 text-gray-600" />
                        {new Date(card.dueDate).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Editing controls for Labels & Due Date */}
              {isEditing && (
                <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm ml-9 space-y-5">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    <div>
                      <label className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 block">Due Date</label>
                      <div className="flex items-center gap-2">
                        <div className="relative flex-1">
                          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <FiCalendar className="text-gray-400" />
                          </div>
                          <input
                            type="date"
                            value={formData.dueDate}
                            onChange={(e) => setFormData(prev => ({ ...prev, dueDate: e.target.value }))}
                            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-4 focus:ring-blue-100 focus:border-blue-500 text-sm transition-all"
                          />
                        </div>
                      </div>
                    </div>
                    <div>
                      <label className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 block">Reminder</label>
                      <select
                        value={formData.reminderType}
                        onChange={(e) => setFormData(prev => ({ ...prev, reminderType: e.target.value }))}
                        disabled={!formData.dueDate}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-4 focus:ring-blue-100 focus:border-blue-500 text-sm disabled:bg-gray-100 disabled:text-gray-400 disabled:border-gray-200 transition-all"
                      >
                        <option value="ONE_DAY_BEFORE">1 day before (9 AM)</option>
                        <option value="TWO_DAYS_BEFORE">2 days before (9 AM)</option>
                        <option value="ONE_WEEK_BEFORE">1 week before (9 AM)</option>
                        <option value="AT_DUE_TIME">At due time (9 AM)</option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <label className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 block">Labels</label>
                    <div className="flex flex-wrap gap-2 mb-3">
                      {formData.labels.map((label) => (
                        <span
                          key={label.id}
                          className="inline-flex items-center px-3 py-1 min-w-[50px] justify-between text-sm font-semibold rounded-md text-white cursor-pointer hover:opacity-90 shadow-sm transition-opacity"
                          style={{ backgroundColor: label.color }}
                          onClick={() => handleRemoveLabel(label.id)}
                        >
                          {label.text || '\u00A0'}
                          <FiX className="ml-2 h-3.5 w-3.5 opacity-80" />
                        </span>
                      ))}
                      {formData.labels.length === 0 && <span className="text-sm text-gray-400 italic">No labels added</span>}
                    </div>

                    <div className="flex items-center gap-2">
                      <select
                        value={newLabel.color}
                        onChange={(e) => setNewLabel(prev => ({ ...prev, color: e.target.value }))}
                        className="px-2 py-2 border border-gray-300 rounded-lg text-sm w-32 focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all font-semibold"
                        style={{ backgroundColor: newLabel.color, color: 'white' }}
                      >
                        {LABEL_COLORS.map(color => (
                          <option key={color} value={color} style={{ backgroundColor: color, color: 'white' }}>
                            Color
                          </option>
                        ))}
                      </select>
                      <input
                        type="text"
                        value={newLabel.text}
                        onChange={(e) => setNewLabel(prev => ({ ...prev, text: e.target.value }))}
                        placeholder="Label text..."
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm flex-1 focus:outline-none focus:ring-4 focus:ring-blue-100 focus:border-blue-500 transition-all"
                      />
                      <button
                        type="button"
                        onClick={handleAddLabel}
                        disabled={!newLabel.text.trim()}
                        className="px-4 py-2 bg-gray-800 text-white hover:bg-gray-900 rounded-lg text-sm font-semibold disabled:opacity-50 transition-colors shadow-sm"
                      >
                        Add
                      </button>
                    </div>
                  </div>
                </div>
              )}

              <div className="flex items-start gap-3">
                <FiAlignLeft className="h-6 w-6 text-gray-600 mt-1 flex-shrink-0" />
                <div className="flex-1 w-full relative">
                  <div className="flex items-center justify-between mb-3 mt-1">
                    <h3 className="text-lg font-semibold text-gray-900">Description</h3>
                  </div>
                  
                  <RichTextEditor 
                    content={formData.description}
                    onSave={handleDescriptionSave}
                    onCancel={() => {
                      setFormData(prev => ({ ...prev, description: card.description || '' }))
                    }}
                    placeholder="Add a more detailed description..."
                    editable={!isViewer}
                  />
                </div>
              </div>

              {/* Comments Section */}
              <div className="flex items-start gap-3 pt-6 border-t border-gray-200/70">
                <FiMessageSquare className="h-6 w-6 text-gray-600 mt-1 flex-shrink-0" />
                <div className="flex-1 w-full">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4 mt-1">Activity</h3>
                  
                  {isViewer ? (
                    <div className="bg-yellow-50 border border-yellow-200 p-4 rounded-xl text-sm text-yellow-800 mb-6 flex items-center shadow-sm">
                      <FiMessageSquare className="mr-2 h-5 w-5 text-yellow-600" />
                      You have view-only access. Comments and edits are disabled.
                    </div>
                  ) : (
                    <form onSubmit={handleAddComment} className="mb-8 bg-white p-2 rounded-xl border border-gray-300 focus-within:ring-4 focus-within:ring-blue-100 focus-within:border-blue-500 shadow-sm transition-all duration-200">
                      <textarea
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="Write a comment..."
                        className="w-full px-3 py-2 bg-transparent outline-none resize-y min-h-[60px] text-[15px] placeholder-gray-500 text-gray-800"
                      />
                      <div className="flex justify-end pt-2 pb-1 pr-1">
                        <button
                          type="submit"
                          disabled={!newComment.trim() || addCommentMutation.isPending}
                          className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold disabled:opacity-50 transition-colors shadow-sm focus:ring-4 focus:ring-blue-100"
                        >
                          Save comment
                        </button>
                      </div>
                    </form>
                  )}

                  <div className="space-y-4">
                    {comments?.length === 0 && (
                      <p className="text-gray-500 text-sm italic">No comments yet. Be the first to start the discussion!</p>
                    )}
                    {comments?.map((comment) => (
                      <div key={comment.id} className="flex gap-3">
                        <div className="h-9 w-9 bg-blue-100 text-blue-700 rounded-full flex items-center justify-center font-bold text-sm shrink-0 uppercase ring-2 ring-white shadow-sm">
                          {comment.author?.fullName?.charAt(0) || 'U'}
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-bold text-[14px] text-gray-900">
                              {comment.author?.fullName || 'Unknown User'}
                            </span>
                            <span className="text-xs font-medium text-gray-500">
                              {new Date(comment.createdAt).toLocaleString(undefined, {
                                month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit'
                              })}
                            </span>
                          </div>
                          <div className="bg-white p-3.5 rounded-b-xl rounded-tr-xl border border-gray-200 shadow-sm text-[15px] text-gray-800 whitespace-pre-wrap leading-relaxed">
                            {comment.content}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Right Sidebar */}
            <div className="w-full md:w-48 flex flex-col gap-6 shrink-0 mt-2 md:mt-0">
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">Actions</h4>
                <button
                  onClick={() => setIsEditing(true)}
                  disabled={isViewer}
                  className="w-full flex items-center px-4 py-2 bg-gray-200/70 hover:bg-gray-300 text-gray-800 rounded-md text-sm font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FiTag className="mr-2.5 h-4 w-4" />
                  Labels
                </button>
                <button
                  onClick={() => setIsEditing(true)}
                  disabled={isViewer}
                  className="w-full flex items-center px-4 py-2 bg-gray-200/70 hover:bg-gray-300 text-gray-800 rounded-md text-sm font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FiCalendar className="mr-2.5 h-4 w-4" />
                  Dates
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CardModal
