// src/components/EmployeeList/EmployeeList.js

import React, { useEffect, useMemo, useState } from "react";
import {
  Card,
  Select,
  message,
  Typography,
  Spin,
  Table,
  Button,
  Badge,
  Tooltip,
  Tag,
} from "antd";
import {
  TeamOutlined,
  ReloadOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  GlobalOutlined,
} from "@ant-design/icons";

import GetEmployeeListAPI from "../API/GetEmployeeListAPI/GetEmployeeListAPI";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import "./EmployeeList.css";

const { Title, Text } = Typography;

function EmployeeList() {
  const role = useMemo(
    () => localStorage.getItem("_User_role_for_MSIPL") || "user",
    []
  );

  const [employees, setEmployees] = useState([]);
  const [loadingEmployees, setLoadingEmployees] = useState(false);
  const [regions, setRegions] = useState([]);
  const [loadingRegions, setLoadingRegions] = useState(false);
  const [selectedRegion, setSelectedRegion] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    document.title = "Employee List";
  }, []);

  const fetchRegions = async () => {
    setLoadingRegions(true);
    try {
      const data = await GetRegionAPI();
      if (Array.isArray(data)) {
        setRegions(data);
      } else {
        setRegions([]);
      }
    } catch (error) {
      console.error("API Error (Regions):", error);
      message.error("Failed to fetch regions.", 2);
    } finally {
      setLoadingRegions(false);
    }
  };

  const fetchEmployees = async (region) => {
    setLoadingEmployees(true);
    try {
      const data = await GetEmployeeListAPI(region);
      if (Array.isArray(data)) {
        setEmployees(data);
      } else {
        setEmployees([]);
        message.warning("Employee list is empty or invalid.", 2);
      }
    } catch (error) {
      console.error("API Error (Employee List):", error);
      message.error(
        error?.message || "Failed to fetch employees. Please try again.",
        3
      );
    } finally {
      setLoadingEmployees(false);
    }
  };

  useEffect(() => {
    fetchEmployees(null);
    if (role === "admin") {
      fetchRegions();
    }
  }, [role]);

  const handleRegionChange = (value) => {
    const regionToFetch = value || null;
    setSelectedRegion(regionToFetch);
    fetchEmployees(regionToFetch);
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchEmployees(selectedRegion);
    if (role === "admin") {
      await fetchRegions();
    }
    setRefreshing(false);
    message.success("List refreshed");
  };

  // Stats
  const totalEmployees = employees.length;
  const onlineCount = employees.filter((e) => e.online).length;

  const columns = [
    {
      title: "Status",
      dataIndex: "online",
      key: "status",
      width: 100,
      render: (isOnline, record) => {
        if (isOnline) {
          return (
            <Tooltip title="Currently active">
              <Badge status="success" text={<Tag color="green">Online</Tag>} />
            </Tooltip>
          );
        }
        let lastSeenText = "Offline";
        if (record.lastActiveAt) {
          const lastActive = new Date(record.lastActiveAt);
          const now = new Date();
          const diffMinutes = Math.floor((now - lastActive) / 60000);
          if (diffMinutes < 60) {
            lastSeenText = `${diffMinutes}m ago`;
          } else if (diffMinutes < 1440) {
            lastSeenText = `${Math.floor(diffMinutes / 60)}h ago`;
          } else {
            lastSeenText = `${Math.floor(diffMinutes / 1440)}d ago`;
          }
        }
        return (
          <Tooltip
            title={
              record.lastActiveAt
                ? `Last seen: ${new Date(record.lastActiveAt).toLocaleString()}`
                : "Never logged in"
            }
          >
            <Badge
              status="default"
              text={<Text type="secondary">{lastSeenText}</Text>}
            />
          </Tooltip>
        );
      },
      sorter: (a, b) => (a.online === b.online ? 0 : a.online ? -1 : 1),
    },
    {
      title: (
        <span>
          <UserOutlined /> Name
        </span>
      ),
      dataIndex: "name",
      key: "name",
      sorter: (a, b) => a.name.localeCompare(b.name),
    },
    {
      title: (
        <span>
          <MailOutlined /> Email
        </span>
      ),
      dataIndex: "email",
      key: "email",
    },
    {
      title: (
        <span>
          <PhoneOutlined /> Mobile No
        </span>
      ),
      dataIndex: "mobileNo",
      key: "mobileNo",
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",
      render: (role) => {
        const colors = {
          admin: "red",
          manager: "blue",
          employee: "green",
        };
        return (
          <Tag color={colors[role?.toLowerCase()] || "default"}>
            {role || "N/A"}
          </Tag>
        );
      },
      filters: [
        { text: "Admin", value: "admin" },
        { text: "Manager", value: "manager" },
        { text: "Employee", value: "employee" },
      ],
      onFilter: (value, record) =>
        String(record.role || "").toLowerCase() === value,
    },
    {
      title: (
        <span>
          <GlobalOutlined /> Region
        </span>
      ),
      dataIndex: "regionName",
      key: "regionName",
      ...(role !== "admin" && { responsive: ["md"] }),
    },
  ];

  const finalColumns =
    role === "admin"
      ? columns
      : columns.filter((col) => col.key !== "regionName");

  return (
    <div className="employee-list-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-icon">
            <TeamOutlined />
          </div>
          <div className="header-text">
            <Title level={3} className="header-title">
              Employee List
            </Title>
            <Text className="header-subtitle">
              Browse, filter, and manage all employees in the system
            </Text>
          </div>
        </div>
        <Button
          icon={<ReloadOutlined spin={refreshing} />}
          onClick={handleRefresh}
          loading={refreshing}
          className="refresh-btn"
        >
          Refresh
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="employee-stats">
        <div className="stat-card total">
          <div className="stat-icon">
            <TeamOutlined />
          </div>
          <div className="stat-info">
            <span className="stat-value">{totalEmployees}</span>
            <span className="stat-label">Total Employees</span>
            <span className="stat-sublabel">Registered users</span>
          </div>
        </div>

        <div className="stat-card online">
          <div className="stat-icon online-icon">
            <Badge status="success" />
          </div>
          <div className="stat-info">
            <span className="stat-value">{onlineCount}</span>
            <span className="stat-label">Online Now</span>
            <span className="stat-sublabel">Currently active</span>
          </div>
        </div>

        <div className="stat-card offline">
          <div className="stat-icon">
            <Badge status="default" />
          </div>
          <div className="stat-info">
            <span className="stat-value">{totalEmployees - onlineCount}</span>
            <span className="stat-label">Offline</span>
            <span className="stat-sublabel">Not active</span>
          </div>
        </div>

        {role === "admin" && (
          <div className="stat-card filter">
            <div className="stat-content">
              <span className="filter-label">
                <GlobalOutlined /> Filter by Region
              </span>
              <Select
                showSearch
                placeholder={
                  loadingRegions ? "Loading..." : "All Regions"
                }
                loading={loadingRegions}
                disabled={loadingRegions}
                allowClear
                style={{ width: "100%" }}
                size="large"
                onChange={handleRegionChange}
                value={selectedRegion}
                optionFilterProp="children"
                filterOption={(input, option) =>
                  (option?.children ?? "")
                    .toLowerCase()
                    .includes(input.toLowerCase())
                }
              >
                {regions.map((region) => (
                  <Select.Option key={region} value={region}>
                    {region}
                  </Select.Option>
                ))}
              </Select>
            </div>
          </div>
        )}
      </div>

      {/* Table Card */}
      <Card className="employee-table-card" bordered={false}>
        <Spin spinning={loadingEmployees} tip="Loading employees...">
          <Table
            dataSource={employees}
            columns={finalColumns}
            rowKey="email"
            pagination={{ pageSize: 10, showSizeChanger: true }}
            scroll={{ x: true }}
            className="employee-table"
          />
        </Spin>
      </Card>
    </div>
  );
}

export default EmployeeList;