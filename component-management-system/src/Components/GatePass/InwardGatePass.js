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
  message,
  Divider,
} from "antd";
import { PlusOutlined, MinusCircleOutlined } from "@ant-design/icons";
import GetItemStatusOptionAPI from "../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import GetKeywordAPI from "../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import WarrantyOptionAPI from "../API/RepairingOption/WarrantyOptionAPI";
import GatepassInwardAPI from "../API/GatePassAPI/GatepassInwardAPI";
import "./InwardGatePass.css";

const themeColor = "#1f3b57";

function InwardGatePass() {
  const [form] = Form.useForm();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [regions, setRegions] = useState([]);
  const [keywords, setKeywords] = useState([]);
  const [subkeywords, setSubKeywords] = useState([]);
  const [itemStatusOption, setItemStatusOption] = useState([]);
  const [warrantyOption, setWarrantyOption] = useState([]);
  const [fruValue, setFruValue] = useState([]);
  const [itemDetails, setItemDetails] = useState([{}]);

  useEffect(() => {
    (async () => {
      try {
        const [status, region, keyword, warranty] = await Promise.all([
          GetItemStatusOptionAPI(),
          GetRegionAPI(),
          GetKeywordAPI(),
          WarrantyOptionAPI(),
        ]);
        setItemStatusOption(status || []);
        setRegions(region || []);
        setKeywords(keyword || []);
        setWarrantyOption(warranty || []);
      } catch (e) {
        message.error("Failed to load initial data.");
      }
    })();
  }, []);

  const updateSubKeyword = (index, selectedKeyword) => {
    if (!selectedKeyword) {
        setSubKeywords((prev) => {
            const updated = [...prev];
            updated[index] = [];
            return updated;
          });
      return;
    };
    (async () => {
      try {
        const data = await GetSubKeywordAPI(selectedKeyword);
        if (data) {
          const subKeywordValues = data.subKeywordList.map(
            (item) => item.subKeyword
          );
          setSubKeywords((prev) => {
            const updated = [...prev];
            updated[index] = subKeywordValues;
            return updated;
          });
        }
      } catch (error) {
        message.error("Failed to fetch sub-keywords.");
      }
    })();
  };

  const handleKeywordChange = (index, selectedKeyword) => {
    form.setFieldValue(["itemList", index, "subkeywordName"], null);
    updateSubKeyword(index, selectedKeyword);
  };

  const addItemDetail = () => {
    setItemDetails([...itemDetails, {}]);
  };

  const removeItemDetail = (index) => {
    if (itemDetails.length > 1) {
      const updatedItemDetails = [...itemDetails];
      updatedItemDetails.splice(index, 1);
      setItemDetails(updatedItemDetails);

      const updatedFruValues = [...fruValue];
      updatedFruValues.splice(index, 1);
      setFruValue(updatedFruValues);
    } else {
      message.warning("At least 1 item is required");
    }
  };

  const onFinish = async (values) => {
    const data = {
      partyName: values.partyName,
      partyContact: values.partyContact,
      partyAddress: values.partyAddress,
      region: values.region,
      itemList: values.itemList,
    };
    await GatepassInwardAPI(data);
    message.success("GatePass Inward generated successfully!");
    form.resetFields();
    setItemDetails([{}]);
    setFruValue([]);
    setSubKeywords([]);
  };

  const handleFruChange = (index, isChecked) => {
    setFruValue((curr) => {
      const updated = [...curr];
      updated[index] = isChecked;
      return updated;
    });
  };

  return (
    <div className="inward-gate-pass-container">
      <Card bordered={false} className="gate-pass-card">
        <h2 className="gate-pass-title">Inward GatePass</h2>

        <Form
          layout="vertical"
          form={form}
          onFinish={onFinish}
          autoComplete="off"
        >
          <Row gutter={16}>
            <Col xs={24} sm={12}>
              <Form.Item
                label="Party Name"
                name="partyName"
                rules={[{ required: true, message: "Please enter Party Name" }]}
              >
                <Input placeholder="Enter party name" />
              </Form.Item>
            </Col>

            <Col xs={24} sm={12}>
              <Form.Item
                label="Party Address"
                name="partyAddress"
                rules={[{ required: true, message: "Please enter Party Address" }]}
              >
                <Input placeholder="Enter party address" />
              </Form.Item>
            </Col>

            <Col xs={24} sm={12}>
              <Form.Item
                label="Party Contact"
                name="partyContact"
                rules={[
                  {
                    pattern: /^\d+$/,
                    message: "Please enter numbers only.",
                  },
                  {
                    len: 10,
                    message: "Contact number must be 10 digits.",
                  },
                ]}
              >
                <Input placeholder="Enter 10-digit contact number" />
              </Form.Item>
            </Col>

            {role === "admin" && (
              <Col xs={24} sm={12}>
                <Form.Item
                  label="Region"
                  name="region"
                  rules={[{ required: true, message: "Please select a Region" }]}
                >
                  <Select
                    placeholder="Select Region"
                    showSearch
                    allowClear
                    optionFilterProp="children"
                    filterOption={(input, option) =>
                      (option?.children ?? "")
                        .toLowerCase()
                        .includes(input.toLowerCase())
                    }
                    filterSort={(optionA, optionB) =>
                      (optionA?.children ?? "")
                        .toLowerCase()
                        .localeCompare((optionB?.children ?? "").toLowerCase())
                    }
                  >
                    {regions.map((region) => (
                      <Select.Option key={region} value={region}>
                        {region}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            )}
          </Row>

          <Divider className="item-details-divider">Item Details</Divider>
          
          <div className="item-list-container">
            {itemDetails.map((_, index) => (
              <Card
                key={index}
                size="small"
                className="item-detail-card"
                title={`Item ${index + 1}`}
                extra={
                    itemDetails.length > 1 && (
                    <Button
                        type="link"
                        icon={<MinusCircleOutlined />}
                        danger
                        onClick={() => removeItemDetail(index)}
                    >
                        Remove
                    </Button>
                    )
                }
              >
                <Row gutter={16}>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "serialNo"]}
                      label="Serial No"
                      rules={[{ required: true, message: "Please enter Serial No" }]}
                    >
                      <Input placeholder="Enter serial no" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "partNo"]}
                      label="Part No"
                    >
                      <Input placeholder="Enter part no" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "rackNo"]}
                      label="Rack No"
                      rules={[{ required: true, message: "Please enter Rack No" }]}
                    >
                      <Input placeholder="Enter rack no" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "itemStatus"]}
                      label="Item Status"
                      rules={[{ required: true, message: "Please select Status" }]}
                    >
                      <Select
                        placeholder="Select Status"
                        showSearch
                        allowClear
                        optionFilterProp="children"
                      >
                        {itemStatusOption.map((opt) => (
                          <Select.Option key={opt} value={opt}>
                            {opt}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "keywordName"]}
                      label="Keyword"
                      rules={[{ required: true, message: "Please select a Keyword" }]}
                    >
                      <Select
                        placeholder="Select Keyword"
                        showSearch
                        allowClear
                        optionFilterProp="children"
                        onChange={(val) => handleKeywordChange(index, val)}
                      >
                        {keywords.map((key) => (
                          <Select.Option key={key} value={key}>
                            {key}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                  {subkeywords[index]?.length > 0 && (
                    <Col xs={24} sm={12} md={8}>
                      <Form.Item
                        name={["itemList", index, "subkeywordName"]}
                        label="Sub Keyword"
                      >
                        <Select
                          placeholder="Select sub-keyword"
                          showSearch
                          allowClear
                          optionFilterProp="children"
                        >
                          {subkeywords[index].map((sub) => (
                            <Select.Option key={sub} value={sub}>
                              {sub}
                            </Select.Option>
                          ))}
                        </Select>
                      </Form.Item>
                    </Col>
                  )}
                   <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "spareLocation"]} label="Spare Location">
                      <Input placeholder="Enter spare location" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "systemName"]} label="System Name">
                      <Input placeholder="Enter system name" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "moduleFor"]} label="Module For">
                      <Input placeholder="Enter module for" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "moduleVersion"]} label="Module Version">
                      <Input placeholder="Enter module version" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "docketInward"]} label="Docket Inward">
                      <Input placeholder="Enter docket inward" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item name={["itemList", index, "remark"]} label="Remark">
                      <Input placeholder="Enter remark" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12} md={8}>
                    <Form.Item
                      name={["itemList", index, "fru"]}
                      valuePropName="checked"
                      label="With RMA"
                    >
                      <Switch
                        onChange={(checked) => handleFruChange(index, checked)}
                        style={{
                          backgroundColor: fruValue[index] ? themeColor : "#ccc",
                        }}
                      />
                    </Form.Item>
                  </Col>

                  {fruValue[index] && (
                    <>
                      <Col xs={24} sm={12} md={8}>
                        <Form.Item
                          name={["itemList", index, "rmaNo"]}
                          label="RMA No"
                          rules={[{ required: true, message: "Please enter RMA No" }]}
                        >
                          <Input placeholder="Enter RMA No" />
                        </Form.Item>
                      </Col>
                      <Col xs={24} sm={12} md={8}>
                        <Form.Item
                          name={["itemList", index, "warrantyDetails"]}
                          label="Warranty Details"
                          rules={[{ required: true, message: "Please select Warranty Status" }]}
                        >
                          <Select placeholder="Select warranty status">
                            {warrantyOption.map((opt) => (
                              <Select.Option key={opt} value={opt}>
                                {opt}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>
                      </Col>
                      <Col xs={24} sm={12} md={8}>
                        <Form.Item
                          name={["itemList", index, "faultDescription"]}
                          label="Fault Description"
                        >
                          <Input placeholder="Enter fault description" />
                        </Form.Item>
                      </Col>
                      <Col xs={24} sm={12} md={8}>
                        <Form.Item
                          name={["itemList", index, "faultRemark"]}
                          label="Fault Remark"
                        >
                          <Input placeholder="Enter fault remark" />
                        </Form.Item>
                      </Col>
                    </>
                  )}
                </Row>
              </Card>
            ))}
          </div>

          <Form.Item>
            <Button
              type="dashed"
              icon={<PlusOutlined />}
              onClick={addItemDetail}
              block
              className="add-item-button"
            >
              Add Another Item
            </Button>
          </Form.Item>

          <Form.Item style={{ textAlign: "center", marginTop: "20px" }}>
            <Button
              type="primary"
              htmlType="submit"
              className="submit-gatepass-button"
            >
              Generate GatePass Inward
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default InwardGatePass;