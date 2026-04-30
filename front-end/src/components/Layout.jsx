import { useLocation } from 'react-router-dom';

function Layout({ children }) {
  const location = useLocation();
  const isAuthPage = location.pathname.includes('/login') || location.pathname.includes('/register');

  return <div className={isAuthPage ? 'auth-page' : ''}>{children}</div>;
}

export default Layout;