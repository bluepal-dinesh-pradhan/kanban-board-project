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
  const [loadingMore, setLoadingMore] = useState(false)
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const { isAuthenticated } = useAuth()
  const seenNotificationIds = useRef(new Set())
  const PAGE_SIZE = 20

  // Request browser notification permission on mount
  useEffect(() => {
    if (isAuthenticated && 'Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }
  }, [isAuthenticated])

  const normalizePageData = (data) => {
    if (data && Array.isArray(data.content)) {
      return { items: data.content, hasNext: !!data.hasNext }
    }
    if (Array.isArray(data)) {
      return { items: data, hasNext: false }
    }
    return { items: [], hasNext: false }
  }

  const fetchNotifications = async ({ page: pageParam = 0, append = false, merge = false } = {}) => {
    if (!isAuthenticated) return
    
    try {
      if (append) {
        setLoadingMore(true)
      } else {
        setLoading(true)
      }
      const response = await notificationAPI.getNotifications({ page: pageParam, size: PAGE_SIZE })
      const { items, hasNext } = normalizePageData(response.data.data)
      if (!merge) {
        setHasMore(hasNext)
        setPage(pageParam)
      }

      if (append) {
        setNotifications(prev => {
          const seen = new Set(prev.map(n => n.id))
          const merged = [...prev]
          items.forEach(item => {
            if (!seen.has(item.id)) {
              merged.push(item)
              seen.add(item.id)
            }
          })
          return merged
        })
        return
      }

      if (merge) {
        if (page === 0) {
          setHasMore(hasNext)
          setPage(0)
        }
        setNotifications(prev => {
          const incomingIds = new Set(items.map(n => n.id))
          const rest = prev.filter(n => !incomingIds.has(n.id))
          return [...items, ...rest]
        })
        return
      }

      setNotifications(items)
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      setLoading(false)
      setLoadingMore(false)
    }
  }

  const loadMoreNotifications = async () => {
    if (loadingMore || !hasMore) return
    await fetchNotifications({ page: page + 1, append: true })
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
  // Poll for new notifications
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (!isAuthenticated || !token) {
      // Clear notifications on logout
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    // Small delay to ensure token is set in axios interceptor
    const initialTimeout = setTimeout(() => {
      fetchNotifications({ page: 0, merge: false });
      fetchUnreadCount();
      seenNotificationIds.current = new Set();
    }, 500);

    const interval = setInterval(async () => {
      const currentToken = localStorage.getItem('accessToken');
      if (!currentToken) {
        clearInterval(interval);
        return;
      }
      try {
        await fetchUnreadCount();
        await fetchNotifications({ page: 0, merge: true });
      } catch (error) {
        if (error.response?.status === 401) {
          clearInterval(interval);
        }
      }
    }, 30000);

    return () => {
      clearTimeout(initialTimeout);
      clearInterval(interval);
    };
  }, [isAuthenticated]);

  const value = {
    notifications,
    unreadCount,
    loading,
    loadingMore,
    hasMore,
    fetchNotifications,
    loadMoreNotifications,
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
