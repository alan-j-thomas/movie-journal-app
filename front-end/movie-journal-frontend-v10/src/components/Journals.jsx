import React, { useContext, useEffect, useState } from 'react';
import JournalService from '../service/journalService';
import '../components/css/Journals.css';
import { getMovieByTitle, listMovies } from '../service/ListMovies';
import { AuthContext } from './AuthContext';
import axios from 'axios';

function Journals() {
  const [journals, setJournals] = useState([]);
  const [journalForm, setJournalForm] = useState({ title: '', content: '', moodTag: '' });
  const [movieName, setMovieName] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [imageUrls, setImageUrls] = useState({});
  const [movieOptions, setMovieOptions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const { user } = useContext(AuthContext);
  const userId = user?.userId;

  useEffect(() => {
    if (userId) {
      fetchJournals(userId);
    }

  }, [userId]);

  const fetchJournals = async (userId) => {
    try {
      const { data } = await JournalService.getJournalsByUser(userId);
      setJournals([...data]);
    } catch (error) {
      console.error("Error fetching journals:", error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setJournalForm((prev) => ({ ...prev, [name]: value }));
  };

  const resetForm = () => {
    setJournalForm({ title: '', content: '', moodTag: '' });
    setMovieName('');
    setShowForm(false);
    setEditingId(null);
  };

  const handleAddJournal = async (e) => {
    e.preventDefault();
    if (!journalForm.title.trim() || !journalForm.content.trim()) return;

    try {
      let movieId = null;

      if (movieName.trim()) {
        const movieRes = await getMovieByTitle(movieName);
        console.log(movieRes?.data);

        const movie = movieRes?.data;
        if (!movie || !movie.movieId) {
          alert("Movie not found");
          return;
        }
        movieId = movie.movieId;
      }

      const journalData = {
        ...journalForm,
        userId: userId,
        movieTitle: movieName,

      };

      await JournalService.addToJournal(journalData);
      fetchJournals(userId);
      resetForm();
    } catch (error) {
      console.error('Error adding journal:', error);
    }
  };

  const handleEdit = (journal) => {
    setEditingId(journal.journalId);
    setJournalForm({
      title: journal.title,
      content: journal.content,
      moodTag: journal.moodTag || '',
    });
    setMovieName(journal.movies?.[0]?.title || '');
    setShowForm(true);
    setOpenMenuId(null);
  };

  const handleUpdateJournal = async (e) => {
    e.preventDefault();
    if (!journalForm.title.trim() || !journalForm.content.trim()) return;

    try {
      let movieId = null;

      if (movieName.trim()) {
        const movieRes = await getMovieByTitle(movieName);
        const movie = movieRes?.data;
        if (!movie || !movie.movieId) {
          alert("Movie not found");
          return;
        }
        movieId = movie.movieId;
      }

      const updatedJournal = {
        journalId: editingId,
        ...journalForm,
        userId: userId, // Ensure userId is included
        movieTitle: movieName,
        movieId: movieId
      };

      const response = await JournalService.updateJournal(editingId, updatedJournal);
      fetchJournals(userId);
      resetForm();
    } catch (error) {
      console.error("Error updating journal:", error);
    }
  };


  const handleDeleteJournal = async (journalId) => {
    try {
      await JournalService.deleteJournal(journalId);
      setOpenMenuId(null);
      fetchJournals(userId);
    } catch (error) {
      console.error('Error deleting journal:', error);
    }
  };

  const toggleMenu = (journalId) => {
    setOpenMenuId((prev) => (prev === journalId ? null : journalId));
  };

  useEffect(() => {
    const fetchImages = async () => {
      const token = localStorage.getItem('token');
      const urls = {};
      await Promise.all(journals.map(async (journal) => {
        const movie = journal.movies?.[0];
        if (movie?.movieId) {
          try {
            const res = await axios.get(`http://localhost:9090/movie/${movie.movieId}/image`, {
              headers: token ? { Authorization: `Bearer ${token}` } : {},
              responseType: 'blob',
            });
            const imageUrl = URL.createObjectURL(res.data);
            urls[journal.journalId] = imageUrl;
          } catch (err) {
            urls[journal.journalId] = 'src/assets/poster.jpg';
          }
        } else {
          urls[journal.journalId] = 'src/assets/poster.jpg';
        }
      }));
      setImageUrls(urls);
    };
    if (journals.length > 0) fetchImages();
  }, [journals]);

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

  return (
    <div className="journals-page">
      <h2 className="page-title">Journals</h2>

      <button className='toggle-form-btn'
        onClick={() => {
          if (showForm) {
            resetForm();
          } else {
            setShowForm(true);
          }
        }}>
        {showForm ? "Close Journal Form" : "Add New Journal"}
      </button>

      {showForm && (
        <form className="journal-form" onSubmit={editingId ? handleUpdateJournal : handleAddJournal}>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              placeholder="Movie name"
              value={movieName}
              onChange={e => {
                setMovieName(e.target.value);
                setShowDropdown(true);
              }}
              onFocus={() => {
                setShowDropdown(true);
                if (!movieName) setMovieName(' '); // Trigger dropdown on click if empty
              }}
              onBlur={() => setTimeout(() => setShowDropdown(false), 150)}
              disabled={!!editingId} // Disable while editing
            />
            {showDropdown && movieName && (
              <ul className="movie-dropdown">
                {movieOptions.filter(title => title.toLowerCase().includes(movieName.toLowerCase().trim())).slice(0, 8).map(title => (
                  <li key={title} onMouseDown={() => { setMovieName(title.trim()); setShowDropdown(false); }}>{title}</li>
                ))}
                {movieOptions.filter(title => title.toLowerCase().includes(movieName.toLowerCase().trim())).length === 0 && (
                  <li style={{ color: '#888' }}>No matches</li>
                )}
              </ul>
            )}
          </div>
          <input
            type="text"
            name="title"
            placeholder="Journal Title"
            value={journalForm.title}
            onChange={handleInputChange}
            maxLength={100}
            required
          />
          <textarea
            name="content"
            placeholder="Write your thoughts here..."
            value={journalForm.content}
            onChange={handleInputChange}
            rows="5"
            required
          />
          <input
            type="text"
            name="moodTag"
            placeholder="Mood (e.g., Happy, Nostalgic)"
            value={journalForm.moodTag}
            onChange={handleInputChange}
            maxLength={30}
          />
          <button type="submit">{editingId ? "Update Journal" : "Add Journal"}</button>
        </form>
      )}

      {/* Move the message outside of .journals-grid */}
      {journals.length === 0 && (
        <div className="no-journals">
          No journals yet. Start documenting your journey!
        </div>
      )}

      <div className="journals-grid">
        {journals.map((journal) => (
          <div key={journal.journalId} className="journal-card">
            <div className="card-top">
              {journal.movies?.[0]?.movieId ? (
                <img
                  src={imageUrls[journal.journalId] || 'src/assets/poster.jpg'}
                  alt={journal.movies[0].title}
                  className="card-poster"
                />
              ) : (
                <div className="no-poster">No Poster</div>
              )}

              <div className="menu-container">
                <button className="journal-menu-button" onClick={() => toggleMenu(journal.journalId)}> ⋮ </button>
                {openMenuId === journal.journalId && (
                  <div className="journal-dropdown-menu">
                    <button onClick={() => handleEdit(journal)}>Edit</button>
                    <button onClick={() => handleDeleteJournal(journal.journalId)}>Delete</button>
                  </div>
                )}
              </div>
            </div>

            <div className="card-body">
              <h3 className="card-title">{journal.title}</h3>
              <p className="card-content">{journal.content}</p>
              {journal.moodTag && (
                <p className="card-mood"><strong>Mood:</strong> {journal.moodTag}</p>
              )}
              {journal.movies?.length > 0 && (
                <div className="movies-mentioned">
                  <strong>Movie Mentioned:</strong>
                  <ul>
                    {journal.movies.map((movie) => (
                      <li key={movie.movieId}>
                        {movie.title} ({movie.releaseYear})
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Journals;
