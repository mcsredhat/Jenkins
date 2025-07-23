import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';

// Home component
const Home = () => (
  <div className="page">
    <h1>Welcome to React Docker App</h1>
    <p>This is a simple React application running in a Docker container with nginx.</p>
    <div className="features">
      <h2>Features</h2>
      <ul>
        <li>Multi-stage Docker build</li>
        <li>Non-root user security</li>
        <li>Nginx web server</li>
        <li>React Router for SPA routing</li>
        <li>Health check endpoint</li>
      </ul>
    </div>
  </div>
);

// About component
const About = () => (
  <div className="page">
    <h1>About This App</h1>
    <p>This React application demonstrates best practices for containerizing React apps with Docker.</p>
    <div className="tech-stack">
      <h2>Technology Stack</h2>
      <ul>
        <li>React 18</li>
        <li>React Router</li>
        <li>Docker Multi-stage Build</li>
        <li>Nginx Alpine</li>
        <li>ESLint & Prettier</li>
      </ul>
    </div>
  </div>
);

// Contact component
const Contact = () => (
  <div className="page">
    <h1>Contact</h1>
    <p>Get in touch with us!</p>
    <div className="contact-info">
      <p>Email: farajassulai@gmail.com</p>
      <p>This is a demo application for Docker containerization.</p>
    </div>
  </div>
);

// Navigation component
const Navigation = () => (
  <nav className="navbar">
    <div className="nav-container">
      <Link to="/" className="nav-logo">
        React Docker App
      </Link>
      <ul className="nav-menu">
        <li className="nav-item">
          <Link to="/" className="nav-link">
            Home
          </Link>
        </li>
        <li className="nav-item">
          <Link to="/about" className="nav-link">
            About
          </Link>
        </li>
        <li className="nav-item">
          <Link to="/contact" className="nav-link">
            Contact
          </Link>
        </li>
      </ul>
    </div>
  </nav>
);

// Main App component
function App() {
  return (
    <Router>
      <div className="App">
        <Navigation />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
          </Routes>
        </main>
        <footer className="footer">
          <p>&copy; 2024 React Docker App. Built with React and Docker.</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;