import { useState, useMemo, lazy, Suspense } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { FiPlus, FiFolder, FiHome, FiGrid, FiChevronDown, FiUsers, FiSettings } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Navbar from '../components/Navbar'
import BoardCard from '../components/BoardCard'
import { SkeletonBoard } from '../components/common/Skeleton'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/common/Avatar'
import toast from 'react-hot-toast'

const CreateBoardModal = lazy(() => import('../components/CreateBoardModal'))

const BoardListPage = () => {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [workspaceOpen, setWorkspaceOpen] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [showMembersModal, setShowMembersModal] = useState(false)
  const { user } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const activeNav = useMemo(() => {
    if (location.pathname === '/home' || location.pathname === '/') return 'home'
    if (location.pathname.startsWith('/boards')) return 'boards'
    return null
  }, [location.pathname])

  const PAGE_SIZE = 20
  const {
    data,
    isLoading,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage
  } = useInfiniteQuery({
    queryKey: ['boards', 'paged'],
    queryFn: async ({ pageParam = 0 }) => {
      const response = await boardAPI.getBoards({ page: pageParam, size: PAGE_SIZE })
      const payload = response.data.data
      if (payload && Array.isArray(payload.content)) {
        return payload
      }
      return { content: payload || [], hasNext: false }
    },
    getNextPageParam: (lastPage, pages) => (lastPage?.hasNext ? pages.length : undefined),
    refetchInterval: 30000
  })

  const boards = data?.pages?.flatMap(page => page.content) || []

  const filteredBoards = useMemo(() => {
    if (!boards) return []
    const query = searchQuery.trim().toLowerCase()
    if (!query) return boards
    return boards.filter((board) => board.title.toLowerCase().includes(query))
  }, [boards, searchQuery])

  const content = (() => {
    if (isLoading) {
      return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5">
          {Array.from({ length: 8 }).map((_, i) => (
            <SkeletonBoard key={i} className="bg-slate-200" />
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5">
        {filteredBoards?.map((board) => (
          <BoardCard key={board.id} board={board} />
        ))}
        <button
          onClick={() => setShowCreateModal(true)}
          className="h-[110px] rounded-xl border-2 border-dashed border-slate-300 hover:border-blue-500 bg-white hover:bg-[#f0f7ff] flex flex-col items-center justify-center text-slate-500 hover:text-blue-600 transition-all duration-300 group shadow-sm"
        >
          <FiPlus className="w-6 h-6 mb-1 group-hover:scale-110 transition-transform" />
          <span className="text-sm font-bold tracking-tight">Create new board</span>
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
            <div className="space-y-1.5">
              <Link
                to="/home"
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] transition-all duration-300 ${
                  activeNav === 'home'
                    ? 'bg-blue-100/50 text-blue-700 font-bold shadow-sm ring-1 ring-blue-200/50'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 font-semibold'
                }`}
              >
                <FiHome className={`w-4 h-4 ${activeNav === 'home' ? 'text-blue-600' : 'text-slate-400'}`} />
                Home
              </Link>
              <Link
                to="/boards"
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] transition-all duration-300 ${
                  activeNav === 'boards'
                    ? 'bg-blue-100/50 text-blue-700 font-bold shadow-sm ring-1 ring-blue-200/50'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 font-semibold'
                }`}
              >
                <FiGrid className={`w-4 h-4 ${activeNav === 'boards' ? 'text-blue-600' : 'text-slate-400'}`} />
                Boards
              </Link>
            </div>

            <div>
              <div className="flex items-center justify-between text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-3">
                Workspaces
                <button
                  onClick={() => setWorkspaceOpen(!workspaceOpen)}
                  className="text-slate-400 hover:text-slate-600 p-1 hover:bg-slate-100 rounded-md transition-colors"
                >
                  <FiChevronDown className={`w-4 h-4 transition-transform duration-200 ${workspaceOpen ? 'rotate-180' : ''}`} />
                </button>
              </div>
              <div className="rounded-xl border border-slate-200 bg-slate-50/50 px-3 py-3 hover:shadow-sm transition-shadow duration-200">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-600 to-blue-800 text-white flex items-center justify-center text-sm font-bold shadow-sm">
                    K
                  </div>
                  <div className="text-sm font-semibold text-slate-800 tracking-tight">Kanban Workspace</div>
                </div>
                {workspaceOpen && (
                  <div className="mt-3.5 space-y-0.5">
                    <button
                      onClick={() => navigate('/boards')}
                      className="w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-[13px] font-medium text-slate-600 hover:bg-white hover:text-slate-900 hover:shadow-sm transition-all"
                    >
                      <FiGrid className="w-4 h-4 text-slate-400" />
                      Boards
                    </button>
                    <button
                      onClick={() => setShowMembersModal(true)}
                      className="w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-[13px] font-medium text-slate-600 hover:bg-white hover:text-slate-900 hover:shadow-sm transition-all"
                    >
                      <FiUsers className="w-4 h-4 text-slate-400" />
                      Members
                    </button>
                    <button
                      onClick={() => toast('Workspace settings coming soon!')}
                      className="w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-[13px] font-medium text-slate-600 hover:bg-white hover:text-slate-900 hover:shadow-sm transition-all"
                    >
                      <FiSettings className="w-4 h-4 text-slate-400" />
                      Settings
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </aside>

        <main className="flex-1 px-8 py-8 h-full">
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

          {!isLoading && !error && hasNextPage && searchQuery.trim().length === 0 && (
            <div className="mt-6 flex justify-center">
              <button
                onClick={() => fetchNextPage()}
                disabled={isFetchingNextPage}
                className="px-5 py-2.5 text-sm font-semibold text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-lg transition-all disabled:opacity-60"
              >
                {isFetchingNextPage ? 'Loading...' : 'Load more'}
              </button>
            </div>
          )}

          {showCreateModal && (
            <Suspense fallback={<div className="fixed inset-0 bg-black/40 flex items-center justify-center"><div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent"></div></div>}>
              <CreateBoardModal onClose={() => setShowCreateModal(false)} />
            </Suspense>
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
