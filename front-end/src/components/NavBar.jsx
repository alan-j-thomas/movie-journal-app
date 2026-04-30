import React, { useContext, useState, useEffect } from 'react'
import bellIcon from '../assets/bell.webp';
import NotificationPanel from './NotificationPanel';
import styles from './css/NotificationPanel.module.css';
import { NavLink, useNavigate } from "react-router-dom"
import '../components/css/NavBar.css'
import AuthContext from './AuthContext.jsx'

const NavBar = ({ setUser }) => {

  const navigate = useNavigate();
  const { user: contextUser } = useContext(AuthContext) || {};
  const storedUser = JSON.parse(localStorage.getItem("user")) || {};
  const user = contextUser || storedUser;
  console.log('User in NavBar:', user); // Debug log
  const role = user?.role || storedUser?.role;
  const username = user?.userName || user?.sub || storedUser?.userName || storedUser?.sub || "Guest";

  const [isMenuVisible, setIsMenuVisible] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const hasUnread = notifications.some(n => n.unread);
  const notificationPanelRef = React.useRef(null);

  // Fetch notifications for the logged-in user
  useEffect(() => {
    if (!user || !user.id) return;
    const token = localStorage.getItem("token");
    const axiosConfig = token ? { headers: { Authorization: `Bearer ${token}` } } : {};
    // Fetch only when panel is opened or on mount
    if (showNotifications) {
      fetchNotifications();
    }
    async function fetchNotifications() {
      try {
        const res = await fetch(`http://localhost:8087/notifications/user/${user.id}`);
        if (!res.ok) throw new Error('Failed to fetch notifications');
        const data = await res.json();
        console.log('Fetched notifications:', data);
        // If backend returns a message field, use it; else fallback to generic
        setNotifications(
          Array.isArray(data)
            ? data.map(n => ({
                text: n.message,
                unread: true // You can add a read/unread field in backend if needed
              }))
            : []
        );
      } catch (err) {
        setNotifications([]);
        // Optionally show error
      }
    }
  }, [user?.id, showNotifications]);

  const handleNotificationClick = () => {
    console.log('Notification bell clicked, user:', user); // Debug log
    setShowNotifications((prev) => !prev);
  };

  const toggleMenu = () => {
    setIsMenuVisible((prev) => !prev);
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    setUser(null);
    navigate("/login");
  };

  // Close notification panel when clicking outside
  useEffect(() => {
    if (!showNotifications) return;
    function handleClickOutside(event) {
      if (
        notificationPanelRef.current &&
        !notificationPanelRef.current.contains(event.target) &&
        !event.target.closest('.' + styles['notification-icon-btn'])
      ) {
        setShowNotifications(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showNotifications]);

  return (
    <div className='container-nav'>

      <div className='inner-container'>
        <img src='src/assets/logo.png' id='logo' />
        <h2>MovieLedger</h2>
      </div>

      <div className='nav-links'>
        {role === 'ADMIN' ? (
          <>
            <NavLink to="/movies">Movies</NavLink>
            <NavLink to="/users">Users</NavLink>
            <NavLink to="/add-movie">Add Movie</NavLink>
          </>
        ) : (

          <>
            <NavLink to="/">Home</NavLink>
            <NavLink to="/movies">Movies</NavLink>
            <NavLink to="/watchlists">WatchLists</NavLink>
            <NavLink to="/journals">Journals</NavLink>
            <NavLink to="/friends">Friends</NavLink>
          </>
          
        )}
      </div>

      <div className="profile-container" style={{position: 'relative'}}>
        <button
          className={styles['notification-icon-btn']}
          onClick={handleNotificationClick}
          aria-label="Show notifications"
        >
          <img
            src={bellIcon}
            alt="Notifications"
            style={{ width: '28px', height: '28px', objectFit: 'contain'}}
          />
        </button>
        {showNotifications && (
          <div ref={notificationPanelRef}>
            <NotificationPanel
              notifications={notifications}
              onClose={() => setShowNotifications(false)}
            />
          </div>
        )}
        <button className="profile-button" onClick={toggleMenu}> 
           <img src='src/assets/image.png'></img>
        </button>
        {isMenuVisible && (  
          <div className="profile-dropdown-menu">
            <p>{username}</p>
            <br></br>
            <button id="logoutbtn" onClick={handleLogout}>Logout</button>
          </div>
        )}
      </div>
    </div>
  )
}

export default NavBar