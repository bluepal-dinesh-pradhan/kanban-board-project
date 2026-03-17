import { Link } from 'react-router-dom'
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
      <div
        className={`
          relative h-24 rounded-xl bg-gradient-to-br ${gradientClass}
          p-4 text-white shadow-sm hover:shadow-md
          transition-all duration-200 overflow-hidden
        `}
      >
        <div className="absolute inset-0 bg-black/10"></div>
        <div className="relative z-10 h-full flex items-start">
          <h3 className="font-semibold text-base leading-tight line-clamp-2">
            {board.title}
          </h3>
        </div>
      </div>
    </Link>
  )
}

export default BoardCard
