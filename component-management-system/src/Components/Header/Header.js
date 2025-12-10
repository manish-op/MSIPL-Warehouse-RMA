import React, { useState, useEffect, useContext } from "react";
import { useLocation, Link } from "react-router-dom";
import "./Header.css";
import logo from "../../images/images.png";
import { ThemeContext } from "../../context/ThemeContext";
import "./ThemeToggle.css";
import { FaSignInAlt, FaSignOutAlt } from "react-icons/fa";
import NotificationBell from "../NotificationBell/NotificationBell"; 

function Header() {
  const location = useLocation();
  const [showButton, setShowButton] = useState(true);
  const { theme, toggleTheme } = useContext(ThemeContext);

  useEffect(() => {

    setShowButton(location.pathname === "/");
  }, [location]);

  return (
    <header className="header">
      <div className="header-left">
        <img src={logo} alt="Company Logo" className="logo" />
        <span className="companyName">Motorola Solutions India Pvt Ltd</span>
      </div>

      <div className="header-right">
        <label className="switch">
          <input type="checkbox" onChange={toggleTheme} checked={theme === "dark"} />
          <span className="slider round"></span>
        </label>

        {!showButton && (
          <Link
            to="https://sites.google.com/motorolasolutions.com/india-managed-support-services/india-support-services/fso-useful-links"
            className="header-btn header-icon-btn"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Service Help"
          >
            Service Webpage
          </Link>
        )}

        {!showButton && <NotificationBell />}

        {showButton ? (
          <Link to="/login" className="header-btn">
            Sign In
            <FaSignInAlt className="header-btn-icon" />
          </Link>
        ) : (
          <Link to="/dashboard/logout" className="header-btn">
            Logout
            <FaSignOutAlt className="header-btn-icon" />
          </Link>
        )}
      </div>
    </header>
  );
}

export default Header;