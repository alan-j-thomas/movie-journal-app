import { useState } from 'react'
import './App.css'
import MovieCard from './components/MovieCard'
import WatchList from './components/WatchList'
import Journals from './components/Journals'
import {
  BrowserRouter,
  createBrowserRouter,
  RouterProvider,
  Routes,
  Route
} from "react-router-dom";
import NavBar from './components/NavBar';
import Home from './components/Home';
import Login from './components/Login'
import Signup from './components/Signup'
import PrivateRoute from './components/PrivateRoute'
import AuthProvider from './components/AuthContext'
import Layout from './components/Layout'
import AddMovie from './components/AddMovie';
import Users from './components/Users';
import Friends from './components/Friends';
import FloatingAIButton from './components/FloatingAIButton';


function App() {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const handleLogin = (user) => {
    setUser(user);
    localStorage.setItem("user", JSON.stringify(user));
  };

  const isAdmin = user?.role === 'ADMIN';

  const routes = createBrowserRouter([
    {
      path: "/register",
      element: <Layout><Signup /></Layout>
    },
    {
      path: "/login",
      element: <Layout><Login onLogin={handleLogin} /></Layout>
    },
    {
      path: "/",
      element: isAdmin ? <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><MovieCard enableFilters={true} /></AuthProvider></PrivateRoute></div>
        : <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><Home /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/movies",
      element: <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><MovieCard enableFilters={true} /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/users",
      element: <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><Users /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/add-movie",
      element: <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><AddMovie /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/watchlists",
      element: !isAdmin && <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><WatchList /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/journals",
      element: !isAdmin && <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><Journals /></AuthProvider></PrivateRoute></div>
    },
    {
      path: "/friends",
      element: !isAdmin && <div><PrivateRoute><AuthProvider><NavBar setUser={setUser} /><Friends /></AuthProvider></PrivateRoute></div>
    }
  ]);

  return (
    <>
      <RouterProvider router={routes} />
      <FloatingAIButton />
    </>
  );
}
export default App;
