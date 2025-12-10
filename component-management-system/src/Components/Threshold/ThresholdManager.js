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
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import CreateThresholdApi from "../API/ThresholdAPI/ThresholdApi"
import "./ThresholdManager.css";

const { Title } = Typography;

function ThresholdManager() {
  const [form] = Form.useForm();
  const [regions, setRegions] = useState([]);
  const [loading, setLoading] = useState(false);

  // Get regions from your API on component load
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

  // Handle the form submission
  const onFinish = async (values) => {
    setLoading(true);
    message.loading({ content: "Creating rule...", key: "threshold" });

    const success = await CreateThresholdApi(values);

    if (success) {
      message.success({ content: "Rule created successfully!", key: "threshold", duration: 2 });
      form.resetFields(); // Clear the form
    } else {
      message.error({ content: "Failed to create rule.", key: "threshold", duration: 2 });
    }
    setLoading(false);
  };

  return (
    <div className="p-4 sm:p-8 md:p-12 bg-gray-100 min-h-screen flex items-center justify-center">
      <Card
        className="w-full max-w-lg rounded-xl shadow-lg"
        title={
          <Title level={3} className="text-center mb-4 text-gray-800">
            Set Inventory Threshold
          </Title>
        }
      >
        <Form
          form={form}
          name="threshold_form"
          onFinish={onFinish}
          layout="vertical"
        >
          <Form.Item
            name="partNo"
            label="Part Number"
            rules={[{ required: true, message: "Please input a Part Number!" }]}
          >
            <Input placeholder="Enter the PartNo (e.g., '1')" />
          </Form.Item>

          <Form.Item
            name="regionName"
            label="Region"
            rules={[{ required: true, message: "Please select a region!" }]}
          >
            <Select
              showSearch
              placeholder="Select Region"
              optionFilterProp="children"
              filterOption={(input, option) =>
                (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
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
              { type: 'number', min: 1, message: 'Quantity must be at least 1' }
            ]}
          >
            <InputNumber 
              min={1} 
              style={{ width: '100%' }}
              placeholder="Enter the minimum required stock"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              className="w-full"
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