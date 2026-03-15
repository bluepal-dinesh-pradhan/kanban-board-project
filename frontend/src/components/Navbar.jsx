import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { FiLogOut, FiUser } from 'react-icons/fi'

const Navbar = () => {
  const { user, logout } = useAuth()

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/boards" className="text-xl font-bold text-indigo-600 hover:text-indigo-700">
              Kanban Board
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex items-center text-gray-700">
              <FiUser className="mr-2 h-4 w-4" />
              <span className="text-sm font-medium">{user?.fullName}</span>
            </div>
            <button
              onClick={logout}
              className="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
            >
              <FiLogOut className="mr-2 h-4 w-4" />
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar