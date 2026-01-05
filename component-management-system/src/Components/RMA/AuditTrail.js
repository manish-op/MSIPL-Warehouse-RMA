// src/Components/RMA/AuditTrail.js
import React, { useState, useEffect } from "react";
import {
    Card,
    Table,
    Typography,
    Tag,
    Space,
    Input,
    Select,
    Spin,
    Empty,
    Tooltip,
    Row,
    Col,
    Statistic,
} from "antd";
import {
    HistoryOutlined,
    SearchOutlined,
    UserOutlined,
    ClockCircleOutlined,
    SwapOutlined,
    FilterOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA";
import RmaLayout from "./RmaLayout";
import "./AuditTrail.css";

const { Title, Text } = Typography;
const { Option } = Select;

const AuditTrail = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchText, setSearchText] = useState("");
    const [filterAction, setFilterAction] = useState("all");

    useEffect(() => {
        fetchAuditLogs();
    }, []);

    const fetchAuditLogs = async () => {
        setLoading(true);
        try {
            const response = await RmaApi.getAuditLogs();
            if (response.success && Array.isArray(response.data)) {
                setLogs(response.data);
            } else if (Array.isArray(response)) {
                setLogs(response);
            }
        } catch (error) {
            console.error("Failed to fetch audit logs:", error);
        }
        setLoading(false);
    };

    const getActionColor = (action) => {
        switch (action?.toUpperCase()) {
            case "ASSIGNED":
                return "blue";
            case "STATUS_CHANGED":
                return "orange";
            case "CREATED":
                return "green";
            case "REPAIRED":
                return "success";
            case "CANT_BE_REPAIRED":
                return "error";
            default:
                return "default";
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return date.toLocaleString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    const filteredLogs = logs.filter((log) => {
        const matchesSearch =
            searchText === "" ||
            log.rmaNo?.toLowerCase().includes(searchText.toLowerCase()) ||
            log.performedByEmail?.toLowerCase().includes(searchText.toLowerCase()) ||
            log.performedByName?.toLowerCase().includes(searchText.toLowerCase());

        const matchesAction =
            filterAction === "all" || log.action === filterAction;

        return matchesSearch && matchesAction;
    });

    const columns = [
        {
            title: "Timestamp",
            dataIndex: "performedAt",
            key: "performedAt",
            width: 160,
            render: (text) => (
                <Space>
                    <ClockCircleOutlined />
                    <Text style={{ fontSize: 12 }}>{formatDate(text)}</Text>
                </Space>
            ),
            sorter: (a, b) => new Date(a.performedAt) - new Date(b.performedAt),
            defaultSortOrder: "descend",
        },
        {
            title: "RMA No.",
            dataIndex: "rmaNo",
            key: "rmaNo",
            width: 180,
            render: (text) => (
                <Tag color="purple" style={{ fontFamily: "monospace" }}>
                    {text || "-"}
                </Tag>
            ),
        },
        {
            title: "Action",
            dataIndex: "action",
            key: "action",
            width: 140,
            render: (action) => (
                <Tag color={getActionColor(action)} icon={<SwapOutlined />}>
                    {action?.replace("_", " ")}
                </Tag>
            ),
        },
        {
            title: "Changes",
            key: "changes",
            width: 300,
            render: (_, record) => (
                <div className="changes-cell">
                    <div className="old-value">
                        <Text type="secondary" style={{ fontSize: 11 }}>
                            From:
                        </Text>{" "}
                        <Text delete style={{ fontSize: 12 }}>
                            {record.oldValue || "-"}
                        </Text>
                    </div>
                    <div className="new-value">
                        <Text type="secondary" style={{ fontSize: 11 }}>
                            To:
                        </Text>{" "}
                        <Text strong style={{ fontSize: 12 }}>
                            {record.newValue || "-"}
                        </Text>
                    </div>
                </div>
            ),
        },
        {
            title: "Performed By",
            key: "performedBy",
            width: 200,
            render: (_, record) => (
                <Tooltip title={record.performedByEmail}>
                    <Space>
                        <UserOutlined />
                        <Text>{record.performedByName || record.performedByEmail || "-"}</Text>
                    </Space>
                </Tooltip>
            ),
        },
        {
            title: "IP Address",
            dataIndex: "ipAddress",
            key: "ipAddress",
            width: 130,
            render: (ip) => (
                <Text code style={{ fontSize: 11 }}>
                    {ip || "-"}
                </Text>
            ),
        },
        {
            title: "Remarks",
            dataIndex: "remarks",
            key: "remarks",
            ellipsis: true,
            render: (text) => (
                <Tooltip title={text}>
                    <Text style={{ fontSize: 12 }}>{text || "-"}</Text>
                </Tooltip>
            ),
        },
    ];

    const actionCounts = logs.reduce((acc, log) => {
        acc[log.action] = (acc[log.action] || 0) + 1;
        return acc;
    }, {});

    return (
        <RmaLayout>
            <div className="audit-trail-container">
                <div className="audit-trail-header">
                    <HistoryOutlined className="header-icon" />
                    <div>
                        <Title level={2} style={{ margin: 0 }}>
                            Audit Trail
                        </Title>
                        <Text className="header-subtext">
                            Track all RMA item status changes and assignments
                        </Text>
                    </div>
                </div>

                {/* Stats Cards */}
                <Row gutter={16} style={{ margin: "24px" }}>
                    <Col xs={12} sm={6}>
                        <Card className="stat-mini-card">
                            <Statistic
                                title="Total Logs"
                                value={logs.length}
                                prefix={<HistoryOutlined />}
                            />
                        </Card>
                    </Col>
                    <Col xs={12} sm={6}>
                        <Card className="stat-mini-card">
                            <Statistic
                                title="Assignments"
                                value={actionCounts["ASSIGNED"] || 0}
                                valueStyle={{ color: "#1890ff" }}
                            />
                        </Card>
                    </Col>
                    <Col xs={12} sm={6}>
                        <Card className="stat-mini-card">
                            <Statistic
                                title="Status Changes"
                                value={actionCounts["STATUS_CHANGED"] || 0}
                                valueStyle={{ color: "#fa8c16" }}
                            />
                        </Card>
                    </Col>
                    <Col xs={12} sm={6}>
                        <Card className="stat-mini-card">
                            <Statistic
                                title="Today"
                                value={
                                    logs.filter((l) => {
                                        const today = new Date().toDateString();
                                        return new Date(l.performedAt).toDateString() === today;
                                    }).length
                                }
                                valueStyle={{ color: "#52c41a" }}
                            />
                        </Card>
                    </Col>
                </Row>

                {/* Filters */}
                <Card className="filter-card" style={{ margin: "0 24px 24px" }}>
                    <Row gutter={16} align="middle">
                        <Col xs={24} md={12}>
                            <Input
                                placeholder="Search by RMA No., user email, or name..."
                                prefix={<SearchOutlined />}
                                value={searchText}
                                onChange={(e) => setSearchText(e.target.value)}
                                allowClear
                                size="large"
                            />
                        </Col>
                        <Col xs={24} md={8}>
                            <Select
                                style={{ width: "100%" }}
                                placeholder="Filter by action"
                                value={filterAction}
                                onChange={setFilterAction}
                                size="large"
                                suffixIcon={<FilterOutlined />}
                            >
                                <Option value="all">All Actions</Option>
                                <Option value="ASSIGNED">Assigned</Option>
                                <Option value="STATUS_CHANGED">Status Changed</Option>
                                <Option value="CREATED">Created</Option>
                            </Select>
                        </Col>
                        <Col xs={24} md={4}>
                            <Text type="secondary">
                                Showing {filteredLogs.length} of {logs.length} logs
                            </Text>
                        </Col>
                    </Row>
                </Card>

                {/* Table */}
                <Card className="table-card" style={{ margin: "0 24px 24px" }}>
                    {loading ? (
                        <div style={{ textAlign: "center", padding: 60 }}>
                            <Spin size="large" />
                            <div style={{ marginTop: 16 }}>Loading audit logs...</div>
                        </div>
                    ) : filteredLogs.length === 0 ? (
                        <Empty
                            description="No audit logs found"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    ) : (
                        <Table
                            columns={columns}
                            dataSource={filteredLogs}
                            rowKey="id"
                            pagination={{
                                pageSize: 15,
                                showSizeChanger: true,
                                showTotal: (total, range) =>
                                    `${range[0]}-${range[1]} of ${total} logs`,
                            }}
                            scroll={{ x: 1200 }}
                            size="middle"
                        />
                    )}
                </Card>
            </div>
        </RmaLayout>
    );
};

export default AuditTrail;
