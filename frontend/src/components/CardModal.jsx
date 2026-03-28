import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiCalendar, FiTag, FiMessageSquare, FiSave, FiList, FiClock, FiAlignLeft, FiTrash2, FiEdit3, FiUser } from 'react-icons/fi'
import { cardAPI, boardAPI } from '../api/endpoints'
import { useAuth } from '../context/AuthContext'
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

const PRIORITY_OPTIONS = [
  { value: 'URGENT', label: 'Urgent', color: '#dc2626', dot: 'bg-red-500' },
  { value: 'HIGH', label: 'High', color: '#f97316', dot: 'bg-orange-500' },
  { value: 'MEDIUM', label: 'Medium', color: '#eab308', dot: 'bg-yellow-500' },
  { value: 'LOW', label: 'Low', color: '#3b82f6', dot: 'bg-blue-500' },
  { value: 'NONE', label: 'None', color: '#9ca3af', dot: 'bg-gray-400' },
]

const CardModal = ({ cardId, boardId, onClose, isOwner = false, isEditor = false, isViewer = false, canEdit: canEditProp }) => {
  const canEdit = typeof canEditProp === 'boolean' ? canEditProp : (isOwner || isEditor)
  const { user } = useAuth()
  const [isTitleEditing, setIsTitleEditing] = useState(false)
  const [isDescriptionEditing, setIsDescriptionEditing] = useState(false)
  const [isDueDateEditing, setIsDueDateEditing] = useState(false)
  const [dueDateDraft, setDueDateDraft] = useState('')
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

  const toDateTimeLocal = (dateValue) => {
    if (!dateValue) return ''
    if (dateValue.includes('T')) return dateValue.slice(0, 16)
    return `${dateValue}T00:00`
  }

  const toLocalDate = (dateValue) => {
    if (!dateValue) return null
    return dateValue.split('T')[0]
  }

  const { data: card } = useQuery({
    queryKey: ['card', cardId],
    enabled: Boolean(cardId),
    queryFn: async () => {
      const response = await cardAPI.getCard(cardId)
      return response.data.data
    }
  })

  const { data: comments } = useQuery({
    queryKey: ['card', cardId, 'comments'],
    enabled: Boolean(cardId),
    queryFn: async () => {
      const response = await cardAPI.getComments(cardId)
      return response.data.data
    }
  })

  const { data: membersData } = useQuery({
    queryKey: ['boardMembers', boardId],
    queryFn: async () => {
      const response = await boardAPI.getBoardMembers(boardId)
      return response.data.data
    }
  })

  const assignCardMutation = useMutation({
    mutationFn: async (assigneeId) => {
      const response = await cardAPI.assignCard(cardId, assigneeId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.success('Card assigned!', { id: 'card-assigned' })
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to assign card')
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
        reminderType,
        priority: card.priority || 'NONE'
      })
      setDueDateDraft(toDateTimeLocal(card.dueDate || ''))
      setIsDueDateEditing(false)
    }
  }, [card])

  const updateCardMutation = useMutation({
    mutationFn: async (cardData) => {
      const response = await cardAPI.updateCard(cardId, cardData)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      queryClient.invalidateQueries(['boards'])
      setIsTitleEditing(false)
      setIsDescriptionEditing(false)
      toast.success('Card updated successfully!', { id: 'card-updated' })
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to update card', { id: 'card-update-error' })
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

  const archiveCardMutation = useMutation({
    mutationFn: async () => {
      const response = await cardAPI.archiveCard(cardId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      queryClient.invalidateQueries(['boards'])
      toast.success('Card archived!', { id: 'card-archived' })
      onClose()
    }
  })

  const deleteCardMutation = useMutation({
    mutationFn: async () => {
      const response = await cardAPI.deleteCard(cardId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ['card', cardId] })
      queryClient.removeQueries({ queryKey: ['card', cardId, 'comments'] })
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      queryClient.invalidateQueries(['boards'])
      toast.success('Card deleted', { id: 'card-deleted' })
      onClose()
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to delete card', { id: 'card-delete-error' })
    }
  })

  const deleteCommentMutation = useMutation({
    mutationFn: async (commentId) => {
      const response = await cardAPI.deleteComment(boardId, cardId, commentId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'comments'])
      toast.success('Comment deleted', { id: 'comment-deleted' })
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to delete comment')
    }
  })

  const handleDeleteComment = (commentId) => {
    if (window.confirm('Are you sure you want to delete this comment?')) {
      deleteCommentMutation.mutate(commentId)
    }
  }

  const handleTitleSave = () => {
    if (!canEdit) return
    updateCardMutation.mutate(formData)
  }

  const handleAddComment = (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    addCommentMutation.mutate(newComment)
  }

  const handleRemoveLabel = (labelId) => {
    if (!canEdit) return
    if (window.confirm('Are you sure you want to remove this label?')) {
      const newLabels = formData.labels.filter(l => l.id !== labelId)
      setFormData(prev => ({ ...prev, labels: newLabels }))
      updateCardMutation.mutate({ ...formData, labels: newLabels })
    }
  }

  const handleAddLabel = () => {
    if (!canEdit) return
    if (!newLabel.text.trim()) return
    const newLabels = [...formData.labels, { ...newLabel }]
    setFormData(prev => ({ ...prev, labels: newLabels }))
    updateCardMutation.mutate({ ...formData, labels: newLabels })
    setNewLabel({ color: LABEL_COLORS[0], text: '' })
  }

  const handleDescriptionSave = (content) => {
    if (!canEdit) return
    setFormData(prev => ({ ...prev, description: content }))
    updateCardMutation.mutate({ ...formData, description: content })
  }

  const handleDueDateChange = (e) => {
    if (!canEdit) return
    const newDate = e.target.value
    setDueDateDraft(newDate)
    setIsDueDateEditing(true)
  }

  const handleRemoveDueDate = () => {
    if (!canEdit) return
    setDueDateDraft('')
    setIsDueDateEditing(true)
  }

  const handleSetDueDate = () => {
    if (!canEdit) return
    const payloadDate = toLocalDate(dueDateDraft)
    setFormData(prev => ({ ...prev, dueDate: payloadDate || '' }))
    updateCardMutation.mutate({ ...formData, dueDate: payloadDate })
    setIsDueDateEditing(false)
  }

  const handleCancelDueDate = () => {
    setDueDateDraft(toDateTimeLocal(formData.dueDate || ''))
    setIsDueDateEditing(false)
  }

  const handleReminderChange = (e) => {
    if (!canEdit) return
    const newReminder = e.target.value
    setFormData(prev => ({ ...prev, reminderType: newReminder }))
    updateCardMutation.mutate({ ...formData, reminderType: newReminder })
  }

  const handleDeleteCard = () => {
    if (!canEdit) return
    if (window.confirm('Are you sure you want to delete this card? This action cannot be undone.')) {
      deleteCardMutation.mutate()
    }
  }

  if (!card) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-2xl rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="px-8 py-6 border-b border-gray-100 flex justify-between items-start bg-gray-50/50">
          <div className="flex-1 mr-4">
            <div className="flex items-center gap-2 mb-2">
              <FiList className="h-5 w-5 text-blue-600" />
              <span className="text-xs font-bold text-blue-600 uppercase tracking-wider">In column {card.columnTitle}</span>
            </div>
            {isTitleEditing ? (
              <form onSubmit={(e) => { e.preventDefault(); handleTitleSave() }} className="space-y-3">
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  onKeyDown={(e) => {
                    if (e.key === 'Escape') {
                      setIsTitleEditing(false)
                      setFormData(prev => ({ ...prev, title: card.title }))
                    }
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      handleTitleSave()
                    }
                  }}
                  className="text-2xl font-bold w-full px-3 py-2 border-2 border-blue-500 rounded-xl outline-none shadow-sm shadow-blue-50"
                  autoFocus
                />
                <div className="flex gap-2">
                  <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm">Save</button>
                  <button type="button" onClick={() => {
                    setIsTitleEditing(false)
                    setFormData(prev => ({ ...prev, title: card.title }))
                  }} className="text-gray-500 hover:text-gray-700 font-medium px-4 py-2 rounded-lg hover:bg-gray-100 transition-colors">Cancel</button>
                </div>
              </form>
            ) : (
              <div className="flex items-start gap-2">
                <h2 className="text-2xl font-bold text-gray-900 leading-tight px-1 rounded transition-colors">
                  {formData.title}
                </h2>
                {canEdit && (
                  <button
                    onClick={() => setIsTitleEditing(true)}
                    className="mt-1 p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Edit title"
                  >
                    <FiEdit3 className="h-4 w-4" />
                  </button>
                )}
              </div>
            )}
          </div>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-all">
            <FiX className="h-6 w-6" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto px-8 py-6 space-y-8 custom-scrollbar">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="md:col-span-2 space-y-8">
              {/* Labels Section */}
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <FiTag className="h-6 w-6 text-gray-600" />
                  <h3 className="text-lg font-semibold text-gray-900">Labels</h3>
                </div>
                <div className="pl-9 space-y-4">
                  <div className="flex flex-wrap gap-2">
                    {formData.labels.map((label, index) => (
                      <div
                        key={label.id || index}
                        className="group relative inline-flex items-center"
                      >
                        <span
                          className="px-3 py-1.5 rounded-lg text-xs font-bold text-white shadow-sm transition-transform group-hover:scale-105"
                          style={{ backgroundColor: label.color }}
                        >
                          {label.text}
                        </span>
                        {canEdit && (
                          <button
                            onClick={() => handleRemoveLabel(label.id)}
                            className="absolute -top-1 -right-1 bg-white text-gray-500 rounded-full p-0.5 shadow-md opacity-0 group-hover:opacity-100 transition-all hover:text-red-500"
                          >
                            <FiX className="w-3 h-3" />
                          </button>
                        )}
                      </div>
                    ))}
                    {formData.labels.length === 0 && <span className="text-sm text-gray-400 italic">No labels added</span>}
                  </div>

                  {canEdit && (
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
                  )}
                </div>
              </div>

              <div className="flex items-start gap-3">
                <FiAlignLeft className="h-6 w-6 text-gray-600 mt-1 flex-shrink-0" />
                <div className="flex-1 w-full relative">
                <div className="flex items-center justify-between mb-3 mt-1">
                    <h3 className="text-lg font-semibold text-gray-900">Description</h3>
                    {canEdit && !isDescriptionEditing && (
                      <button
                        onClick={() => setIsDescriptionEditing(true)}
                        className="text-sm font-semibold text-blue-600 hover:text-blue-700 px-3 py-1.5 rounded-lg hover:bg-blue-50 transition-colors"
                      >
                        Edit
                      </button>
                    )}
                  </div>
                  
                  <RichTextEditor 
                    content={formData.description}
                    onSave={handleDescriptionSave}
                    onCancel={() => {
                      setFormData(prev => ({ ...prev, description: card.description || '' }))
                    }}
                    placeholder="Add a more detailed description..."
                    editable={canEdit}
                    allowClickToEdit={false}
                    forceEditing={isDescriptionEditing}
                    onEditingChange={setIsDescriptionEditing}
                  />
                  {canEdit && isDescriptionEditing && (
                    <div className="text-xs text-gray-400 mt-2">
                      Tip: Use the toolbar above to format text, then click Save.
                    </div>
                  )}
                </div>
              </div>

              {/* Comments Section */}
              <div className="flex items-start gap-3 pt-6 border-t border-gray-200/70">
                <FiMessageSquare className="h-6 w-6 text-gray-600 mt-1 flex-shrink-0" />
                <div className="flex-1 w-full">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4 mt-1">Activity</h3>
                  
                  {isViewer && (
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-xl text-sm text-slate-500 mb-6 flex items-center">
                      <FiMessageSquare className="mr-2 h-5 w-5 text-slate-400" />
                      You are in view-only mode, but you can still participate in the discussion.
                    </div>
                  )}
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
                            {(isOwner || comment.author?.id === user?.id) && (
                              <button
                                onClick={() => handleDeleteComment(comment.id)}
                                disabled={deleteCommentMutation.isPending}
                                className="ml-auto p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
                                title="Delete comment"
                              >
                                <FiTrash2 className="h-3.5 w-3.5" />
                              </button>
                            )}
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

            <div className="space-y-6">
              <div className="bg-gray-50 rounded-xl p-5 border border-gray-100 space-y-6">
                {/* Priority Section */}
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-gray-600 mb-2">
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
                    </svg>
                    <span className="text-xs font-bold uppercase tracking-wider">Priority</span>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {PRIORITY_OPTIONS.map((opt) => (
                      <button
                        key={opt.value}
                        type="button"
                        onClick={() => {
                          if (!canEdit) return
                          setFormData(prev => ({ ...prev, priority: opt.value }))
                          updateCardMutation.mutate({ ...formData, priority: opt.value })
                        }}
                        disabled={!canEdit}
                        className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold border transition-all ${
                          formData.priority === opt.value
                            ? 'ring-2 ring-offset-1 shadow-sm'
                            : 'opacity-60 hover:opacity-100'
                        } ${!canEdit ? 'cursor-default' : 'cursor-pointer'}`}
                        style={{
                          backgroundColor: formData.priority === opt.value ? opt.color + '18' : 'transparent',
                          borderColor: formData.priority === opt.value ? opt.color : '#e5e7eb',
                          color: opt.color,
                          ringColor: opt.color,
                        }}
                      >
                        <span className={`w-2 h-2 rounded-full ${opt.dot}`}></span>
                        {opt.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Assignee Section */}
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-gray-600 mb-2">
                    <FiUser className="w-4 h-4" />
                    <span className="text-xs font-bold uppercase tracking-wider">Assignee</span>
                    {canEdit && card?.assigneeId && (
                      <button
                        type="button"
                        onClick={() => assignCardMutation.mutate(null)}
                        className="ml-auto text-xs font-semibold text-red-500 hover:text-red-600 hover:underline"
                      >
                        Remove
                      </button>
                    )}
                  </div>
                  {card?.assigneeId ? (
                    <div className="flex items-center gap-2.5 p-2.5 bg-white rounded-lg border border-gray-200">
                      <div className="h-8 w-8 rounded-full bg-gradient-to-br from-blue-600 to-purple-600 flex items-center justify-center text-white text-xs font-bold uppercase">
                        {card.assigneeName?.charAt(0) || 'U'}
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-gray-900">{card.assigneeName}</div>
                        <div className="text-xs text-gray-500">{card.assigneeEmail}</div>
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-gray-400 italic">No one assigned</div>
                  )}
                  {canEdit && (
                    <select
                      value={card?.assigneeId || ''}
                      onChange={(e) => {
                        const val = e.target.value
                        assignCardMutation.mutate(val ? Number(val) : null)
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all font-medium bg-white mt-1"
                    >
                      <option value="">Select member...</option>
                      {membersData?.members?.map((member) => (
                        <option key={member.user.id} value={member.user.id}>
                          {member.user.fullName}
                        </option>
                      ))}
                    </select>
                  )}
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-gray-600 mb-2">
                    <FiCalendar className="w-4 h-4" />
                    <span className="text-xs font-bold uppercase tracking-wider">Due Date</span>
                    {canEdit && formData.dueDate && (
                      <button
                        type="button"
                        onClick={handleRemoveDueDate}
                        className="ml-auto text-xs font-semibold text-red-500 hover:text-red-600 hover:underline"
                      >
                        Remove
                      </button>
                    )}
                  </div>
                  <input
                    type="datetime-local"
                    value={dueDateDraft}
                    onChange={handleDueDateChange}
                    readOnly={!canEdit}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all font-medium"
                  />
                  {canEdit && isDueDateEditing && (
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={handleSetDueDate}
                        className="px-3 py-1.5 bg-blue-600 text-white text-xs font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        Set
                      </button>
                      <button
                        type="button"
                        onClick={handleCancelDueDate}
                        className="px-3 py-1.5 text-gray-600 text-xs font-semibold hover:bg-gray-100 rounded-lg transition-colors"
                      >
                        Cancel
                      </button>
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-gray-600 mb-2">
                    <FiClock className="w-4 h-4" />
                    <span className="text-xs font-bold uppercase tracking-wider">Reminder</span>
                  </div>
                  <select
                    value={formData.reminderType}
                    onChange={handleReminderChange}
                    disabled={!canEdit}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all font-medium bg-white"
                  >
                    <option value="NONE">None</option>
                    <option value="AT_TIME">At time of due date</option>
                    <option value="FIVE_MIN_BEFORE">5 minutes before</option>
                    <option value="TEN_MIN_BEFORE">10 minutes before</option>
                    <option value="FIFTEEN_MIN_BEFORE">15 minutes before</option>
                    <option value="ONE_HOUR_BEFORE">1 hour before</option>
                    <option value="TWO_HOURS_BEFORE">2 hours before</option>
                    <option value="ONE_DAY_BEFORE">1 day before</option>
                    <option value="TWO_DAYS_BEFORE">2 days before</option>
                  </select>
                </div>
              </div>

              {canEdit && !isViewer && (
                <div className="space-y-4">
                  <div className="text-xs font-bold text-gray-400 uppercase tracking-widest px-1">Actions</div>
                  <button
                    onClick={() => archiveCardMutation.mutate()}
                    className="w-full flex items-center gap-2 px-4 py-2.5 bg-gray-50 text-gray-700 hover:bg-red-50 hover:text-red-700 rounded-xl text-sm font-semibold border border-gray-200 hover:border-red-200 transition-all shadow-sm"
                  >
                    <FiTrash2 className="w-4 h-4" />
                    Archive Card
                  </button>
                  <button
                    onClick={handleDeleteCard}
                    className="w-full flex items-center gap-2 px-4 py-2.5 bg-red-600 text-white hover:bg-red-700 rounded-xl text-sm font-semibold transition-all shadow-sm"
                  >
                    <FiTrash2 className="w-4 h-4" />
                    Delete Card
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CardModal
