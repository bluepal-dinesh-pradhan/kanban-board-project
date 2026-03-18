import { createContext, useContext, useState, useEffect, useRef } from 'react'
import { notificationAPI } from '../api/endpoints'
import { useAuth } from './AuthContext'

const NotificationContext = createContext()

export const useNotifications = () => {
  const context = useContext(NotificationContext)
  if (!context) {
    throw new Error('useNotifications must be used within NotificationProvider')
  }
  return context
}

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const { isAuthenticated } = useAuth()
  const seenNotificationIds = useRef(new Set())

  // Request browser notification permission on mount
  useEffect(() => {
    if (isAuthenticated && 'Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }
  }, [isAuthenticated])

  const fetchNotifications = async () => {
    if (!isAuthenticated) return
    
    try {
      setLoading(true)
      const response = await notificationAPI.getNotifications()
      setNotifications(response.data.data)
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchUnreadCount = async () => {
    if (!isAuthenticated) return
    
    try {
      const response = await notificationAPI.getUnreadCount()
      setUnreadCount(response.data.data)
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  }

  const markAsRead = async (notificationId) => {
    try {
      await notificationAPI.markAsRead(notificationId)
      setNotifications(prev => 
        prev.map(notif => 
          notif.id === notificationId ? { ...notif, isRead: true } : notif
        )
      )
      setUnreadCount(prev => Math.max(0, prev - 1))
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  const markAllAsRead = async () => {
    try {
      await notificationAPI.markAllAsRead()
      setNotifications(prev => 
        prev.map(notif => ({ ...notif, isRead: true }))
      )
      setUnreadCount(0)
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error)
    }
  }

  const showBrowserNotification = (title, message, cardId, boardId) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      const notification = new Notification(title, {
        body: message,
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        tag: `card-${cardId}`,
        requireInteraction: true
      })

      notification.onclick = () => {
        window.focus()
        // Navigate to the board
        window.location.href = `/boards/${boardId}`
        notification.close()
      }

      // Auto close after 10 seconds
      setTimeout(() => notification.close(), 10000)
    }
  }

  useEffect(() => {
    if (!notifications.length) return
    notifications.forEach((notification) => {
      if (seenNotificationIds.current.has(notification.id)) return
      seenNotificationIds.current.add(notification.id)

      if (!notification.isRead && notification.type === 'DUE_DATE_REMINDER') {
        showBrowserNotification(
          notification.title || 'Due Date Reminder',
          notification.message,
          notification.cardId,
          notification.boardId
        )
      }
    })
  }, [notifications])

  // Poll for new notifications every 2 minutes
  useEffect(() => {
    if (!isAuthenticated) return

    fetchNotifications()
    fetchUnreadCount()

    seenNotificationIds.current = new Set()

    const interval = setInterval(() => {
      fetchUnreadCount()
      fetchNotifications()
    }, 30000) // 30 seconds

    return () => clearInterval(interval)
  }, [isAuthenticated])

  const value = {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    showBrowserNotification
  }

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  )
}
