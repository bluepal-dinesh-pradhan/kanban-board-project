import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FiX, FiCalendar, FiTag, FiMessageSquare, FiSave, FiList, FiClock, FiAlignLeft, FiTrash2, FiEdit3, FiUser, FiCheckSquare, FiCopy, FiPaperclip, FiDownload } from 'react-icons/fi'
import { cardAPI, boardAPI } from '../api/endpoints'
import api from '../api/axios'
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
  const [activeTab, setActiveTab] = useState('details')
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

  const [newChecklistItem, setNewChecklistItem] = useState('')

  const { data: checklistItems } = useQuery({
    queryKey: ['card', cardId, 'checklists'],
    enabled: Boolean(cardId),
    queryFn: async () => {
      const response = await cardAPI.getChecklists(cardId)
      return response.data.data
    }
  })

  const addChecklistMutation = useMutation({
    mutationFn: async (title) => {
      const response = await cardAPI.addChecklistItem(cardId, title)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'checklists'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setNewChecklistItem('')
    }
  })

  const toggleChecklistMutation = useMutation({
    mutationFn: async (itemId) => {
      const response = await cardAPI.toggleChecklistItem(itemId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'checklists'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
    }
  })

  const deleteChecklistMutation = useMutation({
    mutationFn: async (itemId) => {
      const response = await cardAPI.deleteChecklistItem(itemId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'checklists'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
    }
  })

  const fileInputRef = useState(null)
  const { data: attachments } = useQuery({
    queryKey: ['card', cardId, 'attachments'],
    enabled: Boolean(cardId),
    queryFn: async () => {
      const response = await cardAPI.getAttachments(cardId)
      return response.data.data
    }
  })
  const uploadMutation = useMutation({
    mutationFn: async (file) => {
      const response = await cardAPI.uploadAttachment(cardId, file)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'attachments'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.success('File uploaded!', { id: 'file-uploaded' })
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to upload file')
    }
  })
  const deleteAttachmentMutation = useMutation({
    mutationFn: async (attachmentId) => {
      const response = await cardAPI.deleteAttachment(attachmentId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['card', cardId, 'attachments'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.success('Attachment deleted', { id: 'attachment-deleted' })
    }
  })
  const handleFileUpload = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        toast.error('File size must be under 10MB')
        return
      }
      uploadMutation.mutate(file)
    }
    e.target.value = '' // reset input
  }
  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B'
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }
  const getFileIcon = (fileType) => {
    if (!fileType) return '📄'
    if (fileType.startsWith('image/')) return '🖼️'
    if (fileType.includes('pdf')) return '📕'
    if (fileType.includes('word') || fileType.includes('document')) return '📘'
    if (fileType.includes('sheet') || fileType.includes('excel')) return '📗'
    if (fileType.includes('zip') || fileType.includes('archive')) return '📦'
    if (fileType.includes('text')) return '📝'
    return '📄'
  }
  const duplicateCardMutation = useMutation({
    mutationFn: async () => {
      const response = await cardAPI.duplicateCard(cardId)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.success('Card duplicated!', { id: 'card-duplicated' })
      onClose()
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to duplicate card')
    }
  })

  const checklistTotal = checklistItems?.length || 0
  const checklistCompleted = checklistItems?.filter(i => i.completed).length || 0
  const checklistPercent = checklistTotal > 0 ? Math.round((checklistCompleted / checklistTotal) * 100) : 0

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

  const tabs = [
    { id: 'details', label: 'Details', icon: <FiAlignLeft className="w-4 h-4" />, count: null },
    { id: 'checklist', label: 'Checklist', icon: <FiCheckSquare className="w-4 h-4" />, count: checklistTotal > 0 ? `${checklistCompleted}/${checklistTotal}` : null },
    { id: 'attachments', label: 'Attachments', icon: <FiPaperclip className="w-4 h-4" />, count: attachments?.length || 0 },
    { id: 'activity', label: 'Activity', icon: <FiMessageSquare className="w-4 h-4" />, count: comments?.length || 0 },
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white dark:bg-gray-900 w-full max-w-5xl rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden animate-in zoom-in-95 duration-200">
        
        {/* Header Area */}
        <div className="px-6 py-5 border-b border-gray-100 dark:border-gray-800 flex justify-between items-start bg-gray-50/30 dark:bg-gray-800/20">
          <div className="flex-1 mr-4">
            <div className="flex flex-wrap gap-2 mb-3">
              {formData.labels.map((label, index) => (
                <span
                  key={label.id || index}
                  className="px-2.5 py-1 rounded-full text-[10px] font-bold text-white shadow-sm uppercase tracking-wider"
                  style={{ backgroundColor: label.color }}
                >
                  {label.text}
                </span>
              ))}
              <span className="text-[10px] font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest bg-blue-50 dark:bg-blue-900/30 px-2.5 py-1 rounded-full border border-blue-100 dark:border-blue-800">
                {card.columnTitle}
              </span>
            </div>

            {isTitleEditing ? (
              <form onSubmit={(e) => { e.preventDefault(); handleTitleSave() }} className="flex items-center gap-2">
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  onKeyDown={(e) => {
                    if (e.key === 'Escape') {
                      setIsTitleEditing(false)
                      setFormData(prev => ({ ...prev, title: card.title }))
                    }
                  }}
                  className="text-2xl font-bold w-full px-3 py-1.5 border-2 border-blue-500 dark:bg-gray-800 dark:text-white rounded-xl outline-none"
                  autoFocus
                />
                <button type="submit" className="bg-blue-600 text-white p-2 rounded-xl hover:bg-blue-700 transition-colors">
                  <FiSave className="w-5 h-5" />
                </button>
              </form>
            ) : (
              <div className="flex items-center gap-2 group">
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white leading-tight">
                  {formData.title}
                </h2>
                {canEdit && (
                  <button
                    onClick={() => setIsTitleEditing(true)}
                    className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded-lg opacity-0 group-hover:opacity-100 transition-all"
                  >
                    <FiEdit3 className="h-5 w-5" />
                  </button>
                )}
              </div>
            )}
          </div>
          <button 
            onClick={onClose} 
            className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-all"
          >
            <FiX className="h-6 w-6" />
          </button>
        </div>

        {/* Tab Bar */}
        <div className="flex px-6 border-b border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 overflow-x-auto no-scrollbar">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-4 text-sm font-semibold border-b-2 transition-all shrink-0 ${
                activeTab === tab.id
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800'
              }`}
            >
              {tab.icon}
              {tab.label}
              {tab.count !== null && (
                <span className={`px-2 py-0.5 rounded-full text-[10px] ${
                  activeTab === tab.id ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 dark:bg-gray-800 text-gray-500'
                }`}>
                  {tab.count}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Main Content Area */}
        <div className="flex-1 flex overflow-hidden">
          {/* LEFT COLUMN (70%) - TAB CONTENT */}
          <div className="flex-1 overflow-y-auto p-8 custom-scrollbar bg-white dark:bg-gray-900">
            {activeTab === 'details' && (
              <div className="space-y-8 animate-in fade-in slide-in-from-left-4 duration-300">
                <div className="relative">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                       <FiAlignLeft className="w-5 h-5 text-blue-600" />
                       Description
                    </h3>
                    {canEdit && !isDescriptionEditing && (
                      <button
                        onClick={() => setIsDescriptionEditing(true)}
                        className="text-sm font-bold text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/30 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        Edit
                      </button>
                    )}
                  </div>
                  <div className="bg-gray-50 dark:bg-gray-800/50 rounded-2xl p-2 border border-gray-100 dark:border-gray-800">
                    <RichTextEditor 
                      content={formData.description}
                      onSave={handleDescriptionSave}
                      onCancel={() => {
                        setFormData(prev => ({ ...prev, description: card.description || '' }))
                        setIsDescriptionEditing(false)
                      }}
                      placeholder="Add a more detailed description..."
                      editable={canEdit}
                      allowClickToEdit={false}
                      forceEditing={isDescriptionEditing}
                      onEditingChange={setIsDescriptionEditing}
                    />
                  </div>
                </div>

                {/* Quick Label Add */}
                {canEdit && (
                  <div className="p-6 bg-blue-50 dark:bg-blue-900/10 rounded-2xl border border-blue-100 dark:border-blue-900/30">
                    <h4 className="text-sm font-bold text-blue-700 dark:text-blue-400 mb-3 uppercase tracking-wider">Quick label add</h4>
                    <div className="flex flex-col sm:flex-row gap-2">
                      <select
                        value={newLabel.color}
                        onChange={(e) => setNewLabel(prev => ({ ...prev, color: e.target.value }))}
                        className="px-3 py-2 border rounded-xl text-sm font-bold text-white shadow-sm outline-none shrink-0"
                        style={{ backgroundColor: newLabel.color }}
                      >
                        {LABEL_COLORS.map(color => (
                          <option key={color} value={color} style={{ backgroundColor: color }}>
                            Color
                          </option>
                        ))}
                      </select>
                      <input
                        type="text"
                        value={newLabel.text}
                        onChange={(e) => setNewLabel(prev => ({ ...prev, text: e.target.value }))}
                        placeholder="New label name..."
                        className="px-4 py-2 bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-xl text-sm flex-1 outline-none focus:ring-2 focus:ring-blue-500"
                      />
                      <button
                        onClick={handleAddLabel}
                        disabled={!newLabel.text.trim()}
                        className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold disabled:opacity-50 shadow-lg shadow-blue-200 dark:shadow-none transition-all"
                      >
                        Add Label
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'checklist' && (
              <div className="space-y-6 animate-in fade-in slide-in-from-left-4 duration-300">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                    <FiCheckSquare className="w-5 h-5 text-blue-600" />
                    Project Checklist
                  </h3>
                  <span className="text-sm font-bold text-blue-600 bg-blue-50 dark:bg-blue-900/30 px-3 py-1 rounded-full border border-blue-100 dark:border-blue-800">
                    {checklistPercent}% Complete
                  </span>
                </div>

                <div className="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-3 overflow-hidden border border-gray-200 dark:border-gray-700">
                  <div
                    className={`h-full transition-all duration-500 ease-out ${
                      checklistPercent === 100 ? 'bg-green-500' : 'bg-blue-600'
                    }`}
                    style={{ width: `${checklistPercent}%` }}
                  />
                </div>

                <div className="space-y-2">
                  {checklistItems?.map((item) => (
                    <div
                      key={item.id}
                      className={`flex items-center gap-3 p-4 bg-white dark:bg-gray-800/50 rounded-xl border border-gray-100 dark:border-gray-800 group transition-all hover:bg-gray-50 dark:hover:bg-gray-800 shadow-sm ${
                        item.completed ? 'opacity-60' : ''
                      }`}
                    >
                      <button
                        onClick={() => canEdit && toggleChecklistMutation.mutate(item.id)}
                        className={`h-6 w-6 rounded-lg border-2 flex items-center justify-center transition-all ${
                          item.completed 
                            ? 'bg-blue-600 border-blue-600' 
                            : 'border-gray-300 dark:border-gray-600 hover:border-blue-500'
                        }`}
                      >
                        {item.completed && <FiCheckSquare className="text-white w-4 h-4" />}
                      </button>
                      <span className={`flex-1 text-sm font-medium ${
                        item.completed ? 'line-through text-gray-400 dark:text-gray-500' : 'text-gray-700 dark:text-gray-200'
                      }`}>
                        {item.title}
                      </span>
                      {canEdit && (
                        <button
                          onClick={() => deleteChecklistMutation.mutate(item.id)}
                          className="p-1.5 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all rounded-lg"
                        >
                          <FiTrash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  ))}
                  {checklistTotal === 0 && (
                    <div className="text-center py-12 border-2 border-dashed border-gray-200 dark:border-gray-800 rounded-2xl">
                        <FiCheckSquare className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                        <p className="text-gray-400 font-medium italic">Your checklist is empty</p>
                    </div>
                  )}
                </div>

                {canEdit && (
                  <form
                    onSubmit={(e) => {
                      e.preventDefault()
                      if (newChecklistItem.trim()) {
                        addChecklistMutation.mutate(newChecklistItem.trim())
                      }
                    }}
                    className="flex gap-2"
                  >
                    <input
                      type="text"
                      value={newChecklistItem}
                      onChange={(e) => setNewChecklistItem(e.target.value)}
                      placeholder="Add a task to complete..."
                      className="flex-1 px-4 py-3 bg-gray-50 dark:bg-gray-800 border dark:border-gray-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                    />
                    <button
                      type="submit"
                      disabled={!newChecklistItem.trim() || addChecklistMutation.isPending}
                      className="px-6 py-3 bg-gray-900 dark:bg-gray-700 text-white rounded-xl text-sm font-bold disabled:opacity-50 hover:bg-black transition-all"
                    >
                      Add Task
                    </button>
                  </form>
                )}
              </div>
            )}

            {activeTab === 'attachments' && (
              <div className="space-y-6 animate-in fade-in slide-in-from-left-4 duration-300">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                    <FiPaperclip className="w-5 h-5 text-blue-600" />
                    File Repository
                  </h3>
                  {canEdit && (
                    <label className="cursor-pointer px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold transition-all shadow-lg shadow-blue-200 dark:shadow-none">
                      {uploadMutation.isPending ? 'Uploading...' : 'Upload File'}
                      <input
                        type="file"
                        className="hidden"
                        onChange={handleFileUpload}
                        disabled={uploadMutation.isPending}
                      />
                    </label>
                  )}
                </div>

                <div className="grid gap-3">
                  {attachments?.map((att) => (
                    <div
                      key={att.id}
                      className="flex items-center gap-4 p-4 bg-white dark:bg-gray-800/50 rounded-2xl border border-gray-100 dark:border-gray-800 group hover:border-blue-200 dark:hover:border-blue-900 shadow-sm transition-all"
                    >
                      <div className="h-12 w-12 rounded-xl bg-gray-50 dark:bg-gray-800 flex items-center justify-center text-3xl font-bold shadow-sm">
                        {getFileIcon(att.fileType)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="text-sm font-bold text-gray-900 dark:text-white truncate">
                          {att.fileName}
                        </div>
                        <div className="text-[11px] font-semibold text-gray-500 mt-1 uppercase tracking-wider">
                          {formatFileSize(att.fileSize)} • {att.uploadedByName}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={async () => {
                            try {
                              const response = await api.get(`/attachments/${att.id}/download`, {
                                responseType: 'blob'
                              })
                              const url = window.URL.createObjectURL(new Blob([response.data]))
                              const link = document.createElement('a')
                              link.href = url
                              link.setAttribute('download', att.fileName)
                              document.body.appendChild(link)
                              link.click()
                              link.remove()
                              window.URL.revokeObjectURL(url)
                            } catch (e) {
                              toast.error('Failed to download file')
                            }
                          }}
                          className="p-2 text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded-xl transition-all"
                          title="Download"
                        >
                          <FiDownload className="w-5 h-5" />
                        </button>
                        {canEdit && (
                          <button
                            onClick={() => {
                              if (window.confirm('Permanent removal?')) {
                                deleteAttachmentMutation.mutate(att.id)
                              }
                            }}
                            className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-xl transition-all"
                            title="Delete"
                          >
                            <FiTrash2 className="w-5 h-5" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                  {(!attachments || attachments.length === 0) && (
                    <div className="text-center py-12 border-2 border-dashed border-gray-200 dark:border-gray-800 rounded-2xl">
                        <FiPaperclip className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                        <p className="text-gray-400 font-medium italic">No files attached to this card</p>
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === 'activity' && (
              <div className="space-y-8 animate-in fade-in slide-in-from-left-4 duration-300">
                <header>
                  <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2 mb-1">
                    <FiMessageSquare className="w-5 h-5 text-blue-600" />
                    Discussion
                  </h3>
                  <p className="text-xs text-gray-500 font-medium">Keep your team updated on the progress</p>
                </header>
                
                <form onSubmit={handleAddComment} className="bg-white dark:bg-gray-800 p-3 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-xl shadow-gray-100 dark:shadow-none focus-within:ring-2 focus-within:ring-blue-500 transition-all">
                  <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="Share your thoughts..."
                    className="w-full px-4 py-3 bg-transparent outline-none resize-none min-h-[100px] text-[15px] dark:text-white"
                  />
                  <div className="flex justify-end pt-3 border-t dark:border-gray-700 mt-2">
                    <button
                      type="submit"
                      disabled={!newComment.trim() || addCommentMutation.isPending}
                      className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-xl text-sm font-bold disabled:opacity-50 transition-all shadow-lg shadow-blue-200 dark:shadow-none"
                    >
                      {addCommentMutation.isPending ? 'Sending...' : 'Post Comment'}
                    </button>
                  </div>
                </form>

                <div className="space-y-6">
                  {comments?.map((comment) => (
                    <div key={comment.id} className="flex gap-4 group">
                      <div className="h-10 w-10 bg-gradient-to-br from-blue-600 to-indigo-700 text-white rounded-xl flex items-center justify-center font-bold text-sm shrink-0 shadow-md">
                        {comment.author?.fullName?.charAt(0) || 'U'}
                      </div>
                      <div className="flex-1 bg-gray-50 dark:bg-gray-800/40 p-4 rounded-2xl border border-gray-100 dark:border-gray-800 relative">
                        <div className="flex items-center gap-3 mb-1.5">
                          <span className="font-bold text-sm text-gray-900 dark:text-gray-100">
                            {comment.author?.fullName || 'Anonymous'}
                          </span>
                          <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                            {new Date(comment.createdAt).toLocaleDateString()}
                          </span>
                        </div>
                        <p className="text-gray-700 dark:text-gray-300 text-[14px] leading-relaxed whitespace-pre-wrap">
                          {comment.content}
                        </p>
                        {(isOwner || comment.author?.id === user?.id) && (
                          <button
                            onClick={() => handleDeleteComment(comment.id)}
                            className="absolute top-4 right-4 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all"
                          >
                            <FiTrash2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                  {comments?.length === 0 && (
                    <div className="text-center py-10">
                        <p className="text-gray-400 text-sm italic">No discussion yet</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* RIGHT COLUMN (30%) - SIDEBAR (ALWAYS VISIBLE) */}
          <div className="w-80 border-l border-gray-50 dark:border-gray-800 bg-gray-50/30 dark:bg-gray-900/50 p-6 overflow-y-auto no-scrollbar space-y-8">
            {/* Priority */}
            <div>
              <h4 className="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest mb-3">Priority Status</h4>
              <div className="grid grid-cols-1 gap-1.5">
                {PRIORITY_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    onClick={() => canEdit && updateCardMutation.mutate({ ...formData, priority: opt.value })}
                    className={`flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-bold transition-all border-2 ${
                      formData.priority === opt.value
                        ? 'bg-white dark:bg-gray-800 border-gray-900 dark:border-white text-gray-900 dark:text-white shadow-sm'
                        : 'bg-transparent border-transparent text-gray-500 hover:bg-white dark:hover:bg-gray-800 hover:text-gray-700 dark:hover:text-gray-300'
                    }`}
                  >
                    <div className={`h-2.5 w-2.5 rounded-full ${opt.dot}`} />
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Assignee */}
            <div>
              <h4 className="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest mb-3">Assignee</h4>
              <div className="space-y-3">
                <div className="flex items-center gap-3 p-3 bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm">
                  <div className="h-9 w-9 rounded-xl bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 flex items-center justify-center font-bold text-sm">
                    {card.assigneeName?.charAt(0) || <FiUser />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-bold text-gray-900 dark:text-white truncate">
                      {card.assigneeName || 'Unassigned'}
                    </div>
                    {card.assigneeEmail && (
                      <div className="text-[10px] text-gray-400 truncate">{card.assigneeEmail}</div>
                    )}
                  </div>
                </div>
                {canEdit && (
                  <select
                    value={card.assigneeId || ''}
                    onChange={(e) => assignCardMutation.mutate(e.target.value ? Number(e.target.value) : null)}
                    className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl text-sm font-semibold outline-none focus:ring-2 focus:ring-blue-500 transition-all shadow-sm"
                  >
                    <option value="">Assign to member...</option>
                    {membersData?.members?.map(m => (
                      <option key={m.user.id} value={m.user.id}>{m.user.fullName}</option>
                    ))}
                  </select>
                )}
              </div>
            </div>

            {/* Labels Manager */}
            <div>
              <h4 className="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest mb-3">Labels Manager</h4>
              <div className="space-y-3">
                <div className="flex flex-wrap gap-2">
                  {formData.labels.map((label, index) => (
                    <div key={label.id || index} className="group relative">
                      <span
                        className="px-3 py-1.5 rounded-lg text-[10px] font-bold text-white shadow-sm uppercase"
                        style={{ backgroundColor: label.color }}
                      >
                        {label.text}
                      </span>
                      {canEdit && (
                        <button
                          onClick={() => handleRemoveLabel(label.id)}
                          className="absolute -top-1.5 -right-1.5 bg-white dark:bg-gray-800 text-gray-400 rounded-full p-0.5 shadow-md opacity-0 group-hover:opacity-100 transition-all hover:text-red-500"
                        >
                          <FiX className="w-3 h-3" />
                        </button>
                      )}
                    </div>
                  ))}
                  {formData.labels.length === 0 && <span className="text-xs text-gray-400 italic">No labels</span>}
                </div>
              </div>
            </div>

            {/* Due Date & Reminder */}
            <div>
              <h4 className="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest mb-3">Schedule</h4>
              <div className="space-y-4">
                <div className="space-y-2">
                    <label className="text-[10px] font-bold text-gray-500 uppercase flex items-center gap-1.5">
                        <FiCalendar className="w-3 h-3" /> Deadline
                    </label>
                    <div className="flex flex-col gap-2">
                        <input
                            type="date"
                            value={dueDateDraft}
                            onChange={handleDueDateChange}
                            disabled={!canEdit}
                            className={`w-full px-3 py-2 bg-white dark:bg-gray-800 border rounded-xl text-xs font-bold outline-none transition-all ${
                                isDueDateEditing 
                                    ? 'border-blue-500 ring-4 ring-blue-50' 
                                    : 'border-gray-200 dark:border-gray-700'
                            }`}
                        />
                        {isDueDateEditing && (
                            <div className="grid grid-cols-2 gap-2">
                                <button
                                    onClick={handleSetDueDate}
                                    className="px-3 py-2 bg-blue-600 text-white rounded-lg text-xs font-bold hover:bg-blue-700"
                                >
                                    Apply
                                </button>
                                <button
                                    onClick={handleCancelDueDate}
                                    className="px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-lg text-xs font-bold"
                                >
                                    Reset
                                </button>
                            </div>
                        )}
                        {!isDueDateEditing && formData.dueDate && canEdit && (
                             <button
                                onClick={handleRemoveDueDate}
                                className="text-[10px] font-bold text-red-500 hover:text-red-700 text-left px-1"
                             >
                                Clear deadline
                             </button>
                        )}
                    </div>
                </div>

                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-gray-500 uppercase flex items-center gap-1.5">
                    <FiClock className="w-3 h-3" /> Alert Reminder
                  </label>
                  <select
                    value={formData.reminderType}
                    onChange={handleReminderChange}
                    disabled={!canEdit}
                    className="w-full px-3 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl text-xs font-bold outline-none focus:ring-2 focus:ring-blue-500 shadow-sm"
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
            </div>

            {/* Actions */}
            {canEdit && !isViewer && (
              <div className="pt-6 border-t border-gray-100 dark:border-gray-800">
                <h4 className="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest mb-3">Card Control</h4>
                <div className="space-y-2">
                  <button
                    onClick={() => duplicateCardMutation.mutate()}
                    className="w-full flex items-center gap-3 px-3 py-2 text-xs font-bold text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl transition-all"
                  >
                    <FiCopy className="w-4 h-4" /> Duplicate
                  </button>
                  <button
                    onClick={() => archiveCardMutation.mutate()}
                    className="w-full flex items-center gap-3 px-3 py-2 text-xs font-bold text-gray-600 dark:text-gray-400 hover:bg-amber-50 dark:hover:bg-amber-900/10 hover:text-amber-600 rounded-xl transition-all"
                  >
                    <FiCopy className="w-4 h-4" /> Archive
                  </button>
                  <button
                    onClick={handleDeleteCard}
                    className="w-full flex items-center gap-3 px-3 py-2 text-xs font-bold text-red-500 hover:bg-red-50 dark:hover:bg-red-900/10 rounded-xl transition-all"
                  >
                    <FiTrash2 className="w-4 h-4" /> Delete Card
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

export default CardModal
