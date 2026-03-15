import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'
import { FiPlus, FiUsers, FiActivity } from 'react-icons/fi'
import { boardAPI, columnAPI, cardAPI } from '../api/endpoints'
import CardModal from '../components/CardModal'
import ActivityFeed from '../components/ActivityFeed'
import InviteModal from '../components/InviteModal'
import toast from 'react-hot-toast'

const BoardPage = () => {
  const { boardId } = useParams()
  const [showCreateCard, setShowCreateCard] = useState(null)
  const [showCreateColumn, setShowCreateColumn] = useState(false)
  const [showActivityFeed, setShowActivityFeed] = useState(false)
  const [showInviteModal, setShowInviteModal] = useState(false)
  const [selectedCard, setSelectedCard] = useState(null)
  const [columnTitle, setColumnTitle] = useState('')
  const [cardForm, setCardForm] = useState({ title: '', description: '', dueDate: '' })
  const queryClient = useQueryClient()

  const { data: columns, isLoading } = useQuery({
    queryKey: ['board', boardId, 'columns'],
    queryFn: async () => {
      const response = await boardAPI.getBoardColumns(boardId)
      return response.data.data
    }
  })

  const createColumnMutation = useMutation({
    mutationFn: async (title) => {
      const response = await columnAPI.createColumn(boardId, { title })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setShowCreateColumn(false)
      setColumnTitle('')
      toast.success('Column created successfully!')
    }
  })

  const createCardMutation = useMutation({
    mutationFn: async ({ columnId, cardData }) => {
      const response = await cardAPI.createCard(boardId, { ...cardData, columnId })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      setShowCreateCard(null)
      setCardForm({ title: '', description: '', dueDate: '' })
      toast.success('Card created successfully!')
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
    onError: () => {
      // Revert optimistic update
      queryClient.invalidateQueries(['board', boardId, 'columns'])
      toast.error('Failed to move card')
    }
  })

  const handleDragEnd = (result) => {
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
      
      // Find source and destination columns
      const sourceColIndex = newData.findIndex(col => col.id === parseInt(source.droppableId.replace('col-', '')))
      const destColIndex = newData.findIndex(col => col.id === targetColumnId)
      
      if (sourceColIndex !== -1 && destColIndex !== -1) {
        // Remove card from source
        const [movedCard] = newData[sourceColIndex].cards.splice(source.index, 1)
        
        // Add card to destination
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
    if (cardForm.title.trim()) {
      createCardMutation.mutate({
        columnId: showCreateCard,
        cardData: {
          title: cardForm.title.trim(),
          description: cardForm.description.trim() || null,
          dueDate: cardForm.dueDate || null
        }
      })
    }
  }

  const formatDate = (date) => {
    if (!date) return null
    return new Date(date).toLocaleDateString()
  }

  const isOverdue = (dueDate) => {
    if (!dueDate) return false
    return new Date(dueDate) < new Date()
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  return (
    <div className="h-screen flex flex-col">
      {/* Toolbar */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex justify-between items-center">
          <h1 className="text-xl font-semibold text-gray-900">Board</h1>
          <div className="flex space-x-3">
            <button
              onClick={() => setShowInviteModal(true)}
              className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
            >
              <FiUsers className="mr-2 h-4 w-4" />
              Invite
            </button>
            <button
              onClick={() => setShowActivityFeed(true)}
              className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
            >
              <FiActivity className="mr-2 h-4 w-4" />
              Activity
            </button>
          </div>
        </div>
      </div>

      {/* Board Content */}
      <div className="flex-1 overflow-x-auto p-6">
        <DragDropContext onDragEnd={handleDragEnd}>
          <div className="flex space-x-6 min-w-max">
            {columns?.map((column) => (
              <div key={column.id} className="w-80 bg-gray-100 rounded-lg p-4 flex-shrink-0">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="font-medium text-gray-900">{column.title}</h3>
                  <span className="text-sm text-gray-500">{column.cards.length}</span>
                </div>

                <Droppable droppableId={`col-${column.id}`}>
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className={`space-y-3 min-h-[200px] ${
                        snapshot.isDraggingOver ? 'bg-blue-50 rounded-md p-2' : ''
                      }`}
                    >
                      {column.cards.map((card, index) => (
                        <Draggable
                          key={card.id}
                          draggableId={`card-${card.id}`}
                          index={index}
                        >
                          {(provided, snapshot) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              onClick={() => setSelectedCard(card.id)}
                              className={`bg-white p-3 rounded-md shadow-sm hover:shadow-md cursor-pointer border ${
                                snapshot.isDragging ? 'rotate-2 shadow-lg' : ''
                              }`}
                            >
                              <h4 className="font-medium text-gray-900 mb-2">{card.title}</h4>
                              
                              {card.description && (
                                <p className="text-sm text-gray-600 mb-2 line-clamp-2">{card.description}</p>
                              )}

                              <div className="flex flex-wrap gap-1 mb-2">
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

                              <div className="flex justify-between items-center text-xs text-gray-500">
                                {card.dueDate && (
                                  <span className={isOverdue(card.dueDate) ? 'text-red-500 font-medium' : ''}>
                                    Due: {formatDate(card.dueDate)}
                                  </span>
                                )}
                                
                                {card.commentCount > 0 && (
                                  <span className="flex items-center">
                                    💬 {card.commentCount}
                                  </span>
                                )}
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
                {showCreateCard === column.id ? (
                  <div className="bg-white p-3 rounded-md shadow-sm border mt-3">
                    <form onSubmit={handleCreateCard}>
                      <textarea
                        value={cardForm.title}
                        onChange={(e) => setCardForm(prev => ({ ...prev, title: e.target.value }))}
                        placeholder="Enter a title for this card..."
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                        rows={2}
                        autoFocus
                      />
                      
                      <textarea
                        value={cardForm.description}
                        onChange={(e) => setCardForm(prev => ({ ...prev, description: e.target.value }))}
                        placeholder="Add a description (optional)"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none mt-2"
                        rows={2}
                      />

                      <input
                        type="date"
                        value={cardForm.dueDate}
                        onChange={(e) => setCardForm(prev => ({ ...prev, dueDate: e.target.value }))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mt-2"
                      />

                      <div className="flex space-x-2 mt-3">
                        <button
                          type="submit"
                          disabled={!cardForm.title.trim() || createCardMutation.isPending}
                          className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                        >
                          {createCardMutation.isPending ? 'Adding...' : 'Add Card'}
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setShowCreateCard(null)
                            setCardForm({ title: '', description: '', dueDate: '' })
                          }}
                          className="text-gray-600 hover:text-gray-800 px-3 py-1 text-sm"
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                ) : (
                  <button
                    onClick={() => setShowCreateCard(column.id)}
                    className="w-full mt-3 p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-200 rounded-md text-sm flex items-center"
                  >
                    <FiPlus className="mr-1 h-4 w-4" />
                    Add a card
                  </button>
                )}
              </div>
            ))}

            {/* Add Column */}
            <div className="w-80 flex-shrink-0">
              {showCreateColumn ? (
                <div className="bg-gray-100 rounded-lg p-4">
                  <form onSubmit={handleCreateColumn}>
                    <input
                      type="text"
                      value={columnTitle}
                      onChange={(e) => setColumnTitle(e.target.value)}
                      placeholder="Enter column title"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      autoFocus
                    />
                    <div className="flex space-x-2 mt-3">
                      <button
                        type="submit"
                        disabled={!columnTitle.trim() || createColumnMutation.isPending}
                        className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                      >
                        Add Column
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setShowCreateColumn(false)
                          setColumnTitle('')
                        }}
                        className="text-gray-600 hover:text-gray-800 px-3 py-1 text-sm"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              ) : (
                <button
                  onClick={() => setShowCreateColumn(true)}
                  className="w-full bg-gray-200 hover:bg-gray-300 text-gray-700 p-4 rounded-lg text-left flex items-center"
                >
                  <FiPlus className="mr-2 h-4 w-4" />
                  Add another column
                </button>
              )}
            </div>
          </div>
        </DragDropContext>
      </div>

      {/* Modals */}
      {selectedCard && (
        <CardModal
          cardId={selectedCard}
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
    </div>
  )
}

export default BoardPage