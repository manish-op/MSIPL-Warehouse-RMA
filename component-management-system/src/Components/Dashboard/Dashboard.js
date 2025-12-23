import React, { useState, useEffect } from "react";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import "./Dashboard.css";
import Sidebar from "../Sidebar/Sidebar";
import { Outlet } from "react-router-dom";
import { ItemProvider } from "../Items/UpdateItem/ItemContext";
import useNavigationGuard from "../../hooks/useNavigationGuard"; // Import navigation guard


function Dashboard() {
  // Protect navigation from Warehouse to RMA
  useNavigationGuard("warehouse");

  // Lifted state for Sidebar
  const [collapsed, setCollapsed] = useState(window.innerWidth < 768);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  // Handle Screen Resize
  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      if (mobile) {
        setCollapsed(true);
      }
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return (
    <>

      <ItemProvider>

        <div className="mainPage">
          {/* Pass toggle handler to Header so it shows the hamburger on mobile */}
          <Header onToggleSidebar={() => setCollapsed(!collapsed)} />
          <div className="Dashboard-content">
            {/* Pass state and handlers to Sidebar */}
            <Sidebar
              collapsed={collapsed}
              setCollapsed={setCollapsed}
              isMobile={isMobile}
            />
            <div className="content">
              <Outlet />
            </div>
          </div>
          <Footer />
        </div>

      </ItemProvider>
    </>
  );
}

export default Dashboard;