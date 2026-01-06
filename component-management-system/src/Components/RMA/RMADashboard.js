import React, { useState, useEffect, useContext } from "react";
import {
  Row, Col, Card, Statistic, Spin, message, Modal,
  Table, Select, Tag, Button, Avatar, Tooltip as AntTooltip, Input, Progress, Typography
} from "antd";
import {
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  InboxOutlined,
  RiseOutlined,
  SearchOutlined,
  WarningOutlined,
  CloseCircleOutlined,
  SafetyCertificateOutlined,
  BarChartOutlined
} from "@ant-design/icons";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip as RechartsTooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import RmaLayout from "./RmaLayout";
import { RmaApi } from "../API/RMA";
import { TatIconIndicator } from "./TatIndicator";
import { ThemeContext } from "../../context/ThemeContext";
import "./RmaDashboard.css";

const { Title, Text } = Typography;
const { Option } = Select;
const { Search } = Input;

// --- Trend Chart Data is now fetched from API (stats.dailyTrends) ---
const PIE_COLORS = ['#52c41a', '#fa8c16', '#f5222d']; // Green, Orange, Red

function RmaDashboard() {
  const { theme } = useContext(ThemeContext);
  const isDarkMode = theme === "dark";

  // --- State ---
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState(""); // 'requests', 'items', 'repaired', 'unrepaired'
  const [modalData, setModalData] = useState([]);
  const [modalLoading, setModalLoading] = useState(false);
  const [timeFilter, setTimeFilter] = useState("all");

  // Search State
  const [searchQuery, setSearchQuery] = useState("");

  // TAT Report State
  const [reportVisible, setReportVisible] = useState(false);
  const [reportData, setReportData] = useState([]);
  const [reportLoading, setReportLoading] = useState(false);

  // User info
  const name = localStorage.getItem("name") || "Admin User";
  const userRegion = localStorage.getItem("region") || "";
  const userRole = localStorage.getItem("_User_role_for_MSIPL") || "";

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
      message.error("Failed to load RMA statistics: " + (result.error || "Unknown error"));
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
        // Match backend countUnrepaired logic: items NOT in completed statuses
        const allItems = await RmaApi.getAllItems();
        if (allItems.success) {
          const completedStatuses = ['repaired', 'replaced', 'dispatched', 'received_at_gurgaon', 'delivered', 'closed', 'dispatched_to_customer', 'delivered_to_customer', 'repaired_at_depot', 'received_at_depot', 'dispatched_to_depot'];
          result = {
            success: true,
            data: allItems.data.filter(item => {
              const status = (item.repairStatus || '').toLowerCase();
              // Explicitly include Can't Be Repaired / BER items
              if (status === 'cant_be_repaired' || status === 'ber') return true;

              // Include if status is empty/null OR not in completed list
              if (!status) return true;
              return !completedStatuses.some(s => status.includes(s));
            })
          };
        } else {
          result = allItems;
        }
      } else if (modalType === "search") {
        result = await RmaApi.searchItems(searchQuery);
      }

      if (result && result.success) {
        setModalData(result.data);
      } else {
        message.error(result.error || "Failed to load details");
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
    if (type === 'requests') setTimeFilter('all');
  };

  const handleModalClose = () => {
    setModalVisible(false);
    setModalData([]);
    // Do not clear searchQuery here so user can see what they searched, or clear if preferred
  };

  const onSearch = (value) => {
    if (!value || value.trim() === "") {
      message.warning("Please enter a search term");
      return;
    }
    setSearchQuery(value);
    setModalType("search");
    setModalVisible(true);
  };

  // TAT Compliance Report Handler
  const handleViewReport = async () => {
    setReportVisible(true);
    setReportLoading(true);
    try {
      const result = await RmaApi.getTatComplianceReport();
      if (result.success) {
        setReportData(result.data);
      } else {
        message.error("Failed to load TAT compliance report: " + (result.error || "Unknown error"));
      }
    } catch (error) {
      message.error("Error loading report");
    } finally {
      setReportLoading(false);
    }
  };

  const reportColumns = [
    {
      title: "Company", dataIndex: "companyName", key: "companyName", width: 180,
      render: (val) => <span style={{ fontWeight: 500 }}>{val}</span>
    },
    {
      title: "Default TAT", dataIndex: "defaultTat", key: "defaultTat", align: "center",
      render: (val) => val ? <Tag color="blue">{val} days</Tag> : <span style={{ color: '#999' }}>-</span>
    },
    { title: "Total Requests", dataIndex: "totalRequests", key: "totalRequests", align: "center" },
    { title: "With TAT", dataIndex: "requestsWithTat", key: "requestsWithTat", align: "center" },
    {
      title: "Completed (Within TAT)",
      dataIndex: "completedWithinTat",
      key: "completedWithinTat",
      align: "center",
      render: (val) => <Tag color="success">{val || 0}</Tag>
    },
    {
      title: "Completed (After TAT)",
      dataIndex: "completedAfterTat",
      key: "completedAfterTat",
      align: "center",
      render: (val) => val > 0 ? <Tag color="error">{val}</Tag> : <Tag color="default">0</Tag>
    },
    {
      title: "Still Open",
      dataIndex: "stillOpen",
      key: "stillOpen",
      align: "center",
      render: (val, record) => (
        <AntTooltip title={`On Track: ${record.onTrack || 0}, At Risk: ${record.atRisk || 0}, Breached: ${record.breached || 0}`}>
          <Tag color={record.breached > 0 ? "error" : record.atRisk > 0 ? "warning" : "processing"}>
            {val || 0}
          </Tag>
        </AntTooltip>
      )
    },
    {
      title: "Compliance Rate",
      key: "complianceRate",
      align: "center",
      width: 130,
      render: (_, record) => {
        const rate = record.complianceRate;
        if (rate === null || rate === undefined) return <span style={{ color: '#999' }}>N/A</span>;
        const status = rate >= 80 ? 'success' : rate >= 50 ? 'warning' : 'error';
        return (
          <div className={`compliance-indicator status-${status}`} style={{
            display: 'inline-flex',
            alignItems: 'center',
            padding: '4px 12px',
            borderRadius: '20px',
            border: '1px solid currentColor',
          }}>
            <span style={{
              fontSize: '14px',
              fontWeight: 600,
            }}>
              {rate.toFixed(1)}%
            </span>
          </div>
        );
      }
    },
  ];

  // --- Table Columns ---
  const requestColumns = [
    { title: "Request No", dataIndex: "requestNumber", key: "requestNumber" },
    { title: "Company", dataIndex: "companyName", key: "companyName" },
    { title: "Date  & Time", dataIndex: "createdDate", key: "createdDate", render: (date) => new Date(date).toLocaleString() },
    {
      title: "Items", dataIndex: "itemsCount", key: "itemsCount", align: "center", render: (val, record) =>
        typeof val === "number"
          ? val
          : Array.isArray(record.items)
            ? record.items.length
            : 0,
    },
    {
      title: "TAT Status",
      key: "tatStatus",
      align: "center",
      render: (_, record) => (
        <TatIconIndicator dueDate={record.dueDate} tat={record.tat} size={20} />
      )
    },
    {
      title: "Created By",
      dataIndex: "createdByEmail",
      key: "createdByEmail",
      render: (email) => email ? <span style={{ color: '#666' }}>{email}</span> : <span style={{ color: '#ccc' }}>-</span>
    }
  ];

  const itemColumns = [
    { title: "Product", dataIndex: "product", key: "product" },
    { title: "Serial No", dataIndex: "serialNo", key: "serialNo" },
    { title: "Model", dataIndex: "model", key: "model", render: (val) => val || "-" },
    {
      title: "RMA No", dataIndex: "itemRmaNo", key: "itemRmaNo", render: (val, record) => {
        // itemRmaNo contains the user-updated RMA number from rma_item table
        const rmaNumber = val || record.rmaNo;
        if (!rmaNumber || rmaNumber.trim() === '') return <Tag color="default">-</Tag>;
        // Hide auto-generated format (RMA-DDMMYYYY-HHMMSS)
        if (/^RMA-\d{8}-\d{6}$/.test(rmaNumber)) return <Tag color="default">-</Tag>;
        return <Tag color="purple">{rmaNumber}</Tag>;
      }
    },
    {
      title: "Status", dataIndex: "repairStatus", key: "repairStatus",
      render: (status) => {
        let color = 'default';
        const s = (status || '').toUpperCase();
        // Completed statuses (green)
        if (['REPAIRED', 'REPLACED', 'REPAIRED_AT_DEPOT', 'DELIVERED', 'DELIVERED_TO_CUSTOMER', 'CLOSED'].includes(s)) color = 'green';
        // Pending statuses (orange/blue)
        else if (s === 'UNASSIGNED' || !status) color = 'orange';
        else if (s === 'ASSIGNED') color = 'blue';
        else if (s === 'REPAIRING') color = 'geekblue';
        // Dispatched (cyan)
        else if (['DISPATCHED', 'DISPATCHED_TO_CUSTOMER', 'DISPATCHED_TO_DEPOT'].includes(s)) color = 'cyan';
        // Can't be repaired / BER (red)
        else if (['BER', 'BER_AT_DEPOT', 'CANT_BE_REPAIRED'].includes(s)) color = 'red';
        // Depot stages (purple)
        else if (['AT_DEPOT_REPAIRED', 'RECEIVED_AT_DEPOT', 'RECEIVED_AT_GURGAON'].includes(s)) color = 'purple';
        return <Tag color={color}>{status || 'UNASSIGNED'}</Tag>
      }
    },
    {
      title: "TAT",
      key: "tatStatus",
      align: "center",
      render: (_, record) => (
        <TatIconIndicator
          dueDate={record.requestDueDate || record.dueDate}
          tat={record.requestTat || record.tat}
          size={18}
        />
      )
    },
    { title: "Technician", dataIndex: "assignedToName", key: "assignedToName" }
  ];

  const getModalTitle = () => {
    switch (modalType) {
      case 'requests': return 'RMA Requests Details';
      case 'items': return 'All Inventory Items';
      case 'repaired': return 'Repaired History';
      case 'unrepaired': return 'Unassigned / Pending Items';
      case 'search': return `Search Results for "${searchQuery}"`;
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
            {/* User Region & Role Display */}
            <div style={{
              marginTop: 8,
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              fontFamily: "'Inter', 'Segoe UI', sans-serif"
            }}>
              {userRegion && (
                <Tag
                  color="geekblue"
                  style={{
                    fontSize: '13px',
                    padding: '4px 12px',
                    borderRadius: 20,
                    fontWeight: 500,
                    letterSpacing: '0.3px'
                  }}
                >
                  📍 Region: {userRegion}
                </Tag>
              )}
              {userRole && (
                <Tag
                  color={userRole.toLowerCase() === 'admin' ? 'gold' : 'cyan'}
                  style={{
                    fontSize: '13px',
                    padding: '4px 12px',
                    borderRadius: 20,
                    fontWeight: 500,
                    letterSpacing: '0.3px'
                  }}
                >
                  👤 {userRole}
                </Tag>
              )}
            </div>
          </div>
          <div className="dashboard-search-wrapper">
            <Search
              placeholder="Search by Product, Serial No, or Model No"
              allowClear
              enterButton={<Button type="primary" icon={<SearchOutlined />}>Search</Button>}
              size="large"
              onSearch={onSearch}
              className="dashboard-search-input"
            />
          </div>
        </div>

        {loading ? (
          <div className="loading-container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <Spin size="large" />
            <div style={{ marginTop: 16, fontSize: '16px', fontWeight: 500 }}>Loading Dashboard...</div>
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
                    title="Repaired/Replaced"
                    value={stats?.repairedCount || 0}
                    valueStyle={{ fontWeight: 'bold', fontSize: '28px' }}
                  />
                  <div className="kpi-trend text-success">Completed</div>
                </Card>
              </Col>

              {/* Card 4: Unassigned */}
              <Col xs={24} sm={12} md={6}>
                <Card className="kpi-card kpi-orange" hoverable onClick={() => handleCardClick('unrepaired')}>
                  <div className="kpi-icon-wrapper"><ClockCircleOutlined /></div>
                  <Statistic
                    title="Unassigned / Pending"
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
                <Card title="Incoming Requests Trend (Last 7 Days)" variant="borderless" className="chart-card">
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={stats?.dailyTrends || []} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#eee" />
                      <XAxis dataKey="name" axisLine={false} tickLine={false} />
                      <YAxis axisLine={false} tickLine={false} />
                      <RechartsTooltip
                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: 'var(--card-shadow)', backgroundColor: 'var(--component-background)', color: 'var(--text-color)' }}
                      />
                      <Line
                        type="monotone"
                        dataKey="requests"
                        stroke="#1890ff"
                        strokeWidth={3}
                        dot={{ r: 4, fill: '#1890ff', strokeWidth: 2, stroke: '#fff' }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </Card>
              </Col>

              {/* Right: Ratio Pie Chart */}
              <Col xs={24} lg={8}>
                <Card title="Repair Outcome Ratio" variant="borderless" className="chart-card">
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
                      <Legend verticalAlign="bottom" height={36} />
                    </PieChart>
                  </ResponsiveContainer>
                </Card>
              </Col>
            </Row>

            {/* 4. SLA Compliance Section */}
            {stats?.totalWithTat > 0 && (
              <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
                <Col xs={24}>
                  <Card
                    title={
                      <span>
                        <SafetyCertificateOutlined style={{ marginRight: 8 }} />
                        TAT Compliance Status
                      </span>
                    }
                    variant="borderless"
                    className="chart-card"
                  >
                    <Row gutter={[16, 16]}>
                      {/* Compliance Rate */}
                      <Col xs={24} sm={12} md={6}>
                        <div className="compliance-rate-container">
                          <Title level={2} className={`compliance-percentage status-${stats?.complianceRate >= 80 ? 'success' : stats?.complianceRate >= 50 ? 'warning' : 'error'}`} style={{
                            margin: 0
                          }}>
                            {stats?.complianceRate?.toFixed(1) || 0}%
                          </Title>
                          <Text className="compliance-label" style={{
                            display: 'block',
                            marginTop: 8
                          }}>
                            Compliance Rate
                          </Text>
                        </div>
                      </Col>

                      {/* On Track */}
                      <Col xs={24} sm={12} md={6}>
                        <Card size="small" className="tat-card tat-on-track" hoverable>
                          <Statistic
                            title={<span><CheckCircleOutlined /> On Track</span>}
                            value={stats?.onTrackCount || 0}
                            suffix="requests"
                          />
                        </Card>
                      </Col>

                      {/* At Risk */}
                      <Col xs={24} sm={12} md={6}>
                        <Card size="small" className="tat-card tat-at-risk" hoverable>
                          <Statistic
                            title={<span><WarningOutlined /> At Risk</span>}
                            value={stats?.atRiskCount || 0}
                            suffix="requests"
                          />
                        </Card>
                      </Col>

                      {/* Breached */}
                      <Col xs={24} sm={12} md={6}>
                        <Card size="small" className="tat-card tat-breached" hoverable>
                          <Statistic
                            title={<span><CloseCircleOutlined /> Breached</span>}
                            value={stats?.breachedCount || 0}
                            suffix="requests"
                          />
                        </Card>
                      </Col>
                    </Row>
                    <div style={{ marginTop: 16, textAlign: 'center' }}>
                      <Button
                        type="primary"
                        icon={<BarChartOutlined />}
                        onClick={handleViewReport}
                      >
                        View Detailed Report
                      </Button>
                    </div>
                  </Card>
                </Col>
              </Row>
            )}
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
          zIndex={1200}
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

        {/* TAT Compliance Report Modal */}
        <Modal
          title={
            <span>
              <BarChartOutlined style={{ marginRight: 8 }} />
              TAT Compliance Report - Customer Breakdown
            </span>
          }
          open={reportVisible}
          onCancel={() => { setReportVisible(false); setReportData([]); }}
          footer={null}
          width={1200}
          className="rma-detail-modal"
          zIndex={1200}
        >
          <Table
            columns={reportColumns}
            dataSource={reportData}
            loading={reportLoading}
            rowKey={(record) => record.companyName || Math.random()}
            pagination={{ pageSize: 10 }}
            scroll={{ x: 'max-content' }}
            size="small"
            summary={(pageData) => {
              if (pageData.length === 0) return null;
              const totalCompleted = pageData.reduce((sum, r) => sum + (r.completedWithinTat || 0) + (r.completedAfterTat || 0), 0);
              const withinTat = pageData.reduce((sum, r) => sum + (r.completedWithinTat || 0), 0);
              const avgCompliance = totalCompleted > 0 ? (withinTat / totalCompleted * 100).toFixed(1) : 0;
              return (
                <Table.Summary.Row>
                  <Table.Summary.Cell index={0}><strong>Total</strong></Table.Summary.Cell>
                  <Table.Summary.Cell index={1} align="center">-</Table.Summary.Cell>
                  <Table.Summary.Cell index={2} align="center"><strong>{pageData.reduce((s, r) => s + (r.totalRequests || 0), 0)}</strong></Table.Summary.Cell>
                  <Table.Summary.Cell index={3} align="center"><strong>{pageData.reduce((s, r) => s + (r.requestsWithTat || 0), 0)}</strong></Table.Summary.Cell>
                  <Table.Summary.Cell index={4} align="center"><Tag color="success"><strong>{withinTat}</strong></Tag></Table.Summary.Cell>
                  <Table.Summary.Cell index={5} align="center"><Tag color="error"><strong>{pageData.reduce((s, r) => s + (r.completedAfterTat || 0), 0)}</strong></Tag></Table.Summary.Cell>
                  <Table.Summary.Cell index={6} align="center"><strong>{pageData.reduce((s, r) => s + (r.stillOpen || 0), 0)}</strong></Table.Summary.Cell>
                  <Table.Summary.Cell index={7} align="center"><strong>{avgCompliance}%</strong></Table.Summary.Cell>
                </Table.Summary.Row>
              );
            }}
          />
        </Modal>
      </div>
    </RmaLayout>
  );
}

export default RmaDashboard;
