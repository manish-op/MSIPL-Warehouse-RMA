import React, { useState } from "react";
import {
    Card,
    Row,
    Col,
    Select,
    DatePicker,
    Button,
    Typography,
    Spin,
    message,
    Space,
    Divider,
} from "antd";
import {
    DownloadOutlined,
    UserOutlined,
    TeamOutlined,
    BarChartOutlined,
    ToolOutlined,
    ClockCircleOutlined,
    CalendarOutlined,
    FileTextOutlined,
} from "@ant-design/icons";
import RmaLayout from "./RmaLayout";
import Cookies from "js-cookie";
import { URL as API_URL } from "../API/URL";
import "./ReportsPage.css";

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

/**
 * Get auth token from cookie and decode it
 */
const getToken = () => {
    const encodedToken = Cookies.get("authToken");
    if (!encodedToken || encodedToken === "undefined" || encodedToken === "null") {
        return null;
    }
    try {
        return atob(encodedToken);
    } catch (error) {
        console.error("Failed to decode authToken:", error);
        return null;
    }
};

const REPORT_TYPES = [
    {
        key: "by-creator",
        label: "RMA by Creator",
        description: "RMA requests grouped by the user who created them",
        icon: <UserOutlined />,
        color: "#1890ff",
    },
    {
        key: "by-customer",
        label: "RMA by Customer",
        description: "RMA requests grouped by customer company",
        icon: <TeamOutlined />,
        color: "#52c41a",
    },
    {
        key: "by-status",
        label: "RMA by Status",
        description: "Items grouped by repair status (Unrepaired, Assigned, etc.)",
        icon: <BarChartOutlined />,
        color: "#722ed1",
    },
    {
        key: "by-repair-type",
        label: "RMA by Repair Type",
        description: "Items grouped by LOCAL vs DEPOT repair type",
        icon: <ToolOutlined />,
        color: "#fa8c16",
    },
    {
        key: "by-technician",
        label: "RMA by Technician",
        description: "Items grouped by assigned technician",
        icon: <UserOutlined />,
        color: "#13c2c2",
    },
    {
        key: "tat-compliance",
        label: "TAT Compliance",
        description: "Items within vs exceeding Turn Around Time",
        icon: <ClockCircleOutlined />,
        color: "#eb2f96",
    },
    {
        key: "monthly-summary",
        label: "Monthly Summary",
        description: "Overall summary with status and repair type breakdown",
        icon: <CalendarOutlined />,
        color: "#f5222d",
    },
];

const ReportsPage = () => {
    const [selectedReport, setSelectedReport] = useState(null);
    const [dateRange, setDateRange] = useState([null, null]);
    const [loading, setLoading] = useState(false);

    const handleGenerateReport = async () => {
        if (!selectedReport) {
            message.warning("Please select a report type");
            return;
        }

        setLoading(true);
        try {
            const token = getToken();
            if (!token) {
                message.error("Authentication token not found. Please login again.");
                setLoading(false);
                return;
            }

            // Build query parameters
            let url = `${API_URL}/rma/reports/${selectedReport}`;
            const params = new URLSearchParams();

            if (dateRange[0]) {
                params.append("startDate", dateRange[0].toISOString());
            }
            if (dateRange[1]) {
                params.append("endDate", dateRange[1].toISOString());
            }

            if (params.toString()) {
                url += `?${params.toString()}`;
            }

            const response = await fetch(url, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error("Failed to generate report");
            }

            // Download the PDF
            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = downloadUrl;

            // Get filename from report type
            const reportType = REPORT_TYPES.find(r => r.key === selectedReport);
            const filename = `rma_${selectedReport.replace(/-/g, "_")}_report.pdf`;
            link.download = filename;

            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);

            message.success(`${reportType?.label || "Report"} generated successfully!`);
        } catch (error) {
            console.error("Error generating report:", error);
            message.error("Failed to generate report. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleReportCardClick = (reportKey) => {
        setSelectedReport(reportKey);
    };

    return (
        <RmaLayout>
            <div className="reports-page">
                <div className="reports-header">
                    <Title level={2}>
                        <FileTextOutlined /> RMA Reports
                    </Title>
                    <Text type="secondary">
                        Generate comprehensive PDF reports on various RMA aspects
                    </Text>
                </div>

                <Divider />

                {/* Report Type Selection */}
                <div className="reports-section">
                    <Title level={4}>Select Report Type</Title>
                    <Row gutter={[16, 16]}>
                        {REPORT_TYPES.map((report) => (
                            <Col xs={24} sm={12} md={8} lg={6} key={report.key}>
                                <Card
                                    hoverable
                                    className={`report-card ${selectedReport === report.key ? "selected" : ""}`}
                                    onClick={() => handleReportCardClick(report.key)}
                                    style={{ borderColor: selectedReport === report.key ? report.color : undefined }}
                                >
                                    <div className="report-card-icon" style={{ color: report.color }}>
                                        {report.icon}
                                    </div>
                                    <div className="report-card-content">
                                        <Text strong>{report.label}</Text>
                                        <Text type="secondary" className="report-description">
                                            {report.description}
                                        </Text>
                                    </div>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                </div>

                <Divider />

                {/* Filters and Generate */}
                <div className="reports-section">
                    <Title level={4}>Filter Options</Title>
                    <Row gutter={[24, 16]} align="middle">
                        <Col xs={24} md={12} lg={8}>
                            <div className="filter-group">
                                <Text strong>Report Type:</Text>
                                <Select
                                    style={{ width: "100%" }}
                                    placeholder="Select report type"
                                    value={selectedReport}
                                    onChange={setSelectedReport}
                                    size="large"
                                >
                                    {REPORT_TYPES.map((report) => (
                                        <Option key={report.key} value={report.key}>
                                            {report.icon} {report.label}
                                        </Option>
                                    ))}
                                </Select>
                            </div>
                        </Col>
                        <Col xs={24} md={12} lg={8}>
                            <div className="filter-group">
                                <Text strong>Date Range (Optional):</Text>
                                <RangePicker
                                    style={{ width: "100%" }}
                                    size="large"
                                    value={dateRange}
                                    onChange={(dates) => setDateRange(dates || [null, null])}
                                    format="DD/MM/YYYY"
                                />
                            </div>
                        </Col>
                        <Col xs={24} lg={8}>
                            <div className="filter-group generate-btn-container">
                                <Button
                                    type="primary"
                                    size="large"
                                    icon={<DownloadOutlined />}
                                    onClick={handleGenerateReport}
                                    loading={loading}
                                    disabled={!selectedReport}
                                    className="generate-btn"
                                >
                                    {loading ? "Generating..." : "Generate PDF Report"}
                                </Button>
                            </div>
                        </Col>
                    </Row>
                </div>

                {/* Loading Overlay */}
                {loading && (
                    <div className="loading-overlay">
                        <Spin size="large" tip="Generating your report..." />
                    </div>
                )}
            </div>
        </RmaLayout>
    );
};

export default ReportsPage;
