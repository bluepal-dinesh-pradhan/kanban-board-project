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
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-end z-50">
      <div className="bg-white w-80 h-full shadow-2xl transform transition-transform duration-300 ease-out">
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50">
            <div className="flex items-center space-x-2">
              <FiActivity className="w-5 h-5 text-gray-600" />
              <h2 className="text-lg font-semibold text-gray-900">Activity</h2>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto">
            {isLoading ? (
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              </div>
            ) : activities?.length === 0 ? (
              <div className="text-center py-12 px-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <FiActivity className="w-8 h-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">No activity yet</h3>
                <p className="text-sm text-gray-500">
                  Activity will show up here as you and your teammates work on this board.
                </p>
              </div>
            ) : (
              <div className="p-4">
                <div className="space-y-4">
                  {activities?.map((activity) => {
                    const IconComponent = getActivityIcon(activity.action)
                    const colorClasses = getActivityColor(activity.action)
                    
                    return (
                      <div key={activity.id} className="flex space-x-3">
                        <div className="flex-shrink-0">
                          <Avatar name={activity.user.fullName} size="sm" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start space-x-2">
                            <div className={`p-1 rounded-full ${colorClasses} flex-shrink-0 mt-0.5`}>
                              <IconComponent className="w-3 h-3" />
                            </div>
                            <div className="flex-1">
                              <p className="text-sm text-gray-900 leading-relaxed">
                                <span className="font-medium">{activity.user.fullName}</span>
                                {' '}
                                <span className="text-gray-700">{getActionDescription(activity)}</span>
                              </p>
                              <p className="text-xs text-gray-500 mt-1">
                                {timeAgo(activity.createdAt)}
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default ActivityFeed
