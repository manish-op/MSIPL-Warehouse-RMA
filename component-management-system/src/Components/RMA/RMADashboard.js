// src/Components/RMA/RMADashboard.js
import React, { useState, useEffect } from "react";
import { Row, Col, Card, Statistic, Spin, message, Avatar, Modal, Table, Select, Tag } from "antd";
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

const { Option } = Select;

function RmaDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState(""); // 'requests', 'items', 'repaired', 'unrepaired'
  const [modalData, setModalData] = useState([]);
  const [modalLoading, setModalLoading] = useState(false);
  const [timeFilter, setTimeFilter] = useState("all"); // 'all', 'year', 'month', 'week'

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

  // Fetch detailed data when modal opens or filter changes
  useEffect(() => {
    if (modalVisible) {
        fetchModalData();
    }
  }, [modalVisible, modalType, timeFilter]);

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

  const fetchModalData = async () => {
    setModalLoading(true);
    let result;

    try {
        if (modalType === "requests") {
            result = await RmaApi.getRmaRequests(timeFilter);
        } else if (modalType === "items") {
             // For items, we fetch all and can filter client side if needed, or backend
             result = await RmaApi.getAllItems();
        } else if (modalType === "repaired") {
             result = await RmaApi.getRepairedItems();
        } else if (modalType === "unrepaired") {
             // We can use getUnassignedItems + getAssignedItems or just filter getAllItems
             // For consistency with stats, "Unrepaired" usually implies not yet REPAIRED.
             // Let's use getAllItems and filter for status != 'REPAIRED'
             const allItems = await RmaApi.getAllItems();
             if(allItems.success) {
                 result = {
                     success: true,
                     data: allItems.data.filter(item => item.repairStatus !== 'REPAIRED')
                 };
             } else {
                 result = allItems;
             }
        }

        if (result && result.success) {
            setModalData(result.data);
        } else {
            message.error("Failed to load details");
        }
    } catch (error) {
        message.error("Error loading data");
    } finally {
        setModalLoading(false);
    }
  };

  const handleCardClick = (type) => {
      setModalType(type);
      setModalVisible(true);
      // Reset filter when opening requests
      if(type === 'requests') setTimeFilter('all');
  };

  const handleModalClose = () => {
      setModalVisible(false);
      setModalData([]);
  };

  // Columns definition
  const requestColumns = [
      { title: "RMA No", dataIndex: "rmaNo", key: "rmaNo" },
      { title: "Request No", dataIndex: "requestNumber", key: "requestNumber" }, // Add requestNumber if needed
      { title: "Company", dataIndex: "companyName", key: "companyName" },
      { title: "Date", dataIndex: "createdDate", key: "createdDate", render: (date) => new Date(date).toLocaleString() },
      { title: "Items Count", key: "itemsCount", render: (_, record) => record.items ? record.items.length : 0 },
      { title: "Status", key: "status", render: () => <Tag color="blue">Submitted</Tag> } // Assuming status
  ];

  const itemColumns = [
      { title: "Product", dataIndex: "product", key: "product" },
      { title: "Serial No", dataIndex: "serialNo", key: "serialNo" },
      { title: "RMA No", dataIndex: "rmaNo", key: "rmaNo" },
      { title: "Status", dataIndex: "repairStatus", key: "repairStatus", 
        render: (status) => {
            let color = 'default';
            if(status === 'REPAIRED') color = 'green';
            if(status === 'UNASSIGNED') color = 'orange';
            if(status === 'ASSIGNED') color = 'blue';
            if(status === 'REPAIRING') color = 'geekblue';
            if(status === 'BER') color = 'red';
            return <Tag color={color}>{status}</Tag>
        }
      },
      { title: "Technician", dataIndex: "assignedToName", key: "assignedToName" }
  ];

  const getModalTitle = () => {
      switch(modalType) {
          case 'requests': return 'RMA Requests Details';
          case 'items': return 'All Items Details';
          case 'repaired': return 'Repaired Items Details';
          case 'unrepaired': return 'Unrepaired Items Details';
          default: return 'Details';
      }
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
              <Card 
                className="stat-card stat-card-blue clickable-card" 
                hoverable
                onClick={() => handleCardClick('requests')}
              >
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
              <Card 
                  className="stat-card stat-card-purple clickable-card" 
                  hoverable
                  onClick={() => handleCardClick('items')}
              >
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
              <Card 
                  className="stat-card stat-card-green clickable-card" 
                  hoverable
                  onClick={() => handleCardClick('repaired')}
              >
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
              <Card 
                  className="stat-card stat-card-orange clickable-card" 
                  hoverable
                  onClick={() => handleCardClick('unrepaired')}
              >
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

        {/* Detail Modal */}
        <Modal
            title={getModalTitle()}
            open={modalVisible}
            onCancel={handleModalClose}
            footer={null}
            width={1000}
        >
            {modalType === 'requests' && (
                <div style={{ marginBottom: 16, textAlign: 'right' }}>
                    <span style={{ marginRight: 8 }}>Filter by Date:</span>
                    <Select defaultValue="all" value={timeFilter} onChange={setTimeFilter} style={{ width: 120 }}>
                        <Option value="all">All Time</Option>
                        <Option value="year">Last Year</Option>
                        <Option value="month">Last Month</Option>
                        <Option value="week">Last Week</Option>
                    </Select>
                </div>
            )}
            
            <Table
                columns={modalType === 'requests' ? requestColumns : itemColumns}
                dataSource={modalData}
                loading={modalLoading}
                rowKey="id"
                pagination={{ pageSize: 10 }}
                scroll={{ x: 'max-content' }}
            />
        </Modal>
      </div>
    </RmaLayout>
  );
}

export default RmaDashboard;
