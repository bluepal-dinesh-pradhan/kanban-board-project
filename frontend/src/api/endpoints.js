import api from './axios'

const buildParams = (paramsOrPage, size) => {
  if (paramsOrPage && typeof paramsOrPage === 'object') {
    return paramsOrPage
  }
  if (paramsOrPage !== undefined && size !== undefined) {
    return { page: paramsOrPage, size }
  }
  return undefined
}

// Board endpoints
export const boardAPI = {
  getBoards: (paramsOrPage, size) => api.get('/boards', { params: buildParams(paramsOrPage, size) }),
  getBoard: (id) => api.get(`/boards/${id}`),
  createBoard: (boardData) => api.post('/boards', boardData),
  updateBoard: (id, boardData) => api.patch(`/boards/${id}`, boardData),
  getBoardColumns: (id, paramsOrPage, size) => api.get(`/boards/${id}/columns`, { params: buildParams(paramsOrPage, size) }),
  getBoardMembers: (id) => api.get(`/boards/${id}/members`),
  inviteMember: (boardId, inviteData) => api.post(`/boards/${boardId}/members`, inviteData),
  removeMember: (boardId, memberId) => api.delete(`/boards/${boardId}/members/${memberId}`),
  cancelInvitation: (boardId, invitationId) => api.delete(`/boards/${boardId}/invitations/${invitationId}`),
  getBoardActivity: (id, paramsOrPage, size) => api.get(`/boards/${id}/activity`, { params: buildParams(paramsOrPage, size) }),
  deleteBoard: (id) => api.delete(`/boards/${id}`),
}

// Invitation endpoints
export const invitationAPI = {
  getInvitation: (token) => api.get(`/invitations/${encodeURIComponent(token)}`),
  acceptInvitation: (token) => api.post(`/invitations/${encodeURIComponent(token)}/accept`),
}

// Column endpoints
export const columnAPI = {
  createColumn: (boardId, columnData) => api.post(`/boards/${boardId}/columns`, columnData),
  updateColumn: (columnId, columnData) => api.patch(`/columns/${columnId}`, columnData),
  deleteColumn: (columnId) => api.delete(`/columns/${columnId}`),
  moveColumn: (columnId, moveData) => api.post(`/columns/${columnId}/move`, moveData),
}

// Card endpoints
export const cardAPI = {
  getCard: (id) => api.get(`/cards/${id}`),
  createCard: (boardId, cardData) => api.post(`/cards/boards/${boardId}`, cardData),
  updateCard: (id, cardData) => api.patch(`/cards/${id}`, cardData),
  moveCard: (id, moveData) => api.post(`/cards/${id}/move`, moveData),
  archiveCard: (id) => api.post(`/cards/${id}/archive`),
  deleteCard: (id) => api.delete(`/cards/${id}`),
  getComments: (cardId, paramsOrPage, size) => api.get(`/cards/${cardId}/comments`, { params: buildParams(paramsOrPage, size) }),
  addComment: (cardId, commentData) => api.post(`/cards/${cardId}/comments`, commentData),
  deleteComment: (boardId, cardId, commentId) => api.delete(`/cards/boards/${boardId}/cards/${cardId}/comments/${commentId}`),
}

// User endpoints
export const userAPI = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (profileData) => api.patch('/users/profile', profileData),
}

// Notification endpoints
export const notificationAPI = {
  getNotifications: (paramsOrPage, size) => api.get('/notifications', { params: buildParams(paramsOrPage, size) }),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.patch('/notifications/mark-all-read'),
}

// Auth endpoints
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
}

export default api;
