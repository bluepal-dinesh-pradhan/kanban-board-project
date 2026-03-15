import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FiPlus, FiCalendar, FiUsers } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import toast from 'react-hot-toast'

const BoardListPage = () => {
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [boardTitle, setBoardTitle] = useState('')
  const queryClient = useQueryClient()

  const { data: boards, isLoading, error } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    }
  })

  const createBoardMutation = useMutation({
    mutationFn: async (title) => {
      const response = await boardAPI.createBoard({ title })
      return response.data.data
    },
    onSuccess: (newBoard) => {
      queryClient.invalidateQueries(['boards'])
      setShowCreateForm(false)
      setBoardTitle('')
      toast.success('Board created successfully!')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create board')
    }
  })

  const handleCreateBoard = (e) => {
    e.preventDefault()
    if (boardTitle.trim()) {
      createBoardMutation.mutate(boardTitle.trim())
    }
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const getRoleColor = (role) => {
    switch (role) {
      case 'OWNER':
        return 'bg-purple-100 text-purple-800'
      case 'EDITOR':
        return 'bg-blue-100 text-blue-800'
      case 'VIEWER':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="text-center">
            <p className="text-red-500 text-lg">Failed to load boards</p>
          </div>
        </div>
      </div>
    )
  }
  return (
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div className="px-4 py-6 sm:px-0">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">My Boards</h1>
            <p className="mt-2 text-gray-600">Manage your Kanban boards</p>
          </div>
          <button
            onClick={() => setShowCreateForm(true)}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            <FiPlus className="mr-2 h-4 w-4" />
            New Board
          </button>
        </div>

        {showCreateForm && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg shadow-xl max-w-md w-full mx-4">
              <h2 className="text-lg font-medium mb-4">Create New Board</h2>
              <form onSubmit={handleCreateBoard}>
                <input
                  type="text"
                  value={boardTitle}
                  onChange={(e) => setBoardTitle(e.target.value)}
                  placeholder="Enter board title"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  autoFocus
                />
                <div className="flex justify-end space-x-3 mt-4">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCreateForm(false)
                      setBoardTitle('')
                    }}
                    className="px-4 py-2 text-gray-600 hover:text-gray-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={!boardTitle.trim() || createBoardMutation.isPending}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md disabled:opacity-50"
                  >
                    {createBoardMutation.isPending ? 'Creating...' : 'Create'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {boards?.length === 0 ? (
          <div className="text-center py-12">
            <FiUsers className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No boards</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by creating a new board.</p>
            <div className="mt-6">
              <button
                onClick={() => setShowCreateForm(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
              >
                <FiPlus className="mr-2 h-4 w-4" />
                New Board
              </button>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {boards?.map((board) => (
              <Link
                key={board.id}
                to={`/board/${board.id}`}
                className="group bg-white p-6 rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 hover:border-indigo-300"
              >
                <div className="flex items-start justify-between">
                  <h3 className="text-lg font-medium text-gray-900 group-hover:text-indigo-600 truncate">
                    {board.title}
                  </h3>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRoleColor(board.role)}`}>
                    {board.role}
                  </span>
                </div>
                
                <div className="mt-4 flex items-center text-sm text-gray-500">
                  <FiCalendar className="mr-1 h-4 w-4" />
                  Created {formatDate(board.createdAt)}
                </div>
                
                <div className="mt-2 text-sm text-gray-500">
                  Click to open board
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default BoardListPage