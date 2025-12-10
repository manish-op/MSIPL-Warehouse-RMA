import React, { createContext, useState, useEffect } from 'react';

// 1. Creating the context
export const ThemeContext = createContext();

// 2. Creating the provider component
export const ThemeProvider = ({ children }) => {
  // State to hold the current theme, 'light' or 'dark'
  const [theme, setTheme] = useState('light');

  // Function to toggle the theme
  const toggleTheme = () => {
    setTheme(prevTheme => (prevTheme === 'light' ? 'dark' : 'light'));
  };

  // Effect to apply the theme to the root HTML element
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};