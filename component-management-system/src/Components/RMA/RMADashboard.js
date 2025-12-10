// src/Components/RMA/RMADashboard.js
import React, { useState, useEffect } from "react";
import { Row, Col, Card, Statistic, Spin, message, Avatar } from "antd";
import {
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  InboxOutlined,
  UserOutlined
} from "@ant-design/icons";
import RmaLayout from "./RmaLayout";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import "./RmaDashboard.css";

function RmaDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  // User info from localStorage
  const name = localStorage.getItem("name") || "Admin User";
  const email = localStorage.getItem("email") || "admin@example.com";
  const mobile = localStorage.getItem("mobile") || "+1 234 567 890";
  const role = (localStorage.getItem("_User_role_for_MSIPL") || "Administrator").toUpperCase();

  // Greeting based on time of day
  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good morning";
    if (hour < 18) return "Good afternoon";
    return "Good evening";
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    setLoading(true);
    const result = await RmaApi.getRmaDashboardStats();
    if (result.success) {
      setStats(result.data);
    } else {
      message.error("Failed to load RMA statistics");
    }
    setLoading(false);
  };

  return (
    <RmaLayout>
      <div className="rma-dashboard">
        <h1 className="dashboard-title">RMA Dashboard</h1>

        <Row gutter={[24, 24]} justify="center" style={{ marginTop: 24 }}>
          {/* User Profile Section */}
          <Col xs={24} sm={24} md={12} lg={10} xl={8}>
            <Card className="user-profile-card">
              <div className="profile-header">
                <Avatar className="profile-avatar" size={100} icon={<UserOutlined />} />
                <h2 className="user-name">{`${getGreeting()}, ${name}`}</h2>
              </div>
              <div className="user-details-list">
                <p><strong>Email:</strong> {email}</p>
                <p><strong>Mobile No:</strong> {mobile}</p>
                <p><strong>User Role:</strong> {role}</p>
              </div>
            </Card>
          </Col>
        </Row>

        {/* Statistics Section */}
        <h2 style={{ marginTop: 32, marginBottom: 16, fontSize: 18, fontWeight: 600 }}>RMA Statistics</h2>

        {loading ? (
          <div style={{ textAlign: "center", padding: "50px" }}>
            <Spin size="large" />
          </div>
        ) : (
          <Row gutter={[24, 24]}>
            {/* Total RMA Requests */}
            <Col xs={24} sm={12} md={6}>
              <Card className="stat-card stat-card-blue" hoverable>
                <Statistic
                  title="Total RMA Requests"
                  value={stats?.totalRequests || 0}
                  prefix={<FileTextOutlined />}
                  valueStyle={{ color: "#1890ff" }}
                />
              </Card>
            </Col>

            {/* Total Items */}
            <Col xs={24} sm={12} md={6}>
              <Card className="stat-card stat-card-purple" hoverable>
                <Statistic
                  title="Total Items"
                  value={stats?.totalItems || 0}
                  prefix={<InboxOutlined />}
                  valueStyle={{ color: "#722ed1" }}
                />
              </Card>
            </Col>

            {/* Repaired Items */}
            <Col xs={24} sm={12} md={6}>
              <Card className="stat-card stat-card-green" hoverable>
                <Statistic
                  title="Repaired Items"
                  value={stats?.repairedCount || 0}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: "#52c41a" }}
                />
              </Card>
            </Col>

            {/* Unrepaired Items */}
            <Col xs={24} sm={12} md={6}>
              <Card className="stat-card stat-card-orange" hoverable>
                <Statistic
                  title="Unrepaired Items"
                  value={stats?.unrepairedCount || 0}
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: "#fa8c16" }}
                />
              </Card>
            </Col>
          </Row>
        )}
      </div>
    </RmaLayout>
  );
}

export default RmaDashboard;
