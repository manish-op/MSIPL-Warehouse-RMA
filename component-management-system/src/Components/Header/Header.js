import React, { useState, useEffect, useContext } from "react";
import { Dropdown } from "antd";
import { useLocation, Link } from "react-router-dom";
import "./Header.css";
import logo from "../../images/images.png";
import { ThemeContext } from "../../context/ThemeContext";
import "./ThemeToggle.css";
import { FaSignInAlt, FaSignOutAlt, FaBars } from "react-icons/fa";
import NotificationBell from "../NotificationBell/NotificationBell";

function Header({ onToggleSidebar }) {
  const location = useLocation();
  const [showButton, setShowButton] = useState(true);
  const { theme, toggleTheme } = useContext(ThemeContext);

  useEffect(() => {

    setShowButton(location.pathname === "/");
  }, [location]);

  return (
    <header className="header">
      <div className="header-left">
        {onToggleSidebar && (
          <button
            className="menu-toggle-btn"
            onClick={onToggleSidebar}
            aria-label="Toggle Sidebar"
          >
            <FaBars />
          </button>
        )}
        <img src={logo} alt="Company Logo" className="logo" />
        <span className="companyName">Motorola Solutions India Pvt Ltd</span>
      </div>

      <div className="header-right">
        <label className="switch">
          <input type="checkbox" onChange={toggleTheme} checked={theme === "dark"} />
          <span className="slider round"></span>
        </label>

        {!showButton && location.pathname.startsWith("/dashboard") && <NotificationBell />}

        {!showButton ? (
          <Dropdown
            menu={{
              items: [
                {
                  key: 'customerSla',
                  label: 'Customer Sla',
                  children: [
                    {
                      key: 'addSla',
                      label: <Link to="/customer-sla/add">Add New</Link>,
                    },
                    {
                      key: 'manageSla',
                      label: <Link to="/customer-sla/manage">Manage</Link>,
                    },
                  ],
                },
                {
                  key: 'service',
                  label: (
                    <a
                      href="https://sites.google.com/motorolasolutions.com/india-managed-support-services/india-support-services/fso-useful-links"
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                      <FaSignInAlt style={{ transform: 'rotate(0deg)' }} /> Service Webpage
                    </a>
                  ),
                },
                {
                  type: 'divider',
                },
                {
                  key: 'logout',
                  label: (
                    <Link to="/dashboard/logout" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <FaSignOutAlt /> Logout
                    </Link>
                  ),
                  danger: true,
                },
              ]
            }}
            trigger={['click']}
          >
            <button className="header-btn">
              <span style={{ marginRight: 8 }}></span>
              <FaBars /> {/* Using Bars or ChevronDown */}
            </button>
          </Dropdown>
        ) : (
          <Link to="/login" className="header-btn">
            Sign In
            <FaSignInAlt className="header-btn-icon" />
          </Link>
        )}
      </div>
    </header>
  );
}

export default Header;