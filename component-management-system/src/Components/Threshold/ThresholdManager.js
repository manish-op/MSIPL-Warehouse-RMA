import React, { useState, useEffect } from "react";
import {
  Form,
  Select,
  Button,
  message,
  Card,
  Typography,
  Input,
  InputNumber,
} from "antd";
import {
  AlertOutlined,
  NumberOutlined,
  EnvironmentOutlined,
  StockOutlined,
} from "@ant-design/icons";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import CreateThresholdApi from "../API/ThresholdAPI/ThresholdApi";
import "./ThresholdManager.css";

const { Title, Text } = Typography;

function ThresholdManager() {
  const [form] = Form.useForm();
  const [regions, setRegions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI();
        if (data) {
          setRegions(data);
        }
      } catch (error) {
        console.error("API Error:", error);
      }
    };
    fetchRegions();
  }, []);

  const onFinish = async (values) => {
    setLoading(true);
    message.loading({ content: "Creating rule...", key: "threshold" });

    const success = await CreateThresholdApi(values);

    if (success) {
      message.success({
        content: "Threshold rule created successfully!",
        key: "threshold",
        duration: 2,
      });
      form.resetFields();
    } else {
      message.error({
        content: "Failed to create rule.",
        key: "threshold",
        duration: 2,
      });
    }
    setLoading(false);
  };

  return (
    <div className="threshold-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-icon">
            <AlertOutlined />
          </div>
          <div className="header-text">
            <Title level={3} className="header-title">
              Threshold Management
            </Title>
            <Text className="header-subtitle">
              Set inventory alert thresholds for low stock notifications
            </Text>
          </div>
        </div>
      </div>

      {/* Main Card */}
      <Card className="threshold-card" bordered={false}>
        <div className="card-description">
          <Text className="desc-text">
            Create rules to get notified when inventory levels fall below the
            specified minimum quantity.
          </Text>
        </div>

        <Form
          form={form}
          name="threshold_form"
          onFinish={onFinish}
          layout="vertical"
          requiredMark={false}
        >
          <Form.Item
            name="partNo"
            label="Part Number"
            rules={[{ required: true, message: "Please input a Part Number!" }]}
          >
            <Input
              prefix={<NumberOutlined className="input-icon" />}
              placeholder="Enter the Part Number"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="regionName"
            label="Region"
            rules={[{ required: true, message: "Please select a region!" }]}
          >
            <Select
              showSearch
              size="large"
              placeholder="Select Region"
              optionFilterProp="children"
              suffixIcon={<EnvironmentOutlined className="input-icon" />}
              filterOption={(input, option) =>
                (option?.children ?? "")
                  .toLowerCase()
                  .includes(input.toLowerCase())
              }
            >
              {regions.map((region) => (
                <Select.Option key={region} value={region}>
                  {region}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="minQuantity"
            label="Minimum Quantity"
            rules={[
              { required: true, message: "Please set a minimum quantity!" },
              { type: "number", min: 1, message: "Quantity must be at least 1" },
            ]}
          >
            <InputNumber
              min={1}
              size="large"
              style={{ width: "100%" }}
              placeholder="Enter the minimum required stock"
              prefix={<StockOutlined className="input-icon" />}
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              icon={<AlertOutlined />}
              block
              size="large"
              className="submit-btn"
            >
              Set Threshold Rule
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default ThresholdManager;