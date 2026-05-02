import React, { useState } from 'react';
import axios from 'axios';
import '../components/css/auth.css';
import { NavLink, useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';


function Login({ onLogin }) {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateForm = () => {
    if (!form.username) {
      setError('Username is required');
      return false;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    
    setLoading(true);
    try {
      const res = await axios.post('http://localhost:9898/auth/login', form, {
        headers: { 'Content-Type': 'application/json' },
      });
      const { token } = res.data;
      localStorage.setItem('token', token);
      const user = jwtDecode(token);

      onLogin(user);
      if (user?.role === 'ADMIN') {
        navigate('/movies', { replace: true });
      } else {
        navigate('/');
      }
    } catch (err) {
      setError('Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg">
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
        <h2>LOGIN</h2>
        {error && <p className="error-msg">{error}</p>}
        <form onSubmit={handleSubmit} className="auth-form">
          <input type="text" name="username" placeholder="Username" value={form.username} onChange={handleChange} required />
          <input type="password" name="password" placeholder="Password" value={form.password} onChange={handleChange} required />
          <button type="submit" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
        </form>
        <p>Not having an account? <NavLink to="/register">Click here to Register</NavLink></p>
      </div>
    </div>
  );
}

export default Login;