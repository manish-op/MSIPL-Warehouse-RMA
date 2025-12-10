import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Button,
    Modal,
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
    UserAddOutlined,
    ToolOutlined,
    ExclamationCircleOutlined,
    AppstoreOutlined,
    ReloadOutlined,
    EditOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

export default function UnrepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [assignModalVisible, setAssignModalVisible] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [assigneeName, setAssigneeName] = useState("");
    const [assigneeEmail, setAssigneeEmail] = useState("");
    const [assigning, setAssigning] = useState(false);
    // Bulk assign state
    const [bulkAssignModalVisible, setBulkAssignModalVisible] = useState(false);
    const [selectedRmaNo, setSelectedRmaNo] = useState(null);
    const [selectedRmaItemCount, setSelectedRmaItemCount] = useState(0);
    // Edit RMA state
    const [editRmaModalVisible, setEditRmaModalVisible] = useState(false);
    const [newRmaNo, setNewRmaNo] = useState("");
    const [updatingRma, setUpdatingRma] = useState(false);

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getUnassignedItems();
        if (result.success) {
            setItems(result.data || []);
        } else {
            message.error("Failed to load unassigned items");
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

    const openAssignModal = (item) => {
        setSelectedItem(item);
        setAssigneeName("");
        setAssigneeEmail("");
        setAssignModalVisible(true);
    };

    const handleAssign = async () => {
        if (!assigneeName.trim()) {
            message.warning("Please enter technician name");
            return;
        }
        if (!assigneeEmail.trim()) {
            message.warning("Please enter technician email");
            return;
        }

        setAssigning(true);
        const result = await RmaApi.assignItem(selectedItem.id, assigneeEmail, assigneeName);
        if (result.success) {
            message.success("Item assigned successfully!");
            setAssignModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to assign item");
        }
        setAssigning(false);
    };

    // Bulk assign handlers
    const openBulkAssignModal = (rmaNo, itemCount) => {
        setSelectedRmaNo(rmaNo);
        setSelectedRmaItemCount(itemCount);
        setAssigneeName("");
        setAssigneeEmail("");
        setBulkAssignModalVisible(true);
    };

    const handleBulkAssign = async () => {
        if (!assigneeName.trim()) {
            message.warning("Please enter technician name");
            return;
        }
        if (!assigneeEmail.trim()) {
            message.warning("Please enter technician email");
            return;
        }

        setAssigning(true);
        const result = await RmaApi.bulkAssignByRmaNo(selectedRmaNo, assigneeEmail, assigneeName);
        if (result.success) {
            message.success(`All ${selectedRmaItemCount} items assigned successfully!`);
            setBulkAssignModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to assign items");
        }
        setAssigning(false);
    };

    const openEditRmaModal = (item) => {
        setSelectedItem(item);
        setNewRmaNo("");
        setEditRmaModalVisible(true);
    };

    const handleUpdateRma = async () => {
        if (!newRmaNo.trim()) {
            message.warning("Please enter RMA Number");
            return;
        }

        setUpdatingRma(true);
        const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);
        if (result.success) {
            message.success("RMA Number updated successfully!");
            setEditRmaModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to update RMA Number");
        }
        setUpdatingRma(false);
    };

    // Check if item has a custom RMA number (not inherited from request)
    const hasCustomRmaNo = (item) => {
        // Item has custom RMA if rmaNo field is directly set on the item
        // This checks the item-level rmaNo, not the parent request's requestNumber
        return item.itemRmaNo && item.itemRmaNo.trim() !== "";
    };

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section */}
                <div className="unrepaired-header">
                    <div className="header-content">
                        <div className="header-title">
                            <ToolOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Unrepaired Items
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items awaiting technician assignment
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
                                <AppstoreOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">RMA Requests</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <ExclamationCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Pending Items</div>
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
                            description="No unassigned items found"
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
                                            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                                                <Tag color="#1890ff" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                                <Badge
                                                    count={rmaItems.length}
                                                    style={{ backgroundColor: "#1890ff" }}
                                                    overflowCount={99}
                                                />
                                            </div>
                                            <Button
                                                type="primary"
                                                icon={<UserAddOutlined />}
                                                onClick={() => openBulkAssignModal(rmaNo, rmaItems.length)}
                                                size="small"
                                                style={{ background: "#52c41a", borderColor: "#52c41a" }}
                                            >
                                                Assign All ({rmaItems.length})
                                            </Button>
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
                                                            icon={<UserAddOutlined />}
                                                            onClick={() => openAssignModal(item)}
                                                            className="assign-btn"
                                                        >
                                                            Assign Technician
                                                        </Button>,
                                                        !item.itemRmaNo && (
                                                            <Button
                                                                key="add-rma"
                                                                icon={<EditOutlined />}
                                                                onClick={() => openEditRmaModal(item)}
                                                            >
                                                                Add RMA
                                                            </Button>
                                                        )
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
                                                            <Text type="secondary">Model</Text>
                                                            <Text>{item.model || "N/A"}</Text>
                                                        </div>
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
                                                        {item.itemRmaNo && (
                                                            <div className="item-row" style={{ marginTop: 8 }}>
                                                                <Text type="secondary">RMA Number</Text>
                                                                <Tag color="green">{item.itemRmaNo}</Tag>
                                                            </div>
                                                        )}
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

                {/* Assign Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <UserAddOutlined style={{ color: "#1890ff" }} />
                            <span>Assign Technician</span>
                        </div>
                    }
                    open={assignModalVisible}
                    onCancel={() => setAssignModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setAssignModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="assign"
                            type="primary"
                            loading={assigning}
                            onClick={handleAssign}
                            style={{ backgroundColor: "#1890ff", borderColor: "#1890ff" }}
                        >
                            Assign Technician
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
                                    <Text type="secondary">Serial No.</Text>
                                    <div><Text code>{selectedItem?.serialNo}</Text></div>
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
                            <Text strong>Technician Name *</Text>
                            <Input
                                placeholder="Enter technician name"
                                value={assigneeName}
                                onChange={(e) => setAssigneeName(e.target.value)}
                                prefix={<UserAddOutlined style={{ color: "#bfbfbf" }} />}
                                size="large"
                                style={{ marginTop: 4 }}
                            />
                        </div>
                        <div>
                            <Text strong>Technician Email *</Text>
                            <Input
                                placeholder="Enter technician email"
                                value={assigneeEmail}
                                onChange={(e) => setAssigneeEmail(e.target.value)}
                                type="email"
                                size="large"
                                style={{ marginTop: 4 }}
                            />
                        </div>
                    </Space>
                </Modal>

                {/* Bulk Assign Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <UserAddOutlined style={{ color: "#52c41a" }} />
                            <span>Assign All Items</span>
                        </div>
                    }
                    open={bulkAssignModalVisible}
                    onCancel={() => setBulkAssignModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setBulkAssignModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="assign"
                            type="primary"
                            loading={assigning}
                            onClick={handleBulkAssign}
                            style={{ backgroundColor: "#52c41a", borderColor: "#52c41a" }}
                        >
                            Assign All Items
                        </Button>
                    ]}
                    className="assign-modal"
                >
                    <Card size="small" style={{ marginBottom: 16, backgroundColor: "#f6ffed", border: "1px solid #b7eb8f" }}>
                        <div style={{ textAlign: "center" }}>
                            <Tag color="#1890ff" style={{ fontSize: 16, padding: "8px 16px" }}>
                                RMA: {selectedRmaNo}
                            </Tag>
                            <div style={{ marginTop: 12 }}>
                                <Text type="secondary">Total items to assign: </Text>
                                <Text strong style={{ fontSize: 18, color: "#52c41a" }}>{selectedRmaItemCount}</Text>
                            </div>
                        </div>
                    </Card>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>Technician Name *</Text>
                            <Input
                                placeholder="Enter technician name"
                                value={assigneeName}
                                onChange={(e) => setAssigneeName(e.target.value)}
                                prefix={<UserAddOutlined style={{ color: "#bfbfbf" }} />}
                                size="large"
                                style={{ marginTop: 4 }}
                            />
                        </div>
                        <div>
                            <Text strong>Technician Email *</Text>
                            <Input
                                placeholder="Enter technician email"
                                value={assigneeEmail}
                                onChange={(e) => setAssigneeEmail(e.target.value)}
                                type="email"
                                size="large"
                                style={{ marginTop: 4 }}
                            />
                        </div>
                    </Space>
                </Modal>

                {/* Add RMA Modal */}
                <Modal
                    title="Add RMA Number"
                    open={editRmaModalVisible}
                    onCancel={() => setEditRmaModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setEditRmaModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="submit"
                            type="primary"
                            loading={updatingRma}
                            onClick={handleUpdateRma}
                        >
                            Add
                        </Button>
                    ]}
                >
                    <Text strong>RMA Number:</Text>
                    <Input
                        placeholder="Enter RMA Number"
                        value={newRmaNo}
                        onChange={(e) => setNewRmaNo(e.target.value)}
                        style={{ marginTop: 8 }}
                    />
                </Modal>
            </div>
        </RmaLayout>
    );
}