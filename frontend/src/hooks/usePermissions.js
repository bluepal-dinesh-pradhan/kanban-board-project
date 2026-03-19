import { useQuery } from '@tanstack/react-query'
import { boardAPI } from '../api/endpoints'
import { useAuth } from '../context/AuthContext'

/**
 * Custom hook to manage board permissions.
 * Returns { isOwner, isEditor, isViewer, canEdit, canInvite, canDeleteBoard, isLoading }
 */
export const usePermissions = (boardId) => {
  const { user } = useAuth()

  const { data: boards, isLoading } = useQuery({
    queryKey: ['boards'],
    queryFn: async () => {
      const response = await boardAPI.getBoards()
      return response.data.data
    },
    staleTime: 5 * 60 * 1000
  })

  const board = boards?.find((item) => String(item.id) === String(boardId))
  const role = board?.role || null

  const isOwner = role === 'OWNER'
  const isEditor = role === 'EDITOR'
  const isViewer = role === 'VIEWER'
  const canEdit = isOwner || isEditor
  const canInvite = isOwner
  const canDeleteBoard = isOwner

  return {
    isOwner,
    isEditor,
    isViewer,
    canEdit,
    canInvite,
    canDeleteBoard,
    isLoading
  }
}
