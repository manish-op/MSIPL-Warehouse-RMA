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
    Table,
} from "antd";
import {
    UserAddOutlined,
    ToolOutlined,
    ExclamationCircleOutlined,
    AppstoreOutlined,
    ReloadOutlined,
    EditOutlined,
    FileTextOutlined,
    PrinterOutlined,
    EyeOutlined,
    DownloadOutlined,
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
    // Gatepass state
    const [generatingGatepass, setGeneratingGatepass] = useState(null);
    // FRU Sticker state
    const [stickerModalVisible, setStickerModalVisible] = useState(false);
    const [stickerItems, setStickerItems] = useState([]);
    // Gatepass Preview state
    const [gatepassPreviewVisible, setGatepassPreviewVisible] = useState(false);
    const [gatepassItems, setGatepassItems] = useState([]);
    const [gatepassRmaNo, setGatepassRmaNo] = useState("");

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

        // Save scroll position before update
        const scrollY = window.scrollY;

        setUpdatingRma(true);
        const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);
        if (result.success) {
            message.success("RMA Number updated successfully!");
            setEditRmaModalVisible(false);
            await loadItems();
            // Restore scroll position after data reloads
            setTimeout(() => window.scrollTo(0, scrollY), 50);
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

    // Generate Inward Gatepass PDF
    const handleGenerateGatepass = async (requestNumber) => {
        setGeneratingGatepass(requestNumber);
        const result = await RmaApi.generateInwardGatepass(requestNumber);
        if (result.success && result.blob) {
            // Create download link
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `InwardGatepass_${requestNumber}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('Gatepass generated and downloaded!');
        } else {
            message.error(result.error || 'Failed to generate gatepass');
        }
        setGeneratingGatepass(null);
    };

    // Open Gatepass Preview Modal
    const openGatepassPreview = (rmaItems, rmaNo) => {
        setGatepassItems(rmaItems);
        setGatepassRmaNo(rmaNo);
        setGatepassPreviewVisible(true);
    };

    // Open FRU Sticker Modal for an RMA request
    const openStickerModal = (rmaItems, rmaNo) => {
        // Use itemRmaNo (manually updated) if available, otherwise fall back to the request number
        const itemsWithRma = rmaItems.map(item => ({
            ...item,
            displayRmaNo: item.itemRmaNo || rmaNo // Prioritize manually assigned RMA number
        }));
        setStickerItems(itemsWithRma);
        setStickerModalVisible(true);
    };

    // Print stickers function
    const handlePrintStickers = () => {
        const printWindow = window.open('', '_blank');
        if (!printWindow) {
            message.error('Please allow popups to print stickers');
            return;
        }

        const stickersHtml = stickerItems.map((item, index) => `
            <div class="sticker">
                <div class="sticker-header">
                    <img src="/companyLogo.png" alt="Company Logo" class="company-logo" onerror="this.style.display='none'" />
                    <span class="company-name">Motorola Solutions India Pvt. Ltd.</span>
                </div>
                <div class="sticker-divider"></div>
                <div class="sticker-content">
                    <div class="sticker-row">
                        <span class="label">RMA No:</span>
                        <span class="value">${item.displayRmaNo || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Customer:</span>
                        <span class="value">${item.companyName || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Received:</span>
                        <span class="value">${item.receivedDate ? new Date(item.receivedDate).toLocaleDateString('en-IN') : 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Product:</span>
                        <span class="value">${item.product || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Serial No:</span>
                        <span class="value">${item.serialNo || 'N/A'}</span>
                    </div>
                    <div class="sticker-row fault-row">
                        <span class="label">Fault:</span>
                        <span class="value fault-text">${item.faultDescription || 'No description'}</span>
                    </div>
                </div>
            </div>
        `).join('');

        printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>FRU Stickers - ${stickerItems[0]?.displayRmaNo || 'RMA'}</title>
                <style>
                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                    }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        padding: 10mm;
                        background: #f5f5f5;
                    }
                    .stickers-container {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 10mm;
                    }
                    .sticker {
                        width: 85mm;
                        min-height: 55mm;
                        border: 1.5px solid #333;
                        border-radius: 4px;
                        background: #fff;
                        padding: 3mm;
                        page-break-inside: avoid;
                    }
                    .sticker-header {
                        display: flex;
                        align-items: center;
                        gap: 3mm;
                        padding-bottom: 2mm;
                    }
                    .company-logo {
                        width: 8mm;
                        height: 8mm;
                        object-fit: contain;
                    }
                    .company-name {
                        font-weight: 600;
                        font-size: 9pt;
                        color: #333;
                    }
                    .sticker-divider {
                        height: 1px;
                        background: #333;
                        margin-bottom: 2mm;
                    }
                    .sticker-content {
                        font-size: 8pt;
                    }
                    .sticker-row {
                        display: flex;
                        margin-bottom: 1.5mm;
                    }
                    .label {
                        font-weight: 600;
                        width: 20mm;
                        color: #555;
                        flex-shrink: 0;
                    }
                    .value {
                        color: #333;
                        word-break: break-word;
                    }
                    .fault-row {
                        margin-top: 1mm;
                    }
                    .fault-text {
                        font-size: 7.5pt;
                        line-height: 1.3;
                    }
                    @media print {
                        body {
                            background: #fff;
                            padding: 5mm;
                        }
                        .stickers-container {
                            gap: 5mm;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="stickers-container">
                    ${stickersHtml}
                </div>
                <script>
                    window.onload = function() {
                        window.print();
                    }
                </script>
            </body>
            </html>
        `);
        printWindow.document.close();
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
                                            <Space>
                                                <Button
                                                    icon={<FileTextOutlined />}
                                                    onClick={() => openGatepassPreview(rmaItems, rmaNo)}
                                                    size="small"
                                                >
                                                    Preview Gatepass
                                                </Button>
                                                <Button
                                                    icon={<PrinterOutlined />}
                                                    onClick={() => openStickerModal(rmaItems, rmaNo)}
                                                    size="small"
                                                    style={{ background: "#722ed1", borderColor: "#722ed1", color: "#fff" }}
                                                >
                                                    Print Stickers ({rmaItems.length})
                                                </Button>
                                                <Button
                                                    type="primary"
                                                    icon={<UserAddOutlined />}
                                                    onClick={() => openBulkAssignModal(rmaNo, rmaItems.length)}
                                                    size="small"
                                                    style={{ background: "#52c41a", borderColor: "#52c41a" }}
                                                >
                                                    Assign All ({rmaItems.length})
                                                </Button>
                                            </Space>
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

                {/* FRU Sticker Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <PrinterOutlined style={{ color: "#722ed1" }} />
                            <span>FRU Stickers Preview</span>
                        </div>
                    }
                    open={stickerModalVisible}
                    onCancel={() => setStickerModalVisible(false)}
                    width={800}
                    footer={[
                        <Button key="cancel" onClick={() => setStickerModalVisible(false)}>
                            Close
                        </Button>,
                        <Button
                            key="print"
                            type="primary"
                            icon={<PrinterOutlined />}
                            onClick={handlePrintStickers}
                            style={{ background: "#722ed1", borderColor: "#722ed1" }}
                        >
                            Print All Stickers ({stickerItems.length})
                        </Button>
                    ]}
                    className="sticker-modal"
                >
                    <div className="sticker-preview-container">
                        <Row gutter={[16, 16]}>
                            {stickerItems.map((item, index) => (
                                <Col xs={24} md={12} key={item.id || index}>
                                    <div className="sticker-preview">
                                        <div className="sticker-preview-header">
                                            <img
                                                src="/companyLogo.png"
                                                alt="Logo"
                                                className="sticker-preview-logo"
                                                onError={(e) => { e.target.style.display = 'none'; }}
                                            />
                                            <span className="sticker-preview-company">
                                                Motorola Solutions India Pvt. Ltd.
                                            </span>
                                        </div>
                                        <Divider style={{ margin: "8px 0" }} />
                                        <div className="sticker-preview-content">
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">RMA No:</Text>
                                                <Text strong>{item.displayRmaNo || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Customer:</Text>
                                                <Text>{item.companyName || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Received:</Text>
                                                <Text>
                                                    {item.receivedDate
                                                        ? new Date(item.receivedDate).toLocaleDateString('en-IN')
                                                        : 'N/A'}
                                                </Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Product:</Text>
                                                <Text>{item.product || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Serial No:</Text>
                                                <Text code>{item.serialNo || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Fault:</Text>
                                                <Paragraph
                                                    ellipsis={{ rows: 2 }}
                                                    style={{ margin: 0, fontSize: 12 }}
                                                >
                                                    {item.faultDescription || 'No description'}
                                                </Paragraph>
                                            </div>
                                        </div>
                                    </div>
                                </Col>
                            ))}
                        </Row>
                    </div>
                </Modal>

                {/* Gatepass Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <EyeOutlined style={{ color: "#1890ff" }} />
                            <span>Gatepass Preview - {gatepassRmaNo}</span>
                        </div>
                    }
                    open={gatepassPreviewVisible}
                    onCancel={() => setGatepassPreviewVisible(false)}
                    width={900}
                    footer={[
                        <Button key="cancel" onClick={() => setGatepassPreviewVisible(false)}>
                            Close
                        </Button>,
                        <Button
                            key="download"
                            type="primary"
                            icon={<DownloadOutlined />}
                            onClick={() => {
                                handleGenerateGatepass(gatepassRmaNo);
                                setGatepassPreviewVisible(false);
                            }}
                            loading={generatingGatepass === gatepassRmaNo}
                        >
                            Download PDF
                        </Button>
                    ]}
                    className="gatepass-modal"
                >
                    <div className="gatepass-preview-container">
                        {/* Header Info */}
                        <Card size="small" className="gatepass-info-card">
                            <Row gutter={16}>
                                <Col span={8}>
                                    <Text type="secondary">RMA Request Number</Text>
                                    <div>
                                        <Tag color="blue" style={{ fontSize: 14, padding: "4px 12px" }}>
                                            {gatepassRmaNo}
                                        </Tag>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Customer</Text>
                                    <div>
                                        <Text strong>{gatepassItems[0]?.companyName || 'N/A'}</Text>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Total Items</Text>
                                    <div>
                                        <Badge
                                            count={gatepassItems.length}
                                            style={{ backgroundColor: "#52c41a" }}
                                            showZero
                                        />
                                    </div>
                                </Col>
                            </Row>
                        </Card>

                        {/* Items Table */}
                        <Table
                            dataSource={gatepassItems.map((item, index) => ({ ...item, key: item.id || index, slNo: index + 1 }))}
                            columns={[
                                {
                                    title: 'Sl.No',
                                    dataIndex: 'slNo',
                                    key: 'slNo',
                                    width: 60,
                                    align: 'center',
                                },
                                {
                                    title: 'Product',
                                    dataIndex: 'product',
                                    key: 'product',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Model',
                                    dataIndex: 'model',
                                    key: 'model',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Serial No',
                                    dataIndex: 'serialNo',
                                    key: 'serialNo',
                                    render: (text) => <Text code>{text || 'N/A'}</Text>,
                                },
                                {
                                    title: 'Fault Description',
                                    dataIndex: 'faultDescription',
                                    key: 'faultDescription',
                                    ellipsis: true,
                                    render: (text) => text || 'No description',
                                },
                                {
                                    title: 'RMA No',
                                    dataIndex: 'itemRmaNo',
                                    key: 'itemRmaNo',
                                    render: (text) => text ? <Tag color="green">{text}</Tag> : <Text type="secondary">Not assigned</Text>,
                                },
                            ]}
                            pagination={false}
                            size="small"
                            bordered
                            style={{ marginTop: 16 }}
                        />
                    </div>
                </Modal>
            </div >
        </RmaLayout >
    );
}