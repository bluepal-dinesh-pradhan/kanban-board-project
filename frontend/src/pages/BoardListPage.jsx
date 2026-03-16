import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FiPlus, FiFolder } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Navbar from '../components/Navbar'
import BoardCard from '../components/BoardCard'
import CreateBoardModal from '../components/CreateBoardModal'
import { SkeletonBoard } from '../components/common/Skeleton'
import EmptyState from '../components/common/EmptyState'

const BoardListPage = () => {
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { data: boards, isLoading, error } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    }
  })

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">My Boards</h1>
              <p className="text-gray-600 mt-1">Loading your boards...</p>
            </div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <SkeletonBoard key={i} className="bg-gray-200" />
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
          <div className="text-center py-12">
            <p className="text-red-600">Failed to load boards. Please try again.</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              My Boards
              {boards && boards.length > 0 && (
                <span className="ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {boards.length}
                </span>
              )}
            </h1>
            <p className="text-gray-600 mt-1">Organize your work, your way</p>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors duration-200"
          >
            <FiPlus className="w-4 h-4 mr-2" />
            Create Board
          </button>
        </div>

        {boards?.length === 0 ? (
          <EmptyState
            icon={FiFolder}
            title="No boards yet"
            description="Create your first board to get started"
            action={
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors duration-200"
              >
                <FiPlus className="w-5 h-5 mr-2" />
                Create your first board
              </button>
            }
          />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {boards?.map((board) => (
              <BoardCard key={board.id} board={board} />
            ))}
            
            {/* Create new board card */}
            <button
              onClick={() => setShowCreateModal(true)}
              className="h-24 rounded-xl border-2 border-dashed border-gray-300 hover:border-gray-400 bg-gray-50 hover:bg-gray-100 flex flex-col items-center justify-center text-gray-500 hover:text-gray-600 transition-all duration-200 group"
            >
              <FiPlus className="w-6 h-6 mb-1 group-hover:scale-110 transition-transform duration-200" />
              <span className="text-sm font-medium">Create new board</span>
            </button>
          </div>
        )}

        {showCreateModal && (
          <CreateBoardModal onClose={() => setShowCreateModal(false)} />
        )}
      </div>
    </div>
  )
}

export default BoardListPage