import React, { useState, useEffect, useMemo } from "react";
import { Menu, Layout, Button } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import { 
  RiMenu3Line, 
  RiMenuFold4Line, 
  RiDashboardLine, 
  RiUserSettingsLine, 
  RiShieldUserLine, 
  RiPriceTag3Line, 
  RiMapPinLine, 
  RiBox3Line, 
  RiLogoutBoxRLine,
  RiFileExcel2Line,      // For Import/Export
  RiFileList3Line,       // For Activity Logs
  RiAlarmWarningLine,    // For Thresholds/Alerts
  RiSettings4Line        // For Options
} from "react-icons/ri";
import "./Sidebar.css";

const { Sider } = Layout;

function Sidebar() {
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const navigate = useNavigate();
  const location = useLocation();
  
  // 1. Initialize state based on screen size
  const [collapsed, setCollapsed] = useState(window.innerWidth < 768);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  const [openKeys, setOpenKeys] = useState(["dashboard"]);

  // 2. Handle Screen Resize
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

  // 3. Define Menu Items (Restored All Original Options)
  const menuItems = useMemo(() => {
    const items = [
      {
        label: "Dashboard",
        key: "dashboard",
        icon: <RiDashboardLine />,
        children: [
          { label: "Profile", key: "profile" },
          { label: "Change Password", key: "changePassword" },
        ],
      },
      (role === "admin" || role === "manager") && {
        label: "Employee Management",
        key: "employee",
        icon: <RiUserSettingsLine />,
        children: [
          { label: "Add Employee", key: "addEmployee" },
          role === "admin" && { label: "Change Role", key: "changEmployeeRole" },
          role === "admin" && { label: "Change Region", key: "changEmployeeRegion" },
          { label: "Reset Password", key: "changEmployeePass" },
        ].filter(Boolean),
      },
      role === "admin" && {
        label: "Keyword Management",
        key: "keyword",
        icon: <RiPriceTag3Line />,
        children: [
          { label: "Add Keyword", key: "addKeyword" },
          { label: "Update Keyword", key: "updateKeyword" },
          { label: "Add SubKeyword", key: "addSubKeyword" },
          { label: "Update SubKeyword", key: "updateSubKeyword" },
        ],
      },
      role === "admin" && {
        label: "Region Management",
        key: "region",
        icon: <RiMapPinLine />,
        children: [
          { label: "Add Region", key: "addNewRegion" },
          { label: "Update Region", key: "updateRegion" },
        ],
      },
      {
        label: "Item Management",
        key: "item",
        icon: <RiBox3Line />,
        children: [
          (role === "admin" || role === "manager") && { label: "Add Item", key: "addItem" },
          { label: "Search By Keyword", key: "getItemByKeyword" },
          { label: "Search By Serial", key: "getItemBySerial" },
          { label: "Item History", key: "itemHistory" },
        ].filter(Boolean),
      },
      {
        label: "GatePass",
        key: "gatepass",
        icon: <RiShieldUserLine />,
        children: [
          { label: "Inward Gatepass", key: "inwardGatePass" },
          { label: "Outward Gatepass", key: "outwardGatePass" },
        ],
      },
      // --- RESTORED SECTIONS BELOW ---
      role === "admin" && {
        label: "Options & Status",
        key: "options_group",
        icon: <RiSettings4Line />, 
        children: [
          { label: "Add Avail. Option", key: "addAvailStatus" },
          { label: "Update Avail. Option", key: "updateAvailStatus" },
          { label: "Add Item Status", key: "addItemStatus" },
          { label: "Update Item Status", key: "UpdateItemStatus" },
        ],
      },
      (role === "admin" || role === "manager") && {
        label: "Import / Export",
        key: "import_export",
        icon: <RiFileExcel2Line />,
        children: [
          { label: "Import/Export CSV", key: "import_export_CSV" },
        ],
      },
      (role === "admin" || role === "manager") && {
        label: "Activity Logs",
        key: "activity_logs_group",
        icon: <RiFileList3Line />,
        children: [
          { label: "Chat", key: "activity-logs" },
          { label: "Users List", key: "all-users" },
           { label: "Activity Logs", key: "items/activity" },
        ],
      },
      (role === "admin" || role === "manager") && {
        label: "Thresholds & Alerts",
        key: "threshold_group",
        icon: <RiAlarmWarningLine />,
        children: [
          { label: "Add Threshold", key: "thresholds" },
          { label: "Active Alerts", key: "alerts/active" },
        ],
      },
      // --- END RESTORED SECTIONS ---
      {
        label: "Logout",
        key: "logout",
        icon: <RiLogoutBoxRLine />,
        danger: true,
      },
    ];
    return items.filter(Boolean);
  }, [role]);

  const handleOpenChange = (keys) => {
    const latestOpenKey = keys.find((key) => openKeys.indexOf(key) === -1);
    if (latestOpenKey) {
      setOpenKeys([latestOpenKey]);
    } else {
      setOpenKeys(keys.length > 0 ? [keys[keys.length - 1]] : []);
    }
  };

  const onMenuItemClick = (item) => {
    navigate(item.key);
    if (isMobile) {
      setCollapsed(true);
    }
  };

  return (
    <>
      {/* Mobile Overlay */}
      <div 
        className={`sidebar-overlay ${isMobile && !collapsed ? "visible" : ""}`} 
        onClick={() => setCollapsed(true)}
      />

      {/* Mobile Toggle Button */}
      {isMobile && collapsed && (
        <Button
          type="primary"
          className="mobile-toggle-btn"
          onClick={() => setCollapsed(false)}
          icon={<RiMenu3Line />}
        />
      )}

      <Sider
        width={260}
        theme="dark"
        className={`custom-sidebar ${isMobile ? 'mobile-sider' : ''}`}
        collapsible
        collapsed={collapsed}
        trigger={null}
        collapsedWidth={isMobile ? 0 : 80}
        style={{
            height: '100vh',
            position: isMobile ? 'fixed' : 'sticky',
            top: 0,
            left: 0,
            zIndex: 1001
        }}
      >
        <div className="sidebar-header">
           <div className="logo-text">{collapsed }</div>
           {!isMobile && (
             <div className="desktop-toggle" onClick={() => setCollapsed(!collapsed)}>
                {collapsed ? <RiMenu3Line /> : <RiMenuFold4Line />}
             </div>
           )}
           {isMobile && !collapsed && (
              <div className="mobile-close" onClick={() => setCollapsed(true)}>
                  <RiMenuFold4Line />
              </div>
           )}
        </div>

        <Menu
          mode="inline"
          theme="dark"
          selectedKeys={[location.pathname.replace("/", "")]}
          openKeys={collapsed ? [] : openKeys}
          onOpenChange={handleOpenChange}
          onClick={onMenuItemClick}
          items={menuItems}
          className="custom-menu"
        />
      </Sider>
    </>
  );
}

export default Sidebar;