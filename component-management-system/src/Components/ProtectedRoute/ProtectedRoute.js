import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import Cookies from 'js-cookie'; // Or your preferred method for accessing cookies

function ProtectedRoute() {
  const isAuthenticated = !!Cookies.get('authToken'); // Checks for the JWT
  return isAuthenticated ? (<><Outlet /></>) : <Navigate to="/login" />;
}

export default ProtectedRoute;