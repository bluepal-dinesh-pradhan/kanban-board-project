import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const getWsUrl = () => {
  // Use the same base as API but with /ws endpoint
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  // Remove trailing /api if present
  const base = apiUrl.replace(/\/api\/?$/, '')
  return `${base}/ws`
}

/**
 * Hook to connect to WebSocket and subscribe to board-level events.
 * 
 * @param {string|number} boardId - The board to subscribe to
 * @param {function} onBoardEvent - Callback when a board event arrives
 */
const useWebSocket = (boardId, onBoardEvent) => {
  const clientRef = useRef(null)
  const onBoardEventRef = useRef(onBoardEvent)

  // Keep callback ref updated without re-triggering effect
  useEffect(() => {
    onBoardEventRef.current = onBoardEvent
  }, [onBoardEvent])

  useEffect(() => {
    if (!boardId) return

    const token = localStorage.getItem('accessToken')
    if (!token) return

    const client = new Client({
      webSocketFactory: () => new SockJS(getWsUrl()),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('[WebSocket] Connected to board', boardId)

        // Subscribe to board events (card/column/comment changes)
        client.subscribe(`/topic/board/${boardId}`, (message) => {
          try {
            const event = JSON.parse(message.body)
            onBoardEventRef.current?.(event)
          } catch (e) {
            console.warn('[WebSocket] Failed to parse board event:', e)
          }
        })

        // Subscribe to activity feed updates
        client.subscribe(`/topic/board/${boardId}/activity`, (message) => {
          try {
            const activity = JSON.parse(message.body)
            onBoardEventRef.current?.({
              eventType: 'activity.new',
              payload: activity,
            })
          } catch (e) {
            console.warn('[WebSocket] Failed to parse activity event:', e)
          }
        })
      },

      onDisconnect: () => {
        console.log('[WebSocket] Disconnected from board', boardId)
      },

      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers?.message || frame)
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      console.log('[WebSocket] Cleaning up connection for board', boardId)
      if (clientRef.current?.active) {
        clientRef.current.deactivate()
      }
    }
  }, [boardId])

  return clientRef
}

export default useWebSocket