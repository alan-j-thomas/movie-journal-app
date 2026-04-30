import axios from "axios";

const REST_API_BASE_URL = "http://localhost:9090/users";

function getAuthHeader() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const getUserIdByUserName = (userName) => {
    return axios.get(`${REST_API_BASE_URL}/username/${userName}`, { headers: getAuthHeader() });
}