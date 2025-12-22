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
    SafetyCertificateOutlined,
    BarcodeOutlined,
    InfoCircleOutlined
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import Cookies from "js-cookie";
import "./UnrepairedPage.css"; // Ensure this contains the modern CSS classes
import { URL } from "../API/URL";

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
        productName: "", // Product name from RMA item for filtering
        replacementSerial: null // Add tracking for selected replacement
    });

    // --- SEARCH STATE ---
    const [searchOptions, setSearchOptions] = useState([]);
    const [searchLoading, setSearchLoading] = useState(false);

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getCantBeRepairedItems();
        if (result.success) {
            // Filter out BER items (they are now moved to Local Repaired / Local Dispatch)
            const filteredItems = (result.data || []).filter(item => item.repairStatus !== "BER");
            setItems(filteredItems);
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
        const productName = item.product || ""; // Store product name for filtering
        setSelectedItem({
            rmaNo: item.rmaNo || item.itemRmaNo,
            modelNo: item.model,
            productName: productName, // Store for filtering replacement items
            replacementSerial: null // Reset
        });
        setSearchOptions([]); // Clear previous searches
        setIsReplaceModalVisible(true);

        // Auto-search for the model number initially
        if (item.model) {
            handleSearch(item.model, productName);
        }
    };

    // --- SEARCH API CALL ---
    const handleSearch = async (value) => {
        if (!value || value.length < 2) return;
        setSearchLoading(true);
        try {
            const token = atob(Cookies.get("authToken") || "");

            // Use the simplified search endpoint
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
                message.error("Search failed");
            }
        } catch (error) {
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

    // Group items
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
                {/* Header Section - Red Theme for Critical/Replacement */}
                <div className="unrepaired-header header-cant-repair">
                    <div className="header-content">
                        <div className="header-title">
                            <WarningOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0 }}>
                                    Can't Be Repaired
                                </Title>
                                <Text type="secondary">
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
                                <Card key={rmaNo} className="rma-group-card"
                                    title={
                                        <div className="rma-card-header-flex">
                                            <div className="rma-identity">
                                                <div className="rma-id-box">
                                                    <span className="rma-label">RMA Request</span>
                                                    <span className="rma-value">{rmaNo}</span>
                                                </div>
                                                <Badge count={rmaItems.length} />
                                            </div>
                                        </div>
                                    }
                                >
                                    
                                    {/* MODERN CARD GRID LAYOUT */}
                                    <div className="rma-items-grid">
                                        {rmaItems.map((item) => (
                                            <div key={item.id} className="rma-item-card-modern">
                                                {/* Header Strip */}
                                                <div className="item-header">
                                                    <span className="item-product">{item.product || "Product"}</span>
                                                    <Tag color="red" icon={<WarningOutlined />}>REPLACE</Tag>
                                                </div>

                                                {/* Details Grid */}
                                                <div className="item-details-grid">
                                                    <div className="detail-box">
                                                        <span className="label"><BarcodeOutlined/> Serial No</span>
                                                        <span className="value monospace">{item.serialNo || "N/A"}</span>
                                                    </div>
                                                    <div className="detail-box">
                                                        <span className="label">Model</span>
                                                        <span className="value">{item.model || "N/A"}</span>
                                                    </div>
                                                    <div className="detail-box">
                                                        <span className="label">RMA No</span>
                                                        <Tag color="red">{item.itemRmaNo || "N/A"}</Tag>
                                                    </div>
                                                </div>

                                                {/* Fault/Remarks Box */}
                                                <div className="fault-box">
                                                    <span className="label">Remarks</span>
                                                    <p className="fault-desc">{item.repairRemarks || "No remarks provided"}</p>
                                                </div>

                                                {/* Footer Actions */}
                                                <div className="item-footer">
                                                    <Button 
                                                        type="primary" 
                                                        block 
                                                        icon={<SwapOutlined />} 
                                                        onClick={() => handleProcessClick(item)}
                                                    >
                                                        Process Replacement
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    {/* END MODERN CARD GRID */}
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
                    <div className="fault-box" style={{margin: '0 0 24px 0'}}>
                            <Text strong>Action: Replacement</Text><br />
                            <Text type="secondary">Original Item from RMA <strong>{selectedItem.rmaNo}</strong> (Model: {selectedItem.modelNo}) will be marked as REPLACED.</Text>
                        </div>

                        <div>
                            <Text strong>Search Replacement Item:</Text>
                            <Text type="secondary" style={{ display: 'block', marginBottom: 8, fontSize: 12 }}>
                                Search by Model No, Part No, or Serial No. Select an item with 'AVAILABLE' status.
                            </Text>

                            <Select
                                showSearch
                                value={selectedItem.replacementSerial}
                                placeholder="Type to search (e.g. Model, Serial, Part No)"
                                style={{ width: '100%' }}
                                defaultActiveFirstOption={false}
                                showArrow={false}
                                filterOption={false}
                                onSearch={handleSearch}
                                onChange={(val) => setSelectedItem({ ...selectedItem, replacementSerial: val })}
                                notFoundContent={searchLoading ? <Spin size="small" /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No items found" />}
                                loading={searchLoading}
                                size="large"
                                listHeight={300}
                            >
                                {searchOptions.map((d) => (
                                    <Option key={d.serial_No} value={d.serial_No} disabled={d.availableStatusId?.itemAvailableOption?.toLowerCase() !== 'available'}>
                                        <div style={{ padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
                                            {/* Row 1: Product & Status */}
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                                                <Text strong style={{ fontSize: 14 }}>
                                                    {d.keywordEntity?.keywordName || 'Unknown Product'}
                                                </Text>
                                                <Tag color={d.availableStatusId?.itemAvailableOption?.toLowerCase() === 'available' ? 'green' : 'orange'}>
                                                    {d.availableStatusId?.itemAvailableOption || 'Unknown'}
                                                </Tag>
                                            </div>
                                            {/* Row 2: Serial No & Model */}
                                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: '#666', marginBottom: 2 }}>
                                                <span><strong>SN:</strong> {d.serial_No || 'N/A'}</span>
                                                <span><strong>Model:</strong> {d.modelNo || 'N/A'}</span>
                                            </div>
                                            {/* Row 3: Region */}
                                            <div style={{ fontSize: 11, color: '#888' }}>
                                                <strong>Region:</strong> {d.region?.city || 'N/A'}
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