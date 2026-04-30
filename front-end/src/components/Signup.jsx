import React, { useState } from 'react';
import axios from 'axios';
import '../components/css/auth.css';
import { Navigate, useNavigate } from 'react-router-dom';

function Signup() {
  const [form, setForm] = useState({
    name: '',
    username: '',
    password: '',
    role: 'USER'
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateForm = () => {
    // Full Name: required, only letters/spaces, 2-50 chars
    if (!form.name.trim()) {
      setError('Full Name is required.');
      return false;
    }
    if (!/^[A-Za-z ]{2,50}$/.test(form.name.trim())) {
      setError('Full Name must be 2-50 letters and spaces only.');
      return false;
    }
    // Email: required, valid format
    if (!form.username.trim()) {
      setError('Email is required.');
      return false;
    }
    // Password: required, min 6 chars
    if (!form.password) {
      setError('Password is required.');
      return false;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters.');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    if (!validateForm()) return;
    setLoading(true);
    try {
      await axios.post('http://localhost:9898/auth/register', form);
      setMessage('Account created successfully. Please log in.');
      setTimeout(() => navigate('/'), 2000);
      setForm({ name: '', username: '', password: '', role: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Signup failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="signup-bg">
      <div className="posters-bg">
        {/* <img src={darkKnight} alt="poster1" className="poster1" />
        <img src={inception} alt="poster2" className="poster2" />
        <img src={godfather} alt="poster3" className="poster3" />
        <img src={poster} alt="poster4" className="poster4" />
        <img src={dune} alt="poster6" className="poster6" />
        <img src={oppenheimer} alt="poster7" className="poster7" />
        <img src={tron} alt="poster8" className="poster6" /> */}
      </div>
      <div className="auth-container">
        <h2>SIGN UP</h2>
        {message && <p className="success-msg">{message}</p>}
        {error && <p className="error-msg">{error}</p>}
        <form onSubmit={handleSubmit} className="auth-form">
          <input
            type="text"
            name="name"
            placeholder="Full Name"
            value={form.name}
            onChange={handleChange}
            required
          />
          <input
            type="email"
            name="username"
            placeholder="Email"
            value={form.username}
            onChange={handleChange}
            required
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
          />
          {/* <select name="role" value={form.role} onChange={handleChange} required>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select> */}
          <button type="submit" disabled={loading}>
            {loading ? 'Signing up...' : 'Sign Up'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Signup;
