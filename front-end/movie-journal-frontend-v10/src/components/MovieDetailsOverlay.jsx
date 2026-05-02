import React from 'react';
import './css/MovieDetailsOverlay.css';

function MovieDetailsOverlay({ movie, onClose }) {
  if (!movie) return null;

  let posterSrc = '';
  if (movie.imageData && movie.imageType) {
    posterSrc = `data:${movie.imageType};base64,${movie.imageData}`;
  }

  return (
    <div className="movie-overlay-backdrop" onClick={onClose}>
      <div className="movie-overlay-card" onClick={e => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose}>&times;</button>
        <div className="movie-overlay-content">
          <img
            className="movie-overlay-poster"
            src={posterSrc || '/default-poster.png'}
            alt={movie.title}
          />
          <div className="movie-overlay-info">
            <h2>{movie.title}</h2>
            <p><strong>Genre:</strong> {movie.genre}</p>
            <p><strong>Release Year:</strong> {movie.releaseYear}</p>
            <p><strong>Director:</strong> {movie.director || 'N/A'}</p>
            <p><strong>Cast:</strong> {movie.cast || 'N/A'}</p>
            <p><strong>Language:</strong> {movie.language || 'N/A'}</p>
            <p><strong>Rating:</strong> {movie.rating ? movie.rating + '/10' : 'N/A'}</p>
            <p><strong>Summary:</strong> {movie.summary || 'No summary available.'}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MovieDetailsOverlay;
