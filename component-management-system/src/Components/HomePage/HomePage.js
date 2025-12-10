import React, { useEffect } from "react";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { Link } from "react-router-dom";
import "./HomePage.css";

export default function HomePage() {
  useEffect(() => {
    document.title = "MSIPL Warehouse";
  }, []);

  return (
    <>
      <Header />

      <main className="simple-home-container">
        <div className="simple-hero">
          <h1 className="simple-title">MSIPL Warehouse Management</h1>
          <p className="simple-text">Manage inventory and warehouse operations with ease.</p>

          <Link to="/login" className="simple-btn">Sign In</Link>
        </div>
      </main>

      <Footer />
    </>
  );
}
