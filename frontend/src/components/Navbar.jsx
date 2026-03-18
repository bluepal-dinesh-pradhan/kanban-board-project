import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useNotifications } from '../context/NotificationContext'
import Avatar from './common/Avatar'
import { FiChevronDown, FiLogOut, FiPlus, FiBell, FiSearch, FiCheck, FiCheckCircle } from 'react-icons/fi'

const Navbar = ({ searchValue, onSearchChange, onCreate }) => {
  const { user, logout } = useAuth()
  const { notifications, unreadCount, markAsRead, markAllAsRead, fetchNotifications } = useNotifications()
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

  const handleNotificationClick = async (notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id)
    }
    setShowNotifications(false)
    navigate(`/boards/${notification.boardId}`)
  }

  const handleMarkAllRead = async () => {
    await markAllAsRead()
  }

  const handleNotificationToggle = () => {
    setShowNotifications(!showNotifications)
    if (!showNotifications) {
      fetchNotifications()
    }
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
                onClick={handleNotificationToggle}
                className="relative inline-flex items-center justify-center w-9 h-9 rounded-md hover:bg-white/10"
              >
                <FiBell className="w-5 h-5" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-medium">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </button>
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg py-2 z-50 border border-gray-200 max-h-96 overflow-y-auto">
                  <div className="flex items-center justify-between px-4 py-2 border-b border-gray-100">
                    <h3 className="font-medium text-gray-900">Notifications</h3>
                    {unreadCount > 0 && (
                      <button
                        onClick={handleMarkAllRead}
                        className="text-xs text-blue-600 hover:text-blue-800 flex items-center"
                      >
                        <FiCheckCircle className="w-3 h-3 mr-1" />
                        Mark all read
                      </button>
                    )}
                  </div>
                  {notifications.length === 0 ? (
                    <p className="text-sm text-gray-500 px-4 py-6 text-center">No notifications yet.</p>
                  ) : (
                    <div className="max-h-80 overflow-y-auto">
                      {notifications.map((notification) => (
                        <div
                          key={notification.id}
                          onClick={() => handleNotificationClick(notification)}
                          className={`px-4 py-3 hover:bg-gray-50 cursor-pointer border-l-4 ${
                            notification.isRead ? 'border-transparent' : 'border-blue-500 bg-blue-50'
                          }`}
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center">
                                <FiBell className="w-4 h-4 text-amber-500 mr-2 flex-shrink-0" />
                                <p className="text-sm font-medium text-gray-900 truncate">
                                  {notification.title}
                                </p>
                                {!notification.isRead && (
                                  <div className="w-2 h-2 bg-blue-500 rounded-full ml-2 flex-shrink-0"></div>
                                )}
                              </div>
                              <p className="text-xs text-gray-600 mt-1 line-clamp-2">
                                {notification.message}
                              </p>
                              <p className="text-xs text-gray-400 mt-1">
                                {new Date(notification.createdAt).toLocaleString()}
                              </p>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
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
