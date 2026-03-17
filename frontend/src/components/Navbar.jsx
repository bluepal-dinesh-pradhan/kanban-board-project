import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Avatar from './common/Avatar'
import { FiChevronDown, FiLogOut, FiPlus, FiBell, FiSearch } from 'react-icons/fi'

const Navbar = ({ searchValue, onSearchChange, onCreate }) => {
  const { user, logout } = useAuth()
  const [showDropdown, setShowDropdown] = useState(false)
  const [showNotifications, setShowNotifications] = useState(false)
  const [internalSearch, setInternalSearch] = useState('')
  const dropdownRef = useRef(null)
  const notificationRef = useRef(null)
  const navigate = useNavigate()

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false)
      }
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
    setShowDropdown(false)
  }

  const resolvedSearchValue = searchValue ?? internalSearch
  const handleSearchChange = (event) => {
    const nextValue = event.target.value
    if (onSearchChange) {
      onSearchChange(nextValue)
    } else {
      setInternalSearch(nextValue)
    }
  }

  return (
    <nav className="bg-[#0C2A4D] shadow-lg">
      <div className="max-w-[1400px] mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center h-12 gap-4">
          <Link to="/boards" className="flex items-center gap-3 text-white">
            <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
              <div className="w-5 h-5 bg-[#0C2A4D] rounded"></div>
            </div>
            <span className="font-semibold text-lg">Kanban</span>
          </Link>

          <div className="flex-1 flex items-center justify-center">
            <div className="relative w-56 sm:w-72 focus-within:w-96 transition-all duration-200">
              <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="Search"
                value={resolvedSearchValue}
                onChange={handleSearchChange}
                className="w-full h-9 pl-9 pr-3 rounded-md bg-[#163458] text-white placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-white/50"
              />
            </div>
          </div>

          <div className="flex items-center gap-2 text-white">
            <button
              onClick={onCreate}
              className="hidden sm:inline-flex items-center gap-2 px-3 h-9 rounded-md bg-[#0B5FFF] hover:bg-[#0747A6] text-sm font-semibold"
            >
              <FiPlus className="w-4 h-4" />
              Create
            </button>
            <div className="relative" ref={notificationRef}>
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="inline-flex items-center justify-center w-9 h-9 rounded-md hover:bg-white/10"
              >
                <FiBell className="w-5 h-5" />
              </button>
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg py-3 z-50 border border-gray-200">
                  <p className="text-sm text-gray-700 px-4">No new notifications.</p>
                </div>
              )}
            </div>

            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setShowDropdown(!showDropdown)}
                className="flex items-center gap-2 rounded-md px-2 py-1.5 hover:bg-white/10 transition-colors duration-200"
              >
                <Avatar name={user?.fullName || 'DP'} size="sm" />
                <FiChevronDown className={`w-4 h-4 transition-transform duration-200 ${showDropdown ? 'rotate-180' : ''}`} />
              </button>

              {showDropdown && (
                <div className="absolute right-0 mt-2 w-44 bg-white rounded-lg shadow-lg py-1 z-50 border border-gray-200">
                  <div className="px-4 py-2 border-b border-gray-100">
                    <p className="text-sm font-medium text-gray-900">{user?.fullName || 'DP'}</p>
                    <p className="text-xs text-gray-500">{user?.email || 'kanban@workspace.com'}</p>
                  </div>

                  <button
                    onClick={handleLogout}
                    className="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                  >
                    <FiLogOut className="w-4 h-4 mr-3" />
                    Log out
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
