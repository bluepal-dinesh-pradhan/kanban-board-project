import api from './axios'

// Auth endpoints
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
}

// Board endpoints
export const boardAPI = {
  getBoards: () => api.get('/boards'),
  createBoard: (boardData) => api.post('/boards', boardData),
  getBoardColumns: (boardId) => api.get(`/boards/${boardId}/columns`),
  inviteMember: (boardId, inviteData) => api.post(`/boards/${boardId}/members`, inviteData),
  getBoardActivity: (boardId) => api.get(`/boards/${boardId}/activity`),
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
  getCardComments: (cardId) => api.get(`/cards/${cardId}/comments`),
  addComment: (cardId, commentData) => api.post(`/cards/${cardId}/comments`, commentData),
}