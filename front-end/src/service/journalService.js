import axios from 'axios';

const API_URL = 'http://localhost:8083/journals';

function getAuthHeader() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

const JournalService = {
  getAllJournals: () => axios.get(API_URL, { headers: getAuthHeader() }),
  getJournalsByUser: (userId) => axios.get(`${API_URL}/user/${userId}`, { headers: getAuthHeader() }),
  addJournal: (journalData) => axios.post(API_URL, journalData, { headers: getAuthHeader() }),
  addToJournal: (data) => axios.post("http://localhost:8083/journals", data, { headers: getAuthHeader() }),
  deleteJournal: (journalId) => axios.delete(`${API_URL}/${journalId}`, { headers: getAuthHeader() }),
  updateJournal:(editingId, updatedJournal) => axios.put(`${API_URL}/${editingId}`, updatedJournal, { headers: getAuthHeader() })
};

export default JournalService;
