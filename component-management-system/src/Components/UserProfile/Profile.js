import React, { useEffect, useMemo, useState } from "react";
import { Card, Avatar, Spin, message, Button, Typography, Row, Col, Divider, Tag, Tooltip as AntTooltip } from "antd";
import {
  UserOutlined, KeyOutlined, MailOutlined, PhoneOutlined,
  GlobalOutlined, SafetyCertificateOutlined, ReloadOutlined,
  AppstoreOutlined, EnvironmentOutlined, PieChartOutlined, BarChartOutlined
} from "@ant-design/icons";
import { 
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid 
} from 'recharts';
import Cookies from "js-cookie";
import { URL } from "../API/URL";
import { useNavigate } from "react-router-dom";
import ChangePasswordModal from "./ChangePassword";
import "./Profile.css";

const { Title, Text } = Typography;

// --- Animation Hooks (Kept from your code) ---
function useCountUp(target, duration = 1000) {
  const [value, setValue] = useState(0);
  useEffect(() => {
    let start = 0;
    const end = parseInt(target, 10) || 0;
    if (start === end) return;
    let totalMilSec = duration;
    let incrementTime = (totalMilSec / end) * 1000;
    let timer = setInterval(() => {
      start += 1;
      setValue(start);
      if (start === end) clearInterval(timer);
    }, 10); // Simplified for stability in charts
    setValue(end); // Direct set for now to prevent chart flickering
    return () => clearInterval(timer);
  }, [target, duration]);
  return { value };
}

const REGION_COLORS = ["#1677ff", "#52c41a", "#faad14", "#722ed1", "#eb2f96", "#13c2c2"];

export default function Profile() {
  const navigate = useNavigate();

  // Local Storage Data
  const name = localStorage.getItem("name") || "User";
  const email = localStorage.getItem("email") || "N/A";
  const mobile = localStorage.getItem("mobile") || "N/A";
  const role = (localStorage.getItem("_User_role_for_MSIPL") || "N/A").toUpperCase();
  const region = localStorage.getItem("region") || null;

  // State
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [changePasswordVisible, setChangePasswordVisible] = useState(false);

  // Fetch Logic
  function getAuthToken() {
    return localStorage.getItem("authToken") || Cookies.get("authToken");
  }

  async function fetchDashboardSummary() {
    setLoading(true);
    setError(null);
    try {
        // MOCK DATA FOR TESTING (Replace with your actual fetch call)
        // const res = await fetch(URL + "/admin/user/dashboard-summary"...
        
        // Simulating API response for visual demo:
        setTimeout(() => {
            setSummary({
                totalItems: 1250,
                regionCounts: [
                    { region: 'North', count: 450 },
                    { region: 'South', count: 300 },
                    { region: 'East', count: 200 },
                    { region: 'West', count: 300 },
                ]
            });
            setLoading(false);
        }, 800);

    } catch (err) {
      setError(err.message || "Network error");
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchDashboardSummary();
  }, []);

  // Prepare Chart Data
  const regionData = useMemo(() => {
    if (!summary?.regionCounts) return [];
    return summary.regionCounts.map((item, index) => ({
        name: item.region || "Unknown",
        value: item.count,
        color: REGION_COLORS[index % REGION_COLORS.length]
    }));
  }, [summary]);

  const totalItems = summary?.totalItems || 0;

  const getRoleColor = (r) => {
    switch (r) {
      case "ADMIN": return "#f5222d";
      case "MANAGER": return "#722ed1";
      default: return "#1677ff";
    }
  };

  // Custom Tooltip for Recharts
  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="custom-chart-tooltip">
          <p className="tooltip-label">{payload[0].name}</p>
          <p className="tooltip-value">{payload[0].value} Items</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="profile-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-icon">
            <UserOutlined />
          </div>
          <div className="header-text">
            <Title level={3} className="header-title">Welcome back, {name}</Title>
            <Text className="header-subtitle">Manage your profile and view inventory analytics</Text>
          </div>
        </div>
        <Button
          icon={<ReloadOutlined spin={loading} />}
          onClick={fetchDashboardSummary}
          className="refresh-btn"
        >
          Refresh Data
        </Button>
      </div>

      <Row gutter={[24, 24]}>
        {/* LEFT COLUMN: Profile Info */}
        <Col xs={24} lg={8} xl={7}>
          <Card className="profile-card" bordered={false}>
            <div className="profile-avatar-section">
              <Avatar size={110} icon={<UserOutlined />} className="profile-avatar" />
              <div className="profile-name-section">
                <Title level={4} className="profile-name">{name}</Title>
                <Tag color={getRoleColor(role)} className="role-tag">{role}</Tag>
              </div>
            </div>

            <Divider className="profile-divider" />

            <div className="profile-details">
              <div className="detail-item">
                <div className="detail-icon"><MailOutlined /></div>
                <div className="detail-content">
                  <Text className="detail-label">Email</Text>
                  <Text className="detail-value">{email}</Text>
                </div>
              </div>
              <div className="detail-item">
                <div className="detail-icon"><PhoneOutlined /></div>
                <div className="detail-content">
                  <Text className="detail-label">Phone</Text>
                  <Text className="detail-value">{mobile}</Text>
                </div>
              </div>
              {region && (
                <div className="detail-item">
                  <div className="detail-icon"><GlobalOutlined /></div>
                  <div className="detail-content">
                    <Text className="detail-label">Region</Text>
                    <Text className="detail-value">{region}</Text>
                  </div>
                </div>
              )}
            </div>

            <div className="profile-actions">
              <Button type="primary" icon={<KeyOutlined />} onClick={() => setChangePasswordVisible(true)} block className="change-password-btn">
                Change Password
              </Button>
            </div>
          </Card>
        </Col>

        {/* RIGHT COLUMN: Dashboard & Analytics */}
        <Col xs={24} lg={16} xl={17}>
          {loading ? (
             <Card className="stats-card loading-card"><Spin size="large" tip="Loading Analytics..." /></Card>
          ) : error ? (
             <Card className="stats-card"><Text type="danger">{error}</Text></Card>
          ) : (
            <>
              {/* Top Row: KPI Cards */}
              <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col xs={24} sm={12}>
                  <Card className="kpi-card kpi-primary" bordered={false}>
                     <div className="kpi-content">
                        <div>
                            <Text className="kpi-label">Total Inventory</Text>
                            <Title level={2} className="kpi-value">{totalItems.toLocaleString()}</Title>
                        </div>
                        <div className="kpi-icon"><AppstoreOutlined /></div>
                     </div>
                     <div className="kpi-footer">Across {regionData.length} Regions</div>
                  </Card>
                </Col>
                <Col xs={24} sm={12}>
                  <Card className="kpi-card kpi-secondary" bordered={false}>
                     <div className="kpi-content">
                        <div>
                            <Text className="kpi-label">Top Region</Text>
                            <Title level={2} className="kpi-value">
                                {regionData.sort((a,b) => b.value - a.value)[0]?.name || "N/A"}
                            </Title>
                        </div>
                        <div className="kpi-icon"><EnvironmentOutlined /></div>
                     </div>
                     <div className="kpi-footer">Highest Activity</div>
                  </Card>
                </Col>
              </Row>

              {/* Bottom Row: Charts */}
              <Row gutter={[16, 16]}>
                {/* Donut Chart */}
                <Col xs={24} xl={12}>
                    <Card className="chart-card" title={<><PieChartOutlined /> Regional Distribution</>} bordered={false}>
                        <div style={{ width: '100%', height: 300 }}>
                            <ResponsiveContainer>
                                <PieChart>
                                    <Pie
                                        data={regionData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={60}
                                        outerRadius={80}
                                        paddingAngle={5}
                                        dataKey="value"
                                    >
                                        {regionData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} stroke="none" />
                                        ))}
                                    </Pie>
                                    <Tooltip content={<CustomTooltip />} />
                                    <Legend verticalAlign="bottom" height={36} iconType="circle"/>
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </Card>
                </Col>

                {/* Bar Chart */}
                <Col xs={24} xl={12}>
                    <Card className="chart-card" title={<><BarChartOutlined /> Volume by Region</>} bordered={false}>
                        <div style={{ width: '100%', height: 300 }}>
                            <ResponsiveContainer>
                                <BarChart data={regionData} layout="vertical" margin={{ top: 5, right: 30, left: 40, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="rgba(255,255,255,0.1)" />
                                    <XAxis type="number" hide />
                                    <YAxis type="category" dataKey="name" width={80} tick={{fill: '#8c8c8c', fontSize: 12}} />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar dataKey="value" radius={[0, 4, 4, 0]} barSize={20}>
                                        {regionData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </Card>
                </Col>
              </Row>
            </>
          )}
        </Col>
      </Row>

      <ChangePasswordModal
        visible={changePasswordVisible}
        onClose={() => setChangePasswordVisible(false)}
      />
    </div>
  );
}