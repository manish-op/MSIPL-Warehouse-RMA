import React, { useState, useEffect } from "react";
import { Form, Input, Button, Row, Col, Card, message, Modal, Select } from "antd";
import Cookies from "js-cookie";
import { useNavigate } from "react-router-dom";
import { useItemDetails } from "../UpdateItem/ItemContext";
import { URL } from "../../API/URL";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";

const { Option } = Select;

function CheckItemSearchBySerialNo() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { setItemDetails } = useItemDetails();
  const token = atob(Cookies.get("authToken") || "");

  // --- State for Item Data ---
  const [itemData, setItemData] = useState(null);

  // --- State for Modals & Regions ---
  const [regions, setRegions] = useState([]);
  const [isChoiceModalVisible, setIsChoiceModalVisible] = useState(false);
  const [isRegionModalVisible, setIsRegionModalVisible] = useState(false);
  const [currentRegion, setCurrentRegion] = useState("N/A");
  const [selectedRegionForUpdate, setSelectedRegionForUpdate] = useState(null);
  const [regionUpdateLoading, setRegionUpdateLoading] = useState(false);

  // --- 1. Fetch Region Options on Mount ---
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const regionList = await GetRegionAPI();
        setRegions(regionList || []);
      } catch (error) {
        console.error("Error fetching regions:", error);
      }
    };
    fetchRegions();
  }, []);

  // --- 2. Search Handler ---
  const onFinish = async (values) => {
    if (!token) {
      navigate("/login");
      return;
    }

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
        const mess = await response.text();
        message.warning(mess, 2);
      } else {
        const data = await response.json();
        
        // 1. Store the data locally and in Context
        setItemData(data);
        setItemDetails(data); 

        // 2. Extract Current Region safely (Handling various backend structures)
        const regionName = data?.region?.regionName || data?.region?.city || data?.region || "N/A";
        setCurrentRegion(regionName);

        // 3. Show the Choice Modal (Do not navigate yet)
        setIsChoiceModalVisible(true);
      }
    } catch (error) {
      console.error(error);
      message.error("An error occurred while fetching item details.");
    }
  };

  // --- 3. Choice Handlers ---
  
  // Option A: Full Update (Navigate to existing page)
  const proceedToFullUpdate = () => {
    setIsChoiceModalVisible(false);
    navigate("/dashboard/updateItem");
  };

  // Option B: Quick Region Update (Open second modal)
  const proceedToRegionUpdate = () => {
    setIsChoiceModalVisible(false);
    setIsRegionModalVisible(true);
    // Pre-fill dropdown with current region
    setSelectedRegionForUpdate(currentRegion);
  };

  // --- 4. Region Update Submit Handler ---
  const handleRegionUpdateSubmit = async () => {
    if (!selectedRegionForUpdate) {
      message.error("Please select a region");
      return;
    }

    // *** FIX: Robust Serial Number Retrieval ***
    // 1. Try DB field (usually serial_No)
    // 2. Try camelCase (serialNo)
    // 3. Fallback to what user typed in the form input
    const serialNoToSend = 
        itemData?.serial_No || 
        itemData?.serialNo || 
        form.getFieldValue("serialNo");

    if (!serialNoToSend) {
        message.error("Could not identify Serial Number. Please search again.");
        return;
    }

    setRegionUpdateLoading(true);
    
    try {
      const payload = {
        serialNo: serialNoToSend,
        newRegionName: selectedRegionForUpdate,
      };

      const response = await fetch(`${URL}/componentDetails/update-region-only`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        message.success("Region updated successfully!");
        setIsRegionModalVisible(false);
        setIsChoiceModalVisible(false);
        
        // Optional: Reset form to clear state
        // form.resetFields();
      } else {
        const text = await response.text();
        message.error("Failed to update: " + text);
      }
    } catch (error) {
      message.error("Error updating region: " + error.message);
    } finally {
      setRegionUpdateLoading(false);
    }
  };

  return (
    <>
      <title>SearchBySerialno</title>
      <div
        style={{
          display: "flex",
          flex: "1",
          justifyContent: "center",
          alignItems: "center",
          flexDirection: "column",
        }}
      >
        <Card>
          <h2
            style={{
              fontSize: "20px",
              color: "orange",
              textAlign: "center",
              padding: "2px",
              marginBottom: "20px",
            }}
          >
            Search by Serial no.
          </h2>
          <Form
            form={form}
            name="check-item-form"
            onFinish={onFinish}
            style={{
              display: "flex",
              justifyContent: "center",
              marginTop: "20px",
            }}
          >
            <Row gutter={16} align="middle">
              <Col>
                <Form.Item
                  label="Serial No."
                  name="serialNo"
                  rules={[
                    {
                      required: true,
                      message: "Please enter the serial number!",
                    },
                  ]}
                  style={{ marginBottom: 0 }}
                >
                  <Input placeholder="Enter Serial No" />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item style={{ marginBottom: 0 }}>
                  <Button type="primary" htmlType="submit">
                    Search Item
                  </Button>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </Card>

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
            <Button type="primary" size="large" block onClick={proceedToFullUpdate}>
              Full Detail Update (See Details)
            </Button>
            <div style={{ textAlign: "center", color: "#999" }}>- OR -</div>
            <Button
              style={{ backgroundColor: "#52c41a", color: "white" }}
              size="large"
              block
              onClick={proceedToRegionUpdate}
            >
              Quick Region Update
            </Button>
          </div>
        </Modal>

        {/* --- MODAL 2: REGION SELECTION FORM --- */}
        <Modal
          title={`Update Region: ${itemData?.serial_No || form.getFieldValue("serialNo")}`}
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
    </>
  );
}

export default CheckItemSearchBySerialNo;