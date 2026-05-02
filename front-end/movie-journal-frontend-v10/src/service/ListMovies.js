import axios from "axios";

const REST_API_BASE_URL = "http://localhost:8082/movie";

function getAuthHeader() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const listMovies = () => {
    return axios.get(REST_API_BASE_URL, { headers: getAuthHeader() });
}

export const getMovieById = (id) => {
    return axios.get(`${REST_API_BASE_URL}/${id}`, { headers: getAuthHeader() });
}

export const getMovieByTitle = (title) => {
    return axios.get(`${REST_API_BASE_URL}/title/${title}`, { headers: getAuthHeader() });
}
