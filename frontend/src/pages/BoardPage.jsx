import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'
import { FiPlus, FiUsers, FiActivity, FiMoreHorizontal, FiCalendar, FiMessageSquare, FiX, FiStar } from 'react-icons/fi'
import { boardAPI, columnAPI, cardAPI } from '../api/endpoints'
import { getBoardGradient } from '../utils/colors'
import { timeAgo } from '../utils/timeAgo'
import CardModal from '../components/CardModal'
import ActivityFeed from '../components/ActivityFeed'
import InviteModal from '../components/InviteModal'
import Skeleton from '../components/common/Skeleton'
import Navbar from '../components/Navbar'
import CreateBoardModal from '../components/CreateBoardModal'
import toast from 'react-hot-toast'

const BoardPage = () => {
  const { boardId } = useParams()
  const [showCreateCard, setShowCreateCard] = useState(null)
  const [showCreateColumn, setShowCreateColumn] = useState(false)
  const [showActivityFeed, setShowActivityFeed] = useState(false)
  const [showInviteModal, setShowInviteModal] = useState(false)
  const [showCreateBoardModal, setShowCreateBoardModal] = useState(false)
  const [selectedCard, setSelectedCard] = useState(null)
  const [columnTitle, setColumnTitle] = useState('')
  const [cardTitle, setCardTitle] = useState('')
  const queryClient = useQueryClient()

  const { data: columns, isLoading } = useQuery({
    queryKey: ['board', boardId, 'columns'],
    queryFn: async () => {
      const response = await boardAPI.getBoardColumns(boardId)
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

  const { data: boards } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    },
    staleTime: 5 * 60 * 1000
  })

  const board = boards?.find((item) => String(item.id) === String(boardId))
  const boardTitle = board?.title || 'Board'
  const boardBg = getBoardGradient(board?.background || '#0079BF')
  const isOwner = board?.role === 'OWNER'
  const isViewer = board?.role === 'VIEWER'
  const memberCount = membersData?.members?.length || 1
  const memberLabel = memberCount === 1 ? 'member' : 'members'

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

  const isOverdue = (dueDate) => {
    if (!dueDate) return false
    return new Date(dueDate) < new Date()
  }

  if (isLoading) {
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
    <div className="h-screen flex flex-col">
      <Navbar onCreate={() => setShowCreateBoardModal(true)} />
      <div
        className="flex-1 flex flex-col relative"
        style={{ backgroundImage: boardBg }}
      >
        <div className="absolute inset-0 bg-black/20" />
      
      {/* Toolbar */}
      <div className="relative z-10 bg-white/10 backdrop-blur-md border-b border-white/20 px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-4">
            <h1 className="text-xl font-bold text-white drop-shadow-lg">
              {boardTitle}
            </h1>
            <div className="flex items-center space-x-2 text-white/80 text-sm">
              <FiStar className="w-4 h-4" />
              <span>{memberCount} {memberLabel}</span>
            </div>
          </div>
          <div className="flex space-x-3">
            {isOwner && (
              <button
                onClick={() => setShowInviteModal(true)}
                className="inline-flex items-center px-4 py-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white font-medium rounded-lg border border-white/30 transition-all duration-200 hover:scale-105"
              >
                <FiUsers className="mr-2 h-4 w-4" />
                Invite
              </button>
            )}
            <button
              onClick={() => setShowActivityFeed(true)}
              className="inline-flex items-center px-4 py-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white font-medium rounded-lg border border-white/30 transition-all duration-200 hover:scale-105"
            >
              <FiActivity className="mr-2 h-4 w-4" />
              Activity
            </button>
            {!isViewer && (
              <button className="p-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white rounded-lg border border-white/30 transition-all duration-200 hover:scale-105">
                <FiMoreHorizontal className="h-4 w-4" />
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Board Content */}
      <div className="relative z-10 flex-1 overflow-x-auto p-6">
        <DragDropContext onDragEnd={handleDragEnd}>
          <div className="flex space-x-6 min-w-max pb-6">
            {columns?.map((column) => (
              <div key={column.id} className="w-80 bg-white/90 backdrop-blur-sm rounded-xl shadow-lg border border-white/30 flex-shrink-0">
                <div className="p-4 border-b border-gray-200/50">
                  <div className="flex justify-between items-center">
                    <h3 className="font-semibold text-gray-800">{column.title}</h3>
                    <div className="flex items-center space-x-2">
                      <span className="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
                        {column.cards.length}
                      </span>
                      {!isViewer && (
                        <button className="p-1 hover:bg-gray-100 rounded">
                          <FiMoreHorizontal className="w-4 h-4 text-gray-500" />
                        </button>
                      )}
                    </div>
                  </div>
                </div>

                <div className="p-4">
                  <Droppable droppableId={`col-${column.id}`}>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={`space-y-3 min-h-[100px] transition-colors duration-200 ${
                          snapshot.isDraggingOver ? 'bg-blue-50 rounded-lg p-2' : ''
                        }`}
                      >
                        {column.cards.map((card, index) => (
                          <Draggable
                            key={card.id}
                            draggableId={`card-${card.id}`}
                            index={index}
                            isDragDisabled={isViewer}
                          >
                            {(provided, snapshot) => (
                              <div
                                ref={provided.innerRef}
                                {...provided.draggableProps}
                                {...provided.dragHandleProps}
                                onClick={() => setSelectedCard(card.id)}
                                className={`group bg-white rounded-lg shadow-sm hover:shadow-md cursor-pointer border border-gray-200 transition-all duration-200 ${
                                  snapshot.isDragging ? 'rotate-2 shadow-xl scale-105' : 'hover:scale-102'
                                }`}
                              >
                                <div className="p-4">
                                  {card.labels?.length > 0 && (
                                    <div className="flex flex-wrap gap-1 mb-3">
                                      {card.labels.map((label) => (
                                        <div
                                          key={label.id}
                                          className="h-2 w-10 rounded-full"
                                          style={{ backgroundColor: label.color }}
                                        />
                                      ))}
                                    </div>
                                  )}
                                  
                                  <h4 className="font-medium text-gray-900 mb-2 group-hover:text-blue-600 transition-colors">
                                    {card.title}
                                  </h4>
                                  
                                  {card.description && (
                                    <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                                      {card.description}
                                    </p>
                                  )}

                                  <div className="flex items-center justify-between text-xs text-gray-500">
                                    <div className="flex items-center space-x-3">
                                      {card.dueDate && (
                                        <div className={`flex items-center space-x-1 px-2 py-1 rounded-full ${
                                          isOverdue(card.dueDate) 
                                            ? 'bg-red-100 text-red-700' 
                                            : 'bg-yellow-100 text-yellow-700'
                                        }`}>
                                          <FiCalendar className="w-3 h-3" />
                                          <span>{timeAgo(card.dueDate)}</span>
                                        </div>
                                      )}
                                      
                                      {card.commentCount > 0 && (
                                        <div className="flex items-center space-x-1 text-gray-500">
                                          <FiMessageSquare className="w-3 h-3" />
                                          <span>{card.commentCount}</span>
                                        </div>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              </div>
                            )}
                          </Draggable>
                        ))}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>

                  {/* Add Card Form */}
                  {!isViewer && showCreateCard === column.id ? (
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
                  ) : !isViewer ? (
                    <button
                      onClick={() => setShowCreateCard(column.id)}
                      className="w-full mt-3 p-3 text-gray-600 hover:text-gray-800 hover:bg-gray-50 rounded-lg text-sm flex items-center transition-colors group"
                    >
                      <FiPlus className="mr-2 h-4 w-4 group-hover:scale-110 transition-transform" />
                      Add a card
                    </button>
                  ) : null}
                </div>
              </div>
            ))}

            {/* Add Column */}
            <div className="w-80 flex-shrink-0">
              {!isViewer && (
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
                    className="w-full bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white p-4 rounded-xl border border-white/30 text-left flex items-center transition-all duration-200 hover:scale-105"
                  >
                    <FiPlus className="mr-2 h-5 w-5" />
                    Add another list
                  </button>
                )
              )}
            </div>
          </div>
        </DragDropContext>
      </div>

        {/* Keyboard shortcuts hint */}
        <div className="absolute bottom-4 left-4 bg-black/50 backdrop-blur-sm text-white text-xs px-3 py-2 rounded-lg">
          Press <kbd className="bg-white/20 px-1 rounded">N</kbd> for new card, <kbd className="bg-white/20 px-1 rounded">B</kbd> for new board, <kbd className="bg-white/20 px-1 rounded">Esc</kbd> to close
        </div>

        {/* Modals */}
        {selectedCard && (
          <CardModal
            cardId={selectedCard}
            isViewer={isViewer}
            onClose={() => setSelectedCard(null)}
          />
        )}

        {showActivityFeed && (
          <ActivityFeed
            boardId={boardId}
            onClose={() => setShowActivityFeed(false)}
          />
        )}

        {showInviteModal && (
          <InviteModal
            boardId={boardId}
            onClose={() => setShowInviteModal(false)}
          />
        )}

        {showCreateBoardModal && (
          <CreateBoardModal onClose={() => setShowCreateBoardModal(false)} />
        )}
      </div>
    </div>
  )
}

export default BoardPage
