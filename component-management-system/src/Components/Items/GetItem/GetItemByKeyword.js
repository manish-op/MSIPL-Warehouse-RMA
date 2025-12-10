import {
  Form,
  Select,
  Button,
  message,
  Card,
  Table,
  Space,
  Typography,
  Input,
  Row,
  Col,
  Modal,
} from "antd";
import Cookies from "js-cookie";
import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from "react";
import { SearchOutlined, PrinterOutlined, EditOutlined, HistoryOutlined, ClearOutlined } from "@ant-design/icons";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import GetKeywordAPI from "../../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import GetItemStatusOptionAPI from "../../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import GetItemAvailabilityStatusOptionAPI from "../../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import GetItemHistoryBySerialNoAPI from "../../API/ItemRelatedApi/ItemHistory/GetItemHistoryBySerialNoAPI";
import { useItemDetails } from "../UpdateItem/ItemContext";
import UtcToISO from "../../UtcToISO";
import { URL } from "../../API/URL";

import "./GetItemByKeyword.css";
import "./PrintTable.css";

const { Title } = Typography;
const { Option } = Select;

function GetItemByKeyword() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const token = atob(Cookies.get("authToken") || "");
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const { setItemHistory, setItemDetails } = useItemDetails();

  // --- Search Data State ---
  const [regions, setRegions] = useState([]);
  const [keywords, setKeywords] = useState([]);
  const [subkeywords, setSubKeywords] = useState([]);
  const [itemAvailabilityOptions, setItemAvailabilityOptions] = useState([]);
  const [itemStatusOptions, setItemStatusOptions] = useState([]);
  
  const [itemsDetailsFromDb, setItemsDetailsFromDb] = useState([]);
  const [loading, setLoading] = useState(false);

  // --- Update & Modal State ---
  const [isChoiceModalVisible, setIsChoiceModalVisible] = useState(false);
  const [isRegionModalVisible, setIsRegionModalVisible] = useState(false);
  const [selectedSerialForUpdate, setSelectedSerialForUpdate] = useState(null);
  const [selectedRegionForUpdate, setSelectedRegionForUpdate] = useState(null);
  const [currentRegion, setCurrentRegion] = useState("N/A");
  const [regionUpdateLoading, setRegionUpdateLoading] = useState(false);

  // --- 1. INITIAL FETCH & RESTORE STATE ---
  useEffect(() => {
    const fetchDataAndRestore = async () => {
        // A. Fetch Dropdown Options
        setRegions(await GetRegionAPI());
        setKeywords(await GetKeywordAPI());
        setItemAvailabilityOptions(await GetItemAvailabilityStatusOptionAPI());
        setItemStatusOptions(await GetItemStatusOptionAPI());

        // B. Restore Previous Search Results (If any)
        const cachedResults = sessionStorage.getItem("itemSearchResults");
        const cachedValues = sessionStorage.getItem("itemSearchValues");

        if (cachedResults && cachedValues) {
            const parsedResults = JSON.parse(cachedResults);
            const parsedValues = JSON.parse(cachedValues);

            setItemsDetailsFromDb(parsedResults);
            form.setFieldsValue(parsedValues);

            // If a keyword was selected, we need to re-fetch subkeywords so the dropdown works
            if (parsedValues.keyword) {
                handleKeywordChange(parsedValues.keyword);
            }
        }
    };
    fetchDataAndRestore();
  }, [form]);

  const handleKeywordChange = async (selectedKeyword) => {
    // Only reset subkeyword if triggered by user interaction, not restoration
    // We check if the form field is different from the argument to distinguish
    
    if (selectedKeyword) {
      try {
        const data = await GetSubKeywordAPI(selectedKeyword);
        if (data && data.subKeywordList) {
          setSubKeywords(data.subKeywordList.map((item) => item.subKeyword));
        } else {
          setSubKeywords([]);
        }
      } catch (error) {
        setSubKeywords([]);
      }
    } else {
      setSubKeywords([]);
    }
  };

  // --- 2. SEARCH HANDLER (Modified to Save State) ---
  const onFinish = async (values) => {
    if (!token) {
      navigate("/login");
      return;
    }
    setLoading(true);
    // Don't clear immediately, looks smoother
    // setItemsDetailsFromDb([]); 

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
        setItemsDetailsFromDb(data);
        
        // *** SAVE TO SESSION STORAGE ***
        sessionStorage.setItem("itemSearchResults", JSON.stringify(data));
        sessionStorage.setItem("itemSearchValues", JSON.stringify(values));

        if (data.length === 0) message.info("No items found.");
      }
    } catch (error) {
      message.error(`API Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // --- NEW: Reset Handler ---
  const handleReset = () => {
      form.resetFields();
      setItemsDetailsFromDb([]);
      setSubKeywords([]);
      sessionStorage.removeItem("itemSearchResults");
      sessionStorage.removeItem("itemSearchValues");
  };

  // --- Full Update Navigation ---
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
      navigate("/dashboard/updateItem");
    } catch (error) {
      message.error(`Error: ${error.message}`);
    }
  };

  const handleUpdateActionClick = (serialNo) => {
    const item = itemsDetailsFromDb.find((i) => i.serial_No === serialNo);
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

  // --- 3. REGION UPDATE SUBMIT (Refreshes Data & Storage) ---
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
        
        // Refresh data using the stored search values
        const currentSearchValues = form.getFieldsValue();
        // Check if we have valid search criteria to refresh with
        if (currentSearchValues.keyword || currentSearchValues.region || currentSearchValues.partNo) {
             onFinish(currentSearchValues); 
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
    GetItemHistoryBySerialNoAPI({ serialNo }, setItemHistory, navigate);
  };

  const handlePrint = () => window.print();

  // --- Table Columns ---
  const tableColumns = [
    {
      title: "Serial No",
      dataIndex: "serial_No",
      key: "serial_No",
      fixed: "left",
      width: 170, 
      render: (text, record) => (
        <div className="cell-content-wrapper">
          <span style={{ fontWeight: 500 }}>{text}</span>
          <div className="overlay-actions">
            <Space size="small">
              <Button
                className="action-btn-update"
                size="small"
                shape="round"
                icon={<EditOutlined />}
                onClick={(e) => {
                  e.stopPropagation();
                  handleUpdateActionClick(record.serial_No);
                }}
              >
                Update
              </Button>
              <Button
                className="action-btn-history"
                size="small"
                shape="round"
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
    { title: "System Name", dataIndex: "system", key: "system", width: 150 },
    { title: "Module For", dataIndex: "moduleFor", key: "moduleFor", width: 120 },
    { title: "Module Version", dataIndex: "systemVersion", key: "systemVersion", width: 150 },
    { title: "Rack No", dataIndex: "rackNo", key: "rackNo", width: 100 },
    { title: "Box No", dataIndex: "boxNo", key: "boxNo", width: 100 },
    { title: "Spare Location", dataIndex: "spareLocation", key: "spareLocation", width: 150 },
    { title: "Party Name", dataIndex: "partyName", key: "partyName", width: 150 },
    { title: "Description", dataIndex: "itemDescription", key: "itemDescription", width: 200 },
    { title: "Item Status", dataIndex: ["itemStatusId", "itemStatus"], key: "itemStatus", width: 120 },
    { title: "Available Status", dataIndex: ["availableStatusId", "itemAvailableOption"], key: "availableStatus", width: 150 },
    { title: "Last Updated By", dataIndex: "empEmail", key: "empEmail", width: 200 },
    {
      title: "Last Updated Date",
      dataIndex: "update_Date",
      key: "update_Date",
      render: (date) => (date ? UtcToISO(date) : "N/A"),
      width: 180,
    },
  ];

  return (
    <div className="main-content">
      <div className="search-item-container">
        <Card className="search-card" title={<Title level={4}>Search Item by Parameters</Title>}>
          <Form form={form} name="search-by-keyword-form" onFinish={onFinish} layout="vertical">
            <Row gutter={24}>
              {role === "admin" && (
                <Col xs={24} sm={12} md={8} lg={6}>
                  <Form.Item label="Region" name="region" rules={[{ required: true, message: "Please select a region!" }]}>
                    <Select placeholder="Select Region" showSearch allowClear optionFilterProp="children">
                      {regions.map((region) => (<Option key={region} value={region}>{region}</Option>))}
                    </Select>
                  </Form.Item>
                </Col>
              )}
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="Keyword" name="keyword">
                  <Select placeholder="Select Keyword" onChange={handleKeywordChange} showSearch allowClear optionFilterProp="children">
                    {keywords.map((keyword) => (<Option key={keyword} value={keyword}>{keyword}</Option>))}
                  </Select>
                </Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="Sub Keyword" name="subKeyword">
                  <Select placeholder="Select Sub Keyword" disabled={!subkeywords.length} showSearch allowClear optionFilterProp="children">
                    {subkeywords.map((sub) => (<Option key={sub} value={sub}>{sub}</Option>))}
                  </Select>
                </Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="Part No" name="partNo"><Input placeholder="Enter Part No" /></Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="System Name" name="systemName"><Input placeholder="Enter System Name" /></Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="Available Status" name="itemAvailability">
                  <Select placeholder="Select Availability" showSearch allowClear optionFilterProp="children">
                    {itemAvailabilityOptions.map((status) => (<Option key={status} value={status}>{status}</Option>))}
                  </Select>
                </Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8} lg={6}>
                <Form.Item label="Item Status" name="itemStatus">
                  <Select placeholder="Select Status" showSearch allowClear optionFilterProp="children">
                    {itemStatusOptions.map((status) => (<Option key={status} value={status}>{status}</Option>))}
                  </Select>
                </Form.Item>
              </Col>
              <Col xs={24} style={{ textAlign: "right" }}>
                <Space>
                    <Button onClick={handleReset} icon={<ClearOutlined />}>
                        Clear
                    </Button>
                    <Button className="search-button" type="primary" htmlType="submit" icon={<SearchOutlined />} loading={loading}>
                        Search
                    </Button>
                </Space>
              </Col>
            </Row>
          </Form>
        </Card>

        {itemsDetailsFromDb.length > 0 && (
          <Card className="results-card">
            <div className="results-header">
              <Title level={5}>Search Results ({itemsDetailsFromDb.length} items found)</Title>
              <Button icon={<PrinterOutlined />} onClick={handlePrint}>Print Results</Button>
            </div>
            <div className="printable-area">
              <Table
                columns={tableColumns}
                dataSource={itemsDetailsFromDb}
                rowKey="serial_No"
                loading={loading}
                scroll={{ x: 2900, y: 600 }}
                pagination={{ pageSize: 10 }}
              />
            </div>
          </Card>
        )}
      </div>

      {/* --- MODAL 1: SELECT UPDATE TYPE --- */}
      <Modal
        title="Select Update Type"
        open={isChoiceModalVisible}
        onCancel={() => setIsChoiceModalVisible(false)}
        footer={null}
        centered
        width={400}
      >
        <div style={{ display: "flex", flexDirection: "column", gap: "15px", padding: "20px" }}>
          <Button type="primary" size="large" block onClick={proceedToFullUpdate}>Full Detail Update</Button>
          <div style={{ textAlign: "center", color: "#999" }}>- OR -</div>
          <Button style={{ backgroundColor: "#52c41a", color: "white" }} size="large" block onClick={proceedToRegionUpdate}>Quick Region Update</Button>
        </div>
      </Modal>

      {/* --- MODAL 2: REGION SELECTION FORM --- */}
      <Modal
        title={`Update Region: ${selectedSerialForUpdate}`}
        open={isRegionModalVisible}
        onOk={handleRegionUpdateSubmit}
        onCancel={() => setIsRegionModalVisible(false)}
        confirmLoading={regionUpdateLoading}
        okText="Update Now"
      >
        <div style={{ padding: "10px 0" }}>
          <div style={{ marginBottom: "15px", background: "#f5f5f5", padding: "10px", borderRadius: "4px" }}>
            <span style={{ color: "#888", marginRight: "8px" }}>Current Region:</span>
            <span style={{ fontWeight: "bold", fontSize: "15px" }}>{currentRegion}</span>
          </div>

          <p style={{ marginBottom: "5px" }}>Select New Region:</p>
          <Select
            style={{ width: "100%" }}
            placeholder="Select Region"
            onChange={(value) => setSelectedRegionForUpdate(value)}
            showSearch
            value={selectedRegionForUpdate}
            optionFilterProp="children"
          >
            {regions.map((region) => (
              <Option key={region} value={region}>
                {region}
              </Option>
            ))}
          </Select>
        </div>
      </Modal>
    </div>
  );
}

export default GetItemByKeyword;