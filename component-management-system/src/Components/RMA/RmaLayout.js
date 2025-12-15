// src/Components/RMA/RmaLayout.js
import React, { useState } from "react";
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
} from "@ant-design/icons";
import { useNavigate, useLocation } from "react-router-dom";
import Header from "../Header/Header"; // Import Global Header
import useNavigationGuard from "../../hooks/useNavigationGuard"; // Import navigation guard
import Cookies from "js-cookie"; // Import Cookies
import "./RmaDashboard.css";

const { Sider, Footer, Content } = Layout;
const { confirm } = Modal;

const RmaLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

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

  const location = useLocation();
  let selectedKey = "rma-dashboard";
  if (location.pathname.includes("rma-requests")) selectedKey = "rma-request";
  if (location.pathname.includes("unrepaired")) selectedKey = "unrepaired";
  if (location.pathname.includes("assigned")) selectedKey = "assigned";
  if (location.pathname.includes("repaired") && !location.pathname.includes("cant") && !location.pathname.includes("unrepaired")) selectedKey = "repaired";
  if (location.pathname.includes("cant-be-repaired")) selectedKey = "cant-be-repaired";
  if (location.pathname.includes("audit-trail")) selectedKey = "audit-trail";

  const handleMenuClick = ({ key }) => {
    if (key === "rma-dashboard") navigate("/rma-dashboard");
    if (key === "rma-request") navigate("/rma-requests");
    if (key === "unrepaired") navigate("/unrepaired");
    if (key === "assigned") navigate("/assigned");
    if (key === "repaired") navigate("/repaired");
    if (key === "cant-be-repaired") navigate("/cant-be-repaired");
    if (key === "audit-trail") navigate("/audit-trail");
    if (key === "logout") showLogoutConfirm();
  };

  // Determine sidebar theme based on selected key
  const getSidebarTheme = () => {
    if (selectedKey === "repaired") return "theme-green";
    if (selectedKey === "cant-be-repaired") return "theme-red";
    if (selectedKey === "audit-trail") return "theme-purple";
    return "theme-blue"; // default for dashboard, unrepaired, assigned, rma-request
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* Global Header */}
      <Header onLogout={showLogoutConfirm} />

      <Layout>
        {/* Sidebar */}
        <Sider
          width={220}
          collapsible
          collapsed={collapsed}
          trigger={null}
          className={`msipl-sider ${getSidebarTheme()}`}
          theme="dark"
        >
          <div
            className="msipl-sider-toggle"
            onClick={() => setCollapsed(!collapsed)}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>

          {/* Module Switcher */}
          {!collapsed && (
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

          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            theme="dark"
            onClick={handleMenuClick}
            items={[
              {
                key: "rma-dashboard",
                icon: <DashboardOutlined />,
                label: "Dashboard",
              },
              {
                key: "rma-request",
                icon: <FileTextOutlined />,
                label: "RMA Request",
              },
              {
                key: "unrepaired",
                icon: <ToolOutlined />,
                label: "Unrepaired",
              },
              {
                key: "assigned",
                icon: <UserSwitchOutlined />,
                label: "Assigned",
              },
              {
                key: "repaired",
                icon: <CheckCircleOutlined />,
                label: "Repaired",
              },
              {
                key: "cant-be-repaired",
                icon: <WarningOutlined />,
                label: "Can't Be Repaired",
              },
              {
                key: "audit-trail",
                icon: <HistoryOutlined />,
                label: "Audit Trail",
              },
              {
                key: "logout",
                icon: <LogoutOutlined />,
                label: "Logout",
                danger: true,
              },
            ]}
          />
        </Sider>

        {/* Main content + footer */}
        <Layout>
          <Content className="msipl-content">{children}</Content>

          <Footer className="msipl-footer">
            <span>Motorola Solutions India Pvt Ltd</span>
            <span>📍 Address: Gurgaon, Haryana, India</span>
            <span>📞 Contact: 01244192000</span>
            <span>About</span>
            <span>Help</span>
          </Footer>
        </Layout>
      </Layout>
    </Layout>
  );
}

export default RmaLayout;
