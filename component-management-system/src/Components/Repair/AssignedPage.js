import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Button,
    Modal,
    Select,
    Input,
    Space,
    Spin,
    Card,
    Row,
    Col,
    Badge,
    Empty,
    Divider,
} from "antd";
import {
    EditOutlined,
    UserSwitchOutlined,
    TeamOutlined,
    ReloadOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;
const { Option } = Select;

export default function AssignedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [statusModalVisible, setStatusModalVisible] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [newStatus, setNewStatus] = useState("");
    const [remarks, setRemarks] = useState("");
    const [issueFixed, setIssueFixed] = useState("");
    const [updating, setUpdating] = useState(false);

    const getStatusColor = (status) => {
        switch (status?.toUpperCase()) {
            case "ASSIGNED": return "blue";
            case "REPAIRING": return "orange";
            case "BER": return "red";
            default: return "default";
        }
    };

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getAssignedItems();
        if (result.success) {
            setItems(result.data || []);
        } else {
            message.error("Failed to load assigned items");
        }
        setLoading(false);
    };

    useEffect(() => {
        loadItems();
    }, []);

    // Group items by RMA number
    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) {
            acc[rmaNo] = [];
        }
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRmaRequests = Object.keys(groupedItems).length;
    const totalItems = items.length;

    const openStatusModal = (item) => {
        setSelectedItem(item);
        setNewStatus(item.repairStatus || "");
        setRemarks(item.repairRemarks || "");
        setIssueFixed(item.issueFixed || "");
        setStatusModalVisible(true);
    };

    const handleUpdateStatus = async () => {
        if (!newStatus) {
            message.warning("Please select a status");
            return;
        }
        if (!issueFixed.trim()) {
            message.warning("Please enter Issue Fixed description");
            return;
        }

        setUpdating(true);
        const result = await RmaApi.updateItemStatus(selectedItem.id, newStatus, remarks, issueFixed);
        if (result.success) {
            message.success("Status updated successfully!");
            setStatusModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to update status");
        }
        setUpdating(false);
    };

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section */}
                <div className="unrepaired-header">
                    <div className="header-content">
                        <div className="header-title">
                            <UserSwitchOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Assigned Items
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items currently being worked on by technicians
                                </Text>
                            </div>
                        </div>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={loadItems}
                            loading={loading}
                            className="refresh-btn"
                        >
                            Refresh
                        </Button>
                    </div>

                    {/* Stats Row */}
                    <Row gutter={16} className="stats-row">
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <TeamOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">Active RMAs</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <UserSwitchOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">In Progress</div>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </div>

                {/* Content Section */}
                <div className="unrepaired-content">
                    {loading ? (
                        <div className="loading-container">
                            <Spin size="large" />
                            <Text style={{ marginTop: 16 }}>Loading items...</Text>
                        </div>
                    ) : totalRmaRequests === 0 ? (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="No assigned items found"
                            className="empty-state"
                        />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card
                                    key={rmaNo}
                                    className="rma-group-card"
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number">
                                                <Tag color="#1890ff" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                            </span>
                                            <Badge
                                                count={rmaItems.length}
                                                style={{ backgroundColor: "#1890ff" }}
                                                overflowCount={99}
                                            />
                                        </div>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card
                                                    className="item-card"
                                                    size="small"
                                                    hoverable
                                                    actions={[
                                                        <Button
                                                            type="primary"
                                                            icon={<EditOutlined />}
                                                            onClick={() => openStatusModal(item)}
                                                            className="assign-btn"
                                                        >
                                                            Update Status
                                                        </Button>
                                                    ]}
                                                >
                                                    <div className="item-content">
                                                        <div className="item-row">
                                                            <Text type="secondary">Product</Text>
                                                            <Text strong>{item.product || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Serial No.</Text>
                                                            <Text code>{item.serialNo || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Assigned To</Text>
                                                            <Text>{item.assignedToName || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Status</Text>
                                                            <Tag color={getStatusColor(item.repairStatus)}>
                                                                {item.repairStatus || "ASSIGNED"}
                                                            </Tag>
                                                        </div>
                                                        {item.itemRmaNo && (
                                                            <div className="item-row">
                                                                <Text type="secondary">RMA Number</Text>
                                                                <Tag color="green">{item.itemRmaNo}</Tag>
                                                            </div>
                                                        )}
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section">
                                                            <Text type="secondary">Fault Description</Text>
                                                            <Paragraph
                                                                ellipsis={{ rows: 2, expandable: true }}
                                                                className="fault-text"
                                                            >
                                                                {item.faultDescription || "No description"}
                                                            </Paragraph>
                                                        </div>
                                                    </div>
                                                </Card>
                                            </Col>
                                        ))}
                                    </Row>
                                </Card>
                            ))}
                        </div>
                    )}
                </div>

                {/* Status Update Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <EditOutlined style={{ color: "#1890ff" }} />
                            <span>Update Repair Status</span>
                        </div>
                    }
                    open={statusModalVisible}
                    onCancel={() => setStatusModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setStatusModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="update"
                            type="primary"
                            loading={updating}
                            onClick={handleUpdateStatus}
                        >
                            Update Status
                        </Button>
                    ]}
                    className="assign-modal"
                >
                    <div className="modal-item-info">
                        <Card size="small" style={{ marginBottom: 16, backgroundColor: "#e6f7ff" }}>
                            <Row gutter={16}>
                                <Col span={12}>
                                    <Text type="secondary">Product</Text>
                                    <div><Text strong>{selectedItem?.product}</Text></div>
                                </Col>
                                <Col span={12}>
                                    <Text type="secondary">Assigned To</Text>
                                    <div><Text>{selectedItem?.assignedToName}</Text></div>
                                </Col>
                            </Row>
                            <div style={{ marginTop: 12 }}>
                                <Text type="secondary">Fault</Text>
                                <Paragraph style={{ margin: 0 }}>{selectedItem?.faultDescription}</Paragraph>
                            </div>
                        </Card>
                    </div>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>Update Status *</Text>
                            <Select
                                style={{ width: "100%", marginTop: 4 }}
                                size="large"
                                value={newStatus}
                                onChange={(val) => setNewStatus(val)}
                                placeholder="Select status"
                            >
                                <Option value="REPAIRING">Repairing</Option>
                                <Option value="REPAIRED">Repaired</Option>
                                <Option value="CANT_BE_REPAIRED">Can't Be Repaired</Option>
                                <Option value="BER">BER</Option>
                            </Select>
                        </div>
                        <div>
                            <Text strong>Issue Fixed *</Text>
                            <TextArea
                                rows={3}
                                placeholder="Describe the issue that was fixed (mandatory)"
                                value={issueFixed}
                                onChange={(e) => setIssueFixed(e.target.value)}
                                style={{ marginTop: 4 }}
                            />
                        </div>
                        <div>
                            <Text strong>Remarks</Text>
                            <TextArea
                                rows={3}
                                placeholder="Enter remarks about the repair"
                                value={remarks}
                                onChange={(e) => setRemarks(e.target.value)}
                                style={{ marginTop: 4 }}
                            />
                        </div>
                    </Space>
                </Modal>
            </div>
        </RmaLayout>
    );
}
