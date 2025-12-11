import React, { useState, useEffect } from "react";
import {
    Card, Form, Button, Input, Select, message, Typography, Tabs,
} from "antd";
import {
    PlusOutlined, EditOutlined, GlobalOutlined,
} from "@ant-design/icons";
import Cookies from "js-cookie";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import { URL } from "../API/URL";
import "./RegionManagement.css";

const { Title, Text } = Typography;

// Tab 1: Add Region Component
const AddRegionTab = () => {
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);

    const getToken = () => {
        try {
            return atob(Cookies.get("authToken"));
        } catch {
            return null;
        }
    };

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/region", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: values.region,
            });

            const responseText = await response.text();

            if (!response.ok) {
                message.warning(responseText, 2);
            } else {
                message.success("Region added successfully", 1);
                form.resetFields();
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="add-region-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Add a new region to the system for inventory management.
                </Text>
            </div>

            <Form.Item
                label="Region Name"
                name="region"
                rules={[{ required: true, message: "Please enter the new region name!" }]}
            >
                <Input
                    prefix={<GlobalOutlined className="input-icon" />}
                    placeholder="Enter new region name"
                    size="large"
                />
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<PlusOutlined />}
                    loading={submitting}
                    block
                    size="large"
                    className="submit-btn"
                >
                    Add Region
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 2: Update Region Component
const UpdateRegionTab = () => {
    const [form] = Form.useForm();
    const [regions, setRegions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const getToken = () => {
        try {
            return atob(Cookies.get("authToken"));
        } catch {
            return null;
        }
    };

    useEffect(() => {
        const fetchRegions = async () => {
            setLoading(true);
            try {
                const data = await GetRegionAPI();
                if (data) {
                    setRegions(data);
                } else {
                    message.warning("Failed to fetch regions");
                }
            } catch (error) {
                console.error("API Error:", error);
                message.error("An error occurred while loading regions.");
            } finally {
                setLoading(false);
            }
        };
        fetchRegions();
    }, []);

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/region", {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(values),
            });

            if (!response.ok) {
                const errorText = await response.text();
                message.warning(errorText, 2);
            } else {
                message.success("Region updated successfully", 2);
                form.resetFields();
                // Refresh regions list
                const data = await GetRegionAPI();
                if (data) setRegions(data);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="update-region-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Update an existing region name in the system.
                </Text>
            </div>

            <Form.Item
                label="Select Region"
                name="oldRegion"
                rules={[{ required: true, message: "Please select a region!" }]}
            >
                <Select
                    placeholder={loading ? "Loading regions..." : "Select region to update"}
                    loading={loading}
                    disabled={loading || regions.length === 0}
                    size="large"
                    showSearch
                    optionFilterProp="children"
                    filterOption={(input, option) =>
                        (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                    }
                    suffixIcon={<GlobalOutlined className="input-icon" />}
                >
                    {regions.map((region) => (
                        <Select.Option key={region} value={region}>
                            {region}
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item
                label="New Region Name"
                name="updatedRegion"
                rules={[{ required: true, message: "Please enter the new region name!" }]}
            >
                <Input
                    prefix={<EditOutlined className="input-icon" />}
                    placeholder="Enter new region name"
                    size="large"
                />
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<EditOutlined />}
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

// Main Component
export default function RegionManagement() {
    const role = localStorage.getItem("_User_role_for_MSIPL");
    const isAdmin = role === "admin";

    const tabItems = [
        {
            key: "add",
            label: (
                <span className="tab-label">
                    <PlusOutlined />
                    <span>Add Region</span>
                </span>
            ),
            children: <AddRegionTab />,
        },
        {
            key: "update",
            label: (
                <span className="tab-label">
                    <EditOutlined />
                    <span>Update Region</span>
                </span>
            ),
            children: <UpdateRegionTab />,
        },
    ];

    if (!isAdmin) {
        return (
            <div className="region-management-page">
                <div className="page-header">
                    <div className="header-content">
                        <div className="header-icon">
                            <GlobalOutlined />
                        </div>
                        <div className="header-text">
                            <Title level={3} className="header-title">Region Management</Title>
                            <Text className="header-subtitle">
                                Admin access required
                            </Text>
                        </div>
                    </div>
                </div>
                <Card className="management-card" bordered={false}>
                    <Text>Only administrators can manage regions.</Text>
                </Card>
            </div>
        );
    }

    return (
        <div className="region-management-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <GlobalOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Region Management</Title>
                        <Text className="header-subtitle">
                            Add and update regions for inventory management
                        </Text>
                    </div>
                </div>
            </div>

            {/* Main Card with Tabs */}
            <Card className="management-card" bordered={false}>
                <Tabs
                    defaultActiveKey="add"
                    items={tabItems}
                    className="management-tabs"
                />
            </Card>
        </div>
    );
}
