import React, { useState, useEffect } from "react";
import {
    Card, Form, Button, Input, Select, message, Typography, Tabs,
} from "antd";
import {
    UserSwitchOutlined, GlobalOutlined, LockOutlined,
    MailOutlined, SafetyCertificateOutlined, TeamOutlined,
} from "@ant-design/icons";
import GetRoleAPI from "../API/Role/GetRoleAPI";
import ChangeEmployeeRoleAPI from "../API/Role/ChanageEmployeeRole/ChangeEmployeeRoleAPI";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import ChangeEmployeeRegionAPI from "../API/Region/ChangeEmployeeRegion/ChangeEmployeeRegionAPI";
import ChangeEmployeePasswordAPI from "../API/ChangeEmployeePassword/ChangeEmployeePasswordAPI";
import "./EmployeeManagement.css";

const { Title, Text } = Typography;

// Tab 1: Change Role Component
const ChangeRoleTab = () => {
    const [form] = Form.useForm();
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchRoles = async () => {
            setLoading(true);
            try {
                const data = await GetRoleAPI();
                if (data) setRoles(data);
            } catch (error) {
                message.error("Failed to fetch roles");
            } finally {
                setLoading(false);
            }
        };
        fetchRoles();
    }, []);

    const onFinish = async (values) => {
        const currentUserRole = localStorage.getItem("_User_role_for_MSIPL");
        if (currentUserRole !== "admin") {
            message.warning("Only admins can perform this action");
            return;
        }

        setSubmitting(true);
        try {
            await ChangeEmployeeRoleAPI({ empEmail: values.email, role: values.role });
            form.resetFields();
        } catch (error) {
            console.error("Failed to change role:", error);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="change-role-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Change the role/access level of an existing employee in the system.
                </Text>
            </div>

            <Form.Item
                label="Employee Email"
                name="email"
                rules={[
                    { required: true, message: "Please enter employee email" },
                    { type: "email", message: "Please enter a valid email" },
                ]}
            >
                <Input
                    prefix={<MailOutlined className="input-icon" />}
                    placeholder="employee@company.com"
                    size="large"
                />
            </Form.Item>

            <Form.Item
                label="New Role"
                name="role"
                rules={[{ required: true, message: "Please select a role" }]}
            >
                <Select
                    placeholder={loading ? "Loading roles..." : "Select new role"}
                    loading={loading}
                    disabled={loading || roles.length === 0}
                    size="large"
                    suffixIcon={<SafetyCertificateOutlined className="input-icon" />}
                >
                    {roles.map((role) => (
                        <Select.Option key={role} value={role}>
                            {role.charAt(0).toUpperCase() + role.slice(1)}
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<UserSwitchOutlined />}
                    loading={submitting}
                    block
                    size="large"
                    className="submit-btn"
                >
                    Update Role
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 2: Change Region Component
const ChangeRegionTab = () => {
    const [form] = Form.useForm();
    const [regions, setRegions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchRegions = async () => {
            setLoading(true);
            try {
                const data = await GetRegionAPI();
                if (data) setRegions(data);
            } catch (error) {
                message.error("Failed to fetch regions");
            } finally {
                setLoading(false);
            }
        };
        fetchRegions();
    }, []);

    const onFinish = async (values) => {
        const currentUserRole = localStorage.getItem("_User_role_for_MSIPL");
        if (currentUserRole !== "admin") {
            message.warning("Only admins can perform this action");
            return;
        }

        setSubmitting(true);
        try {
            await ChangeEmployeeRegionAPI({ empEmail: values.email, region: values.region });
            form.resetFields();
        } catch (error) {
            console.error("Failed to change region:", error);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="change-region-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Assign or change the region for an employee to manage their inventory access.
                </Text>
            </div>

            <Form.Item
                label="Employee Email"
                name="email"
                rules={[
                    { required: true, message: "Please enter employee email" },
                    { type: "email", message: "Please enter a valid email" },
                ]}
            >
                <Input
                    prefix={<MailOutlined className="input-icon" />}
                    placeholder="employee@company.com"
                    size="large"
                />
            </Form.Item>

            <Form.Item
                label="New Region"
                name="region"
                rules={[{ required: true, message: "Please select a region" }]}
            >
                <Select
                    placeholder={loading ? "Loading regions..." : "Select new region"}
                    loading={loading}
                    disabled={loading || regions.length === 0}
                    size="large"
                    showSearch
                    optionFilterProp="children"
                    suffixIcon={<GlobalOutlined className="input-icon" />}
                >
                    {regions.map((region) => (
                        <Select.Option key={region} value={region}>
                            {region}
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<GlobalOutlined />}
                    loading={submitting}
                    block
                    size="large"
                    className="submit-btn"
                >
                    Update Region
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 3: Reset Password Component
const ResetPasswordTab = () => {
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);

    const onFinish = async (values) => {
        setSubmitting(true);
        try {
            await ChangeEmployeePasswordAPI({
                empEmail: values.email,
                newPassword: values.newPassword,
            });
            form.resetFields();
        } catch (error) {
            console.error("Failed to reset password:", error);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="reset-password-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Reset the password for an employee who has forgotten or needs a new password.
                </Text>
            </div>

            <Form.Item
                label="Employee Email"
                name="email"
                rules={[
                    { required: true, message: "Please enter employee email" },
                    { type: "email", message: "Please enter a valid email" },
                ]}
            >
                <Input
                    prefix={<MailOutlined className="input-icon" />}
                    placeholder="employee@company.com"
                    size="large"
                />
            </Form.Item>

            <Form.Item
                label="New Password"
                name="newPassword"
                rules={[
                    { required: true, message: "Please enter new password" },
                    { min: 6, message: "Password must be at least 6 characters" },
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined className="input-icon" />}
                    placeholder="Enter new password"
                    size="large"
                />
            </Form.Item>

            <Form.Item
                label="Confirm Password"
                name="confirmPassword"
                dependencies={["newPassword"]}
                hasFeedback
                rules={[
                    { required: true, message: "Please confirm the password" },
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (!value || getFieldValue("newPassword") === value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error("Passwords do not match"));
                        },
                    }),
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined className="input-icon" />}
                    placeholder="Confirm new password"
                    size="large"
                />
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<LockOutlined />}
                    loading={submitting}
                    block
                    size="large"
                    className="submit-btn"
                >
                    Reset Password
                </Button>
            </Form.Item>
        </Form>
    );
};

// Main Component
export default function EmployeeManagement() {
    const role = localStorage.getItem("_User_role_for_MSIPL");
    const isAdmin = role === "admin";

    const tabItems = [
        ...(isAdmin ? [{
            key: "role",
            label: (
                <span className="tab-label">
                    <UserSwitchOutlined />
                    <span>Change Role</span>
                </span>
            ),
            children: <ChangeRoleTab />,
        }] : []),
        ...(isAdmin ? [{
            key: "region",
            label: (
                <span className="tab-label">
                    <GlobalOutlined />
                    <span>Change Region</span>
                </span>
            ),
            children: <ChangeRegionTab />,
        }] : []),
        {
            key: "password",
            label: (
                <span className="tab-label">
                    <LockOutlined />
                    <span>Reset Password</span>
                </span>
            ),
            children: <ResetPasswordTab />,
        },
    ];

    return (
        <div className="employee-management-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <TeamOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Employee Management</Title>
                        <Text className="header-subtitle">
                            Manage employee roles, regions, and credentials
                        </Text>
                    </div>
                </div>
            </div>

            {/* Main Card with Tabs */}
            <Card className="management-card" bordered={false}>
                <Tabs
                    defaultActiveKey={isAdmin ? "role" : "password"}
                    items={tabItems}
                    className="management-tabs"
                />
            </Card>
        </div>
    );
}
