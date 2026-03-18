import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useNotifications } from '../context/NotificationContext'
import Avatar from './common/Avatar'
import { FiChevronDown, FiLogOut, FiPlus, FiBell, FiSearch, FiCheck, FiCheckCircle, FiCalendar } from 'react-icons/fi'
import { timeAgo } from '../utils/timeAgo'

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
    <nav className="shadow-[0_2px_10px_rgba(37,99,235,0.3)] z-50 relative" style={{ background: 'linear-gradient(135deg, #1e40af 0%, #2563eb 100%)' }}>
      <div className="max-w-[1400px] mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center h-14 gap-4">
          <Link to="/boards" className="flex items-center gap-2.5 text-white group">
            <div className="w-8 h-8 bg-white/90 rounded-lg flex items-center justify-center group-hover:bg-white">
              <div className="w-5 h-5 bg-blue-700 rounded-[4px]"></div>
            </div>
            <span className="font-semibold text-[17px] tracking-tight">Kanban</span>
          </Link>

          <div className="flex-1 flex items-center justify-center">
            <div className="relative w-56 sm:w-72 focus-within:w-96 transition-all duration-300">
              <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-white/50" />
              <input
                type="text"
                placeholder="Search"
                value={resolvedSearchValue}
                onChange={handleSearchChange}
                className="w-full h-9 pl-9 pr-3 rounded-lg bg-white/[0.15] backdrop-blur-sm text-white placeholder-white/50 text-sm border border-white/20 focus:outline-none focus:bg-white/20 focus:border-white/30 focus:ring-2 focus:ring-white/30 transition-all duration-200"
              />
            </div>
          </div>

          <div className="flex items-center gap-3 text-white">
            <button
              onClick={onCreate}
              className="hidden sm:inline-flex items-center gap-1.5 px-3.5 h-9 rounded-lg bg-white/20 hover:bg-white/30 transition-colors duration-200 text-sm font-semibold border border-white/[0.15] backdrop-blur-sm"
            >
              <FiPlus className="w-4 h-4" />
              Create
            </button>
            <div className="relative" ref={notificationRef}>
              <button
                onClick={handleNotificationToggle}
                className="relative inline-flex items-center justify-center w-9 h-9 rounded-full hover:bg-white/[0.15] transition-colors"
              >
                <FiBell className="w-[18px] h-[18px]" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] rounded-full min-w-[18px] h-[18px] px-1 flex items-center justify-center font-semibold shadow-sm">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </button>
              {showNotifications && (
                <div className="absolute right-0 mt-2.5 w-[380px] bg-white rounded-xl shadow-[0_20px_60px_rgba(0,0,0,0.15)] z-50 border border-gray-100 max-h-[28rem] overflow-hidden animate-slide-in-up">
                  <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
                    <h3 className="font-semibold text-gray-900 text-[15px]">Notifications</h3>
                    {unreadCount > 0 && (
                      <button
                        onClick={handleMarkAllRead}
                        className="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1"
                      >
                        <FiCheckCircle className="w-3.5 h-3.5" />
                        Mark all read
                      </button>
                    )}
                  </div>
                  {notifications.length === 0 ? (
                    <div className="px-5 py-12 text-center bg-gray-50/50">
                      <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                        <FiCheckCircle className="w-6 h-6 text-green-600" />
                      </div>
                      <h4 className="font-semibold text-gray-900 text-sm">You're all caught up!</h4>
                      <p className="text-xs text-gray-500 mt-1">No new notifications at this time.</p>
                    </div>
                  ) : (
                    <>
                      <div className="max-h-[320px] overflow-y-auto scrollbar-thin divide-y divide-[#f1f5f9]">
                        {notifications.map((notification) => (
                          <div
                            key={notification.id}
                            onClick={() => handleNotificationClick(notification)}
                            className={`px-5 py-4 cursor-pointer transition-colors duration-200 border-l-[3px] ${
                              notification.isRead
                                ? 'border-transparent bg-white hover:bg-[#f8fafc]'
                                : 'border-[#2563eb] bg-[#eff6ff] hover:bg-[#dbeafe]'
                            }`}
                          >
                            <div className="flex items-start gap-3">
                              <div className={`mt-0.5 w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${notification.isRead ? 'bg-gray-100' : 'bg-blue-100'}`}>
                                {notification.title.toLowerCase().includes('due') || notification.title.toLowerCase().includes('date') 
                                  ? <FiCalendar className={`w-3.5 h-3.5 ${notification.isRead ? 'text-gray-500' : 'text-blue-600'}`} />
                                  : <FiBell className={`w-3.5 h-3.5 ${notification.isRead ? 'text-gray-500' : 'text-blue-600'}`} />
                                }
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-0.5">
                                  <p className={`text-[13.5px] truncate ${notification.isRead ? 'text-gray-600 font-medium' : 'text-gray-900 font-bold'}`}>
                                    {notification.title}
                                  </p>
                                  {!notification.isRead && (
                                    <div className="w-1.5 h-1.5 bg-[#2563eb] rounded-full flex-shrink-0"></div>
                                  )}
                                </div>
                                <p className={`text-[12.5px] line-clamp-2 leading-relaxed ${notification.isRead ? 'text-gray-500' : 'text-gray-700 font-medium'}`}>
                                  {notification.message}
                                </p>
                                <p className="text-[11px] text-gray-400 mt-1.5 flex items-center gap-1">
                                  <FiBell className="w-2.5 h-2.5 opacity-50" />
                                  {timeAgo(notification.createdAt)}
                                </p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                      <div className="p-2 border-t border-gray-100 bg-gray-50/50">
                        <Link to="/boards" className="block w-full py-2 text-center text-xs font-semibold text-gray-600 hover:text-blue-600 hover:bg-white rounded-lg transition-all">
                          View all notifications
                        </Link>
                      </div>
                    </>
                  )}
                </div>
              )}
            </div>

            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setShowDropdown(!showDropdown)}
                className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-white/10 transition-colors group"
              >
                <div className="ring-2 ring-white/30 rounded-full transition-transform duration-200 group-hover:scale-105 shadow-sm">
                  <Avatar name={user?.fullName || 'JD'} size="sm" />
                </div>
                <FiChevronDown className={`w-3.5 h-3.5 opacity-70 transition-transform duration-200 ${showDropdown ? 'rotate-180' : ''}`} />
              </button>

              {showDropdown && (
                <div className="absolute right-0 mt-2.5 w-60 bg-white rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.15)] p-2 z-50 border border-gray-100 origin-top-right animate-scale-in">
                  <div className="flex flex-col items-center px-4 py-5 border-b border-[#f1f5f9] mb-1">
                    <div className="mb-3 ring-4 ring-gray-50 rounded-full shadow-sm">
                      <Avatar name={user?.fullName || 'JD'} size="lg" />
                    </div>
                    <p className="text-sm font-bold text-gray-900 tracking-tight">{user?.fullName || 'JD'}</p>
                    <p className="text-[11px] text-[#94a3b8] font-semibold uppercase tracking-wider mt-1">{user?.email || 'kanban@workspace.com'}</p>
                  </div>

                  <div className="space-y-0.5">
                    <button
                      onClick={handleLogout}
                      className="flex items-center w-full px-4 py-3 text-sm font-bold text-[#dc2626] hover:bg-red-50 rounded-lg transition-all"
                    >
                      <FiLogOut className="w-4 h-4 mr-3 opacity-80" />
                      Log out
                    </button>
                  </div>
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
