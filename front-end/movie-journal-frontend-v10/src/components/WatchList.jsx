import React, { useContext, useEffect, useState } from "react";
import {
  getWatchlistByUserId,
  deleteWatchlist,
  addToWatchlist,
  updateWatchlist
} from "../service/watchListService";
import { getMovieById, getMovieByTitle, listMovies } from "../service/ListMovies";
import "./css/WatchList.css";
import AuthProvider, { AuthContext } from "./AuthContext";
import axios from 'axios';


const WatchList = () => {
  const [watchlist, setWatchlist] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [movieTitle, setMovieTitle] = useState("");
  const [status, setStatus] = useState("");
  const [note, setNote] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [imageUrls, setImageUrls] = useState({});
  const [movieOptions, setMovieOptions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [statusFilter, setStatusFilter] = useState('ALL');

  const { user } = useContext(AuthContext);
  const userId = user?.userId;


  useEffect(() => {
    if (userId) {
      fetchWatchlist();
    }
  }, [userId]); // Fetch watchlist only when userId is available

  const fetchWatchlist = async () => {
    try {
      if (!userId) return; 

      const response = await getWatchlistByUserId(userId);
      const watchlistItems = response?.data ?? [];

      const items = await Promise.all(
        watchlistItems.map(async (item) => {
          try {
            const movieRes = await getMovieById(item.movie_id);
            return {
              ...item,
              movie: movieRes?.data ?? {
                title: "Unknown",
                posterUrl: "",
                genre: "N/A",
                releaseYear: 0
              }
            };
          } catch (movieErr) {
            return {
              ...item,
              movie: {
                title: "Unknown",
                posterUrl: "",
                genre: "N/A",
                releaseYear: 0
              }
            };
          }
        })
      );

      setWatchlist(items);
    } catch (err) {
      console.error("Failed to load watchlist", err);
    }
  };

  const handleDelete = async (watchId) => {
    try {
      await deleteWatchlist(watchId);
      setWatchlist((prev) => prev.filter((item) => item.watch_id !== watchId));
    } catch (err) {
      console.error("Failed to delete watchlist item:", err);
    }
  };

  const handleAdd = async (e) => {
    e.preventDefault();

    try {
      const movieRes = await getMovieByTitle(movieTitle);
      const movie = movieRes?.data;

      if (!movie || !movie.movie_id) {
        alert("Movie not found");
        return;
      }

      const newEntry = {
        movie_id: movie.movie_id,
        user_id: userId,
        status: status,
        note: note,
      };

      await addToWatchlist(newEntry);
      fetchWatchlist();
      resetForm();
    } catch (err) {
      console.error("Error adding movie to watchlist", err);
    }
  };

  const handleEdit = (item) => {
    setEditingId(item.watch_id);
    setMovieTitle(item.movie.title);
    setStatus(item.status);
    setNote(item.note);
    setShowForm(true);
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const updatedEntry = {
        status: status,
        note: note,
      };

      await updateWatchlist(editingId, updatedEntry);
      fetchWatchlist();
      resetForm();
    } catch (err) {
      console.error("Error updating watchlist item", err);
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setMovieTitle("");
    setStatus("");
    setNote("");
    setEditingId(null);
  };

  useEffect(() => {
    const fetchImages = async () => {
      const token = localStorage.getItem('token');
      const urls = {};
      await Promise.all(watchlist.map(async (item) => {
        const movieId = item.movie?.movie_id;
        if (movieId) {
          try {
            const res = await axios.get(`http://localhost:8082/movie/${movieId}/image`, {
              headers: token ? { Authorization: `Bearer ${token}` } : {},
              responseType: 'blob',
            });
            const imageUrl = URL.createObjectURL(res.data);
            urls[item.watch_id] = imageUrl;
          } catch (err) {
            urls[item.watch_id] = 'src/assets/poster.jpg';
          }
        } else {
          urls[item.watch_id] = 'src/assets/poster.jpg';
        }
      }));
      setImageUrls(urls);
    };
    if (watchlist.length > 0) fetchImages();
  }, [watchlist]);


  useEffect(() => {
    // Fetch all movie titles for dropdown
    async function fetchMovies() {
      try {
        const res = await listMovies();
        setMovieOptions(res.data.map(m => m.title));
      } catch (err) {
        setMovieOptions([]);
      }
    }
    fetchMovies();
  }, []);

  // Filter watchlist by status
  const filteredWatchlist = statusFilter === 'ALL'
    ? watchlist
    : watchlist.filter(item => item.status === statusFilter);

  return (
    <div className="watchlist-container">
      <div className="watchlist-header-row">
        <h2 className="watchlist-title">Your WatchLists</h2>
        <button className="add-btn" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Close Form" : "Add to Watchlist"}
        </button>
        <div className="watchlist-filters">
          <button
            className={`status-btn${statusFilter === 'PLANNED' ? ' active' : ''}`}
            onClick={() => setStatusFilter('PLANNED')}
          >PLANNED</button>
          <button
            className={`status-btn${statusFilter === 'WATCHING' ? ' active' : ''}`}
            onClick={() => setStatusFilter('WATCHING')}
          >WATCHING</button>
          <button
            className={`status-btn${statusFilter === 'COMPLETED' ? ' active' : ''}`}
            onClick={() => setStatusFilter('COMPLETED')}
          >COMPLETED</button>
          <button
            className={`status-btn${statusFilter === 'ALL' ? ' active' : ''}`}
            onClick={() => setStatusFilter('ALL')}
          >ALL</button>
        </div>
      </div>

      {showForm && (
        <form className="watchlist-form" onSubmit={editingId ? handleUpdate : handleAdd}>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              placeholder="Enter movie title"
              value={movieTitle}
              onChange={e => {
                setMovieTitle(e.target.value);
                setShowDropdown(true);
              }}
              onFocus={() => {
                setShowDropdown(true);
                if (!movieTitle) setMovieTitle(' '); // Trigger dropdown on click if empty
              }}
              onBlur={() => setTimeout(() => setShowDropdown(false), 150)}
              disabled={editingId !== null} // Prevent editing title in update mode
              required
            />
            {showDropdown && movieTitle && (
              <ul className="movie-dropdown">
                {movieOptions.filter(title => title.toLowerCase().includes(movieTitle.toLowerCase().trim())).slice(0, 8).map(title => (
                  <li key={title} onMouseDown={() => { setMovieTitle(title.trim()); setShowDropdown(false); }}>{title}</li>
                ))}
                {movieOptions.filter(title => title.toLowerCase().includes(movieTitle.toLowerCase().trim())).length === 0 && (
                  <li style={{ color: '#888' }}>No matches</li>
                )}
              </ul>
            )}
          </div>
          <select value={status} onChange={(e) => setStatus(e.target.value)} required>
            <option value="">Select status</option>
            <option value="PLANNED">Planned</option>
            <option value="WATCHING">Watching</option>
            <option value="COMPLETED">Completed</option>
          </select>
          <input
            type="text"
            placeholder="Note (optional)"
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />
          <button type="submit">{editingId ? "Update Watchlist" : "Add to Watchlist"}</button>
        </form>
      )}

      <div className="watchlist-item-container">
        {filteredWatchlist.map((item) => (
          <div className="watchlist-item" key={item.watch_id}>
            <img
              src={imageUrls[item.watch_id] || 'src/assets/poster.jpg'}
              alt={item.movie.title}
            />
            <div className="watchlist-details">
              <h3>{item.movie.title}</h3>
              <p>Status: {item.status}</p>
              <p>Note: {item.note}</p>
            </div>
            <div className="watchlist-actions">
              <button onClick={() => handleEdit(item)}>Edit</button>
              <br />
              <button onClick={() => handleDelete(item.watch_id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>

    </div>
  );
};

export default WatchList;
