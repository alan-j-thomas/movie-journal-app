
import React, { useEffect, useState, useContext, useMemo } from "react";
import ChatWindow from "./ChatWindow";
import friendsImg from '../assets/friends.png';
import AuthContext from "./AuthContext";
import axios from "axios";
import styles from "./css/Friends.module.css";

const API_BASE = "http://localhost:8085";
const API_BASE_USERS = "http://localhost:9090";

const Friends = () => {
  const auth = useContext(AuthContext);
  const user = useMemo(() => {
    let u = auth?.user || JSON.parse(localStorage.getItem("user"));
    if (u && u.id && !u.userId) {
      u = { ...u, userId: u.id };
    }
    if (u && typeof u.userId === 'string') {
      u = { ...u, userId: parseInt(u.userId, 10) };
    }
    // Debug: show user and localStorage
    console.log('AuthContext:', auth);
    console.log('User from context or localStorage:', u);
    return u;
  }, [auth]);
  const [users, setUsers] = useState([]);
  const [friends, setFriends] = useState([]);
  const [pending, setPending] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]); // full objects for accept logic
  const [loading, setLoading] = useState(true);
  const [declinedRequests, setDeclinedRequests] = useState([]); // Track declined requestIds
  const [activeChatUser, setActiveChatUser] = useState(null);

  useEffect(() => {

    if (!user || !user.userId) {
      console.warn('User or userId missing in Friends.jsx:', user);
      return;
    }
    setLoading(true);
    const token = localStorage.getItem("token");
    const axiosConfig = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : {};
    Promise.all([
      axios.get(`${API_BASE_USERS}/users`, axiosConfig).then(res => res.data),
      axios.get(`${API_BASE}/friend/list/${user.userId}`, axiosConfig).then(res => res.data),
      axios.get(`${API_BASE}/friend/pending/${user.userId}`, axiosConfig).then(res => res.data)
    ])
      .then(([allUsers, friendsList, pendingList]) => {
        // Ensure all users have userId for consistent filtering
        const mappedUsers = allUsers.map(u => ({ ...u, userId: u.userId || u.id }));
        // Filter out declined requests if status field exists
        const filteredPendingList = Array.isArray(pendingList)
          ? pendingList.filter(f => !f.status || f.status.toUpperCase() !== 'DECLINED')
          : [];
        setUsers(mappedUsers);
        setFriends(friendsList.map(f => f.friendId === user.userId ? f.userId : f.friendId));
        setPending(filteredPendingList.map(f => f.friendId === user.userId ? f.userId : f.friendId));
        setPendingRequests(filteredPendingList); // store full objects for accept logic
        setLoading(false);
      })
      .catch((err) => {
        setFriends([]);
        setPending([]);
        setPendingRequests([]);
        setLoading(false);
        console.error('Error fetching friends data:', err);
      });
  }, [user?.userId]);

  // Accept friend request handler
  const handleAcceptFriend = (requestId) => {
    const token = localStorage.getItem("token");
    const axiosConfig = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : {};
    axios.put(`${API_BASE}/friend/accept/${requestId}`, {}, axiosConfig)
      .then(() => {
        // Refresh the list after accepting
        setPending(pending.filter(pid => pid !== requestId));
        // Optionally, trigger a reload by updating userId (or use a reload state)
        window.location.reload();
      })
      .catch((err) => {
        console.error('Error accepting friend:', err);
      });
  };

  const handleAddFriend = (friendId) => {
    if (!user || !user.userId) return;
    const token = localStorage.getItem("token");
    const axiosConfig = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : {};
    axios.post(`${API_BASE}/friend/add/${user.userId}/${friendId}`, {}, axiosConfig)
      .then(() => {
        // Refetch all lists to update UI for both users
        Promise.all([
          axios.get(`${API_BASE_USERS}/users`, axiosConfig).then(res => res.data),
          axios.get(`${API_BASE}/friend/list/${user.userId}`, axiosConfig).then(res => res.data),
          axios.get(`${API_BASE}/friend/pending/${user.userId}`, axiosConfig).then(res => res.data)
        ])
          .then(([allUsers, friendsList, pendingList]) => {
            const mappedUsers = allUsers.map(u => ({ ...u, userId: u.userId || u.id }));
            setUsers(mappedUsers);
            setFriends(friendsList.map(f => f.friendId === user.userId ? f.userId : f.friendId));
            setPending(pendingList.map(f => f.friendId === user.userId ? f.userId : f.friendId));
            setPendingRequests(pendingList);
          });
      })
      .catch((err) => {
        console.error('Error adding friend:', err);
      });
  };

    // Decline friend request handler
  const handleDeclineFriend = (requestId) => {
    const token = localStorage.getItem("token");
    const axiosConfig = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : {};
    console.log('Decline friend requestId:', requestId);
    axios.put(`${API_BASE}/friend/decline/${requestId}`, {}, axiosConfig)
      .then(() => {
        window.location.reload();
      })
      .catch((err) => {
        if (err.response) {
          console.error('Error declining friend:', err.message, 'Status:', err.response.status, 'Data:', err.response.data);
        } else {
          console.error('Error declining friend:', err);
        }
      });
  };


  if (!user) return <div>Please log in to see friends. (User not found)</div>;
  if (!user.userId) return <div>User ID not found. Please log in again.</div>;
  if (loading) return <div>Loading...</div>;

  // Filter out self and all admins
  const filteredUsers = users.filter(u => {
    if (u.userId === user.userId) return false;
    if (u.role && typeof u.role === 'string' && u.role.toUpperCase() === 'ADMIN') return false;
    const uname = (u.userName || u.username || u.email || '').toLowerCase();
    if (uname === 'admin') return false;
    return true;
  });

  // Helper to check if a user is a friend (bidirectional)
  const isFriend = (otherUserId) => {
    return friends.includes(otherUserId);
  };

  // Helper to check if a friend request is pending (either sent or received)
  // Returns: 'incoming' if current user received, 'outgoing' if sent, false otherwise
  // Helper to check if a friend request is pending (either sent or received)
  // Returns: 'incoming' if current user received, 'outgoing' if sent, false otherwise
  const isPending = (otherUserId) => {
    // Outgoing: current user sent request to otherUserId
    const outgoing = pendingRequests.find(f => f.userId === user.userId && f.friendId === otherUserId);
    if (outgoing) return { type: 'outgoing' };
    // Incoming: current user received request from otherUserId
    const incoming = pendingRequests.find(f => f.userId === otherUserId && f.friendId === user.userId);
    if (incoming) return { type: 'incoming', requestId: incoming.requestId };
    return false;
  };

  return (
    <div className={styles.friendsContainer}>
      <h2 className={styles.heading}>
        <img src={friendsImg} alt="friends" style={{height:'1.1em',verticalAlign:'middle',marginLeft:8,marginBottom:4}} />
      </h2>
      {filteredUsers.length === 0 ? (
        <div className={styles.noUsers}>No other users found.</div>
      ) : (
        <div className={styles.cardsWrapper}>
          {filteredUsers.map(u => (
            <div className={styles.userCard} key={u.userId}>
              <div className={styles.userInfo}>
                <span className={styles.userName}>{u.userName}</span>
                <span className={styles.userEmail}>({u.email})</span>
              </div>
              <div className={styles.actionArea}>
                {isFriend(u.userId) ? (
                  <>
                    <span className={styles.friendLabel}>Friend</span>
                    <button className={styles.addButton} style={{marginLeft:8}} onClick={() => setActiveChatUser(u)}>Chat</button>
                  </>
                ) : isPending(u.userId) && isPending(u.userId).type === 'incoming' ? (
                  declinedRequests.includes(isPending(u.userId).requestId) ? (
                    <span className={styles.pendingLabel} style={{ color: '#e74c3c', fontWeight: 'bold' }}>Declined</span>
                  ) : (
                    <>
                      <span className={styles.pendingLabel}>Request Received</span>
                      <button className={styles.addButton} style={{marginLeft:8}} onClick={() => handleAcceptFriend(isPending(u.userId).requestId)}>Accept</button>
                      <button className={styles.addButton} style={{marginLeft:8, background:'#e74c3c', color:'#fff'}} onClick={() => handleDeclineFriend(isPending(u.userId).requestId)}>Decline</button>
                    </>
                  )
                ) : isPending(u.userId) && isPending(u.userId).type === 'outgoing' ? (
                  <span className={styles.pendingLabel}>Pending</span>
                ) : (
                  <button className={styles.addButton} onClick={() => handleAddFriend(u.userId)}>Add Friend</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    {activeChatUser && (
      <ChatWindow
        user={user}
        friendEmail={activeChatUser.email}
        friendName={activeChatUser.userName}
        onClose={() => setActiveChatUser(null)}
      />
    )}
  </div>
  );
};

export default Friends;
