import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
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
    Select,
    Tabs,
    Collapse,
    Tooltip
} from "antd";
import {
    WarningOutlined,
    ExclamationCircleOutlined,
    SwapOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    BarcodeOutlined,
    InfoCircleOutlined,
    CheckCircleOutlined,
    HistoryOutlined,
    UserOutlined,
    CalendarOutlined
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA";
import RmaLayout from "../RMA/RmaLayout";
import Cookies from "js-cookie";
import "./UnrepairedPage.css"; // Ensure this contains the modern CSS classes
import { URL } from "../API/URL";

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;
const { Panel } = Collapse;

export default function CantBeRepairedPage() {
    const [pendingItems, setPendingItems] = useState([]);
    const [replacedItems, setReplacedItems] = useState([]);
    const [activeTab, setActiveTab] = useState("1");
    const [loading, setLoading] = useState(false);
    const [sortOption, setSortOption] = useState("date_desc"); // Default: Newest first
    const [activeKeys, setActiveKeys] = useState([]); // Persistent collapse state
    const location = useLocation();

    useEffect(() => {
        if (location.state?.highlightRma) {
            setActiveKeys((prev) => [...new Set([...prev, location.state.highlightRma])]);
            
            if (location.state?.highlightItemId && (pendingItems.length > 0 || replacedItems.length > 0)) {
                setTimeout(() => {
                    const element = document.getElementById(`item-card-${location.state.highlightItemId}`);
                    if (element) {
                        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        element.style.border = '2px solid #ff4d4f'; // Red for BER
                        element.style.transition = 'all 0.3s';
                        setTimeout(() => { 
                            element.style.border = ''; 
                            element.style.transition = '';
                        }, 3000);
                    }
                }, 800);
            }
        }
    }, [location.state, pendingItems, replacedItems]);

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
        try {
            const result = await RmaApi.getCantBeRepairedItems();
            if (result.success) {
                const allItems = result.data || [];
                // Pending: repairStatus !== 'REPLACED' (and usually BER in this context, but API might return others)
                // Filter out BER if they are handled elsewhere? No, CantBeRepaired page is specifically for BER/Replacement flow.
                // However, previous code filtered OUT "BER" items? 
                // Line 61: const filteredItems = (result.data || []).filter(item => item.repairStatus !== "BER");
                // Wait, if BER items are moved to RepairedPage, then what is left here?
                // Let's re-read line 61 of original file: "const filteredItems = (result.data || []).filter(item => item.repairStatus !== "BER");"
                // This is confusing. If I filter out BER, and the page is "Can't Be Repaired"...
                // Ah, maybe they are "UNREPAIRED" or something?
                // Let's look at RepairedPage.js loadItems:
                /* 
                if (cantRepairedResult.success) {
                    const berItems = (cantRepairedResult.data || []).filter(item =>
                        item.repairStatus === "BER" && item.isDispatched !== true
                    );
                    allLocalItems = [...allLocalItems, ...berItems];
                }
                */
                // So "BER" items ARE in RepairedPage too.
                // In CantBeRepairedPage, we process them. Once processed, they might become "REPLACED".

                const pending = allItems.filter(item => item.repairStatus !== 'REPLACED');
                const replaced = allItems.filter(item => item.repairStatus === 'REPLACED');

                setPendingItems(pending);
                setReplacedItems(replaced);
            } else {
                message.error("Failed to load items");
            }
        } catch (error) {
            console.error("Load items error:", error);
            message.error("Failed to load items");
        } finally {
            setLoading(false);
        }
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

    // Grouping logic
    const groupItemsByRma = (itemsList) => {
        return itemsList.reduce((acc, item) => {
            const rmaNo = item.rmaNo || "Unknown";
            if (!acc[rmaNo]) acc[rmaNo] = [];
            acc[rmaNo].push(item);
            return acc;
        }, {});
    };

    const groupedPending = groupItemsByRma(pendingItems);
    const groupedReplaced = groupItemsByRma(replacedItems);

    const totalPendingRma = Object.keys(groupedPending).length;
    const totalPendingItems = pendingItems.length;
    const totalReplacedItems = replacedItems.length;



    const getSortedGroups = (groups, isHistory = false) => {
        const groupsArray = Object.entries(groups);
        const dateKey = isHistory ? 'repairedDate' : 'receivedDate';

        return groupsArray.sort((a, b) => {
            const itemsA = a[1];
            const itemsB = b[1];
            // Use first item of group for comparison
            const itemA = itemsA[0] || {};
            const itemB = itemsB[0] || {};

            switch (sortOption) {
                case "date_desc":
                    // Most recent first
                    return new Date(itemB[dateKey] || 0) - new Date(itemA[dateKey] || 0);

                case "date_asc":
                    // Oldest first
                    return new Date(itemA[dateKey] || 0) - new Date(itemB[dateKey] || 0);

                case "customer_asc":
                    // A-Z
                    const nameA = (itemA.userName || itemA.companyName || "").toLowerCase();
                    const nameB = (itemB.userName || itemB.companyName || "").toLowerCase();
                    return nameA.localeCompare(nameB);

                default:
                    return 0;
            }
        });
    };

    const renderRmaCollapse = (groupedData, isHistory = false) => {
        if (Object.keys(groupedData).length === 0) {
            return <Empty description={isHistory ? "No replacement history found" : "No pending replacements found"} />;
        }

        const sortedGroups = getSortedGroups(groupedData, isHistory);

        return (
            <Collapse
                className="rma-collapse"
                activeKey={activeKeys}
                onChange={setActiveKeys}
                expandIconPosition="end"
                ghost
            >
                {sortedGroups.map(([rmaNo, rmaItems]) => {
                    const firstItem = rmaItems[0];
                    const createdDate = firstItem.receivedDate ? new Date(firstItem.receivedDate).toLocaleDateString() : "N/A";
                    const itemCount = rmaItems.length;

                    const headerContent = (
                        <div className="rma-collapse-header" style={{ width: '100%', padding: '4px 0' }}>
                            <Row gutter={[16, 16]} align="middle" style={{ width: '100%' }}>
                                <Col xs={12} sm={12} md={8} lg={7} xl={6}>
                                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                                        <Title level={5} style={{ margin: 0, color: 'var(--color-error)' }}>
                                            {rmaNo !== "Unknown" ? rmaNo : "No RMA #"}
                                        </Title>
                                        <Text type="secondary" style={{ fontSize: '12px' }}>
                                            <span role="img" aria-label="user">👤</span> {firstItem.userName || "User"}
                                        </Text>
                                        <Text type="secondary" style={{ fontSize: '12px' }}>
                                            <span role="img" aria-label="customer">🏢Customer ::</span> {firstItem.companyName || "Unknown Customer"}
                                        </Text>
                                    </div>
                                </Col>
                                <Col xs={12} sm={12} md={5} lg={5} xl={4}>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                        <Text type="secondary" style={{ fontSize: '11px' }}>Received Date</Text>
                                        <Text strong>{createdDate}</Text>
                                    </div>
                                </Col>
                                <Col xs={12} sm={8} md={3} lg={3} xl={2}>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                        <Text type="secondary" style={{ fontSize: '11px' }}>Items</Text>
                                        <Badge count={itemCount} style={{ backgroundColor: isHistory ? 'var(--status-success)' : 'var(--status-error)' }} showZero />
                                    </div>
                                </Col>
                                {!isHistory && (
                                    <Col xs={24} sm={24} md={8} lg={9} xl={12} style={{ display: 'flex', justifyContent: 'flex-end', paddingRight: '24px' }}>
                                        <Tag color="error" icon={<WarningOutlined />}>REPLACEMENT REQUIRED</Tag>
                                    </Col>
                                )}
                                {isHistory && (
                                    <Col xs={24} sm={24} md={8} lg={9} xl={12} style={{ display: 'flex', justifyContent: 'flex-end', paddingRight: '24px' }}>
                                        <Tag color="success" icon={<CheckCircleOutlined />}>HISTORY</Tag>
                                    </Col>
                                )}
                            </Row>
                        </div>
                    );

                    return (
                        <Panel
                            key={rmaNo}
                            header={headerContent}
                            className="rma-panel"
                        >
                            <div className="rma-items-grid">
                                {rmaItems.map((item) => (
                                    <div key={item.id} id={`item-card-${item.id}`} className="rma-item-card-ber">
                                        <div className="item-header">
                                            <span className="item-product">{item.product || "Product"}</span>
                                            {isHistory ? (
                                                <Tag color="cyan" icon={<ReloadOutlined />}>REPLACED</Tag>
                                            ) : (
                                                <Tag color="red" icon={<WarningOutlined />}>BER</Tag>
                                            )}
                                        </div>

                                        <div className="item-details-grid">
                                            <div className="detail-box">
                                                <span className="label"><BarcodeOutlined /> Serial No</span>
                                                <span className="value monospace">{item.serialNo}</span>
                                            </div>
                                            <div className="detail-box">
                                                <span className="label">Model</span>
                                                <span className="value">{item.model}</span>
                                            </div>
                                            <div className="detail-box">
                                                <span className="label"><UserOutlined /> Technician</span>
                                                <span className="value">{item.repairedByName || item.assignedToName || "N/A"}</span>
                                            </div>
                                            <div className="detail-box">
                                                <span className="label"><CalendarOutlined /> Date</span>
                                                <span className="value">{item.repairedDate ? new Date(item.repairedDate).toLocaleDateString() : "N/A"}</span>
                                            </div>
                                        </div>

                                        <div className="fault-box">
                                            <span className="label">BER Remarks</span>
                                            <p className="fault-desc">{item.repairRemarks || "No remarks provided."}</p>
                                        </div>

                                        {isHistory && (item.replacementSerial || (item.repairRemarks && item.repairRemarks.includes("| Replaced with unit:"))) && (
                                            <div className="fault-box" style={{ borderTop: 'none', background: '#f6ffed', borderColor: '#b7eb8f' }}>
                                                <span className="label" style={{ color: '#52c41a' }}><SwapOutlined /> Replacement Serial</span>
                                                <p className="fault-desc" style={{ fontWeight: 'bold', color: '#389e0d' }}>
                                                    {item.replacementSerial || (item.repairRemarks ? item.repairRemarks.split("| Replaced with unit:")[1]?.trim() : "")}
                                                </p>
                                            </div>
                                        )}

                                        <div className="item-footer">
                                            {!isHistory ? (
                                                <Tooltip title="Match this item with a new unit from inventory">
                                                    <Button
                                                        type="primary"
                                                        danger
                                                        block
                                                        icon={<SwapOutlined />}
                                                        onClick={() => handleProcessClick(item)}
                                                    >
                                                        Process Replacement
                                                    </Button>
                                                </Tooltip>
                                            ) : (
                                                <Tooltip title="RMA Number for this replacement">
                                                    <div style={{ textAlign: 'center' }}>
                                                        <span style={{ fontSize: '11px', color: '#8c8c8c', textTransform: 'uppercase' }}>RMA #: </span>
                                                        <Tag color="blue">{item.itemRmaNo || item.rmaNo || "N/A"}</Tag>
                                                    </div>
                                                </Tooltip>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </Panel>
                    );
                })}
            </Collapse>
        );
    };

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section */}
                <div className="unrepaired-header header-cant-repair">
                    <div className="header-content">
                        <div className="header-title">
                            <WarningOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0 }}>Can't Be Repaired</Title>
                                <Text type="secondary">Process replacements for Beyond Economic Repair (BER) items</Text>
                            </div>
                        </div>
                        <Select
                            value={sortOption}
                            onChange={setSortOption}
                            className="header-select"
                            style={{ marginRight: 8 }}
                            options={[
                                { value: "date_desc", label: "Date: Newest First" },
                                { value: "date_asc", label: "Date: Oldest First" },
                                { value: "customer_asc", label: "Customer: A-Z" },
                            ]}
                        />
                    </div>

                    <Row gutter={16} className="stats-row">
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <ExclamationCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalPendingRma}</div>
                                    <div className="stat-label">Affected RMAs</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <SwapOutlined />
                                <div>
                                    <div className="stat-value">{totalPendingItems}</div>
                                    <div className="stat-label">Pending Replacement</div>
                                </div>
                            </div>
                        </Col>
                        {totalReplacedItems > 0 && (
                            <Col xs={12} sm={8}>
                                <div className="stat-box">
                                    <HistoryOutlined />
                                    <div>
                                        <div className="stat-value">{totalReplacedItems}</div>
                                        <div className="stat-label">Total Replaced</div>
                                    </div>
                                </div>
                            </Col>
                        )}
                    </Row>
                </div>

                <div className="unrepaired-content">
                    <Tabs
                        activeKey={activeTab}
                        onChange={setActiveTab}
                        items={[
                            {
                                key: "1",
                                label: (
                                    <span>
                                        <WarningOutlined /> Pending Replacement ({totalPendingItems})
                                    </span>
                                ),
                                children: loading ? (
                                    <div className="loading-container"><Spin size="large" /></div>
                                ) : renderRmaCollapse(groupedPending)
                            },
                            {
                                key: "2",
                                label: (
                                    <span>
                                        <HistoryOutlined /> Process History ({totalReplacedItems})
                                    </span>
                                ),
                                children: loading ? (
                                    <div className="loading-container"><Spin size="large" /></div>
                                ) : renderRmaCollapse(groupedReplaced, true)
                            }
                        ]}
                    />
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
                    width={700}
                >
                    <div style={{ padding: '10px 0' }}>
                        <div className="fault-box" style={{ margin: '0 0 24px 0' }}>
                            <Text strong style={{ color: 'var(--color-warning)' }}>Action: Replacement</Text><br />
                            <Text>Original Item from RMA <strong>{selectedItem.rmaNo}</strong> (Model: {selectedItem.modelNo}) will be marked as REPLACED.</Text>
                        </div>

                        <div style={{ marginBottom: 24 }}>
                            <Text strong style={{ fontSize: 16 }}>Search Replacement Item:</Text>
                            <Text style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-color-secondary)' }}>
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
                                        <div style={{ padding: '8px 0', borderBottom: '1px solid var(--border-color)' }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                                                <Text strong style={{ fontSize: 14 }}>
                                                    {d.keywordEntity?.keywordName || 'Unknown Product'}
                                                </Text>
                                                <Tag color={d.availableStatusId?.itemAvailableOption?.toLowerCase() === 'available' ? 'green' : 'orange'}>
                                                    {d.availableStatusId?.itemAvailableOption || 'Unknown'}
                                                </Tag>
                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: '#666', marginBottom: 2 }}>
                                                <span><strong>SN:</strong> {d.serial_No || 'N/A'}</span>
                                                <span><strong>Model:</strong> {d.modelNo || 'N/A'}</span>
                                            </div>
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
