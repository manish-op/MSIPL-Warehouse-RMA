import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Spin,
    Card,
    Row,
    Col,
    Badge,
    Empty,
    Divider,
    Button,
    Modal,
    Table,
} from "antd";
import {
    CheckCircleOutlined,
    TrophyOutlined,
    ReloadOutlined,
    EyeOutlined,
    DownloadOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

export default function RepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [generatingGatepass, setGeneratingGatepass] = useState(null);
    const [previewVisible, setPreviewVisible] = useState(false);
    const [previewData, setPreviewData] = useState({ rmaNo: "", items: [] });

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getRepairedItems();
        if (result.success) {
            // Filter for Local Repair items only (as per requirement)
            const localItems = (result.data || []).filter(item => item.repairType === 'LOCAL');
            setItems(localItems);
        } else {
            message.error("Failed to load repaired items");
        }
        setLoading(false);
    };

    useEffect(() => {
        loadItems();
    }, []);

    // Open Preview Modal
    const openPreview = (rmaItems, rmaNo) => {
        setPreviewData({ rmaNo, items: rmaItems });
        setPreviewVisible(true);
    };

    // Confirm and Generate Outward Gatepass PDF
    const confirmGenerateGatepass = async () => {
        const { rmaNo } = previewData;
        if (!rmaNo || rmaNo === "Unknown") {
            message.error("Cannot generate Gatepass: Invalid RMA Number.");
            return;
        }
        setGeneratingGatepass(rmaNo);
        const result = await RmaApi.generateOutwardGatepass(rmaNo);
        if (result.success && result.blob) {
            // Create download link
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `OutwardGatepass_${rmaNo}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('Outward Gatepass generated and downloaded!');
            setPreviewVisible(false); // Close modal on success
        } else {
            message.error(result.error || 'Failed to generate gatepass');
        }
        setGeneratingGatepass(null);
    };

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

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section - Green Theme for Repaired */}
                <div className="unrepaired-header" style={{ background: "linear-gradient(135deg, #52c41a 0%, #389e0d 100%)", boxShadow: "0 4px 20px rgba(82, 196, 26, 0.3)" }}>
                    <div className="header-content">
                        <div className="header-title">
                            <CheckCircleOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Local Repaired Items
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items repaired locally and ready for dispatch
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
                                <TrophyOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">Pending Dispatch</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <CheckCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Ready Items</div>
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
                            description="No local repaired items found"
                            className="empty-state"
                        />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card
                                    key={rmaNo}
                                    className="rma-group-card"
                                    style={{ borderLeft: "4px solid #52c41a" }}
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number">
                                                <Tag color="#52c41a" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                            </span>
                                            <Badge
                                                count={rmaItems.length}
                                                style={{ backgroundColor: "#52c41a" }}
                                                overflowCount={99}
                                            />
                                        </div>
                                    }
                                    extra={
                                        <Button
                                            type="primary"
                                            // Pass items and the best available RMA Key (Request Number preferred for backend, fallback to Group Key)
                                            onClick={() => openPreview(rmaItems, rmaItems[0].rmaRequest?.requestNumber || rmaNo)}
                                            style={{ backgroundColor: "#135200", borderColor: "#135200" }}
                                        >
                                            Generate Outward Gatepass
                                        </Button>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card
                                                    className="item-card"
                                                    size="small"
                                                    hoverable
                                                    style={{ borderTop: "3px solid #52c41a" }}
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
                                                            <Text type="secondary">Repaired By</Text>
                                                            <Text>{item.repairedByName || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Repair Date</Text>
                                                            <Text>{item.repairedDate ? new Date(item.repairedDate).toLocaleDateString() : "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Status</Text>
                                                            <Tag color="green" icon={<CheckCircleOutlined />}>
                                                                REPAIRED
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
                                                            <Text type="secondary">Repair Remarks</Text>
                                                            <Paragraph
                                                                ellipsis={{ rows: 2, expandable: true }}
                                                                className="fault-text"
                                                            >
                                                                {item.repairRemarks || "No remarks"}
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

                {/* Outward Gatepass Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <EyeOutlined style={{ color: "#1890ff" }} />
                            <span>Outward Gatepass Preview - {previewData.rmaNo}</span>
                        </div>
                    }
                    open={previewVisible}
                    onCancel={() => setPreviewVisible(false)}
                    width={900}
                    footer={[
                        <Button key="cancel" onClick={() => setPreviewVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="download"
                            type="primary"
                            icon={<DownloadOutlined />}
                            onClick={confirmGenerateGatepass}
                            loading={generatingGatepass === previewData.rmaNo}
                            style={{ backgroundColor: "#135200", borderColor: "#135200" }}
                        >
                            Confirm & Generate PDF
                        </Button>
                    ]}
                    className="gatepass-modal"
                >
                    <div className="gatepass-preview-container">
                        {/* Header Info */}
                        <Card size="small" className="gatepass-info-card" style={{ marginBottom: 16 }}>
                            <Row gutter={16}>
                                <Col span={8}>
                                    <Text type="secondary">RMA Number</Text>
                                    <div>
                                        <Tag color="green" style={{ fontSize: 14 }}>
                                            {previewData.rmaNo}
                                        </Tag>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Consignee (Customer)</Text>
                                    <div>
                                        <Text strong>{previewData.items[0]?.rmaRequest?.companyName || previewData.items[0]?.companyName || 'N/A'}</Text>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Total Items</Text>
                                    <div>
                                        <Badge
                                            count={previewData.items.length}
                                            style={{ backgroundColor: "#52c41a" }}
                                            showZero
                                        />
                                    </div>
                                </Col>
                            </Row>
                        </Card>

                        {/* Items Table */}
                        <Table
                            dataSource={previewData.items.map((item, index) => ({ ...item, key: item.id || index, slNo: index + 1 }))}
                            columns={[
                                {
                                    title: 'Sl.No',
                                    dataIndex: 'slNo',
                                    key: 'slNo',
                                    width: 80,
                                    align: 'center',
                                },
                                {
                                    title: 'Product',
                                    dataIndex: 'product',
                                    key: 'product',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Serial No',
                                    dataIndex: 'serialNo',
                                    key: 'serialNo',
                                    render: (text) => <Text code>{text || 'N/A'}</Text>,
                                },
                                {
                                    title: 'Model',
                                    dataIndex: 'model',
                                    key: 'model',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Repair Remarks',
                                    dataIndex: 'repairRemarks',
                                    key: 'repairRemarks',
                                    ellipsis: true,
                                    render: (text) => text || 'N/A',
                                },
                            ]}
                            pagination={false}
                            size="small"
                            bordered
                        />
                    </div>
                </Modal>
            </div>
        </RmaLayout>
    );
}