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
    Modal, // Added Modal
    Input  // Added Input
} from "antd";
import {
    WarningOutlined,
    ExclamationCircleOutlined,
    SwapOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined // Icon for success
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import Cookies from "js-cookie";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

export default function CantBeRepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);

    // --- NEW STATE FOR POPUPS ---
    const [isReplaceModalVisible, setIsReplaceModalVisible] = useState(false);
    const [isStockModalVisible, setIsStockModalVisible] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState(false);
    
    // Tracks the specific item being worked on
    const [selectedItem, setSelectedItem] = useState({
        rmaNo: "",
        modelNo: ""
    });

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getCantBeRepairedItems();
        if (result.success) {
            setItems(result.data || []);
        } else {
            message.error("Failed to load items");
        }
        setLoading(false);
    };

    useEffect(() => {
        loadItems();
    }, []);

    // --- NEW HANDLER: Open the Input Modal ---
    const handleProcessClick = (item) => {
        setSelectedItem({
            rmaNo: item.rmaNo || item.itemRmaNo, // Handle variations in data naming
            modelNo: item.model
        });
        setIsReplaceModalVisible(true);
    };

    // --- NEW HANDLER: Submit to Spring Boot ---
    const handleConfirmReplacement = async () => {
        setConfirmLoading(true);
        try {
            const token = atob(Cookies.get("authToken") || "");
            const response = await fetch('/api/replacement/process', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    rmaNumber: selectedItem.rmaNo,
                    modelNo: selectedItem.modelNo
                })
            });

            const responseText = await response.text();
            console.log("Raw Response:", responseText); // Debugging
            
            let data;
            try {
                data = JSON.parse(responseText);
            } catch (e) {
                // If parsing fails, it's likely HTML error page
                console.error("JSON Parse Error:", e);
                throw new Error("Server returned non-JSON response: " + response.status);
            }

            if (response.ok) {
                // SUCCESS
                message.success({
                    content: `Success! Assigned new Serial: ${data.newSerial}`,
                    icon: <SafetyCertificateOutlined style={{ color: '#52c41a' }} />,
                    duration: 5,
                });
                setIsReplaceModalVisible(false);
                loadItems(); // Refresh list to remove the processed item
            } else {
                // FAILURE
                setIsReplaceModalVisible(false); // Close input modal

                if (data.error === "OUT_OF_STOCK") {
                    // Open Red Warning Modal
                    setIsStockModalVisible(true);
                } else {
                    message.error("Error: " + (data.error || "Unknown error"));
                }
            }
        } catch (error) {
            console.error(error);
            message.error("System connection error");
        } finally {
            setConfirmLoading(false);
        }
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
                {/* Header Section */}
                <div className="unrepaired-header" style={{ background: "linear-gradient(135deg, #f5222d 0%, #cf1322 100%)", boxShadow: "0 4px 20px rgba(245, 34, 45, 0.3)" }}>
                    <div className="header-content">
                        <div className="header-title">
                            <WarningOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Can't Be Repaired
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items requiring replacement
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
                                <ExclamationCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">Affected RMAs</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <SwapOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Need Replacement</div>
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
                            description="No items requiring replacement"
                            className="empty-state"
                        />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card
                                    key={rmaNo}
                                    className="rma-group-card"
                                    style={{ borderLeft: "4px solid #f5222d" }}
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number">
                                                <Tag color="#f5222d" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                            </span>
                                            <Badge
                                                count={rmaItems.length}
                                                style={{ backgroundColor: "#f5222d" }}
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
                                                    style={{ borderTop: "3px solid #f5222d" }}
                                                    actions={[
                                                        <Button
                                                            type="primary"
                                                            icon={<SwapOutlined />}
                                                            style={{ backgroundColor: "#722ed1", borderColor: "#722ed1" }}
                                                            className="assign-btn"
                                                            // --- CONNECTED BUTTON ---
                                                            onClick={() => handleProcessClick(item)}
                                                        >
                                                            Process Replacement
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
                                                            <Text type="secondary">Model</Text>
                                                            <Text>{item.model || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Status</Text>
                                                            <Tag color="red" icon={<WarningOutlined />}>
                                                                {item.repairStatus === "CANT_BE_REPAIRED" ? "Can't Be Repaired" : 
                                                                 item.repairStatus === "BER" ? "BER (Beyond Economic Repair)" : 
                                                                 item.repairStatus || "Unknown Status"}
                                                            </Tag>
                                                        </div>
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section">
                                                            <Text type="secondary">Remarks</Text>
                                                            <Paragraph ellipsis={{ rows: 2, expandable: true }} className="fault-text">
                                                                {item.repairRemarks || "No remarks provided"}
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

                {/* --- MODAL 1: CONFIRM REPLACEMENT --- */}
                <Modal
                    title="Process Replacement"
                    open={isReplaceModalVisible}
                    onCancel={() => setIsReplaceModalVisible(false)}
                    onOk={handleConfirmReplacement}
                    confirmLoading={confirmLoading}
                    okText="Check Inventory & Assign"
                    okButtonProps={{ style: { backgroundColor: '#722ed1' } }}
                >
                    <div style={{ padding: '10px 0' }}>
                        <div style={{ marginBottom: 16 }}>
                            <Text strong>RMA Number:</Text>
                            <Input value={selectedItem.rmaNo} disabled style={{ marginTop: 8 }} />
                        </div>
                        <div>
                            <Text strong>Required Model:</Text>
                            <Input 
                                value={selectedItem.modelNo} 
                                onChange={(e) => setSelectedItem({...selectedItem, modelNo: e.target.value})}
                                style={{ marginTop: 8 }} 
                            />
                            <Text type="secondary" style={{ fontSize: 12 }}>
                                Confirm the model number matches the inventory.
                            </Text>
                        </div>
                    </div>
                </Modal>

                {/* --- MODAL 2: OUT OF STOCK WARNING --- */}
                <Modal
                    title={<span style={{ color: '#f5222d' }}><WarningOutlined /> Out of Stock</span>}
                    open={isStockModalVisible}
                    onCancel={() => setIsStockModalVisible(false)}
                    footer={[
                        <Button key="close" onClick={() => setIsStockModalVisible(false)}>
                            Close
                        </Button>,
                        <Button 
                            key="order" 
                            type="primary" 
                            danger 
                            onClick={() => window.location.href = '/purchase-order/create'}
                        >
                            Create Purchase Order
                        </Button>
                    ]}
                >
                    <div style={{ textAlign: 'center', padding: '20px 0' }}>
                        <ExclamationCircleOutlined style={{ fontSize: 48, color: '#f5222d', marginBottom: 16 }} />
                        <Paragraph style={{ fontSize: 16 }}>
                            The part <strong>{selectedItem.modelNo}</strong> is not available in the inventory.
                        </Paragraph>
                        <Paragraph type="secondary">
                            You need to order a new unit before this replacement can be processed.
                        </Paragraph>
                    </div>
                </Modal>

            </div>
        </RmaLayout>
    );
}   