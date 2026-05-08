import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

export const dumpApi = {
  createDump: (rawText) => api.post('/dump', { rawText }),
  getAllDumps: () => api.get('/dumps'),
  getDumpById: (id) => api.get(`/dumps/${id}`),
  getDumpCount: () => api.get('/dumps/count'),
  getPatternInsight: () => api.get('/pattern-insight'),
  toggleItem: (id, item) => api.patch(`/dumps/${id}/toggle-item`, { item }),
};

export default api;
