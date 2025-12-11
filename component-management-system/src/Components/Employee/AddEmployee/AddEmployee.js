import React, { useEffect, useMemo, useState } from "react";
import {
  Card, Form, Button, Input, Select, message,
  Typography, Space, Spin, Progress, Divider, Row, Col,
} from "antd";
import {
  UserOutlined, MailOutlined, PhoneOutlined, GlobalOutlined,
  LockOutlined, SafetyOutlined, UserAddOutlined, ReloadOutlined,
  CheckCircleOutlined, CloseCircleOutlined,
} from "@ant-design/icons";

import AddEmployeeAPI from "../../API/AddEmployee/AddEmployeeAPI";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import "./AddEmployee.css";

const { Title, Text } = Typography;

function AddEmployee() {
  const [form] = Form.useForm();
  const role = useMemo(() => localStorage.getItem("_User_role_for_MSIPL") || "user", []);
  const [regions, setRegions] = useState([]);
  const [loadingRegions, setLoadingRegions] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const passwordValue = Form.useWatch("password", form);

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

  // Password strength calculation
  const getPasswordStrength = (pwd = "") => {
    const checks = {
      length: pwd.length >= 8,
      uppercase: /[A-Z]/.test(pwd),
      lowercase: /[a-z]/.test(pwd),
      number: /\d/.test(pwd),
      special: /[@$!%*#?&^._-]/.test(pwd),
    };
    const score = Object.values(checks).filter(Boolean).length * 20;
    return { checks, score };
  };

  const { checks: passwordChecks, score: passwordScore } = getPasswordStrength(passwordValue);

  const getStrengthLabel = (score) => {
    if (score <= 20) return { text: "Very Weak", color: "var(--color-error)" };
    if (score <= 40) return { text: "Weak", color: "var(--color-error)" };
    if (score <= 60) return { text: "Fair", color: "var(--color-warning)" };
    if (score <= 80) return { text: "Good", color: "var(--color-warning)" };
    return { text: "Strong", color: "var(--color-success)" };
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

  const strengthInfo = getStrengthLabel(passwordScore);

  return (
    <div className="add-employee-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-icon">
            <UserAddOutlined />
          </div>
          <div className="header-text">
            <Title level={3} className="header-title">Add New Employee</Title>
            <Text className="header-subtitle">
              Create a new employee account with access credentials
            </Text>
          </div>
        </div>
        <Button
          icon={<ReloadOutlined spin={loadingRegions} />}
          onClick={fetchRegions}
          className="refresh-btn"
        >
          Refresh Regions
        </Button>
      </div>

      {/* Main Form Card */}
      <Card className="form-card" bordered={false}>
        <Spin spinning={submitting} tip="Creating employee account...">
          <Form
            form={form}
            layout="vertical"
            name="add-employee"
            onFinish={onFinish}
            validateMessages={validateMessages}
            requiredMark={false}
            scrollToFirstError={{ behavior: "smooth", block: "center" }}
          >
            {/* Personal Information Section */}
            <div className="form-section">
              <div className="section-header">
                <UserOutlined className="section-icon" />
                <Text strong className="section-title">Personal Information</Text>
              </div>
              <Divider className="section-divider" />

              <Row gutter={[24, 0]}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Full Name"
                    name="name"
                    rules={[
                      { required: true },
                      { whitespace: true, message: "Name cannot be empty" },
                      { min: 2 },
                    ]}
                  >
                    <Input
                      placeholder="Enter employee's full name"
                      prefix={<UserOutlined className="input-icon" />}
                      allowClear
                      autoComplete="name"
                      size="large"
                    />
                  </Form.Item>
                </Col>

                <Col xs={24} md={12}>
                  <Form.Item
                    label="Email Address"
                    name="email"
                    rules={[{ required: true, type: "email" }]}
                  >
                    <Input
                      placeholder="employee@company.com"
                      prefix={<MailOutlined className="input-icon" />}
                      allowClear
                      autoComplete="email"
                      size="large"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={[24, 0]}>
                <Col xs={24} md={role === "admin" ? 12 : 24}>
                  <Form.Item
                    label="Mobile Number"
                    name="mobile"
                    rules={[
                      { required: true },
                      {
                        pattern: /^\+?[0-9]{10,15}$/,
                        message: "Enter a valid phone number (10–15 digits)",
                      },
                    ]}
                  >
                    <Input
                      placeholder="+91 XXXXXXXXXX"
                      prefix={<PhoneOutlined className="input-icon" />}
                      allowClear
                      inputMode="tel"
                      autoComplete="tel"
                      maxLength={16}
                      size="large"
                    />
                  </Form.Item>
                </Col>

                {role === "admin" && (
                  <Col xs={24} md={12}>
                    <Form.Item
                      label="Assign Region"
                      name="region"
                      rules={[{ required: true, message: "Please select a region" }]}
                    >
                      <Select
                        showSearch
                        placeholder={loadingRegions ? "Loading regions..." : "Select a region"}
                        suffixIcon={<GlobalOutlined className="input-icon" />}
                        loading={loadingRegions}
                        disabled={loadingRegions || regions.length === 0}
                        allowClear
                        optionFilterProp="children"
                        filterOption={(input, option) =>
                          (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                        }
                        size="large"
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
            </div>

            {/* Security Section */}
            <div className="form-section">
              <div className="section-header">
                <LockOutlined className="section-icon" />
                <Text strong className="section-title"> Credentials</Text>
              </div>
              <Divider className="section-divider" />

              <Row gutter={[24, 0]}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Password"
                    name="password"
                    rules={[
                      { required: true, message: "Please enter a password" },
                      {
                        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*#?&^._-]).{8,}$/,
                        message: "Password doesn't meet requirements",
                      },
                    ]}
                    hasFeedback
                  >
                    <Input.Password
                      placeholder="Create a strong password"
                      prefix={<LockOutlined className="input-icon" />}
                      autoComplete="new-password"
                      size="large"
                    />
                  </Form.Item>
                </Col>

                <Col xs={24} md={12}>
                  <Form.Item
                    label="Confirm Password"
                    name="confirmPassword"
                    dependencies={["password"]}
                    hasFeedback
                    rules={[
                      { required: true, message: "Please confirm the password" },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue("password") === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(new Error("Passwords do not match"));
                        },
                      }),
                    ]}
                  >
                    <Input.Password
                      placeholder="Re-enter password"
                      prefix={<LockOutlined className="input-icon" />}
                      autoComplete="new-password"
                      size="large"
                    />
                  </Form.Item>
                </Col>
              </Row>

              {/* Password Strength Indicator */}
              {passwordValue && (
                <div className="password-strength-section">
                  <div className="strength-header">
                    <Space>
                      <SafetyOutlined />
                      <Text className="strength-label">Password Strength:</Text>
                      <Text strong style={{ color: strengthInfo.color }}>
                        {strengthInfo.text}
                      </Text>
                    </Space>
                  </div>
                  <Progress
                    percent={passwordScore}
                    showInfo={false}
                    strokeColor={{
                      "0%": "var(--color-error)",
                      "50%": "var(--color-warning)",
                      "100%": "var(--color-success)",
                    }}
                    trailColor="var(--border-color)"
                    size="small"
                  />
                  <div className="password-requirements">
                    <Row gutter={[16, 8]}>
                      <Col xs={12}>
                        <div className={`requirement ${passwordChecks.length ? "met" : ""}`}>
                          {passwordChecks.length ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                          <span>At least 8 characters</span>
                        </div>
                      </Col>
                      <Col xs={12}>
                        <div className={`requirement ${passwordChecks.uppercase ? "met" : ""}`}>
                          {passwordChecks.uppercase ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                          <span>Uppercase letter</span>
                        </div>
                      </Col>
                      <Col xs={12}>
                        <div className={`requirement ${passwordChecks.lowercase ? "met" : ""}`}>
                          {passwordChecks.lowercase ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                          <span>Lowercase letter</span>
                        </div>
                      </Col>
                      <Col xs={12}>
                        <div className={`requirement ${passwordChecks.number ? "met" : ""}`}>
                          {passwordChecks.number ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                          <span>Number</span>
                        </div>
                      </Col>
                      <Col xs={24}>
                        <div className={`requirement ${passwordChecks.special ? "met" : ""}`}>
                          {passwordChecks.special ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                          <span>Special character (@$!%*#?&^._-)</span>
                        </div>
                      </Col>
                    </Row>
                  </div>
                </div>
              )}
            </div>

            {/* Submit Button */}
            <div className="form-actions">
              <Button
                type="primary"
                htmlType="submit"
                icon={<UserAddOutlined />}
                size="large"
                loading={submitting}
                className="submit-btn"
              >
                Create Employee Account
              </Button>
            </div>
          </Form>
        </Spin>
      </Card>
    </div>
  );
}

export default AddEmployee;