import { Link } from 'react-router-dom'
import Avatar from './common/Avatar'
import { timeAgo } from '../utils/timeAgo'

const BoardCard = ({ board }) => {
  const backgroundGradients = {
    '#0079BF': 'from-blue-500 to-blue-600',
    '#D29034': 'from-orange-500 to-orange-600', 
    '#519839': 'from-green-500 to-green-600',
    '#B04632': 'from-red-500 to-red-600',
    '#89609E': 'from-purple-500 to-purple-600',
  }

  const gradientClass = backgroundGradients[board.background] || 'from-blue-500 to-blue-600'

  return (
    <Link
      to={`/boards/${board.id}`}
      className="group block"
    >
      <div className={`
        relative h-24 rounded-xl bg-gradient-to-br ${gradientClass} 
        p-4 text-white shadow-md hover:shadow-lg 
        transform hover:scale-105 transition-all duration-200
        overflow-hidden
      `}>
        {/* Background Pattern */}
        <div className="absolute inset-0 bg-black bg-opacity-10"></div>
        
        {/* Content */}
        <div className="relative z-10 h-full flex flex-col justify-between">
          <h3 className="font-bold text-lg leading-tight text-shadow-sm line-clamp-2">
            {board.title}
          </h3>
          
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-1">
              {/* Show first few member avatars */}
              {board.members && board.members.slice(0, 3).map((member, index) => (
                <Avatar 
                  key={member.id}
                  name={member.user.fullName}
                  size="sm"
                  className={`border-2 border-white ${index > 0 ? '-ml-2' : ''}`}
                />
              ))}
              {board.members && board.members.length > 3 && (
                <div className="w-7 h-7 rounded-full bg-white bg-opacity-20 flex items-center justify-center text-xs font-semibold -ml-2">
                  +{board.members.length - 3}
                </div>
              )}
            </div>
            
            <div className="text-xs text-white text-opacity-90">
              {timeAgo(board.createdAt)}
            </div>
          </div>
        </div>
      </div>
    </Link>
  )
}

export default BoardCard