// src/components/ThemeToggleButton.js

import React, { useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext';
// Import your icon images
import SunIcon from '../images/sun-icon.svg'; 
import MoonIcon from '../images/moon-icon.svg'; 
import './ThemeToggleButton.css';

const ThemeToggleButton = () => {
  const { theme, toggleTheme } = useContext(ThemeContext);

  return (
    <button
      onClick={toggleTheme}
      className="theme-toggle-button" // 
     
     
    >
      {/* Conditionally render the icon based on the current theme */}
      {theme === 'light' ? (
        <>
          <img src={SunIcon} alt="Sun icon" className="theme-icon" />
          <span className="toggle-text">Switch to Dark Mode</span>
        </>
      ) : (
        <>
          <img src={MoonIcon} alt="Moon icon" className="theme-icon" />
          <span className="toggle-text">Switch to Light Mode</span>
        </>
      )}
    </button>
  );
};

export default ThemeToggleButton;