import React, { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import "./css/Home.css"; 
import { useContext } from "react";
import { AuthContext } from "./AuthContext";
import logo from "../assets/logo.png";
import MovieCard from "./MovieCard";
import JournalService from '../service/journalService';
import { getWatchlistByUserId } from '../service/watchListService';
import { listMovies } from '../service/ListMovies';

const funFacts = [
  "The longest movie ever made is 'Logistics' (2012), which runs for 857 hours (35 days)!",
  "The first feature-length animated film was 'El Apóstol' (1917) from Argentina.",
  "The iconic roar of the T-Rex in 'Jurassic Park' was made by combining animal sounds, including a baby elephant!",
  "Alfred Hitchcock never won an Oscar for Best Director, despite being nominated five times.",
  "The most expensive movie ever made is 'Pirates of the Caribbean: On Stranger Tides' (2011).",
  "The code in 'The Matrix' is actually recipes for sushi, scanned from a Japanese cookbook.",
  "In 'Psycho' (1960), the sound of stabbing was made by plunging a knife into a melon.",
  "The first movie ever shot in Hollywood was 'In Old California' (1910).",
  "The lightsaber sound in 'Star Wars' was created by combining the hum of an old projector and TV static.",
  "The Oscar statuette’s official name is the 'Academy Award of Merit.'"
];

const Home = () => {
  const { user } = useContext(AuthContext) || {};
  const storedUser = JSON.parse(localStorage.getItem("user")) || {};
  const username = user?.name || storedUser?.name || "Guest";
  const userId = user?.id || storedUser?.id;
  const [fact, setFact] = useState(funFacts[0]);
  const [stats, setStats] = useState({ movies: 0, journals: 0, watchlists: 0 });
  const [loadingStats, setLoadingStats] = useState(true);
  const [featuredMovies, setFeaturedMovies] = useState([]);
  
  // Fetch and filter featured movies (rating > 8, top 4 by rating)
  useEffect(() => {
    const fetchFeatured = async () => {
      try {
        const res = await listMovies();
        let movies = res.data || [];
        // Filter movies with rating > 8
        movies = movies.filter(m => Number(m.rating) > 8);
        // Sort by rating descending, then by title (as a fallback for tie)
        movies.sort((a, b) => Number(b.rating) - Number(a.rating) || a.title.localeCompare(b.title));
        setFeaturedMovies(movies.slice(0, 4));
      } catch (err) {
        setFeaturedMovies([]);
      }
    };
    fetchFeatured();
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setFact(funFacts[Math.floor(Math.random() * funFacts.length)]);
    }, 6000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (!userId) {
      setLoadingStats(false);
      return;
    }
    setLoadingStats(true);
    // Fetch user stats
    const fetchStats = async () => {
      try {
        // Get all movies (diary entries)
        const moviesRes = await listMovies();
        // Get all journals (reviews)
        const journalsRes = await JournalService.getJournalsByUser(userId);
        // Get all watchlist items
        const watchlistRes = await getWatchlistByUserId(userId);
        setStats({
          movies: moviesRes.data.length,
          journals: journalsRes.data.length,
          watchlists: watchlistRes.data.length
        });
      } catch (err) {
        setStats({ movies: 0, journals: 0, watchlists: 0 });
      } finally {
        setLoadingStats(false);
      }
    };
    fetchStats();
  }, [userId]);

  return (
    <div className="home-bg">
      <div className="home-glass">
        <img src={logo} alt="Movie Journal Logo" className="home-logo" />
        <h1 className="home-title">Welcome, {username.split(" ")[0]}</h1>
        <div className="home-tagline">
          Your films. Your lists. Your life in movies.
        </div>
        <div className="home-stats">
          {loadingStats ? (
            <div className="stat" style={{width: '100%'}}>Loading...</div>
          ) : (
            <>
              <div className="stat"><span>{stats.movies}</span> Movies</div>
              <div className="stat"><span>{stats.journals}</span> Journals</div>
              <div className="stat"><span>{stats.watchlists}</span> Watchlists</div>
            </>
          )}
        </div>
        <p className="home-description">
          Track, review, and share your cinematic journey.<br/>
          Discover new favorites, revisit classics, and curate your own movie diary.
        </p>
        <div className="home-fun-fact">{fact}</div>
        <div className="home-buttons">
          <NavLink to="/watchlists" className="home-btn">
            Watchlist
          </NavLink>
          <NavLink to="/journals" className="home-btn">
            Journal
          </NavLink>
        </div>
      </div>
      <div className="featured-movies-section">
        <h2 className="featured-title">Popular on MovieLedger</h2>
        <div className="featured-movies-carousel">
          <MovieCard movies={featuredMovies} />
        </div>
      </div>
    </div>
  );
};

export default Home;
