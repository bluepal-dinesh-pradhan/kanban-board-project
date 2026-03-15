import { useQuery } from '@tanstack/react-query'
import { FiX, FiActivity, FiUser, FiClock } from 'react-icons/fi'
import { boardAPI } from '../api/endpoints'

const ActivityFeed = ({ boardId, onClose }) => {
  const { data: activities, isLoading } = useQuery({
    queryKey: ['board', boardId, 'activity'],
    queryFn: async () => {
      const response = await boardAPI.getBoardActivity(boardId)
      return response.data.data
    }
  })

  const getActionDescription = (activity) => {
    const actionMap = {
      'CREATED_BOARD': 'created this board',
      'CREATED_COLUMN': 'created a column',
      'CREATED_CARD': 'created a card',
      'UPDATED_CARD': 'updated a card',
      'MOVED_CARD': 'moved a card',
      'ADDED_COMMENT': 'added a comment',
      'INVITED_MEMBER': 'invited a member',
    }
    return actionMap[activity.action] || activity.action.toLowerCase()
  }

  const formatTime = (dateString) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInMinutes = Math.floor((now - date) / (1000 * 60))
    
    if (diffInMinutes < 1) return 'just now'
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}h ago`
    return `${Math.floor(diffInMinutes / 1440)}d ago`
  }

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-end z-50">
      <div className="bg-white w-96 h-full shadow-xl overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-center mb-6">
            <div className="flex items-center">
              <FiActivity className="mr-2 h-5 w-5 text-gray-600" />
              <h2 className="text-lg font-medium text-gray-900">Activity</h2>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <FiX className="h-5 w-5" />
            </button>
          </div>

          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
            </div>
          ) : activities?.length === 0 ? (
            <div className="text-center py-8">
              <FiActivity className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No activity yet</h3>
              <p className="mt-1 text-sm text-gray-500">Activity will appear here as you work on the board.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {activities?.map((activity) => (
                <div key={activity.id} className="flex space-x-3">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                      <FiUser className="h-4 w-4 text-gray-600" />
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900">
                      <span className="font-medium">{activity.user.fullName}</span>
                      {' '}
                      <span className="text-gray-600">{getActionDescription(activity)}</span>
                    </p>
                    <div className="flex items-center mt-1 text-xs text-gray-500">
                      <FiClock className="mr-1 h-3 w-3" />
                      {formatTime(activity.createdAt)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default ActivityFeed