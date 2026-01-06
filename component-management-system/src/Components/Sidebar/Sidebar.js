import React, { useState, useMemo } from "react";
import { Menu, Layout, Segmented, Modal } from "antd";
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
  RiSettings4Line,       // For Options
  RiExchangeLine,        // For Module Switch
} from "react-icons/ri";
import { CloseCircleOutlined } from "@ant-design/icons";
import "./Sidebar.css";

const { Sider } = Layout;

// Receive props from Dashboard
function Sidebar({ collapsed, setCollapsed, isMobile }) {
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const navigate = useNavigate();
  const location = useLocation();

  // Module state (warehouse or rma)
  const [currentModule, setCurrentModule] = useState(() => {
    return sessionStorage.getItem("msipl_service_mode") || "warehouse";
  });

  const [openKeys, setOpenKeys] = useState(["dashboard"]);

  // Handle module switch with confirmation
  const handleModuleSwitch = (newModule) => {
    if (newModule === currentModule) return;

    Modal.confirm({
      title: `Switch to ${newModule === "rma" ? "RMA Portal" : "Warehouse"}?`,
      content: `You are about to switch to the ${newModule === "rma" ? "RMA Request Portal" : "Warehouse Management System"}. Continue?`,
      okText: "Switch",
      cancelText: "Cancel",
      onOk: () => {
        setCurrentModule(newModule);
        sessionStorage.setItem("msipl_service_mode", newModule);
        if (newModule === "rma") {
          navigate("/rma-dashboard");
        } else {
          navigate("/dashboard/profile");
        }
      },
    });
  };

  // 3. Define Menu Items (Restored All Original Options)
  const menuItems = useMemo(() => {
    const items = [
      {
        label: "Dashboard",
        key: "dashboard",
        icon: <RiDashboardLine />,
        children: [
          { label: "Profile", key: "profile" },
        ],
      },
      (role === "admin" || role === "manager") && {
        label: "Employee Management",
        key: "employee",
        icon: <RiUserSettingsLine />,
        children: [
          { label: "Add Employee", key: "addEmployee" },
          { label: "Manage Employees", key: "employeeManagement" },
        ].filter(Boolean),
      },
      role === "admin" && {
        label: "Keyword Management",
        key: "keyword",
        icon: <RiPriceTag3Line />,
        children: [
          { label: "Manage Keywords", key: "keywordManagement" },
        ],
      },
      role === "admin" && {
        label: "Region Management",
        key: "region",
        icon: <RiMapPinLine />,
        children: [
          { label: "Manage Regions", key: "regionManagement" },
        ],
      },
      {
        label: "Item Management",
        key: "item",
        icon: <RiBox3Line />,
        children: [
          (role === "admin" || role === "manager") && { label: "Add Item", key: "addItem" },
          { label: "Search Items", key: "itemSearch" },
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
          { label: "Manage Status", key: "statusManagement" },
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

      <Sider
        width={260}
        theme="dark"
        className={`custom-sidebar ${isMobile ? 'mobile-sider' : ''}`}
        collapsible
        collapsed={collapsed}
        trigger={null}
        collapsedWidth={isMobile ? 0 : 80}
        style={{
          minHeight: '100vh',
          position: isMobile ? 'fixed' : 'sticky',
          top: 0,
          left: 0,
          zIndex: 1001
        }}
      >
        <div className="sidebar-header">
          {/* Mobile Sidebar Header with Close Button */}
          {isMobile && !collapsed && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '16px 20px',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
              marginBottom: '8px',
              width: '100%'
            }}>
              <span style={{ color: '#fff', fontSize: '18px', fontWeight: 'bold' }}>Menu</span>
              <CloseCircleOutlined
                style={{ fontSize: '24px', color: '#ff4d4f', cursor: 'pointer' }}
                onClick={() => setCollapsed(true)}
              />
            </div>
          )}

          {!isMobile && (
            <>
              <div className="logo-text">{collapsed}</div>
              <div className="desktop-toggle" onClick={() => setCollapsed(!collapsed)}>
                {collapsed ? <RiMenu3Line /> : <RiMenuFold4Line />}
              </div>
            </>
          )}
        </div>

        {/* Module Switcher */}
        {!collapsed && (
          <div className="module-switcher">
            <div className="module-switcher-label">
              <RiExchangeLine style={{ marginRight: 6 }} />
              <span>Switch Module </span>
            </div>
            <Segmented
              value={currentModule}
              onChange={handleModuleSwitch}
              options={[
                { label: "Warehouse", value: "warehouse" },
                { label: "RMA", value: "rma" },
              ]}
              block
              size="small"
            />
          </div>
        )}

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