import { useState, useMemo, lazy, Suspense } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { FiPlus, FiFolder } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import BoardCard from '../components/BoardCard'
import { SkeletonBoard } from '../components/common/Skeleton'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/common/Avatar'

const CreateBoardModal = lazy(() => import('../components/CreateBoardModal'))

const BoardListPage = () => {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [showMembersModal, setShowMembersModal] = useState(false)
  const { user } = useAuth()

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
        <Sidebar />

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
