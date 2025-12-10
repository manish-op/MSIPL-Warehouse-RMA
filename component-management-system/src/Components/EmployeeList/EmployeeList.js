// src/components/EmployeeList/EmployeeList.js

import React, { useEffect, useMemo, useState } from "react";
import {
  Card,
  Select,
  message,
  Typography,
  Space,
  Spin,
  Divider,
  Table,
  Button,
  Badge,
  Tooltip,
  Tag,
} from "antd";
import {
  TeamOutlined, // For the title
  GlobalOutlined, // For the region select
  ReloadOutlined, // For the refresh button
} from "@ant-design/icons";

// Import the new API
import GetEmployeeListAPI from "../API/GetEmployeeListAPI/GetEmployeeListAPI";
// Import the regions API from your reference
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";

// Import a new CSS file for this component
import "./EmployeeList.css";

const { Text } = Typography;

function EmployeeList() {
  const role = useMemo(() => localStorage.getItem("_User_role_for_MSIPL") || "user", []);

  // State for the employee list and loading
  const [employees, setEmployees] = useState([]);
  const [loadingEmployees, setLoadingEmployees] = useState(false);

  // State for regions (copied from your AddEmployee)
  const [regions, setRegions] = useState([]);
  const [loadingRegions, setLoadingRegions] = useState(false);

  // State to track the currently selected filter
  const [selectedRegion, setSelectedRegion] = useState(null); // null = "All Regions"

  useEffect(() => {
    document.title = "Employee List";
  }, []);

  // Function to fetch regions (copied from your AddEmployee)
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

  // Function to fetch the employee list
  const fetchEmployees = async (region) => {
    setLoadingEmployees(true);
    try {
      // 'region' will be null for "All Regions" or a string
      const data = await GetEmployeeListAPI(region);
      if (Array.isArray(data)) {
        setEmployees(data);
      } else {
        setEmployees([]);
        message.warning("Employee list is empty or invalid.", 2);
      }
    } catch (error) {
      console.error("API Error (Employee List):", error);
      message.error(error?.message || "Failed to fetch employees. Please try again.", 3);
    } finally {
      setLoadingEmployees(false);
    }
  };

  // Load initial data on component mount
  useEffect(() => {
    // Fetch all employees initially
    fetchEmployees(null);

    // Fetch regions if the user is an admin
    if (role === "admin") {
      fetchRegions();
    }
  }, [role]); // Re-run if role changes (though unlikely)

  // Handler for when the region filter changes
  const handleRegionChange = (value) => {
    // 'value' will be undefined/null if "All Regions" is selected
    const regionToFetch = value || null;
    setSelectedRegion(regionToFetch);
    fetchEmployees(regionToFetch);
  };

  // Handler for the refresh button
  const handleRefresh = () => {
    // Re-fetch employees with the currently selected region
    fetchEmployees(selectedRegion);
    // Also refresh the regions list if admin
    if (role === "admin") {
      fetchRegions();
    }
  };

  // Define columns for the Ant Design Table
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
        // Calculate "last seen" text
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
          <Tooltip title={record.lastActiveAt ? `Last seen: ${new Date(record.lastActiveAt).toLocaleString()}` : "Never logged in"}>
            <Badge status="default" text={<Text type="secondary">{lastSeenText}</Text>} />
          </Tooltip>
        );
      },
      sorter: (a, b) => (a.online === b.online ? 0 : a.online ? -1 : 1),
    },
    {
      title: "Name",
      dataIndex: "name",
      key: "name",
      sorter: (a, b) => a.name.localeCompare(b.name),
    },
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
    },
    {
      title: "Mobile No",
      dataIndex: "mobileNo",
      key: "mobileNo",
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",

      filters: [
        { text: 'Admin', value: 'admin' },
        { text: 'Manager', value: 'manager' },
        { text: 'Employee', value: 'employee' },
      ],
      onFilter: (value, record) =>
        String(record.role || '').toLowerCase() === value,
    },
    {
      title: "Region",
      dataIndex: "regionName",
      key: "regionName",

      ...(role !== "admin" && { responsive: ['md'] })
    },
  ];

  // Filter out the 'Region' column if the user is not an admin
  const finalColumns = (role === "admin")
    ? columns
    : columns.filter(col => col.key !== 'regionName');

  return (
    <div className="employee-list-wrapper">
      <Card
        className="employee-list-card"
        bordered={false}
        title={
          <Space align="center">
            <TeamOutlined style={{ color: "var(--primary-color)" }} />
            <span>Employee List</span>
          </Space>
        }
        extra={
          <Space>
            {/* Conditionally render the region filter for admins */}
            {role === "admin" && (
              <Select
                showSearch
                placeholder={loadingRegions ? "Loading regions..." : "Filter by region"}
                prefix={<GlobalOutlined />}
                loading={loadingRegions}
                disabled={loadingRegions}
                allowClear
                style={{ width: 200 }}
                onChange={handleRegionChange} // Set the handler
                value={selectedRegion}       // Control the component's value
                optionFilterProp="children"
                filterOption={(input, option) => (option?.children ?? "").toLowerCase().includes(input.toLowerCase())}
              >

                {regions.map((region) => (
                  <Select.Option key={region} value={region}>{region}</Select.Option>
                ))}
              </Select>
            )}
            <Button
              icon={<ReloadOutlined />}
              onClick={handleRefresh}
              type="text"
              style={{ color: "var(--primary-color)" }}
            >
              Refresh List
            </Button>
          </Space>
        }
      >
        <Text type="secondary" style={{ color: "var(--text-color-secondary)" }}>
          Browse, filter, and manage all employees in the system.
        </Text>

        <Divider style={{ margin: "16px 0" }} />

        <Spin spinning={loadingEmployees} tip="Loading employees...">
          <Table
            dataSource={employees}
            columns={finalColumns}
            rowKey="email" // Use a unique key, 'email' or 'id' if you have it
            pagination={{ pageSize: 10 }}
            scroll={{ x: true }} // For better mobile responsiveness
            className="compact-table"
          />
        </Spin>
      </Card>
    </div>
  );
}

export default EmployeeList;