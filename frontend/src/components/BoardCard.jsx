import { Link } from 'react-router-dom'
import { FiUsers } from 'react-icons/fi'


const BoardCard = ({ board }) => {
  const backgroundGradients = {
    '#0079BF': 'from-blue-600 to-blue-800',
    '#D29034': 'from-orange-500 to-orange-700', 
    '#519839': 'from-emerald-500 to-emerald-700',
    '#B04632': 'from-red-500 to-red-700',
    '#89609E': 'from-purple-500 to-purple-700',
  }

  const gradientClass = backgroundGradients[board.background] || 'from-blue-600 to-blue-800'

  return (
    <Link
      to={`/boards/${board.id}`}
      className="group block h-full"
    >
      <div
        className={`
          relative h-[110px] rounded-xl bg-gradient-to-br ${gradientClass}
          p-4 text-white hover:-translate-y-1.5 hover:shadow-[0_12px_24px_rgba(0,0,0,0.15)]
          transition-all duration-300 ease-out overflow-hidden flex flex-col justify-between
          before:absolute before:inset-0 before:bg-white/10 before:opacity-0 group-hover:before:opacity-100 before:transition-opacity
        `}
      >
        <div className="absolute inset-0 bg-gradient-to-b from-white/10 to-black/30 mix-blend-overlay"></div>
        <div className="relative z-10 w-full flex justify-between items-start">
          <h3 className="font-bold text-[15px] leading-snug line-clamp-2 text-white text-shadow max-w-[85%]">
            {board.title}
          </h3>
        </div>
        
        <div className="relative z-10 flex items-center gap-1.5 text-white/80 text-[11px] font-medium mt-auto group-hover:text-white transition-colors duration-200">
          <FiUsers className="w-3.5 h-3.5" />
          <span>{board.memberCount || 1}</span>
        </div>
      </div>
    </Link>
  )
}

export default BoardCard
