// src/Components/RMA/RmaLayout.js
import React, { useState, useEffect } from "react";
import { Layout, Menu, Modal, Segmented } from "antd";
import {
  DashboardOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ExclamationCircleOutlined,
  LogoutOutlined,
  ToolOutlined,
  CheckCircleOutlined,
  UserSwitchOutlined,
  WarningOutlined,
  HistoryOutlined,
  SwapOutlined,
  CarOutlined,
  CloseCircleOutlined,
  BarChartOutlined,
  UnorderedListOutlined,
  PlusOutlined
} from "@ant-design/icons";
import { useNavigate, useLocation } from "react-router-dom";
import Header from "../Header/Header"; // Import Global Header
import useNavigationGuard from "../../hooks/useNavigationGuard"; // Import navigation guard
import Cookies from "js-cookie"; // Import Cookies
import "./RmaDashboard.css";
import GlobalFooter from "../Footer/Footer";

const { Sider, Footer, Content } = Layout;
const { confirm } = Modal;

const RmaLayout = ({ children, noSidebar = false, sidebarType = 'rma' }) => {
  const [collapsed, setCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // Module state
  const [currentModule, setCurrentModule] = useState(() => {
    return sessionStorage.getItem("msipl_service_mode") || "rma";
  });

  // Protect navigation from RMA to Warehouse
  useNavigationGuard("rma");

  // Handle module switch with confirmation
  const handleModuleSwitch = (newModule) => {
    if (newModule === currentModule) return;

    confirm({
      title: `Switch to ${newModule === "warehouse" ? "Warehouse" : "RMA Portal"}?`,
      icon: <SwapOutlined />,
      content: `You are about to switch to the ${newModule === "warehouse" ? "Warehouse Management System" : "RMA Request Portal"}. Continue?`,
      okText: "Switch",
      cancelText: "Cancel",
      onOk() {
        setCurrentModule(newModule);
        sessionStorage.setItem("msipl_service_mode", newModule);
        if (newModule === "warehouse") {
          navigate("/dashboard/profile");
        } else {
          navigate("/rma-dashboard");
        }
      },
    });
  };

  const showLogoutConfirm = () => {
    confirm({
      title: "Are you sure you want to logout?",
      icon: <ExclamationCircleOutlined />,
      okText: "Logout",
      okType: "danger",
      cancelText: "Cancel",
      onOk() {
        // Clear authentication cookies
        Cookies.remove("authToken");
        Cookies.remove("isLogin");

        // Clear sessionStorage
        sessionStorage.removeItem("msipl_service_mode");

        navigate("/login");
      },
    });
  };

  // Determine active key based on path
  const getSelectedKey = () => {
    const path = location.pathname;
    
    // Customer SLA Keys
    if (path.includes('/customer-sla/manage')) return 'sla-manage';
    if (path.includes('/customer-sla/add')) return 'sla-add';
    if (path.includes('/customer-sla')) return 'sla-dashboard';

    // RMA Keys
    if (path.includes("rma-requests")) return "rma-request";
    if (path.includes("unrepaired")) return "unrepaired";
    if (path.includes("assigned")) return "assigned";
    if (path.includes("repaired") && !path.includes("cant") && !path.includes("unrepaired")) return "repaired";
    if (path.includes("cant-be-repaired")) return "cant-be-repaired";
    if (path.includes("depot-dispatch")) return "depot-dispatch";
    if (path.includes("audit-trail")) return "audit-trail";
    if (path.includes("rma-reports")) return "rma-reports";

    return "rma-dashboard";
  };

  const [selectedKey, setSelectedKey] = useState(getSelectedKey());

  useEffect(() => {
    setSelectedKey(getSelectedKey());
  }, [location.pathname]);

  const handleMenuClick = ({ key }) => {
    if (key === "logout") {
        showLogoutConfirm();
        return;
    }

    // Customer SLA Navigation
    if (key === "sla-dashboard") navigate("/customer-sla");
    if (key === "sla-manage") navigate("/customer-sla/manage");
    if (key === "sla-add") navigate("/customer-sla/add");

    // RMA Navigation
    if (key === "rma-dashboard") navigate("/rma-dashboard");
    if (key === "rma-request") navigate("/rma-requests");
    if (key === "unrepaired") navigate("/unrepaired");
    if (key === "assigned") navigate("/assigned");
    if (key === "repaired") navigate("/repaired");
    if (key === "cant-be-repaired") navigate("/cant-be-repaired");
    if (key === "depot-dispatch") navigate("/depot-dispatch");
    if (key === "audit-trail") navigate("/audit-trail");
    if (key === "rma-reports") navigate("/rma-reports");

    // Auto-close sidebar on mobile after navigation
    if (isMobile) {
      setCollapsed(true);
    }
  };

  // Determine sidebar theme based on selected key
  const getSidebarTheme = () => {
    if (selectedKey === "repaired") return "theme-green";
    if (selectedKey === "cant-be-repaired") return "theme-red";
    if (selectedKey === "audit-trail" || selectedKey === "depot-dispatch" || selectedKey === "rma-reports") return "theme-purple";
    return "theme-blue"; // default for dashboard, unrepaired, assigned, rma-request
  };

  // Menu Items Definition
  const rmaMenuItems = [
    { key: "rma-dashboard", icon: <DashboardOutlined />, label: "Dashboard" },
    { key: "rma-request", icon: <FileTextOutlined />, label: "RMA Request" },
    { key: "unrepaired", icon: <ToolOutlined />, label: "Unrepaired" },
    { key: "assigned", icon: <UserSwitchOutlined />, label: "Assigned" },
    { key: "repaired", icon: <CheckCircleOutlined />, label: "Local Repaired" },
    { key: "depot-dispatch", icon: <CarOutlined />, label: "Depot Dispatch" },
    { key: "cant-be-repaired", icon: <WarningOutlined />, label: "Can't Be Repaired" },
    { key: "rma-reports", icon: <BarChartOutlined />, label: "Reports" },
    { key: "audit-trail", icon: <HistoryOutlined />, label: "Audit Trail" },
    { key: "logout", icon: <LogoutOutlined />, label: "Logout", danger: true },
  ];

  const customerSlaMenuItems = [
    { key: "sla-dashboard", icon: <DashboardOutlined />, label: "Dashboard" },
    { key: "sla-manage", icon: <UnorderedListOutlined />, label: "Manage All SLAs" },
    { key: "sla-add", icon: <PlusOutlined />, label: "Add New SLA" },
    { key: "logout", icon: <LogoutOutlined />, label: "Logout", danger: true },
  ];

  const menuItems = sidebarType === 'customerSla' ? customerSlaMenuItems : rmaMenuItems;

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* Global Header with Sidebar Toggle */}
      <Header
        onLogout={showLogoutConfirm}
        onToggleSidebar={noSidebar ? undefined : () => setCollapsed(!collapsed)}
      />

      <Layout>
        {/* Sidebar */}
        {!noSidebar && (
        <Sider
          width={220}
          collapsible
          collapsed={collapsed}
          onCollapse={(value) => setCollapsed(value)}
          breakpoint="lg"
          collapsedWidth={isMobile ? 0 : 80}
          onBreakpoint={(broken) => {
            setIsMobile(broken);
            if (broken) setCollapsed(true);
          }}
          trigger={null}
          className={`msipl-sider ${getSidebarTheme()}`}
          theme="dark"
          style={{
            height: isMobile ? (collapsed ? 0 : "100vh") : "auto",
            position: isMobile && !collapsed ? "fixed" : "relative",
            zIndex: isMobile ? 1005 : 10,
            left: 0,
            top: isMobile ? 0 : 0,
            display: isMobile && collapsed ? "none" : "block",
            transition: "all 0.3s ease"
          }}
        >
          {/* Mobile Sidebar Header with Close Button */}
          {isMobile && !collapsed && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '16px 20px',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
              marginBottom: '8px'
            }}>
              <span style={{ color: '#fff', fontSize: '18px', fontWeight: 'bold' }}>Menu</span>
              <CloseCircleOutlined
                style={{ fontSize: '24px', color: '#ff4d4f', cursor: 'pointer' }}
                onClick={() => setCollapsed(true)}
              />
            </div>
          )}

          <div
            className="msipl-sider-toggle"
            onClick={() => setCollapsed(!collapsed)}
            style={isMobile ? { display: 'none' } : {}}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>

          {/* Module Switcher - Only show for default RMA */}
          {!collapsed && sidebarType === 'rma' && (
            <div className="module-switcher-rma">
              <div className="module-switcher-label-rma">
                <SwapOutlined style={{ marginRight: 6 }} />
                <span>Switch Module</span>
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
          
          {/* For Customer SLA Header */}
          {!collapsed && sidebarType === 'customerSla' && (
             <div style={{ padding: '16px', color: 'rgba(255,255,255,0.4)', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '1px', borderBottom: '1px solid rgba(255,255,255,0.1)', marginBottom: '10px' }}>
                Customer SLA
             </div>
          )}

          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            theme="dark"
            onClick={handleMenuClick}
            items={menuItems}
          />
        </Sider>
        )}

        {/* Main content + footer */}
        <Layout>
          <Content className="msipl-content">{children}</Content>

          <GlobalFooter />
        </Layout>
      </Layout>
    </Layout>
  );
}

export default RmaLayout;
