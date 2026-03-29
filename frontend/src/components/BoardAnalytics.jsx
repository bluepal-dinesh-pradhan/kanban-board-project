import { useQuery } from '@tanstack/react-query'
import { boardAPI } from '../api/endpoints'
import { FiX, FiTrendingUp } from 'react-icons/fi'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'

const PRIORITY_COLORS = {
  URGENT: '#dc2626',
  HIGH: '#f97316',
  MEDIUM: '#eab308',
  LOW: '#3b82f6',
  NONE: '#9ca3af',
}

const COLUMN_COLORS = ['#3b82f6', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444']

const BoardAnalytics = ({ boardId, onClose }) => {
  const { data: analytics, isLoading } = useQuery({
    queryKey: ['analytics', boardId],
    queryFn: async () => {
      const response = await boardAPI.getAnalytics(boardId)
      return response.data.data
    }
  })

  if (isLoading) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
        <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent"></div>
      </div>
    )
  }

  if (!analytics) return null

  const priorityData = analytics.cardsByPriority?.filter(p => p.count > 0) || []
  const columnData = analytics.cardsByColumn || []

  return (
    <div className="fixed inset-0 z-50">
      <div className="absolute inset-0 bg-black/30" onClick={onClose}></div>
      <div className="absolute right-0 top-0 h-full w-full max-w-lg bg-white dark:bg-[#1e1e21] shadow-[-4px_0_20px_rgba(0,0,0,0.1)] border-l border-gray-100 dark:border-gray-800 flex flex-col">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800">
          <div className="flex items-center space-x-2.5">
            <FiTrendingUp className="w-5 h-5 text-blue-600" />
            <h3 className="text-lg font-bold text-gray-900 dark:text-gray-100">Board Analytics</h3>
          </div>
          <button onClick={onClose} className="p-1.5 text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors">
            <FiX className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto px-6 py-6 space-y-8">

          {/* Stats Grid */}
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-4 border border-blue-100 dark:border-blue-800">
              <div className="text-2xl font-bold text-blue-700 dark:text-blue-400">{analytics.totalCards}</div>
              <div className="text-xs font-semibold text-blue-600/70 dark:text-blue-400/70 uppercase mt-1">Total Cards</div>
            </div>
            <div className="bg-green-50 dark:bg-green-900/20 rounded-xl p-4 border border-green-100 dark:border-green-800">
              <div className="text-2xl font-bold text-green-700 dark:text-green-400">{analytics.completionRate}%</div>
              <div className="text-xs font-semibold text-green-600/70 dark:text-blue-400/70 uppercase mt-1">Completion</div>
            </div>
            <div className="bg-red-50 dark:bg-red-900/20 rounded-xl p-4 border border-red-100 dark:border-red-800">
              <div className="text-2xl font-bold text-red-700 dark:text-red-400">{analytics.overdueCards}</div>
              <div className="text-xs font-semibold text-red-600/70 dark:text-blue-400/70 uppercase mt-1">Overdue</div>
            </div>
            <div className="bg-purple-50 dark:bg-purple-900/20 rounded-xl p-4 border border-purple-100 dark:border-purple-800">
              <div className="text-2xl font-bold text-purple-700 dark:text-purple-400">{analytics.memberCount}</div>
              <div className="text-xs font-semibold text-purple-600/70 dark:text-blue-400/70 uppercase mt-1">Members</div>
            </div>
          </div>

          {/* Additional Stats */}
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-3 text-center">
              <div className="text-lg font-bold text-gray-900 dark:text-gray-100">{analytics.assignedCards}</div>
              <div className="text-[10px] font-semibold text-gray-500 uppercase">Assigned</div>
            </div>
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-3 text-center">
              <div className="text-lg font-bold text-gray-900 dark:text-gray-100">{analytics.unassignedCards}</div>
              <div className="text-[10px] font-semibold text-gray-500 uppercase">Unassigned</div>
            </div>
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-3 text-center">
              <div className="text-lg font-bold text-gray-900 dark:text-gray-100">{analytics.recentActivityCount}</div>
              <div className="text-[10px] font-semibold text-gray-500 uppercase">Actions (7d)</div>
            </div>
          </div>

          {/* Cards by Column — Bar Chart */}
          {columnData.length > 0 && (
            <div>
              <h4 className="text-sm font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-4">Cards by Column</h4>
              <div className="bg-gray-50 dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700">
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={columnData}>
                    <XAxis dataKey="column" tick={{ fontSize: 11 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                    <Tooltip
                      contentStyle={{
                        borderRadius: '8px', fontSize: '13px',
                        border: '1px solid #e5e7eb', boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                      }}
                    />
                    <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                      {columnData.map((entry, i) => (
                        <Cell key={i} fill={COLUMN_COLORS[i % COLUMN_COLORS.length]} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {/* Cards by Priority — Pie Chart */}
          {priorityData.length > 0 && (
            <div>
              <h4 className="text-sm font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-4">Cards by Priority</h4>
              <div className="bg-gray-50 dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700">
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie
                      data={priorityData}
                      dataKey="count"
                      nameKey="priority"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      strokeWidth={2}
                      stroke="#fff"
                    >
                      {priorityData.map((entry, i) => (
                        <Cell key={i} fill={PRIORITY_COLORS[entry.priority] || '#9ca3af'} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        borderRadius: '8px', fontSize: '13px',
                        border: '1px solid #e5e7eb'
                      }}
                    />
                    <Legend
                      formatter={(value) => <span style={{ fontSize: '12px', fontWeight: 600 }}>{value}</span>}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default BoardAnalytics
