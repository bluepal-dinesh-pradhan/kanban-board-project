import { useQuery } from '@tanstack/react-query'
import { FiX, FiActivity, FiPlus, FiEdit3, FiMove, FiMessageSquare, FiUserPlus, FiArchive } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'
import Avatar from './common/Avatar'
import { timeAgo } from '../utils/timeAgo'

const ActivityFeed = ({ boardId, onClose }) => {
  const { data: activities, isLoading } = useQuery({
    queryKey: ['board', boardId, 'activity'],
    queryFn: async () => {
      const response = await boardAPI.getBoardActivity(boardId)
      return response.data.data
    }
  })

  const getActivityIcon = (action) => {
    const iconMap = {
      'CREATED_BOARD': FiPlus,
      'CREATED_COLUMN': FiPlus,
      'CREATED_CARD': FiPlus,
      'UPDATED_CARD': FiEdit3,
      'MOVED_CARD': FiMove,
      'ADDED_COMMENT': FiMessageSquare,
      'INVITED_MEMBER': FiUserPlus,
      'SENT_INVITATION': FiUserPlus,
      'RESENT_INVITATION': FiUserPlus,
      'REMOVED_MEMBER': FiUserPlus,
      'ARCHIVED_CARD': FiArchive,
    }
    return iconMap[action] || FiActivity
  }

  const getActivityColor = (action) => {
    const colorMap = {
      'CREATED_BOARD': 'text-green-600 bg-green-100',
      'CREATED_COLUMN': 'text-blue-600 bg-blue-100',
      'CREATED_CARD': 'text-blue-600 bg-blue-100',
      'UPDATED_CARD': 'text-yellow-600 bg-yellow-100',
      'MOVED_CARD': 'text-purple-600 bg-purple-100',
      'ADDED_COMMENT': 'text-gray-600 bg-gray-100',
      'INVITED_MEMBER': 'text-green-600 bg-green-100',
      'SENT_INVITATION': 'text-green-600 bg-green-100',
      'RESENT_INVITATION': 'text-green-600 bg-green-100',
      'REMOVED_MEMBER': 'text-red-600 bg-red-100',
      'ARCHIVED_CARD': 'text-red-600 bg-red-100',
    }
    return colorMap[action] || 'text-gray-600 bg-gray-100'
  }

  const getActionDescription = (activity) => {
    const actionMap = {
      'CREATED_BOARD': 'created this board',
      'CREATED_COLUMN': 'added a list',
      'CREATED_CARD': 'added a card',
      'UPDATED_CARD': 'updated a card',
      'MOVED_CARD': 'moved a card',
      'ADDED_COMMENT': 'commented on a card',
      'INVITED_MEMBER': 'added a member to this board',
      'SENT_INVITATION': 'sent an invitation',
      'RESENT_INVITATION': 'resent an invitation',
      'REMOVED_MEMBER': 'removed a member from this board',
      'ARCHIVED_CARD': 'archived a card',
    }
    return actionMap[activity.action] || activity.action.toLowerCase().replace('_', ' ')
  }

  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex justify-end z-50">
      <div className="bg-white w-[380px] h-full shadow-[-4px_0_20px_rgba(0,0,0,0.1)] flex flex-col animate-slide-in-right">
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className="flex items-center justify-between p-5 border-b border-[#f1f5f9] bg-white">
            <div className="flex items-center space-x-2.5">
              <div className="w-9 h-9 bg-gray-50 rounded-xl flex items-center justify-center">
                <FiActivity className="w-5 h-5 text-gray-700" />
              </div>
              <h2 className="text-xl font-bold text-gray-900 tracking-tight">Activity</h2>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-all duration-200"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto scrollbar-thin">
            {isLoading ? (
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              </div>
            ) : activities?.length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center p-8 text-center min-h-[400px]">
                <div className="w-20 h-20 bg-blue-50/50 rounded-full flex items-center justify-center mb-6 ring-4 ring-white">
                  <FiActivity className="w-10 h-10 text-blue-300" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">No activity yet</h3>
                <p className="text-[13px] text-gray-500 leading-relaxed max-w-[240px] mx-auto">
                  Updates on cards, lists, and members will appear here as your project progresses.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-[#f1f5f9]">
                {activities?.map((activity) => {
                  const IconComponent = getActivityIcon(activity.action)
                  const colorClasses = getActivityColor(activity.action)
                  
                  return (
                    <div key={activity.id} className="flex space-x-3 px-5 py-4 transition-colors duration-200 hover:bg-[#f8fafc]">
                      <div className="flex-shrink-0 mt-0.5">
                        <Avatar name={activity.user.fullName} size="sm" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start gap-3">
                          <div className={`p-1.5 rounded-lg ${colorClasses} flex-shrink-0 mt-0.5`}>
                            <IconComponent className="w-3.5 h-3.5" />
                          </div>
                          <div className="flex-1">
                            <p className="text-[13.5px] text-gray-900 leading-[1.6]">
                              <span className="font-bold text-gray-950">{activity.user.fullName}</span>
                              {' '}
                              <span className="text-gray-600 italic font-medium">{getActionDescription(activity)}</span>
                            </p>
                            <div className="flex items-center gap-1.5 mt-1.5 text-[11px] text-[#94a3b8] font-medium uppercase tracking-wider">
                              <FiActivity className="w-2.5 h-2.5 opacity-60" />
                              {timeAgo(activity.createdAt)}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default ActivityFeed
