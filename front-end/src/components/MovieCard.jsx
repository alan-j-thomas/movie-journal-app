import React, { useState, useEffect } from 'react'
import '../components/css/MovieCard.css'
import { listMovies } from '../service/ListMovies';
import axios from 'axios';
import MovieDetailsOverlay from './MovieDetailsOverlay';

const fallbackPoster = '/src/assets/logo.png';

const MovieCard = ({ limit, enableFilters, movies: propMovies }) => {
    const [movies, setMovies] = useState([]);
    const [imageUrls, setImageUrls] = useState({});
    const [selectedMovie, setSelectedMovie] = useState(null);

    const [genreFilter, setGenreFilter] = useState('');
    const [search, setSearch] = useState('');

    useEffect(() => {
      if (propMovies) {
        setMovies(propMovies);
      } else {
        listMovies().then((response) => {
          setMovies(response.data);
        }).catch(err => {
          console.error(err);
        });
      }
    }, [propMovies])

    useEffect(() => {
      const fetchImages = async () => {
        const token = localStorage.getItem('token');
        const urls = {};
        await Promise.all(movies.map(async (movie) => {
          if (movie.image_data) {
            try {
              const res = await axios.get(`http://localhost:8082/movie/${movie.movie_id}/image`, {
                headers: token ? { Authorization: `Bearer ${token}` } : {},
                responseType: 'blob',
              });
              const imageUrl = URL.createObjectURL(res.data);
              urls[movie.movie_id] = imageUrl;
            } catch (err) {
              urls[movie.movie_id] = '';
            }
          } else {
            urls[movie.movie_id] = '';
          }
        }));
        setImageUrls(urls);
      };
      if (movies.length > 0) fetchImages();
    }, [movies]);

    // This is to get the unique genres for filter dropdown
    const genres = Array.from(new Set(
      movies.flatMap(m =>
        m.genre ? m.genre.split(',').map(g => g.trim()) : []
      )
    ));

    let filteredMovies = movies;
    if (genreFilter) {
      filteredMovies = filteredMovies.filter(m =>
        m.genre && m.genre.split(',').map(g => g.trim()).includes(genreFilter)
      );
    }
    if (search) {
      filteredMovies = filteredMovies.filter(m => m.title.toLowerCase().includes(search.toLowerCase()));
    }
    const displayMovies = limit ? filteredMovies.slice(0, limit) : filteredMovies;

    return (
      <>
      {enableFilters && (
        <div className="movie-filters">
          <select value={genreFilter} onChange={e => setGenreFilter(e.target.value)}>
            <option value="">All Genres</option>
            {genres.map(g => <option key={g} value={g}>{g}</option>)}
          </select>
          <input
            type="text"
            placeholder="Search by title..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{marginLeft: '12px'}}
          />
        </div>
      )}
      <div className='card-container outer-rounded'>
        {displayMovies.map((movie) => (
          <div className='container' key={movie.movie_id} onClick={() => setSelectedMovie(movie)} style={{cursor:'pointer'}}>
            {imageUrls[movie.movie_id] ? (
              <img
                id='movie-poster'
                src={imageUrls[movie.movie_id]}
                alt={movie.title}
              />
            ) : (
              <div className='fallback-poster'>No Image</div>
            )}
            <div className="card-info">
              <h2>{movie.title} </h2>
              <p>{movie.genre}</p>
              
            </div>
          </div>
        ))}
      </div>
      <MovieDetailsOverlay movie={selectedMovie} onClose={() => setSelectedMovie(null)} />
      </>
    )
}

export default MovieCard