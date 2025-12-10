import React, { useEffect, useMemo, useState } from "react";
import {
  Card, Form, Button, Input, Select, message,
  Typography, Space, Spin, Progress, Divider,
} from "antd";
import {
  UserOutlined, MailOutlined, PhoneOutlined, GlobalOutlined, // Kept 'GlobalOutlined'
  LockOutlined, SafetyOutlined, PlusCircleOutlined, ReloadOutlined,
} from "@ant-design/icons";

import AddEmployeeAPI from "../../API/AddEmployee/AddEmployeeAPI";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import "./AddEmployee.css";

const { Text } = Typography;

function AddEmployee() {
  const [form] = Form.useForm();
  const role = useMemo(() => localStorage.getItem("_User_role_for_MSIPL") || "user", []);
  const [regions, setRegions] = useState([]);
  const [loadingRegions, setLoadingRegions] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const passwordValue = Form.useWatch("password", form);

  const themeColors = {
      error: 'var(--color-error)',
      warning: 'var(--color-warning)',
      success: 'var(--color-success)',
  };

  useEffect(() => {
    document.title = "Add New Employee";
  }, []);

  const fetchRegions = async () => {
    setLoadingRegions(true);
    try {
      const data = await GetRegionAPI();
      if (Array.isArray(data)) {
        setRegions(data);
      } else {
        setRegions([]);
        message.warning("Regions list is empty or invalid.", 2);
      }
    } catch (error) {
      console.error("API Error (Regions):", error);
      message.error("Failed to fetch regions. Please try again.", 2);
    } finally {
      setLoadingRegions(false);
    }
  };

  useEffect(() => {
    fetchRegions();
  }, []);

  const passwordScore = (pwd = "") => {
    let score = 0;
    if (pwd.length >= 8) score += 20;
    if (/[A-Z]/.test(pwd)) score += 20;
    if (/[a-z]/.test(pwd)) score += 20;
    if (/\d/.test(pwd)) score += 20;
    if (/[@$!%*#?&^._-]/.test(pwd)) score += 20;
    return score;
  };

  const onFinish = async (values) => {
    const payload = {
      name: values.name.trim(),
      email: values.email.trim(),
      mobileNo: values.mobile.trim(),
      regionName: values.region || "",
      password: values.password,
    };
    if (role === "admin" && !payload.regionName) {
      message.warning("Admins must select a region.", 2);
      return;
    }
    setSubmitting(true);
    try {
      const res = await AddEmployeeAPI(payload);
      message.success(res?.message || "Employee added successfully!", 2);
      form.resetFields();
    } catch (error) {
      console.error("API Error (Add Employee):", error);
      message.error(error?.message || "Failed to add employee. Please try again.", 3);
    } finally {
      setSubmitting(false);
    }
  };
  

  const validateMessages = {
    // eslint-disable-next-line no-template-curly-in-string
    required: "${label} is required",
    types: { email: "Please enter a valid email address" },
    string: {
      // eslint-disable-next-line no-template-curly-in-string
      min: "${label} must be at least ${min} characters",
    },
  };

  return (
    <div className="add-employee-wrapper">
      <Card
        className="add-employee-card"
        bordered={false}
        title={
          <Space align="center">
            <PlusCircleOutlined style={{ color: "var(--primary-color)" }} />
            <span>Add New Employee</span>
          </Space>
        }
        extra={
          <Button icon={<ReloadOutlined />} onClick={fetchRegions} size="small" type="text" style={{ color: "var(--primary-color)" }} >
            Refresh Regions
          </Button>
        }
      >
        <Text type="secondary"  style={{ color: "var(--text-color-secondary)" }}>
          Fill in the employee details below. Fields marked with * are required.
        </Text>

        <Divider style={{ margin: "16px 0" }} />

        <Spin spinning={submitting} tip="Creating employee...">
          <Form
          className="compact-form"
            form={form}
            layout="vertical"
            name="add-employee"
            onFinish={onFinish}
            validateMessages={validateMessages}
            size="large"
            requiredMark={false}
            scrollToFirstError={{ behavior: "smooth", block: "center" }}
          >
            <Form.Item label="Employee Name" name="name" rules={[{ required: true }, { whitespace: true, message: "Name cannot be empty" }, { min: 2 }]}>
                <Input placeholder="Enter name here" prefix={<UserOutlined />} allowClear autoComplete="name" />
            </Form.Item>

            <Form.Item label="Employee Email" name="email" rules={[{ required: true, type: "email" }]}>
                <Input placeholder="e.g., email@company.com" prefix={<MailOutlined />} allowClear autoComplete="email" />
            </Form.Item>

            <Form.Item label="Employee Mobile Number" name="mobile" rules={[{ required: true }, { pattern: /^\+?[0-9]{10,15}$/, message: "Enter a valid phone number (10–15 digits, optionally starting with +)" }]}>
                <Input placeholder="e.g., +XXXXXXXXXX" prefix={<PhoneOutlined />} allowClear inputMode="tel" autoComplete="tel" maxLength={16} />
            </Form.Item>
            
            {role === "admin" && (
                <Form.Item label="Region" name="region" rules={[{ required: true, message: "Please select a region" }]}>
                    <Select
                      showSearch
                      placeholder={loadingRegions ? "Loading regions..." : "Choose a region"}
                      //  Added the GlobalOutlined icon here
                      prefix={<GlobalOutlined />}
                      loading={loadingRegions}
                      disabled={loadingRegions || regions.length === 0}
                      allowClear
                      optionFilterProp="children"
                      filterOption={(input, option) => (option?.children ?? "").toLowerCase().includes(input.toLowerCase())}
                    >
                      {regions.map((region) => (
                          <Select.Option key={region} value={region}>{region}</Select.Option>
                      ))}
                    </Select>
                </Form.Item>
            )}

            <Form.Item label="Password" name="password" rules={[{ required: true, message: "Please enter a password" }, { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*#?&^._-]).{8,}$/, message: "Min 8 chars with uppercase, lowercase, number & special character" }]} hasFeedback>
                <Input.Password placeholder="Create a strong password" prefix={<LockOutlined />} autoComplete="new-password" />
            </Form.Item>
            
            {passwordValue && (
              <div className="password-meter">
                <Space align="center">
                  <SafetyOutlined />
                  <Text type="secondary">Password strength</Text>
                </Space>
                <Progress
                  percent={passwordScore(passwordValue)}
                  showInfo={false}
                  strokeColor={{
                      "0%": themeColors.error,
                      "50%": themeColors.warning,
                      "100%": themeColors.success,
                  }}
                  status={
                    passwordScore(passwordValue) < 40 ? "exception"
                      : passwordScore(passwordValue) < 80 ? "normal"
                      : "success"
                  }
                />
              </div>
            )}

            <Form.Item label="Confirm Password" name="confirmPassword" dependencies={["password"]} hasFeedback rules={[{ required: true, message: "Please confirm your password" }, ({ getFieldValue }) => ({ validator(_, value) { if (!value || getFieldValue("password") === value) { return Promise.resolve(); } return Promise.reject(new Error("The two passwords do not match")); } })]}>
                <Input.Password placeholder="Re-enter password" prefix={<LockOutlined />} autoComplete="new-password" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<PlusCircleOutlined />} block size="large" loading={submitting}>
                Add New Employee
              </Button>
            </Form.Item>
          </Form>
        </Spin>
      </Card>
    </div>
  );
}

export default AddEmployee;