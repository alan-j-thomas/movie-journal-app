import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from './AuthContext';

const Users = () => {
  const { user } = useContext(AuthContext) || {};
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const token = localStorage.getItem('token');
        const res = await axios.get('http://localhost:9090/users', {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        setUsers(res.data);
      } catch (err) {
        setError('Failed to fetch users');
      } finally {
        setLoading(false);
      }
    };
    if (user?.role === 'ADMIN') fetchUsers();
  }, [user]);

  if (user?.role !== 'ADMIN') return <div style={{color:'#fff',textAlign:'center'}}>Access denied.</div>;
  if (loading) return <div style={{color:'#fff',textAlign:'center'}}>Loading users...</div>;
  if (error) return <div style={{color:'#fff',textAlign:'center'}}>{error}</div>;

  // Robustly filter out all admins and the current admin by role, username, and unique ID
  let adminId = user?.userId || user?.id;
  const filteredUsers = users.filter(u => {
    // Exclude if role is ADMIN (case-insensitive)
    if (u.role && typeof u.role === 'string' && u.role.toUpperCase() === 'ADMIN') return false;
    // Exclude if username/userName/email is 'admin' (case-insensitive)
    const uname = (u.userName || u.username || u.email || '').toLowerCase();
    if (uname === 'admin') return false;
    // Exclude if userId or id matches current admin
    if (u.userId === adminId || u.id === adminId) return false;
    return true;
  });
  return (
    <div style={{maxWidth:700,margin:'40px auto',background:'#23272b',borderRadius:12,padding:32,color:'#fff'}}>
      <h2 style={{textAlign:'center',marginBottom:24}}>All Users</h2>
      <table style={{width:'100%',borderCollapse:'collapse',background:'#23272b'}}>
        <thead>
          <tr style={{background:'#181a1b'}}>
            <th style={{padding:10,borderBottom:'1px solid #444'}}>ID</th>
            <th style={{padding:10,borderBottom:'1px solid #444'}}>Username</th>
            <th style={{padding:10,borderBottom:'1px solid #444'}}>Role</th>
          </tr>
        </thead>
        <tbody>
          {filteredUsers.map(u => (
            <tr key={u.id}>
              <td style={{padding:10,borderBottom:'1px solid #333'}}>{u.userId}</td>
              <td style={{padding:10,borderBottom:'1px solid #333'}}>{u.userName || u.username || u.email}</td>
              <td style={{padding:10,borderBottom:'1px solid #333'}}>{u.role}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Users;
