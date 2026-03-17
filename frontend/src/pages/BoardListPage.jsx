import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { FiPlus, FiFolder, FiHome, FiGrid, FiChevronDown, FiUsers, FiSettings } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Navbar from '../components/Navbar'
import BoardCard from '../components/BoardCard'
import CreateBoardModal from '../components/CreateBoardModal'
import { SkeletonBoard } from '../components/common/Skeleton'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/common/Avatar'
import toast from 'react-hot-toast'

const BoardListPage = () => {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [workspaceOpen, setWorkspaceOpen] = useState(true)
  const [activeNav, setActiveNav] = useState('boards')
  const [searchQuery, setSearchQuery] = useState('')
  const [showMembersModal, setShowMembersModal] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()

  const { data: boards, isLoading, error } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    }
  })

  const filteredBoards = useMemo(() => {
    if (!boards) return []
    const query = searchQuery.trim().toLowerCase()
    if (!query) return boards
    return boards.filter((board) => board.title.toLowerCase().includes(query))
  }, [boards, searchQuery])

  const content = (() => {
    if (isLoading) {
      return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <SkeletonBoard key={i} className="bg-gray-200" />
          ))}
        </div>
      )
    }

    if (error) {
      return (
        <div className="text-center py-12">
          <p className="text-red-600">Failed to load boards. Please try again.</p>
        </div>
      )
    }

    if (boards?.length === 0) {
      return (
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
      )
    }

    if (filteredBoards.length === 0) {
      return (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600">
          No boards match your search.
        </div>
      )
    }

    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {filteredBoards?.map((board) => (
          <BoardCard key={board.id} board={board} />
        ))}
        <button
          onClick={() => setShowCreateModal(true)}
          className="h-24 rounded-xl border border-dashed border-slate-300 hover:border-slate-400 bg-white hover:bg-slate-50 flex flex-col items-center justify-center text-slate-600 hover:text-slate-800 transition-all duration-200"
        >
          <FiPlus className="w-5 h-5 mb-1" />
          <span className="text-sm font-medium">Create new board</span>
        </button>
      </div>
    )
  })()

  return (
    <div className="min-h-screen bg-[#F4F5F7]">
      <Navbar
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        onCreate={() => setShowCreateModal(true)}
      />
      <div className="max-w-[1400px] mx-auto flex">
        <aside className="hidden lg:flex w-64 shrink-0 border-r border-slate-200 bg-white min-h-[calc(100vh-48px)] px-4 py-5">
          <div className="w-full space-y-6">
            <div className="space-y-2">
              <button
                onClick={() => {
                  setActiveNav('home')
                  navigate('/boards')
                }}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium ${
                  activeNav === 'home'
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-slate-700 hover:bg-slate-100'
                }`}
              >
                <FiHome className="w-4 h-4" />
                Home
              </button>
              <button
                onClick={() => {
                  setActiveNav('boards')
                  navigate('/boards')
                }}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium ${
                  activeNav === 'boards'
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-slate-700 hover:bg-slate-100'
                }`}
              >
                <FiGrid className="w-4 h-4" />
                Boards
              </button>
            </div>

            <div>
              <div className="flex items-center justify-between text-xs font-semibold text-slate-500 uppercase tracking-widest px-3 mb-2">
                Workspaces
                <button
                  onClick={() => setWorkspaceOpen(!workspaceOpen)}
                  className="text-slate-500 hover:text-slate-700"
                >
                  <FiChevronDown className={`w-4 h-4 transition-transform ${workspaceOpen ? 'rotate-180' : ''}`} />
                </button>
              </div>
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-3">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-md bg-[#0B5FFF] text-white flex items-center justify-center text-sm font-semibold">
                    K
                  </div>
                  <div className="text-sm font-semibold text-slate-800">Kanban Workspace</div>
                </div>
                {workspaceOpen && (
                  <div className="mt-3 space-y-1">
                    <button
                      onClick={() => navigate('/boards')}
                      className="w-full flex items-center gap-2 px-2 py-2 rounded-md text-sm text-slate-700 hover:bg-white"
                    >
                      <FiGrid className="w-4 h-4 text-slate-500" />
                      Boards
                    </button>
                    <button
                      onClick={() => setShowMembersModal(true)}
                      className="w-full flex items-center gap-2 px-2 py-2 rounded-md text-sm text-slate-700 hover:bg-white"
                    >
                      <FiUsers className="w-4 h-4 text-slate-500" />
                      Members
                    </button>
                    <button
                      onClick={() => toast('Workspace settings coming soon!')}
                      className="w-full flex items-center gap-2 px-2 py-2 rounded-md text-sm text-slate-700 hover:bg-white"
                    >
                      <FiSettings className="w-4 h-4 text-slate-500" />
                      Settings
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </aside>

        <main className="flex-1 px-6 py-6">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 rounded-md bg-[#0B5FFF] text-white flex items-center justify-center text-sm font-semibold">
              K
            </div>
            <div>
              <p className="text-sm text-slate-500">Workspace</p>
              <h1 className="text-2xl font-semibold text-slate-900">Kanban Workspace</h1>
            </div>
          </div>

          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-semibold text-slate-900">Your boards</h2>
              {boards && boards.length > 0 && (
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {boards.length}
                </span>
              )}
            </div>
          </div>

          {content}

          {showCreateModal && (
            <CreateBoardModal onClose={() => setShowCreateModal(false)} />
          )}

          {showMembersModal && (
            <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
              <div className="bg-white rounded-xl shadow-2xl max-w-md w-full mx-4">
                <div className="p-6">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-semibold text-slate-900">Workspace Members</h3>
                    <button
                      onClick={() => setShowMembersModal(false)}
                      className="text-slate-400 hover:text-slate-600"
                    >
                      ✕
                    </button>
                  </div>
                  <div className="flex items-center gap-3 mb-4">
                    <Avatar name={user?.fullName || 'DP'} size="md" />
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{user?.fullName || 'DP'}</p>
                      <p className="text-xs text-slate-500">{user?.email || 'kanban@workspace.com'}</p>
                    </div>
                  </div>
                  <p className="text-sm text-slate-600">
                    Members are managed per board. Use the Invite button inside a board to add people.
                  </p>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}

export default BoardListPage
