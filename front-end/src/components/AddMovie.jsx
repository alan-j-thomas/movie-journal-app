import React, { useState } from 'react';
import '../components/css/AddMovie.css';
import axios from 'axios';

function AddMovie() {
  const [form, setForm] = useState({
    title: '',
    genre: '',
    releaseYear: '',
    director: '',
    cast: '',
    language: '',
    rating: '',
    summary: ''
  });
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === 'image') {
      setImage(files[0]);
      setPreview(URL.createObjectURL(files[0]));
    } else {
      setForm((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccessMsg('');
    setErrorMsg('');
    if (!form.title.trim() || !form.genre.trim() || !form.releaseYear.trim() || !image) {
      setErrorMsg('All fields are required.');
      return;
    }
    try {
      const token = localStorage.getItem('token');
      const formData = new FormData();
      const movieBlob = new Blob([
        JSON.stringify({
          title: form.title,
          genre: form.genre,
          releaseYear: form.releaseYear,
          director: form.director,
          cast: form.cast,
          language: form.language,
          rating: form.rating,
          summary: form.summary
        })
      ], { type: 'application/json' });
      formData.append('movie', movieBlob);
      formData.append('imageFile', image);

      await axios.post('http://localhost:9090/movie/add', formData, {
                headers: token ? { Authorization: `Bearer ${token}` } : {}
              });
      setSuccessMsg('Movie added successfully!');
      setForm({ title: '', genre: '', releaseYear: '', director: '', cast: '', language: '', rating: '', summary: '' });
      setImage(null);
      setPreview(null);
    } catch (error) {
      setErrorMsg('Failed to add movie.');
    }
  };

  return (
    <div className="add-movie-page">
      <h2 className="add-movie-title">Add Movie</h2>
      <form className="add-movie-form" onSubmit={handleSubmit}>
        <input
          type="text"
          name="title"
          placeholder="Movie Title"
          value={form.title}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="genre"
          placeholder="Genre"
          value={form.genre}
          onChange={handleChange}
          required
        />
        <input
          type="number"
          name="releaseYear"
          placeholder="Release Year"
          value={form.releaseYear}
          onChange={handleChange}
          min="1888"
          max={new Date().getFullYear()}
          required
        />
        <input
          type="text"
          name="director"
          placeholder="Director"
          value={form.director}
          onChange={handleChange}
        />
        <input
          type="text"
          name="cast"
          placeholder="Cast (comma separated)"
          value={form.cast}
          onChange={handleChange}
        />
        <input
          type="text"
          name="language"
          placeholder="Language"
          value={form.language}
          onChange={handleChange}
        />
        <input
          type="number"
          name="rating"
          placeholder="Rating (e.g. 7.5)"
          value={form.rating}
          onChange={handleChange}
          min="0"
          max="10"
          step="0.1"
        />
        <textarea
          name="summary"
          placeholder="Summary"
          value={form.summary}
          onChange={handleChange}
          rows={4}
        />
        <input
          type="file"
          name="image"
          accept="image/*"
          onChange={handleChange}
          required
        />
        {preview && (
          <img src={preview} alt="Preview" style={{ maxWidth: '120px', margin: '10px auto', borderRadius: '8px' }} />
        )}
        <button type="submit">Add Movie</button>
        {successMsg && <div className="success-msg">{successMsg}</div>}
        {errorMsg && <div className="error-msg">{errorMsg}</div>}
      </form>
    </div>
  );
}

export default AddMovie;
