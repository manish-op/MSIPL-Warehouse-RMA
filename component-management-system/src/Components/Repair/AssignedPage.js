import React, { useState, useEffect, useRef } from "react";
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
    Dropdown,
    Menu
} from "antd";
import {
    EditOutlined,
    UserSwitchOutlined,
    TeamOutlined,
    ReloadOutlined,
    BarcodeOutlined,
    UserOutlined,
    InfoCircleOutlined,
    WarningOutlined,
    CheckCircleOutlined,
    DownOutlined
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css"; // Uses the shared modern CSS
import BERCertificateForm from "./BERCertificateForm";

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

    // Status options from backend
    const [repairStatuses, setRepairStatuses] = useState([]);

    // Reassignment state
    const [reassignModalVisible, setReassignModalVisible] = useState(false);
    const [employees, setEmployees] = useState([]);
    const [newAssignee, setNewAssignee] = useState(null);
    const [reassignReason, setReassignReason] = useState("");
    const berFormRef = useRef(null);

    //BER Certification
    const [showBERForm, setShowBERForm] = useState(false);
    const [berProductData, setBerProductData] = useState(null);


    const getStatusColor = (status) => {
        switch (status?.toUpperCase()) {
            case "ASSIGNED": return "blue";
            case "REPAIRING": return "orange";
            case "BER": return "red";
            case "REPAIRED": return "green";
            case "CANT_BE_REPAIRED": return "purple";
            default: return "default";
        }
    };

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getAssignedItems();
        if (result.success) {
            // Filter out Depot items, show everything else (Local, legacy nulls, etc.)
            const localItems = (result.data || []).filter(item => item.repairType !== 'DEPOT');
            setItems(localItems);
        } else {
            message.error("Failed to load assigned items");
        }
        setLoading(false);
    };

    const loadEmployees = async () => {
        try {
            const encodedToken = document.cookie.split("authToken=")[1]?.split(";")[0];
            if (!encodedToken) return;

            const response = await fetch("http://localhost:8081/api/all-users", {
                headers: {
                    Authorization: `Bearer ${atob(encodedToken)}`,
                },
            });
            if (response.ok) {
                const data = await response.json();
                setEmployees(data || []);
            }
        } catch (error) {
            console.error("Failed to load employees:", error);
        }
    };

    const loadRepairStatuses = async () => {
        const result = await RmaApi.getRepairStatuses();
        if (result.success) {
            setRepairStatuses(result.data || []);
        } else {
            console.error("Failed to load statuses: ", result.error);
            message.error("Could not load repair statuses");
            setRepairStatuses([]);
        }
    };

    useEffect(() => {
        loadItems();
        loadEmployees();
        loadRepairStatuses();
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

            //if status is BER
            if (newStatus === "BER") {
                setBerProductData({
                    customer: selectedItem.customerName || selectedItem.customer || "",
                    consignee: selectedItem.consignee || "",
                    dateIn: selectedItem.dateIn || "",
                    warrantyStatus: selectedItem.warrantyStatus || "",
                    isStatus: selectedItem.isStatus || "",
                    incomingGIN: selectedItem.ginNo || "",
                    jobID: selectedItem.jobId || "",
                    serialNo: selectedItem.serialNo || "",
                    tanapa: selectedItem.product || "",
                    faultFromCustomer: selectedItem.faultDescription || "",
                });
                setShowBERForm(true);
            }
        } else {
            message.error(result.error || "Failed to update status");
        }
        setUpdating(false);
    };

    const openReassignModal = (item) => {
        setSelectedItem(item);
        setNewAssignee(null);
        setReassignReason("");
        setReassignModalVisible(true);
    };

    const handleReassign = async () => {
        if (!newAssignee) {
            message.warning("Please select a new technician");
            return;
        }
        if (!reassignReason.trim()) {
            message.warning("Please enter a reason for reassignment");
            return;
        }

        setUpdating(true);
        const result = await RmaApi.reassignItem(
            selectedItem.id,
            newAssignee.email,
            newAssignee.name,
            reassignReason
        );
        if (result.success) {
            message.success(result.message || "Item reassigned successfully!");
            setReassignModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to reassign item");
        }
        setUpdating(false);
    };

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section - Light Blue Theme (Matches Unrepaired) */}
                <div className="unrepaired-header header-assigned">
                    <div className="header-content">
                        <div className="header-title">
                            <UserSwitchOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0 }}>
                                    Assigned Items
                                </Title>
                                <Text type="secondary">
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
                                        <div className="rma-card-header-flex">
                                            <div className="rma-identity">
                                                <div className="rma-id-box">
                                                    <span className="rma-label">RMA Request</span>
                                                    <span className="rma-value">{rmaNo}</span>
                                                </div>
                                                <Badge
                                                    count={rmaItems.length}
                                                    overflowCount={99}
                                                />
                                            </div>
                                        </div>
                                    }
                                >
                                    {/* MODERN CARD GRID */}
                                    <div className="rma-items-grid">
                                        {rmaItems.map((item) => (
                                            <div key={item.id} className="rma-item-card-modern">

                                                {/* Header Strip */}
                                                <div className="item-header">
                                                    <span className="item-product">
                                                        {item.product || "Product"}
                                                    </span>
                                                    <Tag color={getStatusColor(item.repairStatus)}>
                                                        {item.repairStatus || "ASSIGNED"}
                                                    </Tag>
                                                </div>

                                                {/* Details Grid */}
                                                <div className="item-details-grid">
                                                    <div className="detail-box">
                                                        <span className="label"><BarcodeOutlined /> Serial No</span>
                                                        <span className="value monospace">{item.serialNo || "N/A"}</span>
                                                    </div>
                                                    <div className="detail-box">
                                                        <span className="label">Model</span>
                                                        <span className="value">{item.model || "N/A"}</span>
                                                    </div>
                                                    <div className="detail-box">
                                                        <span className="label"><UserOutlined /> Technician</span>
                                                        <span className="value">{item.assignedToName || "N/A"}</span>
                                                    </div>
                                                    <div className="detail-box">
                                                        <span className="label">RMA No</span>
                                                        {item.itemRmaNo ? <Tag color="blue">{item.itemRmaNo}</Tag> : <Text type="secondary" style={{ fontSize: '11px' }}>None</Text>}
                                                    </div>
                                                </div>

                                                {/* Fault / Reason Box */}
                                                <div className="fault-box">
                                                    <span className="label">Fault Description</span>
                                                    <p className="fault-desc">{item.faultDescription || "No description provided."}</p>

                                                    {item.lastReassignmentReason && (
                                                        <div style={{ marginTop: '8px', borderTop: '1px dashed var(--border-split)', paddingTop: '4px' }}>
                                                            <span className="label">Reassign Reason</span>
                                                            <p className="fault-desc" style={{ fontStyle: 'italic' }}>{item.lastReassignmentReason}</p>
                                                        </div>
                                                    )}
                                                </div>

                                                {/* Footer Actions */}
                                                <div className="item-footer" style={{ display: 'flex', gap: '8px' }}>
                                                    <Button
                                                        type="primary"
                                                        block
                                                        size="small"
                                                        icon={<EditOutlined />}
                                                        onClick={() => openStatusModal(item)}
                                                    >
                                                        Update
                                                    </Button>
                                                    <Button
                                                        block
                                                        size="small"
                                                        icon={<UserSwitchOutlined />}
                                                        onClick={() => openReassignModal(item)}
                                                    >
                                                        Reassign
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
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
                            style={{ backgroundColor: "#1890ff", borderColor: "#1890ff" }}
                        >
                            Update Status
                        </Button>
                    ]}
                >
                    <div className="fault-box" style={{ margin: '0 0 16px 0' }}>
                        <Row gutter={16}>
                            <Col span={12}>
                                <Text type="secondary" style={{ fontSize: '12px' }}>Product</Text>
                                <div><Text strong>{selectedItem?.product}</Text></div>
                            </Col>
                            <Col span={12}>
                                <Text type="secondary" style={{ fontSize: '12px' }}>Technician</Text>
                                <div><Text strong>{selectedItem?.assignedToName}</Text></div>
                            </Col>
                        </Row>
                        <div style={{ marginTop: '8px' }}>
                            <Text type="secondary" style={{ fontSize: '12px' }}>Fault</Text>
                            <Paragraph style={{ margin: 0, fontSize: '13px' }} ellipsis={{ rows: 2 }}>{selectedItem?.faultDescription}</Paragraph>
                        </div>
                    </div>

                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>New Status *</Text>
                            <Select
                                style={{ width: "100%", marginTop: 4 }}
                                size="large"
                                value={newStatus}
                                onChange={(val) => setNewStatus(val)}
                                placeholder="Select status"
                            >
                                {repairStatuses.map(status => (
                                    <Option key={status.value} value={status.value}>
                                        {status.label}
                                    </Option>
                                ))}
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
                                rows={2}
                                placeholder="Enter remarks about the repair"
                                value={remarks}
                                onChange={(e) => setRemarks(e.target.value)}
                                style={{ marginTop: 4 }}
                            />
                        </div>
                    </Space>
                </Modal>

                {/* Reassign Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <UserSwitchOutlined style={{ color: "#fa8c16" }} />
                            <span>Reassign Technician</span>
                        </div>
                    }
                    open={reassignModalVisible}
                    onCancel={() => setReassignModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setReassignModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="reassign"
                            type="primary"
                            loading={updating}
                            onClick={handleReassign}
                            style={{ backgroundColor: "#fa8c16", borderColor: "#fa8c16" }}
                        >
                            Reassign
                        </Button>
                    ]}
                >
                    <div className="fault-box" style={{ margin: '0 0 16px 0' }}>
                        <Row gutter={16}>
                            <Col span={12}>
                                <Text type="secondary" style={{ fontSize: '12px' }}>Product</Text>
                                <div><Text strong>{selectedItem?.product}</Text></div>
                            </Col>
                            <Col span={12}>
                                <Text type="secondary" style={{ fontSize: '12px' }}>Current Tech</Text>
                                <div><Text strong>{selectedItem?.assignedToName}</Text></div>
                            </Col>
                        </Row>
                    </div>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>New Technician *</Text>
                            <Select
                                style={{ width: "100%", marginTop: 4 }}
                                size="large"
                                placeholder="Select new technician"
                                value={newAssignee ? newAssignee.email : undefined}
                                optionLabelProp="value"
                                onChange={(value) => {
                                    const emp = employees.find(e => e.email === value);
                                    setNewAssignee(emp);
                                }}
                                showSearch
                                filterOption={(input, option) =>
                                    option.children.toLowerCase().includes(input.toLowerCase())
                                }
                            >
                                {employees
                                    .filter(emp => emp.email !== selectedItem?.assignedToEmail)
                                    .map(emp => (
                                        <Option key={emp.email} value={emp.email}>
                                            {emp.name} ({emp.email})
                                        </Option>
                                    ))}
                            </Select>
                        </div>
                        <div>
                            <Text strong>Reason for Reassignment *</Text>
                            <TextArea
                                rows={3}
                                placeholder="Enter the reason for reassigning this item (mandatory)"
                                value={reassignReason}
                                onChange={(e) => setReassignReason(e.target.value)}
                                style={{ marginTop: 4 }}
                            />
                        </div>
                    </Space>
                </Modal>

                {showBERForm && (
                    <Modal
                        open={showBERForm}
                        onCancel={() => setShowBERForm(false)}
                        width={1000}
                        closable={true}
                        maskClosable={false}
                        styles={{ body: { height: '80vh', overflowY: 'auto', padding: 0 } }}
                        footer={[
                            <Button key="close" onClick={() => setShowBERForm(false)}>
                                Close
                            </Button>,
                            <Button key="download" type="primary" onClick={() => berFormRef.current?.handleDownloadPDF()}>
                                Download PDF
                            </Button>,
                        ]}
                    >
                        <BERCertificateForm
                            ref={berFormRef}
                            productData={berProductData}
                            onClose={() => setShowBERForm(false)}
                        />
                    </Modal>
                )}
            </div>
        </RmaLayout>
    );
}