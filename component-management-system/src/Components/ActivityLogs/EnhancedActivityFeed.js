// src/Components/ActivityLogs/EnhancedActivityFeed.js
import React, { useEffect, useState } from "react";
import Cookies from "js-cookie";
import {
    Table,
    Card,
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
    Avatar,
    Badge,
} from "antd";
import {
    HistoryOutlined,
    SearchOutlined,
    UserOutlined,
    ClockCircleOutlined,
    FilterOutlined,
    // AppstoreOutlined,
    EnvironmentOutlined,
    InboxOutlined,
    ReloadOutlined,
} from "@ant-design/icons";
import { formatDistanceToNow } from "date-fns";
import { URL } from "../API/URL";
import "./EnhancedActivityFeed.css";

const { Title, Text } = Typography;
const { Option } = Select;

function getToken() {
    try {
        const cookie = Cookies.get("authToken");
        if (cookie) return atob(cookie);
    } catch (e) { }
    return null;
}

export default function EnhancedActivityFeed() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchText, setSearchText] = useState("");
    const [filterRegion, setFilterRegion] = useState("all");
    const [onlineUsers, setOnlineUsers] = useState(0);

    async function fetchActivities() {
        setLoading(true);
        try {
            const token = getToken();
            const headers = token ? { Authorization: `Bearer ${token}` } : {};
            const res = await fetch(`${URL}/items/activity?limit=100`, { headers });
            if (!res.ok) {
                setItems([]);
                setLoading(false);
                return;
            }
            const json = await res.json();
            setItems(json || []);
        } catch (err) {
            console.error(err);
            setItems([]);
        } finally {
            setLoading(false);
        }
    }

    async function fetchOnlineUsers() {
        try {
            const token = getToken();
            const headers = token ? { Authorization: `Bearer ${token}` } : {};
            const res = await fetch(`${URL}/items/online-users`, { headers });
            if (res.ok) {
                const json = await res.json();
                setOnlineUsers(json.onlineUsers || 0);
            }
        } catch (err) {
            console.error("Failed to fetch online users:", err);
        }
    }

    useEffect(() => {
        fetchActivities();
        fetchOnlineUsers();

        // Refresh online users count every 30 seconds
        const interval = setInterval(fetchOnlineUsers, 30000);
        return () => clearInterval(interval);
    }, []);

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

    const getRegionColor = (region) => {
        const colors = {
            delhi: "blue",
            mumbai: "volcano",
            bangalore: "green",
            chennai: "orange",
            kolkata: "purple",
            hyderabad: "cyan",
        };
        return colors[region?.toLowerCase()] || "default";
    };

    const regions = [...new Set(items.map((item) => item.region).filter(Boolean))];

    const filteredItems = items.filter((item) => {
        const matchesSearch =
            searchText === "" ||
            item.serialNo?.toLowerCase().includes(searchText.toLowerCase()) ||
            item.updatedByEmail?.toLowerCase().includes(searchText.toLowerCase()) ||
            item.remark?.toLowerCase().includes(searchText.toLowerCase());

        const matchesRegion =
            filterRegion === "all" || item.region === filterRegion;

        return matchesSearch && matchesRegion;
    });

    // Derive action from item status if action is not explicitly set
    const deriveActionFromStatus = (record) => {
        // If action is explicitly set, use it
        if (record.action) return record.action;

        const status = record.itemStatus?.toLowerCase() || "";
        const availability = record.availabilityStatus?.toLowerCase() || "";

        // Map status to action - ORDER MATTERS! Check most specific first
        if (status.includes("new") || status.includes("added")) return "ADDED";
        if (status.includes("delete") || status.includes("removed")) return "DELETED";
        if (status.includes("repair") || availability.includes("repair")) return "REPAIRING";
        if (status.includes("faulty") || status.includes("fault")) return "FAULTY";
        if (status.includes("issue") || availability.includes("issue")) return "ISSUED";
        // AVAILABLE must come BEFORE RETURNED to avoid false positives
        if (status.includes("available") || availability.includes("available")) return "AVAILABLE";
        if (status.includes("return") || availability.includes("return")) return "RETURNED";
        if (record.remark?.toLowerCase().includes("region")) return "REGION_CHANGED";

        return "UPDATED";
    };

    const getActionColor = (action) => {
        const colors = {
            ADDED: "cyan",
            ISSUED: "volcano",
            RETURNED: "green",
            UPDATED: "blue",
            REGION_CHANGED: "purple",
            REPAIRING: "orange",
            DELETED: "red",
            FAULTY: "magenta",
            AVAILABLE: "success",
        };
        return colors[action] || "default";
    };

    const getActionIcon = (action) => {
        const icons = {
            ADDED: "➕",
            ISSUED: "📤",
            RETURNED: "📥",
            UPDATED: "✏️",
            REGION_CHANGED: "📍",
            REPAIRING: "🔧",
            DELETED: "🗑️",
            FAULTY: "⚠️",
            AVAILABLE: "✅",
        };
        return icons[action] || "📋";
    };

    const columns = [
        {
            title: "Timestamp",
            dataIndex: "updateDate",
            key: "updateDate",
            width: 180,
            render: (text) => (
                <Space direction="vertical" size={0}>
                    <Space>
                        <ClockCircleOutlined style={{ color: "#8c8c8c" }} />
                        <Text style={{ fontSize: 12 }}>{formatDate(text)}</Text>
                    </Space>
                    <Text type="secondary" style={{ fontSize: 11, marginLeft: 18 }}>
                        {text ? formatDistanceToNow(new Date(text), { addSuffix: true }) : ""}
                    </Text>
                </Space>
            ),
            sorter: (a, b) => new Date(a.updateDate) - new Date(b.updateDate),
            defaultSortOrder: "descend",
        },
        {
            title: "Action",
            key: "action",
            width: 150,
            render: (_, record) => {
                const action = deriveActionFromStatus(record);
                return (
                    <Tag color={getActionColor(action)} style={{ fontWeight: 500 }}>
                        {getActionIcon(action)} {action.replace(/_/g, " ")}
                    </Tag>
                );
            },
            filters: [
                { text: "➕ Added", value: "ADDED" },
                { text: "📤 Issued", value: "ISSUED" },
                { text: "📥 Returned", value: "RETURNED" },
                { text: "✏️ Updated", value: "UPDATED" },
                { text: "📍 Region Changed", value: "REGION_CHANGED" },
                { text: "🔧 Repairing", value: "REPAIRING" },
                { text: "⚠️ Faulty", value: "FAULTY" },
                { text: "✅ Available", value: "AVAILABLE" },
                { text: "🗑️ Deleted", value: "DELETED" },
            ],
            onFilter: (value, record) => deriveActionFromStatus(record) === value,
        },
        {
            title: "Serial No.",
            dataIndex: "serialNo",
            key: "serialNo",
            width: 180,
            render: (text) => (
                <Tag color="geekblue" style={{ fontFamily: "monospace", fontWeight: 500 }}>
                    <InboxOutlined style={{ marginRight: 4 }} />
                    {text || "-"}
                </Tag>
            ),
        },
        {
            title: "Keyword",
            dataIndex: "keyword",
            key: "keyword",
            width: 120,
            render: (text) => text ? <Tag>{text}</Tag> : <Text type="secondary">-</Text>,
        },
        {
            title: "Status",
            key: "status",
            width: 100,
            render: (_, record) => (
                record.availabilityStatus ? (
                    <Tag color={record.availabilityStatus?.toLowerCase() === "available" ? "green" : "orange"}>
                        {record.availabilityStatus}
                    </Tag>
                ) : <Text type="secondary">-</Text>
            ),
        },
        {
            title: "Updated By",
            dataIndex: "updatedByEmail",
            key: "updatedByEmail",
            width: 200,
            render: (email) => (
                <Space>
                    <Avatar size="small" style={{ backgroundColor: "#1890ff" }}>
                        {(email || "U").charAt(0).toUpperCase()}
                    </Avatar>
                    <Tooltip title={email}>
                        <Text ellipsis style={{ maxWidth: 130 }}>
                            {email || "-"}
                        </Text>
                    </Tooltip>
                </Space>
            ),
        },
        {
            title: "Region",
            dataIndex: "region",
            key: "region",
            width: 110,
            render: (region) => (
                region ? (
                    <Tag color={getRegionColor(region)} icon={<EnvironmentOutlined />}>
                        {region}
                    </Tag>
                ) : (
                    <Text type="secondary">-</Text>
                )
            ),
        },
        {
            title: "Remarks",
            dataIndex: "remark",
            key: "remark",
            ellipsis: true,
            render: (text) => (
                <Tooltip title={text}>
                    <div className="remark-cell">
                        {text ? (
                            <Text>{text}</Text>
                        ) : (
                            <Text type="secondary" italic>
                                No remarks
                            </Text>
                        )}
                    </div>
                </Tooltip>
            ),
        },
    ];

    // Calculate stats
    const todayCount = items.filter((item) => {
        const today = new Date().toDateString();
        return new Date(item.updateDate).toDateString() === today;
    }).length;

    const regionCounts = items.reduce((acc, item) => {
        if (item.region) {
            acc[item.region] = (acc[item.region] || 0) + 1;
        }
        return acc;
    }, {});

    const topRegion =
        Object.entries(regionCounts).sort((a, b) => b[1] - a[1])[0]?.[0] || "-";

    // const uniqueUsers = new Set(items.map((item) => item.updatedByEmail)).size;

    return (
        <div className="enhanced-activity-container">
            <div className="enhanced-activity-header">
                <HistoryOutlined className="header-icon" />
                <div>
                    <Title level={2} style={{ margin: 0, color: "#fff" }}>
                        Warehouse Activity Feed
                    </Title>
                    <Text style={{ color: "rgba(255,255,255,0.8)" }}>
                        Track all item updates and changes across the warehouse
                    </Text>
                </div>
                <div style={{ marginLeft: "auto" }}>
                    <Badge count={todayCount} showZero overflowCount={999}>
                        <Tag color="green" style={{ fontSize: 14, padding: "4px 12px" }}>
                            Today's Updates
                        </Tag>
                    </Badge>
                </div>
            </div>

            {/* Stats Cards */}
            <Row gutter={16} style={{ margin: "24px" }}>
                <Col xs={12} sm={6}>
                    <Card className="stat-mini-card">
                        <Statistic
                            title="Total Activities"
                            value={items.length}
                            prefix={<HistoryOutlined />}
                        />
                    </Card>
                </Col>
                <Col xs={12} sm={6}>
                    <Card className="stat-mini-card">
                        <Statistic
                            title="Today"
                            value={todayCount}
                            valueStyle={{ color: "#52c41a" }}
                            prefix={<ClockCircleOutlined />}
                        />
                    </Card>
                </Col>
                <Col xs={12} sm={6}>
                    <Card className="stat-mini-card">
                        <Tooltip title="Users active in the last 5 minutes (updates every 30s)">
                            <Statistic
                                title="Online Users"
                                value={onlineUsers}
                                valueStyle={{ color: "#52c41a" }}
                                prefix={<Badge status="processing" />}
                                suffix={<UserOutlined />}
                            />
                        </Tooltip>
                    </Card>
                </Col>
                <Col xs={12} sm={6}>
                    <Card className="stat-mini-card">
                        <Statistic
                            title="Top Region"
                            value={topRegion}
                            valueStyle={{ color: "#722ed1", fontSize: 20 }}
                            prefix={<EnvironmentOutlined />}
                        />
                    </Card>
                </Col>
            </Row>

            {/* Filters */}
            <Card className="filter-card" style={{ margin: "0 24px 24px" }}>
                <Row gutter={16} align="middle">
                    <Col xs={24} md={10}>
                        <Input
                            placeholder="Search by Serial No., email, or remarks..."
                            prefix={<SearchOutlined />}
                            value={searchText}
                            onChange={(e) => setSearchText(e.target.value)}
                            allowClear
                            size="large"
                        />
                    </Col>
                    <Col xs={24} md={6}>
                        <Select
                            style={{ width: "100%" }}
                            placeholder="Filter by region"
                            value={filterRegion}
                            onChange={setFilterRegion}
                            size="large"
                            suffixIcon={<FilterOutlined />}
                        >
                            <Option value="all">All Regions</Option>
                            {regions.map((region) => (
                                <Option key={region} value={region}>
                                    {region}
                                </Option>
                            ))}
                        </Select>
                    </Col>
                    <Col xs={12} md={4}>
                        <Text
                            type="link"
                            style={{ cursor: "pointer" }}
                            onClick={fetchActivities}
                        >
                            <ReloadOutlined /> Refresh
                        </Text>
                    </Col>
                    <Col xs={12} md={4}>
                        <Text type="secondary">
                            Showing {filteredItems.length} of {items.length}
                        </Text>
                    </Col>
                </Row>
            </Card>

            {/* Table */}
            <Card className="table-card" style={{ margin: "0 24px 24px" }}>
                {loading ? (
                    <div style={{ textAlign: "center", padding: 60 }}>
                        <Spin size="large" />
                        <div style={{ marginTop: 16 }}>Loading activities...</div>
                    </div>
                ) : filteredItems.length === 0 ? (
                    <Empty
                        description="No activities found"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                ) : (
                    <Table
                        columns={columns}
                        dataSource={filteredItems}
                        rowKey="id"
                        pagination={{
                            pageSize: 15,
                            showSizeChanger: true,
                            showTotal: (total, range) =>
                                `${range[0]}-${range[1]} of ${total} activities`,
                        }}
                        scroll={{ x: 900 }}
                        size="middle"
                    />
                )}
            </Card>
        </div>
    );
}
