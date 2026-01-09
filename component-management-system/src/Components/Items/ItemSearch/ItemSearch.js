import React, { useState, useEffect, useCallback } from "react";
import {
    Card,
    Tabs,
    Form,
    Input,
    Button,
    Select,
    Row,
    Col,
    Table,
    Space,
    Modal,
    message,
    Empty,
    Spin,
    Typography,
    Tag,
    Timeline,
} from "antd";
import {
    SearchOutlined,
    PrinterOutlined,
    EditOutlined,
    HistoryOutlined,
    ClearOutlined,
    NumberOutlined,
    FilterOutlined,
    ClockCircleOutlined,
    SwapOutlined,
    FileSearchOutlined,
} from "@ant-design/icons";
import Cookies from "js-cookie";
import { useNavigate } from "react-router-dom";
import { useItemDetails } from "../UpdateItem/ItemContext";
import { URL } from "../../API/URL";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import GetKeywordAPI from "../../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import GetItemStatusOptionAPI from "../../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import GetItemAvailabilityStatusOptionAPI from "../../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import GetItemHistoryBySerialNoAPI from "../../API/ItemRelatedApi/ItemHistory/GetItemHistoryBySerialNoAPI";
import UtcToISO from "../../UtcToISO";
import "./ItemSearch.css";

const { Title, Text } = Typography;
const { Option } = Select;

// Storage keys
const STORAGE_KEYS = {
    SEARCH_RESULTS: "itemSearchResults",
    SEARCH_VALUES: "itemSearchValues",
    RECENT_SEARCHES: "recentSerialSearches",
    ACTIVE_TAB: "itemSearchActiveTab",
};

const MAX_RECENT_SEARCHES = 5;

function ItemSearch() {
    const [quickForm] = Form.useForm();
    const [advancedForm] = Form.useForm();
    const [historyForm] = Form.useForm();
    const navigate = useNavigate();
    const token = atob(Cookies.get("authToken") || "");
    const role = localStorage.getItem("_User_role_for_MSIPL");
    const { setItemDetails } = useItemDetails();

    // Tab state
    const [activeTab, setActiveTab] = useState(() => {
        return sessionStorage.getItem(STORAGE_KEYS.ACTIVE_TAB) || "quick";
    });

    // Data states
    const [regions, setRegions] = useState([]);
    const [keywords, setKeywords] = useState([]);
    const [subkeywords, setSubKeywords] = useState([]);
    const [itemAvailabilityOptions, setItemAvailabilityOptions] = useState([]);
    const [itemStatusOptions, setItemStatusOptions] = useState([]);

    // Results states
    const [quickResult, setQuickResult] = useState(null);
    const [advancedResults, setAdvancedResults] = useState([]);
    const [historyResults, setHistoryResults] = useState([]);

    // Loading states
    const [quickLoading, setQuickLoading] = useState(false);
    const [advancedLoading, setAdvancedLoading] = useState(false);
    const [historyLoading, setHistoryLoading] = useState(false);

    // Modal states
    const [isChoiceModalVisible, setIsChoiceModalVisible] = useState(false);
    const [isRegionModalVisible, setIsRegionModalVisible] = useState(false);
    const [selectedSerialForUpdate, setSelectedSerialForUpdate] = useState(null);
    const [selectedRegionForUpdate, setSelectedRegionForUpdate] = useState(null);
    const [currentRegion, setCurrentRegion] = useState("N/A");
    const [regionUpdateLoading, setRegionUpdateLoading] = useState(false);

    // Recent searches
    const [recentSearches, setRecentSearches] = useState(() => {
        try {
            return JSON.parse(sessionStorage.getItem(STORAGE_KEYS.RECENT_SEARCHES)) || [];
        } catch {
            return [];
        }
    });

    // Fetch dropdown options on mount
    useEffect(() => {
        const fetchOptions = async () => {
            try {
                const [regionsData, keywordsData, availabilityData, statusData] = await Promise.all([
                    GetRegionAPI(),
                    GetKeywordAPI(),
                    GetItemAvailabilityStatusOptionAPI(),
                    GetItemStatusOptionAPI(),
                ]);
                setRegions(regionsData || []);
                setKeywords(keywordsData || []);
                setItemAvailabilityOptions(availabilityData || []);
                setItemStatusOptions(statusData || []);
            } catch (error) {
                // Silent fail
            }
        };
        fetchOptions();

        // Restore advanced search state
        const cachedResults = sessionStorage.getItem(STORAGE_KEYS.SEARCH_RESULTS);
        const cachedValues = sessionStorage.getItem(STORAGE_KEYS.SEARCH_VALUES);
        if (cachedResults && cachedValues) {
            try {
                setAdvancedResults(JSON.parse(cachedResults));
                const values = JSON.parse(cachedValues);
                advancedForm.setFieldsValue(values);
                if (values.keyword) {
                    handleKeywordChange(values.keyword);
                }
            } catch {
                // Silent fail
            }
        }
    }, [advancedForm]); // eslint-disable-line react-hooks/exhaustive-deps

    // Save active tab
    useEffect(() => {
        sessionStorage.setItem(STORAGE_KEYS.ACTIVE_TAB, activeTab);
    }, [activeTab]);

    // Handle keyword change for subkeyword cascade
    const handleKeywordChange = async (selectedKeyword) => {
        if (selectedKeyword) {
            try {
                const data = await GetSubKeywordAPI(selectedKeyword);
                if (data?.subKeywordList) {
                    setSubKeywords(data.subKeywordList.map((item) => item.subKeyword));
                } else {
                    setSubKeywords([]);
                }
            } catch {
                setSubKeywords([]);
            }
        } else {
            setSubKeywords([]);
            advancedForm.setFieldValue("subKeyword", undefined);
        }
    };

    // Add to recent searches
    const addToRecentSearches = useCallback((serialNo) => {
        setRecentSearches((prev) => {
            const filtered = prev.filter((s) => s !== serialNo);
            const updated = [serialNo, ...filtered].slice(0, MAX_RECENT_SEARCHES);
            sessionStorage.setItem(STORAGE_KEYS.RECENT_SEARCHES, JSON.stringify(updated));
            return updated;
        });
    }, []);

    // ========== QUICK SEARCH ==========
    const handleQuickSearch = async (values) => {
        if (!token) {
            navigate("/login");
            return;
        }
        setQuickLoading(true);
        setQuickResult(null);

        try {
            const response = await fetch(`${URL}/componentDetails/serialno`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: values.serialNo,
            });

            if (!response.ok) {
                const text = await response.text();
                message.warning(text || "Item not found", 2);
            } else {
                const data = await response.json();
                setQuickResult(data);
                setItemDetails(data);
                addToRecentSearches(values.serialNo);
            }
        } catch (error) {
            message.error("An error occurred while searching.");
        } finally {
            setQuickLoading(false);
        }
    };

    // ========== ADVANCED SEARCH ==========
    const handleAdvancedSearch = async (values) => {
        if (!token) {
            navigate("/login");
            return;
        }
        setAdvancedLoading(true);

        try {
            const response = await fetch(`${URL}/componentDetails/keyword`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(values),
            });

            const responseText = await response.text();
            if (!response.ok) {
                message.warning(responseText || "An error occurred.", 3);
            } else {
                const data = JSON.parse(responseText);
                setAdvancedResults(data);
                sessionStorage.setItem(STORAGE_KEYS.SEARCH_RESULTS, JSON.stringify(data));
                sessionStorage.setItem(STORAGE_KEYS.SEARCH_VALUES, JSON.stringify(values));
                if (data.length === 0) message.info("No items found.");
            }
        } catch (error) {
            message.error(`API Error: ${error.message}`);
        } finally {
            setAdvancedLoading(false);
        }
    };

    const handleAdvancedReset = () => {
        advancedForm.resetFields();
        setAdvancedResults([]);
        setSubKeywords([]);
        sessionStorage.removeItem(STORAGE_KEYS.SEARCH_RESULTS);
        sessionStorage.removeItem(STORAGE_KEYS.SEARCH_VALUES);
    };

    // ========== HISTORY SEARCH ==========
    const handleHistorySearch = async (values) => {
        setHistoryLoading(true);
        setHistoryResults([]);

        try {
            await GetItemHistoryBySerialNoAPI(
                values,
                (history) => {
                    setHistoryResults(history || []);
                    if (!history || history.length === 0) {
                        message.info("No history found for this serial number.");
                    }
                },
                navigate
            );
            addToRecentSearches(values.serialNo);
        } catch {
            message.error("Failed to fetch history.");
        } finally {
            setHistoryLoading(false);
        }
    };

    // ========== UPDATE HANDLERS ==========
    const handleUpdateClick = async (serialNo) => {
        try {
            const response = await fetch(`${URL}/componentDetails/serialno`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: serialNo,
            });
            if (!response.ok) throw new Error(await response.text());
            const data = await response.json();
            setItemDetails(data);
            navigate("/dashboard/updateItem", { state: { serialNo } });
        } catch (error) {
            message.error(`Error: ${error.message}`);
        }
    };

    const handleUpdateActionClick = (serialNo, item) => {
        const regionName = item?.region?.regionName || item?.region?.city || item?.region || "N/A";
        setCurrentRegion(regionName);
        setSelectedSerialForUpdate(serialNo);
        setIsChoiceModalVisible(true);
    };

    const proceedToFullUpdate = () => {
        setIsChoiceModalVisible(false);
        handleUpdateClick(selectedSerialForUpdate);
    };

    const proceedToRegionUpdate = () => {
        setIsChoiceModalVisible(false);
        setIsRegionModalVisible(true);
        setSelectedRegionForUpdate(currentRegion);
    };

    const handleRegionUpdateSubmit = async () => {
        if (!selectedRegionForUpdate) {
            message.error("Please select a region");
            return;
        }
        setRegionUpdateLoading(true);
        try {
            const response = await fetch(`${URL}/componentDetails/update-region-only`, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    serialNo: selectedSerialForUpdate,
                    newRegionName: selectedRegionForUpdate,
                }),
            });

            if (response.ok) {
                message.success("Region updated successfully!");
                setIsRegionModalVisible(false);
                // Refresh advanced search if we have criteria
                const currentValues = advancedForm.getFieldsValue();
                if (currentValues.keyword || currentValues.region || currentValues.partNo) {
                    handleAdvancedSearch(currentValues);
                }
            } else {
                message.error("Failed to update: " + await response.text());
            }
        } catch (error) {
            message.error("Error updating region: " + error.message);
        } finally {
            setRegionUpdateLoading(false);
        }
    };

    const handleHistoryClick = (serialNo) => {
        setActiveTab("history");
        historyForm.setFieldValue("serialNo", serialNo);
        handleHistorySearch({ serialNo });
    };

    const handlePrint = () => window.print();

    // ========== TABLE COLUMNS ==========
    const advancedColumns = [
        {
            title: "Serial No",
            dataIndex: "serial_No",
            key: "serial_No",
            fixed: "left",
            width: 170,
            render: (text, record) => (
                <div className="cell-content-wrapper">
                    <span className="serial-text">{text}</span>
                    <div className="overlay-actions">
                        <Space size="small">
                            <Button
                                className="action-btn-update"
                                size="small"
                                icon={<EditOutlined />}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleUpdateActionClick(record.serial_No, record);
                                }}
                            >
                                Update
                            </Button>
                            <Button
                                className="action-btn-history"
                                size="small"
                                icon={<HistoryOutlined />}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleHistoryClick(record.serial_No);
                                }}
                            >
                                History
                            </Button>
                        </Space>
                    </div>
                </div>
            ),
        },
        { title: "Part No", dataIndex: "partNo", key: "partNo", width: 120 },
        { title: "Keyword", dataIndex: ["keywordEntity", "keywordName"], key: "keyword", width: 150 },
        { title: "Sub Keyword", dataIndex: ["subKeyWordEntity", "subKeyword"], key: "subKeyword", width: 150 },
        { title: "Model No", dataIndex: "modelNo", key: "modelNo", width: 120 },
        { title: "System", dataIndex: "system", key: "system", width: 150 },
        { title: "Rack No", dataIndex: "rackNo", key: "rackNo", width: 100 },
        { title: "Box No", dataIndex: "boxNo", key: "boxNo", width: 100 },
        { title: "Party Name", dataIndex: "partyName", key: "partyName", width: 150 },
        { title: "Description", dataIndex: "itemDescription", key: "itemDescription", width: 200 },
        { title: "Status", dataIndex: ["itemStatusId", "itemStatus"], key: "itemStatus", width: 120 },
        { title: "Availability", dataIndex: ["availableStatusId", "itemAvailableOption"], key: "availableStatus", width: 150 },
        { title: "Updated By", dataIndex: "empEmail", key: "empEmail", width: 200 },
        {
            title: "Updated Date",
            dataIndex: "update_Date",
            key: "update_Date",
            render: (date) => (date ? UtcToISO(date) : "N/A"),
            width: 180,
        },
    ];

    // ========== TAB CONTENT ==========
    const QuickSearchTab = () => (
        <div className="search-tab-content">
            <div className="quick-search-container">
                <Form form={quickForm} onFinish={handleQuickSearch} layout="inline" className="quick-search-form">
                    <Form.Item
                        name="serialNo"
                        rules={[{ required: true, message: "Enter serial number" }]}
                        className="quick-search-input"
                    >
                        <Input
                            prefix={<NumberOutlined />}
                            placeholder="Enter Serial Number"
                            size="large"
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            icon={<SearchOutlined />}
                            size="large"
                            loading={quickLoading}
                        >
                            Search
                        </Button>
                    </Form.Item>
                </Form>

                {recentSearches.length > 0 && !quickResult && (
                    <div className="recent-searches">
                        <Text type="secondary">Recent:</Text>
                        <Space wrap>
                            {recentSearches.map((serial) => (
                                <Tag
                                    key={serial}
                                    className="recent-tag"
                                    onClick={() => {
                                        quickForm.setFieldValue("serialNo", serial);
                                        handleQuickSearch({ serialNo: serial });
                                    }}
                                >
                                    {serial}
                                </Tag>
                            ))}
                        </Space>
                    </div>
                )}
            </div>

            {quickLoading && (
                <div className="loading-container">
                    <Spin size="large" />
                </div>
            )}

            {quickResult && !quickLoading && (
                <Card className="quick-result-card">
                    <div className="quick-result-header">
                        <Title level={5}>Item Found</Title>
                        <Space>
                            <Button
                                type="primary"
                                icon={<EditOutlined />}
                                onClick={() => {
                                    const serialNo = quickResult.serial_No || quickResult.serialNo || quickForm.getFieldValue("serialNo");
                                    handleUpdateActionClick(serialNo, quickResult);
                                }}
                            >
                                Update
                            </Button>
                            <Button
                                icon={<HistoryOutlined />}
                                onClick={() => {
                                    const serialNo = quickResult.serial_No || quickResult.serialNo || quickForm.getFieldValue("serialNo");
                                    handleHistoryClick(serialNo);
                                }}
                            >
                                View History
                            </Button>
                        </Space>
                    </div>
                    <div className="quick-result-grid">
                        <div className="result-item">
                            <Text type="secondary">Serial No</Text>
                            <Text strong>{quickResult.serial_No || quickResult.serialNo || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Part No</Text>
                            <Text strong>{quickResult.partNo || quickResult.part_No || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Keyword</Text>
                            <Text strong>{quickResult.keywordEntity?.keywordName || quickResult.keyword || quickResult.keywordName || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Sub Keyword</Text>
                            <Text strong>{quickResult.subKeyWordEntity?.subKeyword || quickResult.subKeyword || quickResult.sub_keyword || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Model No</Text>
                            <Text strong>{quickResult.modelNo || quickResult.model_No || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">System</Text>
                            <Text strong>{quickResult.system || quickResult.systemName || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Module For</Text>
                            <Text strong>{quickResult.moduleFor || quickResult.module_for || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">System Version</Text>
                            <Text strong>{quickResult.systemVersion || quickResult.system_version || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Region</Text>
                            <Tag color="purple">{quickResult.region?.regionName || quickResult.region?.city || (typeof quickResult.region === 'string' ? quickResult.region : null) || quickResult.regionName || "N/A"}</Tag>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Rack No</Text>
                            <Text strong>{quickResult.rackNo || quickResult.rack_No || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Box No</Text>
                            <Text strong>{quickResult.boxNo || quickResult.box_No || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Spare Location</Text>
                            <Text strong>{quickResult.spareLocation || quickResult.spare_location || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Party Name</Text>
                            <Text strong>{quickResult.partyName || quickResult.party_name || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Status</Text>
                            <Tag color="blue">{quickResult.itemStatusId?.itemStatus || quickResult.itemStatus || quickResult.item_status || "N/A"}</Tag>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Availability</Text>
                            <Tag color="green">{quickResult.availableStatusId?.itemAvailableOption || quickResult.itemAvailability || quickResult.availableStatus || "N/A"}</Tag>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Updated By</Text>
                            <Text strong>{quickResult.empEmail || quickResult.emp_email || quickResult.updatedBy || "N/A"}</Text>
                        </div>
                        <div className="result-item">
                            <Text type="secondary">Last Updated</Text>
                            <Text strong>{quickResult.update_Date ? UtcToISO(quickResult.update_Date) : quickResult.updateDate ? UtcToISO(quickResult.updateDate) : "N/A"}</Text>
                        </div>
                        <div className="result-item full-width">
                            <Text type="secondary">Description</Text>
                            <Text strong>{quickResult.itemDescription || quickResult.description || quickResult.item_description || "N/A"}</Text>
                        </div>
                    </div>
                </Card>
            )}
        </div>
    );

    const AdvancedSearchTab = () => (
        <div className="search-tab-content">
            <Form form={advancedForm} onFinish={handleAdvancedSearch} layout="vertical">
                <Row gutter={[16, 0]}>
                    {role === "admin" && (
                        <Col xs={24} sm={12} md={6}>
                            <Form.Item label="Region" name="region" rules={[{ required: true, message: "Select region" }]}>
                                <Select placeholder="Select Region" showSearch allowClear optionFilterProp="children">
                                    {regions.map((r) => <Option key={r} value={r}>{r}</Option>)}
                                </Select>
                            </Form.Item>
                        </Col>
                    )}
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="Keyword" name="keyword">
                            <Select placeholder="Select Keyword" onChange={handleKeywordChange} showSearch allowClear optionFilterProp="children">
                                {keywords.map((k) => <Option key={k} value={k}>{k}</Option>)}
                            </Select>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="Sub Keyword" name="subKeyword">
                            <Select placeholder="Select Sub Keyword" disabled={!subkeywords.length} showSearch allowClear optionFilterProp="children">
                                {subkeywords.map((s) => <Option key={s} value={s}>{s}</Option>)}
                            </Select>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="Part No" name="partNo">
                            <Input placeholder="Enter Part No" />
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="System Name" name="systemName">
                            <Input placeholder="Enter System Name" />
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="Availability" name="itemAvailability">
                            <Select placeholder="Select Availability" showSearch allowClear optionFilterProp="children">
                                {itemAvailabilityOptions.map((a) => <Option key={a} value={a}>{a}</Option>)}
                            </Select>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12} md={6}>
                        <Form.Item label="Status" name="itemStatus">
                            <Select placeholder="Select Status" showSearch allowClear optionFilterProp="children">
                                {itemStatusOptions.map((s) => <Option key={s} value={s}>{s}</Option>)}
                            </Select>
                        </Form.Item>
                    </Col>
                    <Col xs={24} className="form-actions">
                        <Space>
                            <Button onClick={handleAdvancedReset} icon={<ClearOutlined />}>Clear</Button>
                            <Button type="primary" htmlType="submit" icon={<SearchOutlined />} loading={advancedLoading}>Search</Button>
                        </Space>
                    </Col>
                </Row>
            </Form>

            {advancedResults.length > 0 && (
                <div className="results-section">
                    <div className="results-header">
                        <Text strong>{advancedResults.length} items found</Text>
                        <Button icon={<PrinterOutlined />} onClick={handlePrint}>Print</Button>
                    </div>
                    <div className="printable-area">
                        <Table
                            columns={advancedColumns}
                            dataSource={advancedResults}
                            rowKey={(record) => record.serial_No || record.serialNo || Math.random()}
                            loading={advancedLoading}
                            scroll={{ x: 2000, y: 500 }}
                            pagination={{
                                pageSize: 10,
                                showSizeChanger: true,
                                showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} items`
                            }}
                            className="results-table"
                            size="small"
                        />
                    </div>
                </div>
            )}
        </div>
    );

    const HistoryTab = () => (
        <div className="search-tab-content">
            <div className="quick-search-container">
                <Form form={historyForm} onFinish={handleHistorySearch} layout="inline" className="quick-search-form">
                    <Form.Item
                        name="serialNo"
                        rules={[{ required: true, message: "Enter serial number" }]}
                        className="quick-search-input"
                    >
                        <Input
                            prefix={<NumberOutlined />}
                            placeholder="Enter Serial Number"
                            size="large"
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            icon={<ClockCircleOutlined />}
                            size="large"
                            loading={historyLoading}
                        >
                            Get History
                        </Button>
                    </Form.Item>
                </Form>
            </div>

            {historyLoading && (
                <div className="loading-container">
                    <Spin size="large" />
                </div>
            )}

            {historyResults.length > 0 && !historyLoading && (
                <Card className="history-card">
                    <Title level={5}>Change History</Title>
                    <Timeline
                        items={historyResults.map((item, index) => ({
                            key: index,
                            color: index === 0 ? "green" : "blue",
                            children: (
                                <div className="history-item">
                                    <Text strong>{item.action || "Updated"}</Text>
                                    <Text type="secondary"> by {item.empEmail || "Unknown"}</Text>
                                    <br />
                                    <Text type="secondary">{item.update_Date ? UtcToISO(item.update_Date) : "N/A"}</Text>
                                    {item.changes && <Text className="history-changes">{item.changes}</Text>}
                                </div>
                            ),
                        }))}
                    />
                </Card>
            )}

            {historyResults.length === 0 && !historyLoading && historyForm.getFieldValue("serialNo") && (
                <Empty description="No history found" />
            )}
        </div>
    );

    const tabItems = [
        {
            key: "quick",
            label: (
                <span><NumberOutlined /> Quick Search</span>
            ),
            children: <QuickSearchTab />,
        },
        {
            key: "advanced",
            label: (
                <span><FilterOutlined /> Advanced Search</span>
            ),
            children: <AdvancedSearchTab />,
        },
        {
            key: "history",
            label: (
                <span><ClockCircleOutlined /> History</span>
            ),
            children: <HistoryTab />,
        },
    ];

    return (
        <div className="item-search-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <FileSearchOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Item Search</Title>
                        <Text className="header-subtitle">
                            Search, view, and manage inventory items
                        </Text>
                    </div>
                </div>
            </div>

            {/* Search Tabs */}
            <Card className="search-card">
                <Tabs
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    items={tabItems}
                    className="search-tabs"
                />
            </Card>

            {/* Update Type Modal */}
            <Modal
                title="Select Update Type"
                open={isChoiceModalVisible}
                onCancel={() => setIsChoiceModalVisible(false)}
                footer={null}
                centered
                width={400}
            >
                <div className="modal-content">
                    <Button type="primary" size="large" block onClick={proceedToFullUpdate} icon={<EditOutlined />}>
                        Full Detail Update
                    </Button>
                    <div className="modal-divider">— OR —</div>
                    <Button className="region-btn" size="large" block onClick={proceedToRegionUpdate} icon={<SwapOutlined />}>
                        Quick Region Update
                    </Button>
                </div>
            </Modal>

            {/* Region Update Modal */}
            <Modal
                title={`Update Region: ${selectedSerialForUpdate}`}
                open={isRegionModalVisible}
                onOk={handleRegionUpdateSubmit}
                onCancel={() => setIsRegionModalVisible(false)}
                confirmLoading={regionUpdateLoading}
                okText="Update Now"
            >
                <div className="region-modal-content">
                    <div className="current-region">
                        <Text type="secondary">Current Region:</Text>
                        <Text strong>{currentRegion}</Text>
                    </div>
                    <div className="new-region">
                        <Text>Select New Region:</Text>
                        <Select
                            style={{ width: "100%", marginTop: 8 }}
                            placeholder="Select Region"
                            onChange={(value) => setSelectedRegionForUpdate(value)}
                            showSearch
                            value={selectedRegionForUpdate}
                            optionFilterProp="children"
                        >
                            {regions.map((r) => <Option key={r} value={r}>{r}</Option>)}
                        </Select>
                    </div>
                </div>
            </Modal>
        </div>
    );
}

export default ItemSearch;
