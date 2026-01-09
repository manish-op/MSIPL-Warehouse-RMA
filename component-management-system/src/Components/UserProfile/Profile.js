import React, { useEffect, useMemo, useState } from "react";
import { Card, Avatar, Spin, message, Button, Typography, Row, Col, Divider, Tag } from "antd";
import {
  UserOutlined, KeyOutlined, MailOutlined, PhoneOutlined,
  GlobalOutlined, SafetyCertificateOutlined, ReloadOutlined,
  AppstoreOutlined, EnvironmentOutlined,
} from "@ant-design/icons";
import Cookies from "js-cookie";
import { URL } from "../API/URL";
import { useNavigate } from "react-router-dom";
import ChangePasswordModal from "./ChangePassword";
import "./Profile.css";

const { Title, Text } = Typography;

function useCountUp(target, duration = 1000) {
  const [value, setValue] = useState(0);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const to = Number(target) || 0;
    const startTime = performance.now();
    let raf = 0;

    function step(ts) {
      const elapsed = ts - startTime;
      const t = Math.min(1, elapsed / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      setProgress(eased);
      const cur = Math.round(to * eased);
      setValue(cur);
      if (t < 1) {
        raf = requestAnimationFrame(step);
      } else {
        setProgress(1);
        setValue(to);
      }
    }

    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [target, duration]);

  return { value, progress };
}

function useCountUpArray(targetArray = [], duration = 1000) {
  // Use JSON.stringify for stable dependency to avoid useMemo size warning
  const targetArrayString = JSON.stringify(targetArray);
  const targetValues = useMemo(() => targetArray.map((v) => Number(v) || 0), [targetArrayString]); // eslint-disable-line react-hooks/exhaustive-deps

  const [values, setValues] = useState(() => targetValues.map(() => 0));
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const from = targetValues.map(() => 0);
    const to = targetValues;
    const startTime = performance.now();
    let raf = 0;

    function step(ts) {
      const elapsed = ts - startTime;
      const t = Math.min(1, elapsed / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      setProgress(eased);

      const next = from.map((f, i) => Math.round(f + (to[i] - f) * eased));
      setValues(next);

      if (t < 1) {
        raf = requestAnimationFrame(step);
      } else {
        setProgress(1);
        setValues(to);
      }
    }

    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [duration, targetArrayString, targetValues]);

  return { values, progress };
}

const REGION_COLORS = [
  "#1677ff",
  "#52c41a",
  "#faad14",
  "#722ed1",
  "#eb2f96",
];

export default function Profile() {
  const navigate = useNavigate();

  // Profile data from local storage
  const name = localStorage.getItem("name") || "User";
  const email = localStorage.getItem("email") || "N/A";
  const mobile = localStorage.getItem("mobile") || "N/A";
  const role = (localStorage.getItem("_User_role_for_MSIPL") || "N/A").toUpperCase();
  const region = localStorage.getItem("region") || null;

  // Local state
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [changePasswordVisible, setChangePasswordVisible] = useState(false);

  function getAuthToken() {
    try {
      const cookie = Cookies.get("authToken");
      if (cookie) return atob(cookie);
    } catch (_) { }
    return localStorage.getItem("authToken");
  }

  async function fetchDashboardSummary() {
    setLoading(true);
    setError(null);
    const token = getAuthToken();
    const headers = {};
    if (token) headers["Authorization"] = `Bearer ${token}`;

    try {
      const res = await fetch(URL + "/admin/user/dashboard-summary", {
        method: "GET",
        credentials: "include",
        headers,
      });

      if (res.status === 401) {
        Cookies.remove("authToken", { path: "/" });
        localStorage.clear();
        message.warning("Session expired. Please login again.");
        navigate("/login");
        return;
      }

      if (!res.ok) {
        const txt = await res.text();
        setError(txt || "Server error");
        setLoading(false);
        return;
      }

      const data = await res.json();
      setSummary(data);
      setLoading(false);
    } catch (err) {
      setError(err.message || "Network error");
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchDashboardSummary();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Prepare animated values
  const totalTarget = summary?.totalItems || 0;
  const { value: animatedTotal, progress: totalProgress } = useCountUp(totalTarget, 1200);

  const regionCounts = useMemo(() => {
    if (!summary || !summary.regionCounts) return [];
    const isAdmin = role === "ADMIN";
    if (!isAdmin && region) {
      return summary.regionCounts.filter((rc) => (rc.region || "").toLowerCase() === (region || "").toLowerCase());
    }
    return summary.regionCounts;
  }, [summary, role, region]);

  const regionTargetCounts = regionCounts.map((r) => r.count || 0);
  const { values: animatedRegionCounts, progress: regionsProgress } = useCountUpArray(regionTargetCounts, 1000);

  const combinedProgress = regionCounts.length > 0 ? (totalProgress + regionsProgress) / 2 : totalProgress;

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good Morning";
    if (hour < 18) return "Good Afternoon";
    return "Good Evening";
  };

  const getRoleColor = (r) => {
    switch (r) {
      case "ADMIN": return "#f5222d";
      case "MANAGER": return "#722ed1";
      default: return "#1677ff";
    }
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
            <Title level={3} className="header-title">{getGreeting()}, {name}!</Title>
            <Text className="header-subtitle">
              Welcome to your personal dashboard
            </Text>
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

      {/* Main Content */}
      <Row gutter={[24, 24]}>
        {/* Left Column - Profile Card */}
        <Col xs={24} lg={8}>
          <Card className="profile-card" bordered={false}>
            {/* Profile Header */}
            <div className="profile-avatar-section">
              <Avatar size={100} icon={<UserOutlined />} className="profile-avatar" />
              <div className="profile-name-section">
                <Title level={4} className="profile-name">{name}</Title>
                <Tag color={getRoleColor(role)} className="role-tag">{role}</Tag>
              </div>
            </div>

            <Divider className="profile-divider" />

            {/* Profile Details */}
            <div className="profile-details">
              <div className="detail-item">
                <div className="detail-icon">
                  <MailOutlined />
                </div>
                <div className="detail-content">
                  <Text className="detail-label">Email Address</Text>
                  <Text className="detail-value">{email}</Text>
                </div>
              </div>

              <div className="detail-item">
                <div className="detail-icon">
                  <PhoneOutlined />
                </div>
                <div className="detail-content">
                  <Text className="detail-label">Mobile Number</Text>
                  <Text className="detail-value">{mobile}</Text>
                </div>
              </div>

              {region && (
                <div className="detail-item">
                  <div className="detail-icon">
                    <GlobalOutlined />
                  </div>
                  <div className="detail-content">
                    <Text className="detail-label">Assigned Region</Text>
                    <Text className="detail-value">{region}</Text>
                  </div>
                </div>
              )}

              <div className="detail-item">
                <div className="detail-icon">
                  <SafetyCertificateOutlined />
                </div>
                <div className="detail-content">
                  <Text className="detail-label">Access Level</Text>
                  <Text className="detail-value">{role}</Text>
                </div>
              </div>
            </div>

            {/* Change Password Button */}
            <div className="profile-actions">
              <Button
                type="primary"
                icon={<KeyOutlined />}
                onClick={() => setChangePasswordVisible(true)}
                block
                className="change-password-btn"
              >
                Change Password
              </Button>
            </div>
          </Card>
        </Col>

        {/* Right Column - Stats */}
        <Col xs={24} lg={16}>
          <Card className="stats-card" bordered={false}>
            {loading ? (
              <div className="centered-spin">
                <Spin size="large" />
                <Text className="loading-text">Loading dashboard data...</Text>
              </div>
            ) : error ? (
              <div className="error-container">
                <Text className="error-text">{error}</Text>
                <Button type="primary" onClick={fetchDashboardSummary} style={{ marginTop: 16 }}>
                  Retry
                </Button>
              </div>
            ) : (
              <>
                {/* Stats Header */}
                <div className="stats-header">
                  <AppstoreOutlined className="stats-header-icon" />
                  <Text strong className="stats-header-title">Inventory Overview</Text>
                </div>
                <Divider className="stats-divider" />

                {/* Stats Boxes */}
                <Row gutter={[16, 16]}>
                  <Col xs={12} sm={6}>
                    <div className="stat-box primary">
                      <div className="stat-icon">
                        <AppstoreOutlined />
                      </div>
                      <div className="stat-content">
                        <Text className="stat-label">Total Items</Text>
                        <Text className="stat-number">{animatedTotal.toLocaleString()}</Text>
                      </div>
                    </div>
                  </Col>
                  <Col xs={12} sm={6}>
                    <div className="stat-box success">
                      <div className="stat-icon" style={{ background: 'rgba(82, 196, 26, 0.1)', color: '#52c41a' }}>
                        <AppstoreOutlined />
                      </div>
                      <div className="stat-content">
                        <Text className="stat-label">Available</Text>
                        <Text className="stat-number" style={{ color: '#52c41a' }}>
                          {(summary?.availableCount || 0).toLocaleString()}
                        </Text>
                      </div>
                    </div>
                  </Col>
                  <Col xs={12} sm={6}>
                    <div className="stat-box purple">
                      <div className="stat-icon" style={{ background: 'rgba(114, 46, 209, 0.1)', color: '#722ed1' }}>
                        <AppstoreOutlined />
                      </div>
                      <div className="stat-content">
                        <Text className="stat-label">Issued</Text>
                        <Text className="stat-number" style={{ color: '#722ed1' }}>
                          {(summary?.issuedCount || 0).toLocaleString()}
                        </Text>
                      </div>
                    </div>
                  </Col>
                  <Col xs={12} sm={6}>
                    <div className="stat-box warning">
                      <div className="stat-icon" style={{ background: 'rgba(250, 140, 22, 0.1)', color: '#fa8c16' }}>
                        <AppstoreOutlined />
                      </div>
                      <div className="stat-content">
                        <Text className="stat-label">Repairing</Text>
                        <Text className="stat-number" style={{ color: '#fa8c16' }}>
                          {(summary?.repairingCount || 0).toLocaleString()}
                        </Text>
                      </div>
                    </div>
                  </Col>
                </Row>

                {/* Progress Bar */}
                <div className="progress-section">
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{ width: `${Math.round(combinedProgress * 100)}%` }}
                    />
                  </div>
                </div>

                {/* Region Cards */}
                {regionCounts.length > 0 && (
                  <>
                    <div className="section-header">
                      <EnvironmentOutlined className="section-icon" />
                      <Text strong className="section-title">Region Breakdown</Text>
                      <Tag color="blue" style={{ marginLeft: 'auto' }}>{regionCounts.length} Regions</Tag>
                    </div>
                    <Row gutter={[16, 16]} className="region-cards-enhanced">
                      {regionCounts.map((rc, idx) => {
                        const color = REGION_COLORS[idx % REGION_COLORS.length];
                        const animated = animatedRegionCounts[idx] ?? 0;
                        const percentage = totalTarget > 0 ? Math.round((animated / totalTarget) * 100) : 0;
                        return (
                          <Col xs={24} sm={12} md={8} key={rc.region || idx}>
                            <div className="region-card-enhanced" style={{ borderLeft: `4px solid ${color}` }}>
                              <div className="region-card-header">
                                <div className="region-avatar" style={{ background: color }}>
                                  {String(rc.region || "R").charAt(0).toUpperCase()}
                                </div>
                                <div className="region-details">
                                  <Text className="region-name-enhanced">{String(rc.region || "UNKNOWN").toUpperCase()}</Text>
                                  <Text className="region-count-enhanced">{animated.toLocaleString()} items</Text>
                                </div>
                                <div className="region-percentage" style={{ color: color }}>
                                  {percentage}%
                                </div>
                              </div>
                              <div className="region-progress-bar">
                                <div
                                  className="region-progress-fill"
                                  style={{
                                    width: `${percentage}%`,
                                    background: `linear-gradient(90deg, ${color}44, ${color})`
                                  }}
                                />
                              </div>
                            </div>
                          </Col>
                        );
                      })}
                    </Row>
                  </>
                )}
              </>
            )}
          </Card>
        </Col>
      </Row>

      {/* Change Password Modal */}
      <ChangePasswordModal
        visible={changePasswordVisible}
        onClose={() => setChangePasswordVisible(false)}
      />
    </div>
  );
}