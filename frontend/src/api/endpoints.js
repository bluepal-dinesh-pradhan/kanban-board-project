import api from './axios'

// Auth endpoints
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  checkUserExists: (email) => api.get(`/auth/check-user?email=${encodeURIComponent(email)}`),
}

// Board endpoints
export const boardAPI = {
  getBoards: () => api.get('/boards'),
  createBoard: (boardData) => api.post('/boards', boardData),
  updateBoard: (boardId, boardData) => api.patch(`/boards/${boardId}`, boardData),
  getBoardColumns: (boardId) => api.get(`/boards/${boardId}/columns`),
  getBoardMembers: (boardId) => api.get(`/boards/${boardId}/members`),
  inviteMember: (boardId, inviteData) => api.post(`/boards/${boardId}/members`, inviteData),
  removeMember: (boardId, memberId) => api.delete(`/boards/${boardId}/members/${memberId}`),
  cancelInvitation: (boardId, invitationId) => api.delete(`/boards/${boardId}/invitations/${invitationId}`),
  getBoardActivity: (boardId) => api.get(`/boards/${boardId}/activity`),
}

// Invitation endpoints
export const invitationAPI = {
  getInvitation: (token) => api.get(`/invitations/${encodeURIComponent(token)}`),
  acceptInvitation: (token) => api.post(`/invitations/${encodeURIComponent(token)}/accept`),
}

// Column endpoints
export const columnAPI = {
  createColumn: (boardId, columnData) => api.post(`/boards/${boardId}/columns`, columnData),
}

// Card endpoints
export const cardAPI = {
  createCard: (boardId, cardData) => api.post(`/boards/${boardId}/cards`, cardData),
  getCard: (cardId) => api.get(`/cards/${cardId}`),
  updateCard: (cardId, cardData) => api.put(`/cards/${cardId}`, cardData),
  moveCard: (cardId, moveData) => api.patch(`/cards/${cardId}/move`, moveData),
  archiveCard: (cardId) => api.patch(`/cards/${cardId}/archive`),
  getCardComments: (cardId) => api.get(`/cards/${cardId}/comments`),
  addComment: (cardId, commentData) => api.post(`/cards/${cardId}/comments`, commentData),
}

// Notification endpoints
export const notificationAPI = {
  getNotifications: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (notificationId) => api.patch(`/notifications/${notificationId}/read`),
  markAllAsRead: () => api.patch('/notifications/mark-all-read'),
}
