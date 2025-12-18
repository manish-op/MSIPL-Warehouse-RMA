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
    Select
} from "antd";
import {
    WarningOutlined,
    ExclamationCircleOutlined,
    SwapOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import Cookies from "js-cookie";
import "./UnrepairedPage.css";
import { URL } from "../API/URL"; // Fixed path

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

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
        modelNo: "",
        replacementSerial: null // Add tracking for selected replacement
    });

    // --- SEARCH STATE ---
    const [searchOptions, setSearchOptions] = useState([]);
    const [searchLoading, setSearchLoading] = useState(false);

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
            rmaNo: item.rmaNo || item.itemRmaNo,
            modelNo: item.model,
            replacementSerial: null // Reset
        });
        setSearchOptions([]); // Clear previous searches
        setIsReplaceModalVisible(true);

        // Auto-search for the model number initially
        if (item.model) {
            handleSearch(item.model);
        }
    };

    // --- SEARCH API CALL ---
    const handleSearch = async (value) => {
        if (!value || value.length < 2) return;
        setSearchLoading(true);
        try {
            const token = atob(Cookies.get("authToken") || "");

            // Use the new simplified search endpoint
            const response = await fetch(`${URL}/replacement/search?query=${encodeURIComponent(value)}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                }
            });

            if (response.ok) {
                const data = await response.json();
                setSearchOptions(data);
            } else {
                console.error("Search failed", response.status);
                message.error("Search failed");
            }
        } catch (error) {
            console.error("Search error:", error);
            message.error("Search error");
        } finally {
            setSearchLoading(false);
        }
    };

    // --- SUBMIT REPLACEMENT ---
    const handleConfirmReplacement = async () => {
        if (!selectedItem.replacementSerial) {
            message.error("Please select a valid replacement item from the list.");
            return;
        }

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
                    modelNo: selectedItem.modelNo, // Still send model for logging/fallback
                    replacementSerial: selectedItem.replacementSerial // SEND THE SPECIFIC SERIAL
                })
            });

            // ... (rest of handling same as before)
            const responseText = await response.text();
            let data;
            try {
                data = JSON.parse(responseText);
            } catch (e) {
                console.error("JSON Parse Error:", e);
                throw new Error("Server returned non-JSON response");
            }

            if (response.ok) {
                message.success({
                    content: `Success! Assigned new Serial: ${data.newSerial}`,
                    icon: <SafetyCertificateOutlined style={{ color: '#52c41a' }} />,
                    duration: 5,
                });
                setIsReplaceModalVisible(false);
                loadItems();
            } else {
                setIsReplaceModalVisible(false);
                if (data.error === "OUT_OF_STOCK") {
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

    // Group items... (Keep existing logic)
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
                {/* ... (Header and Stats - Keep existing) ... */}
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
                        <Button icon={<ReloadOutlined />} onClick={loadItems} loading={loading} className="refresh-btn">
                            Refresh
                        </Button>
                    </div>

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
                        <div className="loading-container"><Spin size="large" /><Text style={{ marginTop: 16 }}>Loading items...</Text></div>
                    ) : totalRmaRequests === 0 ? (
                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No items requiring replacement" className="empty-state" />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card key={rmaNo} className="rma-group-card" style={{ borderLeft: "4px solid #f5222d" }}
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number"><Tag color="#f5222d" style={{ fontSize: 14, padding: "4px 12px" }}>RMA: {rmaNo}</Tag></span>
                                            <Badge count={rmaItems.length} style={{ backgroundColor: "#f5222d" }} overflowCount={99} />
                                        </div>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card className="item-card" size="small" hoverable style={{ borderTop: "3px solid #f5222d" }}
                                                    actions={[
                                                        <Button type="primary" icon={<SwapOutlined />} style={{ backgroundColor: "#722ed1", borderColor: "#722ed1" }} className="assign-btn" onClick={() => handleProcessClick(item)}>
                                                            Process Replacement
                                                        </Button>
                                                    ]}
                                                >
                                                    <div className="item-content">
                                                        <div className="item-row"><Text type="secondary">Product</Text><Text strong>{item.product || "N/A"}</Text></div>
                                                        <div className="item-row"><Text type="secondary">Serial No.</Text><Text code>{item.serialNo || "N/A"}</Text></div>
                                                        <div className="item-row"><Text type="secondary">Model</Text><Text>{item.model || "N/A"}</Text></div>
                                                        <div className="item-row"><Text type="secondary">Status</Text>
                                                            <Tag color="red" icon={<WarningOutlined />}>
                                                                {item.repairStatus === "CANT_BE_REPAIRED" ? "Can't Be Repaired" : item.repairStatus === "BER" ? "BER (Beyond Economic Repair)" : item.repairStatus || "Unknown Status"}
                                                            </Tag>
                                                        </div>
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section"><Text type="secondary">Remarks</Text><Paragraph ellipsis={{ rows: 2, expandable: true }} className="fault-text">{item.repairRemarks || "No remarks provided"}</Paragraph></div>
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
                    okText="Assign Replacement"
                    okButtonProps={{ style: { backgroundColor: '#722ed1' } }}
                    width={700} // Wider for logic table
                >
                    <div style={{ padding: '10px 0' }}>
                        <div style={{ marginBottom: 24, padding: '16px', background: '#fff1f0', border: '1px solid #ffa39e', borderRadius: '4px' }}>
                            <Text strong style={{ color: '#cf1322' }}>Action: Replacement</Text><br />
                            <Text type="secondary">Original Item from RMA <strong>{selectedItem.rmaNo}</strong> (Model: {selectedItem.modelNo}) will be marked as REPLACED.</Text>
                        </div>

                        <div>
                            <Text strong>Search Replacement Item:</Text>
                            <Text type="secondary" style={{ display: 'block', marginBottom: 8, fontSize: 12 }}>
                                Search by Model No, Part No, or Item Name. Select an 'AVAILABLE' item.
                            </Text>

                            <Select
                                showSearch
                                value={selectedItem.replacementSerial}
                                placeholder="Type to search (e.g. Model, Serial, Name)"
                                style={{ width: '100%' }}
                                defaultActiveFirstOption={false}
                                showArrow={false}
                                filterOption={false}
                                onSearch={handleSearch}
                                onChange={(val) => setSelectedItem({ ...selectedItem, replacementSerial: val })}
                                notFoundContent={searchLoading ? <Spin size="small" /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No items found" />}
                                loading={searchLoading}
                                size="large"
                                listHeight={250}
                            >
                                {searchOptions.map((d) => (
                                    <Option key={d.serial_No} value={d.serial_No} disabled={d.availableStatusId?.itemAvailableOption?.toLowerCase() !== 'available'}>
                                        <div style={{ display: 'flex', flexDirection: 'column', padding: '4px 0', borderBottom: '1px solid #f0f0f0' }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                <Text strong>{d.modelNo || d.partNo}</Text>
                                                <Tag color={d.availableStatusId?.itemAvailableOption?.toLowerCase() === 'available' ? 'green' : 'orange'}>
                                                    {d.availableStatusId?.itemAvailableOption || "Unknown"}
                                                </Tag>
                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#888' }}>
                                                <span>SN: {d.serial_No}</span>
                                                <span>{d.itemDescription || "No desc"}</span>
                                            </div>
                                        </div>
                                    </Option>
                                ))}
                            </Select>
                        </div>
                    </div>
                </Modal>

                {/* --- MODAL 2: OUT OF STOCK --- */}
                <Modal
                    title={<span style={{ color: '#f5222d' }}><WarningOutlined /> Out of Stock</span>}
                    open={isStockModalVisible}
                    onCancel={() => setIsStockModalVisible(false)}
                    footer={[
                        <Button key="close" onClick={() => setIsStockModalVisible(false)}>Close</Button>,
                        <Button key="order" type="primary" danger onClick={() => window.location.href = '/purchase-order/create'}>Create Purchase Order</Button>
                    ]}
                >
                    <div style={{ textAlign: 'center', padding: '20px 0' }}>
                        <ExclamationCircleOutlined style={{ fontSize: 48, color: '#f5222d', marginBottom: 16 }} />
                        <Paragraph style={{ fontSize: 16 }}>The part <strong>{selectedItem.modelNo}</strong> is not available in the inventory.</Paragraph>
                        <Paragraph type="secondary">You need to order a new unit before this replacement can be processed.</Paragraph>
                    </div>
                </Modal>

            </div>
        </RmaLayout>
    );
}   