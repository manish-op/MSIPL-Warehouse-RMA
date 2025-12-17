// src/Components/RMA/RMADashboard.js
import React, { useState, useEffect } from "react";
import { 
  Row, Col, Card, Statistic, Spin, message, Modal,
  Table, Select, Tag, Button, Avatar, Tooltip as AntTooltip 
} from "antd";
import {
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  InboxOutlined,
  UserOutlined,
  RiseOutlined,
  FilterOutlined
} from "@ant-design/icons";
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, 
  Tooltip as RechartsTooltip, ResponsiveContainer, 
  PieChart, Pie, Cell, Legend 
} from 'recharts';
import RmaLayout from "./RmaLayout";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import "./RmaDashboard.css"; 

const { Option } = Select;

// --- Trend Chart Data is now fetched from API (stats.dailyTrends) ---

const PIE_COLORS = ['#52c41a', '#fa8c16', '#f5222d']; // Green, Orange, Red

function RmaDashboard() {
  // --- State ---
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState(""); // 'requests', 'items', 'repaired', 'unrepaired'
  const [modalData, setModalData] = useState([]);
  const [modalLoading, setModalLoading] = useState(false);
  const [timeFilter, setTimeFilter] = useState("all");

  // User info
  const name = localStorage.getItem("name") || "Admin User";

  // --- Helpers ---
  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good morning";
    if (hour < 18) return "Good afternoon";
    return "Good evening";
  };

  // Prepare Pie Chart Data dynamically from Stats
  const pieData = stats ? [
    { name: 'Repaired', value: stats.repairedCount || 0 },
    { name: 'Unrepaired', value: stats.unrepairedCount || 0 },
    // Calculate 'Other' (Scrapped/BER) assuming Total = Repaired + Unrepaired + Others
    { name: 'Other/Scrapped', value: (stats.totalItems - (stats.repairedCount + stats.unrepairedCount)) > 0 ? (stats.totalItems - (stats.repairedCount + stats.unrepairedCount)) : 0 },
  ] : [];

  // --- Effects ---
  useEffect(() => {
    fetchStats();
  }, []);

  useEffect(() => {
    if (modalVisible) {
        fetchModalData();
    }
  }, [modalVisible, modalType, timeFilter]);

  // --- API Calls ---
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
             result = await RmaApi.getAllItems();
        } else if (modalType === "repaired") {
             result = await RmaApi.getRepairedItems();
        } else if (modalType === "unrepaired") {
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

  // --- Handlers ---
  const handleCardClick = (type) => {
      setModalType(type);
      setModalVisible(true);
      if(type === 'requests') setTimeFilter('all');
  };

  const handleModalClose = () => {
      setModalVisible(false);
      setModalData([]);
  };

  // --- Table Columns ---
  const requestColumns = [
      { title: "RMA No", key: "manualRmaNo", render: (_, record) => {
          // Display Manual RMA Number from items if available
          if (record.items && record.items.length > 0) {
              // Find first item with a manual RMA number
              const item = record.items.find(i => i.rmaNo);
              return item ? item.rmaNo : "-";
          }
          return "-";
      }},
      { title: "Request No", key: "requestNumber", render: (_, record) => record.requestNumber || record.rmaNo }, 
      { title: "Company", dataIndex: "companyName", key: "companyName" },
      { title: "Date", dataIndex: "createdDate", key: "createdDate", render: (date) => new Date(date).toLocaleString() },
      { title: "Items", dataIndex: "itemsCount", key: "itemsCount", align: "center", render: (val, record)=>
        typeof val === "number"
        ? val
        : Array.isArray(record.items)
        ? record.items.length
        :0,
       },
      { title: "Status", key: "status", render: () => <Tag color="blue">Submitted</Tag> } 
  ];

  const itemColumns = [
      { title: "Product", dataIndex: "product", key: "product" },
      { title: "Serial No", dataIndex: "serialNo", key: "serialNo" },
      { title: "RMA No", dataIndex: "rmaNo", key: "rmaNo", render:(val)=>{
        if (!val) return "-";
        return /^RMA-\d{8}-\d{6}$/.test(val) ? "-" : val;
      }},
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
          case 'items': return 'All Inventory Items';
          case 'repaired': return 'Repaired History';
          case 'unrepaired': return 'Pending / Unrepaired Items';
          default: return 'Details';
      }
  };

  // --- Render ---
  return (
    <RmaLayout>
      <div className="rma-dashboard-container">
        
        {/* 1. Header Section */}
        <div className="dashboard-header">
            <div>
                <h1 className="welcome-text">{getGreeting()}, {name}</h1>
                <p className="sub-text">Overview of your Return Merchandise Authorization status.</p>
            </div>
            <div className="header-actions">
               {/* Optional: Add a Refresh or Filter Button here */}
               <Button icon={<FilterOutlined />}>Last 30 Days</Button>
            </div>
        </div>

        {loading ? (
          <div className="loading-container">
            <Spin size="large" tip="Loading Dashboard..." />
          </div>
        ) : (
          <>
            {/* 2. KPI Cards Row */}
            <Row gutter={[24, 24]}>
              {/* Card 1: Requests */}
              <Col xs={24} sm={12} md={6}>
                <Card className="kpi-card kpi-blue" hoverable onClick={() => handleCardClick('requests')}>
                  <div className="kpi-icon-wrapper"><FileTextOutlined /></div>
                  <Statistic 
                    title="Total Requests" 
                    value={stats?.totalRequests || 0} 
                    valueStyle={{ fontWeight: 'bold', fontSize: '28px' }}
                  />
                  <div className="kpi-trend">
                    <RiseOutlined style={{ color: '#52c41a' }} /> 
                    <span>Active Requests</span>
                  </div>
                </Card>
              </Col>
              
              {/* Card 2: Items */}
              <Col xs={24} sm={12} md={6}>
                <Card className="kpi-card kpi-purple" hoverable onClick={() => handleCardClick('items')}>
                  <div className="kpi-icon-wrapper"><InboxOutlined /></div>
                  <Statistic 
                    title="Total Items" 
                    value={stats?.totalItems || 0}
                    valueStyle={{ fontWeight: 'bold', fontSize: '28px' }}
                   />
                   <div className="kpi-trend text-neutral">Total Inventory</div>
                </Card>
              </Col>

              {/* Card 3: Repaired */}
              <Col xs={24} sm={12} md={6}>
                <Card className="kpi-card kpi-green" hoverable onClick={() => handleCardClick('repaired')}>
                  <div className="kpi-icon-wrapper"><CheckCircleOutlined /></div>
                  <Statistic 
                    title="Repaired" 
                    value={stats?.repairedCount || 0}
                    valueStyle={{ fontWeight: 'bold', fontSize: '28px' }}
                   />
                   <div className="kpi-trend text-success">Completed</div>
                </Card>
              </Col>

              {/* Card 4: Unrepaired */}
              <Col xs={24} sm={12} md={6}>
                <Card className="kpi-card kpi-orange" hoverable onClick={() => handleCardClick('unrepaired')}>
                  <div className="kpi-icon-wrapper"><ClockCircleOutlined /></div>
                  <Statistic 
                    title="Unrepaired / Pending" 
                    value={stats?.unrepairedCount || 0}
                    valueStyle={{ fontWeight: 'bold', fontSize: '28px' }}
                   />
                   <div className="kpi-trend text-warning">Action Required</div>
                </Card>
              </Col>
            </Row>

            {/* 3. Charts Row */}
            <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
                {/* Left: Trend Line Chart */}
                <Col xs={24} lg={16}>
                    <Card title="Incoming Requests Trend (Last 7 Days)" bordered={false} className="chart-card">
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={stats?.dailyTrends || []} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#eee" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} />
                                <YAxis axisLine={false} tickLine={false} />
                                <RechartsTooltip 
                                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                                />
                                <Line 
                                    type="monotone" 
                                    dataKey="requests" 
                                    stroke="#1890ff" 
                                    strokeWidth={3} 
                                    dot={{r: 4, fill: '#1890ff', strokeWidth: 2, stroke: '#fff'}} 
                                    activeDot={{r: 6}} 
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </Card>
                </Col>
                
                {/* Right: Ratio Pie Chart */}
                <Col xs={24} lg={8}>
                    <Card title="Repair Outcome Ratio" bordered={false} className="chart-card">
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={pieData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {pieData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                                    ))}
                                </Pie>
                                <RechartsTooltip />
                                <Legend verticalAlign="bottom" height={36}/>
                            </PieChart>
                        </ResponsiveContainer>
                    </Card>
                </Col>
            </Row>
          </>
        )}

        {/* 4. Detail Modal (Kept from original code) */}
        <Modal
            title={getModalTitle()}
            open={modalVisible}
            onCancel={handleModalClose}
            footer={null}
            width={1000}
            className="rma-detail-modal"
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
                rowKey={(record) => record.id || Math.random()} 
                pagination={{ pageSize: 8 }}
                scroll={{ x: 'max-content' }}
                size="small"
            />
        </Modal>
      </div>
    </RmaLayout>
  );
}

export default RmaDashboard;
