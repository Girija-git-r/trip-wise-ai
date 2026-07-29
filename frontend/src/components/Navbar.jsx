import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const ICONS = {
  '/dashboard': '🏠',
  '/plan-trip': '✨',
  '/my-trips': '🧭',
  '/profile': '👤',
};

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 4);
    }
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  const links = isAuthenticated
    ? [
        { to: '/dashboard', label: 'Dashboard' },
        { to: '/plan-trip', label: 'Plan a Trip' },
        { to: '/my-trips', label: 'My Trips' },
        { to: '/profile', label: 'Profile' },
      ]
    : [];

  return (
    <header className={`navbar ${scrolled ? 'scrolled' : ''}`}>
      <div className="container navbar-inner">
        <Link to={isAuthenticated ? '/dashboard' : '/'} className="navbar-brand">
          <span className="navbar-brand-mark">TripWise</span>
          <span className="navbar-brand-suffix">AI</span>
        </Link>

        <button
          className="navbar-toggle"
          aria-label="Toggle navigation"
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span />
          <span />
          <span />
        </button>

        <nav className={`navbar-links ${menuOpen ? 'open' : ''}`}>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => `navbar-link ${isActive ? 'active' : ''}`}
              onClick={() => setMenuOpen(false)}
            >
              <span className="navbar-link-icon">{ICONS[link.to]}</span>
              {link.label}
            </NavLink>
          ))}

          {isAuthenticated ? (
            <div className="navbar-user">
              <span className="navbar-username">Hi, {user?.name?.split(' ')[0]}</span>
              <button className="btn btn-outline" onClick={handleLogout}>
                Logout
              </button>
            </div>
          ) : (
            <div className="navbar-user">
              <Link to="/login" className="btn btn-outline">Login</Link>
              <Link to="/register" className="btn btn-primary">Get Started</Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
