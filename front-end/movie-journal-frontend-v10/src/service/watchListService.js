import axios from "axios";

const REST_API_BASE_URL = "http://localhost:8084/watchlist";

function getAuthHeader() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const getWatchlistByUserId = (userId) => {
    return axios.get(`${REST_API_BASE_URL}/user/${userId}`, { headers: getAuthHeader() });
}
export const addToWatchlist = (data) => {
    return axios.post(`${REST_API_BASE_URL}`, data, { headers: getAuthHeader() });
}
export const getWatchLists = () => {
    return axios.get(`${REST_API_BASE_URL}`, { headers: getAuthHeader() });
}
export const updateWatchlist = (id, data) => {
    return axios.put(`${REST_API_BASE_URL}/${id}`, data, { headers: getAuthHeader() });
}
export const deleteWatchlist = (id) => {
    return axios.delete(`${REST_API_BASE_URL}/${id}`, { headers: getAuthHeader() });
}


