import { useState, useEffect, useMemo, lazy, Suspense } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'
import { FiPlus, FiUsers, FiActivity, FiMoreHorizontal, FiCalendar, FiMessageSquare, FiX, FiStar, FiFilter, FiSearch } from 'react-icons/fi'
import { boardAPI, columnAPI, cardAPI } from '../api/endpoints'
import { getBoardGradient } from '../utils/colors'
import { timeAgo } from '../utils/timeAgo'
import ActivityFeed from '../components/ActivityFeed'
import Skeleton from '../components/common/Skeleton'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import toast from 'react-hot-toast'

const CardModal = lazy(() => import('../components/CardModal'))
const InviteModal = lazy(() => import('../components/InviteModal'))
const CreateBoardModal = lazy(() => import('../components/CreateBoardModal'))

import { usePermissions } from '../hooks/usePermissions'

const stripHtml = (html) => {
  if (!html) return '';
  return html.replace(/<[^>]*>/g, '').trim();
};

const BoardPage = () => {
  const { boardId } = useParams()
  const [showCreateCard, setShowCreateCard] = useState(null)
  const [showCreateColumn, setShowCreateColumn] = useState(false)
  const [showActivityFeed, setShowActivityFeed] = useState(false)
  const [showInviteModal, setShowInviteModal] = useState(false)
  const [showCreateBoardModal, setShowCreateBoardModal] = useState(false)
  const [showFilterPanel, setShowFilterPanel] = useState(false)
  const [selectedCard, setSelectedCard] = useState(null)
  const [renamingColumnId, setRenamingColumnId] = useState(null)
  const [renamedColumnTitle, setRenamedColumnTitle] = useState('')
  const [isRenamingBoard, setIsRenamingBoard] = useState(false)
  const [renamedBoardTitle, setRenamedBoardTitle] = useState('')
  const [isDragging, setIsDragging] = useState(false)
  const navigate = useNavigate();

  const queryClient = useQueryClient()

  const { data: boards } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    },
    refetchInterval: isDragging ? false : 15000
  })

  const { data: columns, isLoading } = useQuery({
    queryKey: ['board', boardId, 'columns'],
    queryFn: async () => {
      const response = await boardAPI.getBoardColumns(boardId)
      return response.data.data
    },
    refetchInterval: isDragging ? false : 15000
  })

  const { data: membersData } = useQuery({
    queryKey: ['boardMembers', boardId],
    queryFn: async () => {
      const response = await boardAPI.getBoardMembers(boardId)
      return response.data.data
    },
    refetchInterval: isDragging ? false : 15000
  })

  const { isOwner, isEditor, isViewer, canEdit, canInvite, isLoading: permissionsLoading } = usePermissions(boardId)
  
  const board = boards?.find((item) => String(item.id) === String(boardId))
  const boardTitle = board?.title || 'Board'
  const boardBg = getBoardGradient(board?.background || '#0079BF')
  const memberCount = membersData?.members?.length || 1
  const memberLabel = memberCount === 1 ? 'member' : 'members'

  const handleRenameBoard = async () => {
    if (!renamedBoardTitle.trim() || renamedBoardTitle === boardTitle) {
      return setIsRenamingBoard(false)
    }
    try {
      await boardAPI.updateBoard(boardId, { title: renamedBoardTitle.trim() })
      queryClient.invalidateQueries(['boards'])
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.success('Board renamed')
      setIsRenamingBoard(false)
    } catch (e) {
      toast.error('Failed to rename board')
    }
  }

  const handleRenameColumn = async (columnId) => {
    if (!renamedColumnTitle.trim()) return setRenamingColumnId(null)
    try {
      await columnAPI.updateColumn(columnId, { title: renamedColumnTitle.trim() })
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setRenamingColumnId(null)
    } catch (e) {
      toast.error('Failed to rename column')
    }
  }

  const handleDeleteColumn = async (columnId, colTitle) => {
    if (window.confirm(`Delete "${colTitle}" and all its cards? This cannot be undone.`)) {
      try {
        await columnAPI.deleteColumn(columnId)
        queryClient.invalidateQueries(['board', boardId, 'columns'])
        toast.success('Column deleted')
      } catch (e) {
        toast.error('Failed to delete column')
      }
    }
  }

  const deleteBoardMutation = useMutation({
    mutationFn: async () => {
      return boardAPI.deleteBoard(boardId);
    },
    onSuccess: () => {
      toast.success('Board deleted');
      navigate('/boards');
    },
    onError: (error) => {
      toast.error(error?.response?.data?.message || 'Failed to delete board');
    }
  });

  const handleDeleteBoard = () => {
    if (window.confirm('Delete this board? All columns, cards, and data will be permanently deleted. This cannot be undone.')) {
      deleteBoardMutation.mutate();
    }
  };

  const [columnTitle, setColumnTitle] = useState('')
  const [cardTitle, setCardTitle] = useState('')
  const [filterKeyword, setFilterKeyword] = useState('')
  const [selectedLabels, setSelectedLabels] = useState([])
  const [selectedDueDates, setSelectedDueDates] = useState([])
  const [selectedMembers, setSelectedMembers] = useState([])

  const labelKey = (label) => `${label.color || ''}|${label.text || ''}`

  const availableLabels = useMemo(() => {
    const map = new Map()
    columns?.forEach((column) => {
      column.cards?.forEach((card) => {
        card.labels?.forEach((label) => {
          const key = labelKey(label)
          if (!map.has(key)) {
            map.set(key, label)
          }
        })
      })
    })
    return Array.from(map.values())
  }, [columns])

  const activeFilterCount = [
    filterKeyword.trim().length > 0,
    selectedLabels.length > 0,
    selectedDueDates.length > 0,
    selectedMembers.length > 0
  ].filter(Boolean).length

  const matchesDueFilter = (card) => {
    if (selectedDueDates.length === 0) return true
    const today = new Date()
    const dueDate = card.dueDate ? new Date(card.dueDate) : null

    return selectedDueDates.some((filter) => {
      if (filter === 'noDates') {
        return !dueDate
      }
      if (!dueDate) return false

      const diffMs = dueDate.setHours(0, 0, 0, 0) - today.setHours(0, 0, 0, 0)
      const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24))

      if (filter === 'overdue') return diffDays < 0
      if (filter === 'nextDay') return diffDays >= 0 && diffDays <= 1
      if (filter === 'nextWeek') return diffDays >= 0 && diffDays <= 7
      if (filter === 'nextMonth') return diffDays >= 0 && diffDays <= 30
      return false
    })
  }

  const matchesMemberFilter = (card) => {
    if (selectedMembers.length === 0) return true
    if (!card.members || card.members.length === 0) return false
    return card.members.some((member) => selectedMembers.includes(member.id))
  }

  const matchesLabelFilter = (card) => {
    if (selectedLabels.length === 0) return true
    return card.labels?.some((label) => selectedLabels.includes(labelKey(label)))
  }

  const matchesKeywordFilter = (card) => {
    if (!filterKeyword.trim()) return true
    const needle = filterKeyword.trim().toLowerCase()
    const haystack = `${card.title || ''} ${card.description || ''}`.toLowerCase()
    return haystack.includes(needle)
  }

  const matchesFilters = (card) => {
    return (
      matchesKeywordFilter(card) &&
      matchesLabelFilter(card) &&
      matchesDueFilter(card) &&
      matchesMemberFilter(card)
    )
  }

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyPress = (e) => {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
      if (isViewer) return
      
      switch (e.key.toLowerCase()) {
        case 'n':
          if (columns?.length > 0) {
            setShowCreateCard(columns[0].id)
          }
          break
        case 'b':
          setShowCreateColumn(true)
          break
        case 'escape':
          setSelectedCard(null)
          setShowActivityFeed(false)
          setShowInviteModal(false)
          setShowCreateCard(null)
          setShowCreateColumn(false)
          setIsRenamingBoard(false)
          setRenamingColumnId(null)
          break
      }
    }

    window.addEventListener('keydown', handleKeyPress)
    return () => window.removeEventListener('keydown', handleKeyPress)
  }, [columns, isViewer])

  const createColumnMutation = useMutation({
    mutationFn: async (title) => {
      const response = await columnAPI.createColumn(boardId, { title })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setShowCreateColumn(false)
      setColumnTitle('')
      toast.success('Column created successfully!', { id: 'column-created' })
    },
    onError: (error) => {
      if (error.response?.data?.message) return
      toast.error('Failed to create column', { id: 'column-create-error' })
    }
  })

  const createCardMutation = useMutation({
    mutationFn: async ({ columnId, title }) => {
      const response = await cardAPI.createCard(boardId, { title, columnId })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setShowCreateCard(null)
      setCardTitle('')
      toast.success('Card created successfully!', { id: 'card-created' })
    },
    onError: (error) => {
      if (error.response?.data?.message) return
      toast.error('Failed to create card', { id: 'card-create-error' })
    }
  })

  const moveCardMutation = useMutation({
    mutationFn: async ({ cardId, targetColumnId, newPosition }) => {
      const response = await cardAPI.moveCard(cardId, { targetColumnId, newPosition })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
    },
    onError: (error) => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      if (error?.response?.data?.message) return
      toast.error('Failed to move card', { id: 'card-move-error' })
    }
  })

  const handleDragEnd = (result) => {
    if (isViewer) return
    if (!result.destination) return

    const { source, destination, draggableId } = result
    const cardId = parseInt(draggableId.replace('card-', ''))

    if (source.droppableId === destination.droppableId && source.index === destination.index) {
      return
    }

    const targetColumnId = parseInt(destination.droppableId.replace('col-', ''))

    // Optimistic update
    const currentData = queryClient.getQueryData(['board', boardId, 'columns'])
    if (currentData) {
      const newData = [...currentData]
      
      const sourceColIndex = newData.findIndex(col => col.id === parseInt(source.droppableId.replace('col-', '')))
      const destColIndex = newData.findIndex(col => col.id === targetColumnId)
      
      if (sourceColIndex !== -1 && destColIndex !== -1) {
        const [movedCard] = newData[sourceColIndex].cards.splice(source.index, 1)
        newData[destColIndex].cards.splice(destination.index, 0, {
          ...movedCard,
          columnId: targetColumnId,
          position: destination.index
        })
        
        queryClient.setQueryData(['board', boardId, 'columns'], newData)
      }
    }

    moveCardMutation.mutate({
      cardId,
      targetColumnId,
      newPosition: destination.index
    })
  }

  const handleCreateColumn = (e) => {
    e.preventDefault()
    if (columnTitle.trim()) {
      createColumnMutation.mutate(columnTitle.trim())
    }
  }

  const handleCreateCard = (e) => {
    e.preventDefault()
    if (cardTitle.trim()) {
      createCardMutation.mutate({
        columnId: showCreateCard,
        title: cardTitle.trim()
      })
    }
  }

  const clearFilters = () => {
    setFilterKeyword('')
    setSelectedLabels([])
    setSelectedDueDates([])
    setSelectedMembers([])
  }

  const toggleFilterValue = (value, setter) => {
    setter(prev => prev.includes(value) ? prev.filter(item => item !== value) : [...prev, value])
  }

  const getDueDateStyles = (dueDate, isDone) => {
    if (!dueDate) return null
    if (isDone) return 'bg-[#f1f5f9] text-[#64748b] line-through border-[#e2e8f0]'
    
    const now = new Date()
    const due = new Date(dueDate)
    const diffMs = due - now
    const diffHours = diffMs / (1000 * 60 * 60)
    
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const dueDay = new Date(dueDate)
    dueDay.setHours(0, 0, 0, 0)
    const diffDays = Math.ceil((dueDay - today) / (1000 * 60 * 60 * 24))

    if (diffDays < 0) return 'bg-[#fef2f2] text-[#dc2626] border-[#fecaca]' // Overdue
    if (diffHours >= 0 && diffHours <= 24) return 'bg-[#fff7ed] text-[#ea580c] border-[#fed7aa]' // Due soon (24h)
    if (diffDays === 0) return 'bg-[#eff6ff] text-[#2563eb] border-[#bfdbfe]' // Due today
    return 'bg-[#f0fdf4] text-[#16a34a] border-[#bbf7d0]' // On track
  }

  if (isLoading || permissionsLoading) {
    return (
      <div className="h-screen flex flex-col bg-gradient-to-br from-blue-50 via-white to-purple-50">
        <div className="bg-white/80 backdrop-blur-md border-b border-white/20 px-6 py-4">
          <div className="flex justify-between items-center">
            <Skeleton className="h-6 w-48" />
            <div className="flex space-x-3">
              <Skeleton className="h-9 w-20" />
              <Skeleton className="h-9 w-24" />
            </div>
          </div>
        </div>
        <div className="flex-1 overflow-x-auto p-6">
          <div className="flex space-x-6 min-w-max">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="w-80 bg-white/60 backdrop-blur-sm rounded-xl p-4 border border-white/30">
                <Skeleton className="h-6 w-32 mb-4" />
                <div className="space-y-3">
                  {[...Array(3)].map((_, j) => (
                    <div key={j} className="bg-white rounded-lg p-3 shadow-sm">
                      <Skeleton className="h-4 w-full mb-2" />
                      <Skeleton className="h-3 w-2/3" />
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="h-screen flex flex-col overflow-hidden">
      <Navbar
        onCreate={isOwner ? () => setShowCreateBoardModal(true) : null}
      />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <div
          className="flex-1 flex flex-col relative overflow-hidden"
          style={{ backgroundImage: boardBg }}
        >
          <div className="absolute inset-0 bg-black/20" />
          
          {/* Toolbar */}
          <div className="relative z-10 bg-white/10 backdrop-blur-md border-b border-white/20 px-6 py-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center space-x-4">
                {isRenamingBoard ? (
                  <input
                    type="text"
                    value={renamedBoardTitle}
                    onChange={(e) => setRenamedBoardTitle(e.target.value)}
                    onBlur={handleRenameBoard}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleRenameBoard()
                      if (e.key === 'Escape') setIsRenamingBoard(false)
                    }}
                    className="text-xl font-bold text-white bg-white/20 border-b-2 border-white rounded px-2 py-0.5 focus:outline-none focus:bg-white/30 transition-all duration-200"
                    autoFocus
                  />
                ) : (
                  <h1 
                    className={`text-xl font-bold text-white drop-shadow-lg flex items-center group/title cursor-pointer ${isOwner ? 'hover:bg-white/10 rounded px-2 -ml-2' : ''}`}
                    onClick={isOwner ? () => {
                      setIsRenamingBoard(true)
                      setRenamedBoardTitle(boardTitle)
                    } : null}
                  >
                    {boardTitle}
                    {isOwner && (
                      <FiPlus className="ml-2 w-4 h-4 opacity-0 group-hover/title:opacity-100 transition-opacity rotate-45" title="Rename board" />
                    )}
                  </h1>
                )}
                <div className="flex items-center space-x-2 text-white/80 text-sm">
                  <FiStar className="w-4 h-4" />
                  <span>{memberCount} {memberLabel}</span>
                </div>
              </div>
              <div className="flex space-x-3">
                {canInvite && (
                  <button
                    onClick={() => setShowInviteModal(true)}
                    className="inline-flex items-center px-4 py-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white font-medium rounded-lg border border-white/30 transition-all duration-200 hover:scale-105"
                  >
                    <FiUsers className="mr-2 h-4 w-4" />
                    Invite
                  </button>
                )}
                <button
                  onClick={() => setShowFilterPanel(true)}
                  className={`inline-flex items-center px-3 py-2 rounded-lg border transition-all duration-200 ${
                    activeFilterCount > 0
                      ? 'bg-blue-500/90 border-blue-300 text-white'
                      : 'bg-white/20 border-white/30 text-white hover:bg-white/30'
                  }`}
                >
                  <FiFilter className="mr-2 h-4 w-4" />
                  Filter
                  {activeFilterCount > 0 && (
                    <span className="ml-2 inline-flex items-center justify-center min-w-[18px] h-5 px-1.5 rounded-full text-xs font-semibold bg-white text-blue-700">
                      {activeFilterCount}
                    </span>
                  )}
                </button>
                <button
                  onClick={() => setShowActivityFeed(true)}
                  className="inline-flex items-center px-4 py-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white font-medium rounded-lg border border-white/30 transition-all duration-200 hover:scale-105"
                >
                  <FiActivity className="mr-2 h-4 w-4" />
                  Activity
                </button>
                {isOwner && (
                  <button
                    onClick={handleDeleteBoard}
                    disabled={deleteBoardMutation.isPending}
                    className="inline-flex items-center px-4 py-2 bg-red-600/90 hover:bg-red-700 backdrop-blur-sm text-white font-semibold rounded-lg border border-red-300/40 transition-all duration-200 hover:scale-105 disabled:opacity-60 disabled:cursor-not-allowed"
                    title="Delete Board"
                  >
                    Delete Board
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Board Content */}
          <div className="relative z-10 flex-1 overflow-x-auto p-6">
            {activeFilterCount > 0 && (
              <div className="mb-4 rounded-lg bg-blue-500/90 text-white px-4 py-2 flex items-center justify-between">
                <span className="text-sm font-medium">
                  Showing cards matching {activeFilterCount} filter{activeFilterCount > 1 ? 's' : ''}
                </span>
                <button
                  onClick={clearFilters}
                  className="text-sm font-semibold underline underline-offset-4"
                >
                  Clear all
                </button>
              </div>
            )}
            <DragDropContext 
              onDragStart={() => setIsDragging(true)}
              onDragEnd={(result) => {
                setIsDragging(false)
                handleDragEnd(result)
              }}
            >
              <div className="flex space-x-6 min-w-max pb-6">
                {columns?.map((column) => {
                  const visibleCardCount = column.cards.filter(matchesFilters).length
                  const isRenaming = renamingColumnId === column.id
                  return (
                    <div key={column.id} className="w-80 bg-[#f4f5f7] rounded-[12px] flex-shrink-0 flex flex-col max-h-full">
                      <div className="p-3 pb-2 border-b-0">
                        <div className="flex justify-between items-center group/col">
                          {isRenaming ? (
                            <input
                              type="text"
                              value={renamedColumnTitle}
                              onChange={(e) => setRenamedColumnTitle(e.target.value)}
                              onBlur={() => handleRenameColumn(column.id)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') handleRenameColumn(column.id)
                                if (e.key === 'Escape') setRenamingColumnId(null)
                              }}
                              className="flex-1 px-2 py-1 text-sm font-semibold text-slate-700 bg-white border border-blue-500 rounded focus:outline-none"
                              autoFocus
                            />
                          ) : (
                            <h3 className="font-semibold text-[13px] text-[#64748b] tracking-[0.5px] uppercase px-1 flex-1 truncate">
                              {column.title}
                            </h3>
                          )}
                          <div className="flex items-center space-x-1.5 shrink-0">
                            <span className="text-xs font-bold text-blue-700 bg-blue-100 px-2 py-0.5 rounded-full shadow-sm">
                              {visibleCardCount}
                            </span>
                            {canEdit && !isRenaming && (
                              <div className="relative group/menu">
                                <button className="p-1 hover:bg-slate-200 rounded transition-colors">
                                  <FiMoreHorizontal className="w-4 h-4 text-slate-500" />
                                </button>
                                <div className="absolute right-0 top-full mt-1 w-32 bg-white rounded-lg shadow-xl border border-slate-200 py-1 z-20 invisible group-hover/menu:visible opacity-0 group-hover/menu:opacity-100 transition-all duration-200">
                                  <button
                                    onClick={() => {
                                      setRenamingColumnId(column.id)
                                      setRenamedColumnTitle(column.title)
                                    }}
                                    className="w-full text-left px-3 py-1.5 text-[13px] text-slate-700 hover:bg-slate-50 font-medium"
                                  >
                                    Rename
                                  </button>
                                  <button
                                    onClick={() => handleDeleteColumn(column.id, column.title)}
                                    className="w-full text-left px-3 py-1.5 text-[13px] text-red-600 hover:bg-red-50 font-medium"
                                  >
                                    Delete
                                  </button>
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="px-2 pb-2 flex-1 overflow-y-auto scrollbar-thin">
                        <Droppable droppableId={`col-${column.id}`}>
                          {(provided, snapshot) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.droppableProps}
                              className={`space-y-2 min-h-[10px] transition-colors duration-200 ${
                                snapshot.isDraggingOver ? 'bg-blue-50/50 rounded-lg p-1.5' : 'p-1'
                              }`}
                            >
                              {column.cards.map((card, index) => {
                                const isMatch = matchesFilters(card)
                                return (
                                  <Draggable
                                    key={card.id}
                                    draggableId={`card-${card.id}`}
                                    index={index}
                                    isDragDisabled={!canEdit}
                                  >
                                    {(provided, snapshot) => (
                                      <div
                                        ref={provided.innerRef}
                                        {...provided.draggableProps}
                                        {...provided.dragHandleProps}
                                        onClick={() => setSelectedCard(card.id)}
                                        className={`group bg-white rounded-[8px] shadow-[0_1px_4px_rgba(0,0,0,0.1)] hover:-translate-y-[2px] hover:shadow-[0_4px_12px_rgba(0,0,0,0.1)] cursor-pointer border border-transparent hover:border-[#e2e8f0] transition-all duration-150 ease-in-out ${
                                          snapshot.isDragging ? 'rotate-3 shadow-2xl scale-105 z-50' : ''
                                        } ${isMatch ? 'opacity-100' : 'opacity-20'} relative`}
                                      >
                                        <div className="p-3.5">
                                          {card.labels?.length > 0 && (
                                            <div className="flex flex-wrap gap-1.5 mb-2.5">
                                              {card.labels.map((label) => (
                                                <div
                                                  key={label.id}
                                                  className="h-[8px] w-10 rounded-[4px]"
                                                  style={{ backgroundColor: label.color }}
                                                />
                                              ))}
                                            </div>
                                          )}
                                          
                                          <h4 className="font-medium text-[14px] leading-snug text-gray-900 mb-2 group-hover:text-blue-600 transition-colors">
                                            {card.title}
                                          </h4>
                                          
                                          {card.description && (
                                            <p className="text-[12px] text-gray-500 mb-3 line-clamp-2 leading-relaxed">
                                              {stripHtml(card.description)}
                                            </p>
                                          )}

                                          <div className="flex items-center justify-between text-xs text-gray-500">
                                            <div className="flex items-center space-x-2.5">
                                              {card.dueDate && (
                                                <div className={`flex items-center gap-1 px-2 py-[2px] rounded-[4px] border text-[12px] font-medium ${getDueDateStyles(card.dueDate, column.title.toLowerCase() === 'done' || column.title.toLowerCase() === 'completed')}`}>
                                                  <FiCalendar className="w-3 h-3" />
                                                  <span className="leading-none">{timeAgo(card.dueDate)}</span>
                                                </div>
                                              )}
                                              
                                              {card.commentCount > 0 && (
                                                <div className="flex items-center space-x-1 text-gray-400 font-medium">
                                                  <FiMessageSquare className="w-3.5 h-3.5" />
                                                  <span className="text-[11px]">{card.commentCount}</span>
                                                </div>
                                              )}
                                            </div>
                                            {card.members?.length > 0 && (
                                              <div className="flex -space-x-1.5 overflow-hidden ml-2">
                                                {card.members.map((member) => (
                                                  <div
                                                    key={member.id}
                                                    className="inline-block h-[20px] w-[20px] rounded-full ring-2 ring-white bg-gradient-to-br from-blue-600 to-purple-600 shadow-sm flex items-center justify-center text-[9px] font-bold text-white shrink-0 uppercase"
                                                    title={member.user?.fullName || member.fullName || 'User'}
                                                  >
                                                    {(member.user?.fullName || member.fullName || 'U').charAt(0)}
                                                  </div>
                                                ))}
                                              </div>
                                            )}
                                          </div>
                                        </div>
                                      </div>
                                    )}
                                  </Draggable>
                                )
                              })}
                              {provided.placeholder}
                            </div>
                          )}
                        </Droppable>

                        {/* Add Card Form */}
                        {canEdit && showCreateCard === column.id ? (
                          <div className="mt-3">
                            <form onSubmit={handleCreateCard}>
                              <textarea
                                value={cardTitle}
                                onChange={(e) => setCardTitle(e.target.value)}
                                placeholder="Enter a title for this card..."
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                                rows={3}
                                autoFocus
                              />
                              <div className="flex items-center space-x-2 mt-2">
                                <button
                                  type="submit"
                                  disabled={!cardTitle.trim() || createCardMutation.isPending}
                                  className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium disabled:opacity-50 transition-colors"
                                >
                                  {createCardMutation.isPending ? 'Adding...' : 'Add Card'}
                                </button>
                                <button
                                  type="button"
                                  onClick={() => {
                                    setShowCreateCard(null)
                                    setCardTitle('')
                                  }}
                                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                                >
                                  <FiX className="w-4 h-4 text-gray-500" />
                                </button>
                              </div>
                            </form>
                          </div>
                        ) : canEdit ? (
                          <button
                            onClick={() => setShowCreateCard(column.id)}
                            className="w-full mt-1.5 p-2 text-gray-500 hover:text-gray-700 hover:bg-[#e2e8f0] rounded-lg text-sm font-medium flex items-center text-left transition-colors group"
                          >
                            <FiPlus className="mr-2 h-4 w-4 group-hover:scale-110 transition-transform" />
                            Add a card
                          </button>
                        ) : null}
                      </div>
                    </div>
                  )
                })}

                {/* Add Column */}
                <div className="w-80 flex-shrink-0">
                  {canEdit && (
                    showCreateColumn ? (
                      <div className="bg-white/90 backdrop-blur-sm rounded-xl p-4 shadow-lg border border-white/30">
                        <form onSubmit={handleCreateColumn}>
                          <input
                            type="text"
                            value={columnTitle}
                            onChange={(e) => setColumnTitle(e.target.value)}
                            placeholder="Enter column title..."
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            autoFocus
                          />
                          <div className="flex items-center space-x-2 mt-3">
                            <button
                              type="submit"
                              disabled={!columnTitle.trim() || createColumnMutation.isPending}
                              className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium disabled:opacity-50 transition-colors"
                            >
                              {createColumnMutation.isPending ? 'Adding...' : 'Add Column'}
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setShowCreateColumn(false)
                                setColumnTitle('')
                              }}
                              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                            >
                              <FiX className="w-4 h-4 text-gray-500" />
                            </button>
                          </div>
                        </form>
                      </div>
                    ) : (
                      <button
                        onClick={() => setShowCreateColumn(true)}
                        className="w-full bg-black/10 hover:bg-black/20 backdrop-blur-sm text-white/90 px-4 py-3.5 rounded-xl border border-white/10 text-left flex items-center font-medium transition-all duration-200 hover:scale-[1.02]"
                      >
                        <FiPlus className="mr-2 h-5 w-5 opacity-80" />
                        Add another list
                      </button>
                    )
                  )}
                </div>
              </div>
            </DragDropContext>
          </div>

          {showFilterPanel && (
            <div className="fixed inset-0 z-50">
              <div
                className="absolute inset-0 bg-black/30"
                onClick={() => setShowFilterPanel(false)}
              ></div>
              <div className="absolute right-0 top-0 h-full w-full max-w-sm bg-white shadow-[-4px_0_20px_rgba(0,0,0,0.1)] border-l border-gray-100 flex flex-col animate-slide-in-right">
                <div className="flex items-center justify-between px-5 p-4 border-b border-gray-100 bg-white z-10">
                  <div className="flex items-center space-x-2.5">
                    <FiFilter className="w-4 h-4 text-blue-600" />
                    <h3 className="text-lg font-bold text-gray-900 tracking-tight">Filter Boards</h3>
                  </div>
                  <button
                    onClick={() => setShowFilterPanel(false)}
                    className="p-1.5 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                  >
                    <FiX className="w-5 h-5" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto px-5 py-6 space-y-8 scrollbar-thin">
                  {/* Keyword Section */}
                  <div className="space-y-3">
                    <label className="text-[11px] font-bold text-[#94a3b8] uppercase tracking-[0.5px]">
                      Keyword
                    </label>
                    <div className="relative group">
                      <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                      <input
                        value={filterKeyword}
                        onChange={(e) => setFilterKeyword(e.target.value)}
                        placeholder="Search cards..."
                        className="w-full pl-10 pr-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-4 focus:ring-blue-50/50 focus:border-blue-400 text-sm transition-all"
                      />
                    </div>
                  </div>

                  {/* Labels Section */}
                  <div className="space-y-4">
                    <div className="text-[11px] font-bold text-[#94a3b8] uppercase tracking-[0.5px]">
                      Labels
                    </div>
                    <div className="space-y-2.5">
                      {availableLabels.length === 0 && (
                        <p className="text-xs text-gray-500 italic">No labels found on this board</p>
                      )}
                      {availableLabels.map((label) => {
                        const key = labelKey(label)
                        const checked = selectedLabels.includes(key)
                        return (
                          <label key={key} className="flex items-center gap-3 text-sm text-gray-700 cursor-pointer group">
                            <input
                              type="checkbox"
                              checked={checked}
                              onChange={() => toggleFilterValue(key, setSelectedLabels)}
                              className="h-4 w-4 text-blue-600 rounded-[4px] border-gray-300 focus:ring-blue-500 cursor-pointer accent-blue-600"
                            />
                            <span
                              className="inline-flex items-center px-2.5 py-1 rounded-md text-[11px] font-bold text-white shadow-sm transition-transform group-hover:scale-105"
                              style={{ backgroundColor: label.color }}
                            >
                              {label.text || label.color}
                            </span>
                          </label>
                        )
                      })}
                    </div>
                  </div>

                  {/* Due Date Section */}
                  <div className="space-y-4">
                    <div className="text-[11px] font-bold text-[#94a3b8] uppercase tracking-[0.5px]">
                      Due date
                    </div>
                    <div className="space-y-2.5 text-sm text-gray-700">
                      {[
                        { value: 'overdue', label: 'Overdue' },
                        { value: 'nextDay', label: 'Due in next 24 hours' },
                        { value: 'nextWeek', label: 'Due in next week' },
                        { value: 'nextMonth', label: 'Due in next month' },
                        { value: 'noDates', label: 'No dates' },
                      ].map((item) => (
                        <label key={item.value} className="flex items-center gap-3 cursor-pointer group">
                          <input
                            type="checkbox"
                            checked={selectedDueDates.includes(item.value)}
                            onChange={() => toggleFilterValue(item.value, setSelectedDueDates)}
                            className="h-4 w-4 text-blue-600 rounded-[4px] border-gray-300 focus:ring-blue-500 cursor-pointer accent-blue-600"
                          />
                          <span className="group-hover:text-gray-900 transition-colors">{item.label}</span>
                        </label>
                      ))}
                    </div>
                  </div>

                  {/* Members Section */}
                  <div className="space-y-4">
                    <div className="text-[11px] font-bold text-[#94a3b8] uppercase tracking-[0.5px]">
                      Members
                    </div>
                    <div className="space-y-2.5 text-sm text-gray-700">
                      {membersData?.members?.map((member) => (
                        <label key={member.id} className="flex items-center gap-3 cursor-pointer group">
                          <input
                            type="checkbox"
                            checked={selectedMembers.includes(member.id)}
                            onChange={() => toggleFilterValue(member.id, setSelectedMembers)}
                            className="h-4 w-4 text-blue-600 rounded-[4px] border-gray-300 focus:ring-blue-500 cursor-pointer accent-blue-600"
                          />
                          <span className="group-hover:text-gray-900 transition-colors">{member.user.fullName}</span>
                        </label>
                      ))}
                      {!membersData?.members?.length && (
                        <p className="text-xs text-gray-500 italic">No members assigned to this board</p>
                      )}
                    </div>
                  </div>
                </div>

                <div className="sticky bottom-0 border-t border-gray-100 px-5 py-4 flex justify-between items-center bg-gray-50/80 backdrop-blur-md">
                  <button
                    onClick={clearFilters}
                    className="text-xs font-bold text-[#dc2626] hover:text-[#b91c1c] transition-colors uppercase tracking-wider"
                  >
                    Clear all
                  </button>
                  <button
                    onClick={() => setShowFilterPanel(false)}
                    className="px-6 py-2 text-sm font-bold bg-blue-600 text-white rounded-lg hover:bg-blue-700 shadow-md shadow-blue-200 transition-all hover:-translate-y-0.5"
                  >
                    Done
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Keyboard shortcuts hint */}
          <div className="absolute bottom-4 left-4 bg-black/50 backdrop-blur-sm text-white text-xs px-3 py-2 rounded-lg">
            Press <kbd className="bg-white/20 px-1 rounded">N</kbd> for new card, <kbd className="bg-white/20 px-1 rounded">B</kbd> for new board, <kbd className="bg-white/20 px-1 rounded">Esc</kbd> to close
          </div>

          {/* Modals */}
          {selectedCard && (
            <Suspense fallback={<div className="fixed inset-0 bg-black/40 flex items-center justify-center"><div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent"></div></div>}>
              <CardModal
                cardId={selectedCard}
                boardId={boardId}
                isOwner={isOwner}
                isEditor={isEditor}
                canEdit={canEdit}
                isViewer={isViewer}
                onClose={() => setSelectedCard(null)}
              />
            </Suspense>
          )}

          {showActivityFeed && (
            <ActivityFeed
              boardId={boardId}
              onClose={() => setShowActivityFeed(false)}
            />
          )}

          {showInviteModal && (
            <Suspense fallback={<div className="fixed inset-0 bg-black/40 flex items-center justify-center"><div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent"></div></div>}>
              <InviteModal
                boardId={boardId}
                onClose={() => setShowInviteModal(false)}
              />
            </Suspense>
          )}

          {showCreateBoardModal && (
            <Suspense fallback={<div className="fixed inset-0 bg-black/40 flex items-center justify-center"><div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent"></div></div>}>
              <CreateBoardModal onClose={() => setShowCreateBoardModal(false)} />
            </Suspense>
          )}
        </div>
      </div>
    </div>
  )
}


export default BoardPage
