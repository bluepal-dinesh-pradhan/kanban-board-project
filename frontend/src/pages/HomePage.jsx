import { useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { FiHome, FiGrid, FiChevronDown, FiUsers, FiSettings, FiLayers, FiClipboard, FiAlertCircle, FiCalendar, FiActivity } from 'react-icons/fi'
import Navbar from '../components/Navbar'
import Avatar from '../components/common/Avatar'
import Skeleton, { SkeletonText } from '../components/common/Skeleton'
import BoardCard from '../components/BoardCard'
import { boardAPI } from '../api/endpoints'
import { timeAgo } from '../utils/timeAgo'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const normalizeCollection = (payload) => {
  if (!payload) return []
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.content)) return payload.content
  return []
}

const startOfDay = (value) => {
  const date = new Date(value)
  date.setHours(0, 0, 0, 0)
  return date
}

const isDoneColumn = (columnTitle) => {
  const normalized = String(columnTitle || '').trim().toLowerCase()
  return normalized === 'done' || normalized === 'completed'
}

const getGreeting = (now) => {
  const hour = now.getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 18) return 'Good afternoon'
  return 'Good evening'
}

const formatFullDate = (date) =>
  date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })

const activityVerb = (action) => {
  const map = {
    CREATED_BOARD: 'created a board',
    CREATED_COLUMN: 'added a list',
    CREATED_CARD: 'added a card',
    UPDATED_CARD: 'updated a card',
    MOVED_CARD: 'moved a card',
    ADDED_COMMENT: 'commented on a card',
    INVITED_MEMBER: 'added a member',
    SENT_INVITATION: 'sent an invitation',
    RESENT_INVITATION: 'resent an invitation',
    REMOVED_MEMBER: 'removed a member',
    ARCHIVED_CARD: 'archived a card',
  }
  return map[action] || String(action || '').toLowerCase().split('_').join(' ')
}

const dueBadgeClasses = (dueDateIso) => {
  if (!dueDateIso) return 'bg-slate-100 text-slate-600 border-slate-200'
  const now = new Date()
  const today = startOfDay(now)
  const dueDay = startOfDay(dueDateIso)
  const diffDays = Math.ceil((dueDay - today) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) return 'bg-[#fef2f2] text-[#dc2626] border-[#fecaca]'
  if (diffDays === 0) return 'bg-[#eff6ff] text-[#2563eb] border-[#bfdbfe]'
  if (diffDays <= 1) return 'bg-[#fff7ed] text-[#ea580c] border-[#fed7aa]'
  return 'bg-[#f0fdf4] text-[#16a34a] border-[#bbf7d0]'
}

const CompactBoardCardSkeleton = () => (
  <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
    <Skeleton className="h-4 w-2/3 mb-2" />
    <Skeleton className="h-3 w-1/3" />
    <div className="mt-4 flex items-center gap-2">
      <Skeleton className="h-4 w-4 rounded-full" />
      <Skeleton className="h-3 w-10" />
    </div>
  </div>
)

const StatCardSkeleton = () => (
  <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
    <div className="flex items-center justify-between">
      <div>
        <Skeleton className="h-7 w-16 mb-2" />
        <Skeleton className="h-4 w-24" />
      </div>
      <Skeleton className="h-10 w-10 rounded-xl" />
    </div>
  </div>
)

const HomePage = () => {
  const [workspaceOpen, setWorkspaceOpen] = useState(true)
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()

  const activeNav = useMemo(() => {
    if (location.pathname === '/home' || location.pathname === '/') return 'home'
    if (location.pathname.startsWith('/boards')) return 'boards'
    return null
  }, [location.pathname])

  const dashboardQuery = useQuery({
    queryKey: ['home', 'dashboard'],
    queryFn: async () => {
      const boardsResponse = await boardAPI.getBoards()
      const boards = normalizeCollection(boardsResponse?.data?.data)

      const columnsResults = await Promise.allSettled(
        boards.map(async (board) => {
          const response = await boardAPI.getBoardColumns(board.id)
          return { boardId: board.id, columns: normalizeCollection(response?.data?.data) }
        })
      )

      const activityResults = await Promise.allSettled(
        boards.map(async (board) => {
          const response = await boardAPI.getBoardActivity(board.id, { page: 0, size: 10 })
          return { boardId: board.id, activity: normalizeCollection(response?.data?.data) }
        })
      )

      const columnsByBoardId = new Map()
      columnsResults.forEach((result) => {
        if (result.status === 'fulfilled') {
          columnsByBoardId.set(String(result.value.boardId), result.value.columns || [])
        }
      })

      const activities = []
      activityResults.forEach((result) => {
        if (result.status !== 'fulfilled') return
        const boardId = result.value.boardId
        const boardTitle = boards.find((b) => String(b.id) === String(boardId))?.title || 'Board'
        ;(result.value.activity || []).forEach((activity) => {
          activities.push({ ...activity, boardId, boardTitle })
        })
      })

      activities.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

      return {
        boards,
        columnsByBoardId,
        recentActivities: activities.slice(0, 10),
      }
    },
  })

  const now = useMemo(() => new Date(), [])
  const greeting = useMemo(() => getGreeting(now), [now])
  const todayLabel = useMemo(() => formatFullDate(now), [now])
  const firstName = useMemo(() => {
    const name = (user?.fullName || '').trim()
    if (!name) return 'there'
    return name.split(/\s+/)[0]
  }, [user?.fullName])

  const derived = useMemo(() => {
    const boards = dashboardQuery.data?.boards || []
    const columnsByBoardId = dashboardQuery.data?.columnsByBoardId || new Map()

    const allCards = []
    boards.forEach((board) => {
      const columns = columnsByBoardId.get(String(board.id)) || []
      columns.forEach((column) => {
        ;(column.cards || []).forEach((card) => {
          allCards.push({
            ...card,
            boardId: board.id,
            boardTitle: board.title,
            columnTitle: column.title,
          })
        })
      })
    })

    const today = startOfDay(new Date())
    const endExclusive = new Date(today)
    endExclusive.setDate(endExclusive.getDate() + 7)

    const totalBoards = boards.length
    const totalCards = allCards.length

    const isOpenCard = (card) => !isDoneColumn(card.columnTitle)
    const dueDate = (card) => (card?.dueDate ? new Date(card.dueDate) : null)

    const overdueCards = allCards.filter((card) => {
      if (!isOpenCard(card)) return false
      const due = dueDate(card)
      if (!due) return false
      return startOfDay(due) < today
    })

    const dueThisWeekCards = allCards.filter((card) => {
      if (!isOpenCard(card)) return false
      const due = dueDate(card)
      if (!due) return false
      const day = startOfDay(due)
      return day >= today && day < endExclusive
    })

    const upcomingDueItems = dueThisWeekCards
      .map((card) => ({ ...card, due: dueDate(card) }))
      .filter((item) => !!item.due)
      .sort((a, b) => a.due.getTime() - b.due.getTime())

    return {
      totalBoards,
      totalCards,
      overdueCount: overdueCards.length,
      dueThisWeekCount: dueThisWeekCards.length,
      upcomingDueItems: upcomingDueItems.slice(0, 10),
    }
  }, [dashboardQuery.data])

  const isLoading = dashboardQuery.isLoading
  const hasError = !!dashboardQuery.error
  const boards = dashboardQuery.data?.boards || []
  const viewAllActivityHref = dashboardQuery.data?.recentActivities?.[0]?.boardId
    ? `/boards/${dashboardQuery.data.recentActivities[0].boardId}`
    : '/boards'

  return (
    <div className="min-h-screen bg-[#F4F5F7]">
      <Navbar />

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
                  onClick={() => setWorkspaceOpen((prev) => !prev)}
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
                      onClick={() => toast('Workspace members are managed per board.', { id: 'home-members' })}
                      className="w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-[13px] font-medium text-slate-600 hover:bg-white hover:text-slate-900 hover:shadow-sm transition-all"
                    >
                      <FiUsers className="w-4 h-4 text-slate-400" />
                      Members
                    </button>
                    <button
                      onClick={() => toast('Workspace settings coming soon!', { id: 'home-settings' })}
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

        <main className="flex-1 px-6 sm:px-8 py-8 h-full">
          <div className="rounded-2xl bg-white border border-slate-200 shadow-sm px-6 py-6 sm:px-8 sm:py-7 mb-6">
            <p className="text-sm font-semibold text-slate-500">
              {greeting}, <span className="text-slate-900">{firstName}</span>!
            </p>
            <h1 className="text-2xl sm:text-[28px] font-semibold text-slate-900 tracking-tight mt-1">
              {todayLabel}
            </h1>
            <p className="text-sm text-slate-500 mt-2">Here&apos;s what&apos;s happening across your boards</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
            {isLoading ? (
              <>
                <StatCardSkeleton />
                <StatCardSkeleton />
                <StatCardSkeleton />
                <StatCardSkeleton />
              </>
            ) : (
              <>
                <div className="rounded-2xl border border-slate-200 bg-gradient-to-br from-blue-50 to-white p-5 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-3xl font-extrabold text-slate-900">{derived.totalBoards}</div>
                      <div className="text-sm font-semibold text-slate-500 mt-1">Total Boards</div>
                    </div>
                    <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center">
                      <FiLayers className="w-5 h-5" />
                    </div>
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-gradient-to-br from-indigo-50 to-white p-5 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-3xl font-extrabold text-slate-900">{derived.totalCards}</div>
                      <div className="text-sm font-semibold text-slate-500 mt-1">Total Cards</div>
                    </div>
                    <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-700 flex items-center justify-center">
                      <FiClipboard className="w-5 h-5" />
                    </div>
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-gradient-to-br from-rose-50 to-white p-5 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-3xl font-extrabold text-slate-900">{derived.overdueCount}</div>
                      <div className="text-sm font-semibold text-slate-500 mt-1">Overdue Cards</div>
                    </div>
                    <div className="w-10 h-10 rounded-xl bg-rose-100 text-rose-700 flex items-center justify-center">
                      <FiAlertCircle className="w-5 h-5" />
                    </div>
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-gradient-to-br from-emerald-50 to-white p-5 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-3xl font-extrabold text-slate-900">{derived.dueThisWeekCount}</div>
                      <div className="text-sm font-semibold text-slate-500 mt-1">Due This Week</div>
                    </div>
                    <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                      <FiCalendar className="w-5 h-5" />
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>

          {hasError && (
            <div className="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700 mb-6">
              Failed to load your dashboard. Please refresh and try again.
            </div>
          )}

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
              <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-slate-50 flex items-center justify-center">
                    <FiActivity className="w-5 h-5 text-slate-700" />
                  </div>
                  <div>
                    <h2 className="text-base font-semibold text-slate-900">Recent activity</h2>
                    <p className="text-xs text-slate-500 mt-0.5">Last 10 updates across all boards</p>
                  </div>
                </div>
                <Link to={viewAllActivityHref} className="text-sm font-semibold text-blue-600 hover:text-blue-700">
                  View all
                </Link>
              </div>

              <div className="divide-y divide-slate-100">
                {isLoading ? (
                  <div className="p-6">
                    <SkeletonText lines={4} />
                    <div className="mt-5">
                      <SkeletonText lines={4} />
                    </div>
                  </div>
                ) : dashboardQuery.data?.recentActivities?.length ? (
                  dashboardQuery.data.recentActivities.map((activity) => (
                    <button
                      key={`${activity.boardId}:${activity.id}`}
                      onClick={() => navigate(`/boards/${activity.boardId}`)}
                      className="w-full text-left px-6 py-4 hover:bg-slate-50 transition-colors"
                    >
                      <div className="flex items-start gap-3">
                        <div className="mt-0.5">
                          <Avatar name={activity?.user?.fullName || 'User'} size="sm" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-[13.5px] text-slate-900 leading-[1.6]">
                            <span className="font-bold">{activity?.user?.fullName || 'Someone'}</span>{' '}
                            <span className="text-slate-600 italic font-medium">{activityVerb(activity.action)}</span>{' '}
                            {(activity.cardTitle || activity?.card?.title || activity.entityTitle) && (
                              <>
                                <span className="text-slate-700 font-medium">on</span>{' '}
                                <span className="font-semibold text-slate-900">
                                  {activity.cardTitle || activity?.card?.title || activity.entityTitle}
                                </span>{' '}
                              </>
                            )}
                            <span className="text-slate-700 font-medium">in</span>{' '}
                            <span className="font-semibold text-slate-900">{activity.boardTitle}</span>
                          </p>
                          <p className="text-[11px] text-slate-400 mt-1 font-semibold uppercase tracking-wider">
                            {timeAgo(activity.createdAt)}
                          </p>
                        </div>
                      </div>
                    </button>
                  ))
                ) : (
                  <div className="px-6 py-10 text-center">
                    <div className="mx-auto w-14 h-14 rounded-full bg-blue-50 flex items-center justify-center mb-3">
                      <FiActivity className="w-7 h-7 text-blue-300" />
                    </div>
                    <p className="text-sm font-semibold text-slate-900">No activity yet</p>
                    <p className="text-sm text-slate-500 mt-1">Updates will appear as your boards change.</p>
                  </div>
                )}
              </div>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
              <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-slate-50 flex items-center justify-center">
                    <FiCalendar className="w-5 h-5 text-slate-700" />
                  </div>
                  <div>
                    <h2 className="text-base font-semibold text-slate-900">Upcoming due dates</h2>
                    <p className="text-xs text-slate-500 mt-0.5">Next 7 days</p>
                  </div>
                </div>
              </div>

              <div className="divide-y divide-slate-100">
                {isLoading ? (
                  <div className="p-6 space-y-4">
                    {Array.from({ length: 6 }).map((_, idx) => (
                      <div key={idx} className="flex items-center justify-between gap-4">
                        <div className="flex-1">
                          <Skeleton className="h-4 w-2/3 mb-2" />
                          <Skeleton className="h-3 w-1/3" />
                        </div>
                        <Skeleton className="h-7 w-20 rounded-full" />
                      </div>
                    ))}
                  </div>
                ) : derived.upcomingDueItems.length ? (
                  derived.upcomingDueItems.map((card) => (
                    <button
                      key={`${card.boardId}:${card.id || card.title}`}
                      onClick={() => navigate(`/boards/${card.boardId}`)}
                      className="w-full text-left px-6 py-4 hover:bg-slate-50 transition-colors"
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="min-w-0">
                          <p className="text-[13.5px] font-semibold text-slate-900 truncate">{card.title || 'Untitled card'}</p>
                          <p className="text-xs text-slate-500 mt-1 truncate">{card.boardTitle}</p>
                        </div>
                        <div
                          className={`shrink-0 inline-flex items-center px-2.5 py-1 rounded-full border text-[12px] font-semibold ${dueBadgeClasses(
                            card.dueDate
                          )}`}
                        >
                          {new Date(card.dueDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                        </div>
                      </div>
                    </button>
                  ))
                ) : (
                  <div className="px-6 py-10 text-center">
                    <div className="mx-auto w-14 h-14 rounded-full bg-emerald-50 flex items-center justify-center mb-3">
                      <FiCalendar className="w-7 h-7 text-emerald-300" />
                    </div>
                    <p className="text-sm font-semibold text-slate-900">Nothing due soon</p>
                    <p className="text-sm text-slate-500 mt-1">Cards with due dates in the next week show up here.</p>
                  </div>
                )}
              </div>
            </section>
          </div>

          <section className="mt-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100">
              <div>
                <h2 className="text-base font-semibold text-slate-900">Your boards</h2>
                <p className="text-xs text-slate-500 mt-0.5">Quick access to your most recent boards</p>
              </div>
              <Link to="/boards" className="text-sm font-semibold text-blue-600 hover:text-blue-700">
                View all boards
              </Link>
            </div>

            <div className="p-6">
              {isLoading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4 gap-4">
                  <CompactBoardCardSkeleton />
                  <CompactBoardCardSkeleton />
                  <CompactBoardCardSkeleton />
                  <CompactBoardCardSkeleton />
                </div>
              ) : boards.length ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4 gap-4">
                  {boards.slice(0, 8).map((board) => (
                    <div key={board.id} className="scale-[0.98] origin-top-left">
                      <BoardCard board={board} />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-xl border border-slate-200 bg-slate-50 px-5 py-8 text-center">
                  <p className="text-sm font-semibold text-slate-900">No boards yet</p>
                  <p className="text-sm text-slate-500 mt-1">Create a board from the Boards page to get started.</p>
                  <Link
                    to="/boards"
                    className="inline-flex items-center justify-center mt-4 px-4 py-2.5 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition-colors"
                  >
                    Go to Boards
                  </Link>
                </div>
              )}
            </div>
          </section>
        </main>
      </div>
    </div>
  )
}

export default HomePage
