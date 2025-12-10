import React, { useState, useEffect } from "react";
import {
  Form,
  Input,
  Button,
  Select,
  Switch,
  Card,
  Row,
  Col,
  Typography,
  message,
  Divider,
  Modal,
} from "antd";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import GatepassOutwardAPI from "../API/GatePassAPI/GatepassOutwardAPI";
import "./OutwardGatePass.css";

const { Title } = Typography;
const { Option } = Select;

function OutwardGatePass() {
  const [form] = Form.useForm();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [regions, setRegions] = useState([]);
  const [fruValue, setFruValue] = useState([]);
  const [ItemDetails, setItemDetails] = useState([
    {
      fru: false,
      serialNo: "",
      remark: "",
      rmaNo: "",
      docketOutward: "",
    },
  ]);

  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI();
        if (data) setRegions(data);
      } catch (error) {
        console.error("Failed to fetch regions:", error);
      }
    };
    fetchRegions();
  }, []);

  const addItemDetail = () => {
    setItemDetails([
      ...ItemDetails,
      { fru: false, serialNo: "", remark: "", rmaNo: "", docketOutward: "" },
    ]);
  };

  const removeItemDetail = (index) => {
    const updated = [...ItemDetails];
    if (updated.length > 1) {
      updated.splice(index, 1);
      setItemDetails(updated);
    } else {
      message.warning("At least 1 item is required");
    }
  };

  const handleFruChange = (index, isChecked) => {
    setFruValue((prev) => {
      const updated = [...prev];
      updated[index] = isChecked;
      return updated;
    });
  };

  const onFinish = (values) => {
    Modal.confirm({
      title: "Confirm Gate Pass Submission",
      content: "Are you sure you want to submit these details? Please verify them before proceeding.",
      okText: "Yes, Submit",
      cancelText: "Cancel",
      onOk: async () => {
        const payload = {
          partyName: values.partyName,
          partyContact: values.partyContact,
          partyAddress: values.partyAddress,
          region: values.region,
          itemList: values.ItemList.map((item) => ({
            serialNo: item.serialNo,
            rmaNo: item.rmaNo,
            remark: item.remark,
            docketOutward: item.docketOutward,
            fru: item.fru || false,
          })),
        };
        console.log("Submitting:", payload);
        try {
          await GatepassOutwardAPI(payload);
          message.success("Gate Pass submitted successfully!");
          form.resetFields();
          setItemDetails([
            { fru: false, serialNo: "", remark: "", rmaNo: "", docketOutward: "" },
          ]);
          setFruValue([]);
        } catch (error) {
          message.error("Submission failed. Please try again.");
          console.error("Submission error:", error);
        }
      },
      onCancel() {
        console.log("Submission cancelled.");
      },
    });
  };

  return (

    <div className="outward-gate-pass-wrapper" > 
    <Card
      className="outward-gate-pass-card"
    >
      <Title
        level={3}
        className="outward-gate-pass-title"
      >
        Outward GatePass
      </Title>

      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        style={{ width: "100%" }}
      >
        <Row gutter={16}>
          <Col xs={24} sm={12}>
            <Form.Item
              label="Party Name"
              name="partyName"
              rules={[{ required: true, message: "Please enter party name" }]}
            >
              <Input placeholder="Enter party name" />
            </Form.Item>
          </Col>

          <Col xs={24} sm={12}>
            <Form.Item
              label="Party Contact"
              name="partyContact"
              rules={[
                {
                  validator: (_, value) => {
                    if (!value || value === "") {
                      return Promise.resolve();
                    }
                    if (!/^[0-9]{10}$/.test(value)) {
                      return Promise.reject("Please enter a valid 10-digit number");
                    }
                    return Promise.resolve();
                  },
                },
              ]}
            >
              <Input placeholder="Enter contact number" maxLength={10} />
            </Form.Item>
          </Col>

          <Col xs={24}>
            <Form.Item
              label="Party Address"
              name="partyAddress"
              rules={[{ required: true, message: "Please enter address" }]}
            >
              <Input.TextArea rows={2} placeholder="Enter party address" />
            </Form.Item>
          </Col>

          {role === "admin" && (
            <Col xs={24} sm={12}>
              <Form.Item
                label="Region"
                name="region"
                rules={[{ required: true, message: "Please select region" }]}
              >
                <Select
                  placeholder="Select region"
                  showSearch
                  allowClear
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                  }
                  // --- FIX 2: Corrected the sorting function ---
                  filterSort={(optionA, optionB) =>
                    String(optionA?.value ?? '')
                      .toLowerCase()
                      .localeCompare(String(optionB?.value ?? '').toLowerCase())
                  }
                >
                  {regions.map((region) => (
                    <Option key={region} value={region}>
                      {region}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          )}
        </Row>

        <Divider />

        <Title
          level={4}
          style={{ marginTop: "12px", color: "#283593", fontSize: "16px" }}
        >
          Item Details
        </Title>

        {ItemDetails.map((_, index) => (
          <Card
            key={index}
            type="inner"
            style={{
              marginBottom: "16px",
              background: "#fafafa",
              border: "1px solid #e0e0e0",
              borderRadius: "10px",
            }}
          >
            <Row gutter={12}>
              <Col xs={24} sm={12} md={8}>
                <Form.Item
                  name={["ItemList", index, "serialNo"]}
                  label="Serial No"
                  rules={[{ required: true, message: "Please enter Serial No!" }]}
                >
                  <Input placeholder="Enter serial number" />
                </Form.Item>
              </Col>

              <Col xs={24} sm={12} md={8}>
                <Form.Item
                  name={["ItemList", index, "docketOutward"]}
                  label="Docket Outward"
                >
                  <Input placeholder="Enter Docket Outward" />
                </Form.Item>
              </Col>

              <Col xs={24} sm={12} md={8}>
                <Form.Item
                  name={["ItemList", index, "remark"]}
                  label="Remark"
                >
                  <Input placeholder="Enter remark (optional)" />
                </Form.Item>
              </Col>

              <Col xs={24} sm={12} md={6}>
                <Form.Item
                  name={["ItemList", index, "fru"]}
                  label="With RMA"
                  valuePropName="checked"
                >
                  <Switch
                    checked={fruValue[index] || false}
                    onChange={(checked) => handleFruChange(index, checked)}
                  />
                </Form.Item>
              </Col>

              {fruValue[index] && (
                <Col xs={24} sm={12} md={8}>
                  <Form.Item
                    name={["ItemList", index, "rmaNo"]}
                    label="RMA No."
                    rules={[{ required: true, message: "Please enter RMA No." }]}
                  >
                    {/* --- FIX 1: Added the missing Input component --- */}
                    <Input placeholder="Enter RMA No." />
                  </Form.Item>
                </Col>
              )}
            </Row>

            <Button
              type="text"
              danger
              onClick={() => removeItemDetail(index)}
               className="remove-item-button"
              
            >
              Remove Item
            </Button>
          </Card>
        ))}

        <Form.Item>
          <Button type="dashed" onClick={addItemDetail} block>
            + Add Another Item
          </Button>
        </Form.Item>

        <Divider />

        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            style={{ width: "100%", height: "40px", fontWeight: 500 }}
          >
            Generate GatePass Outward
          </Button>
        </Form.Item>
      </Form>
    </Card>
    </div>
  );
}

export default OutwardGatePass;