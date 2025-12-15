import React, { useState, useEffect } from "react";
import {
    Card, Form, Button, Input, Select, message, Typography, Tabs,
} from "antd";
import {
    PlusOutlined, EditOutlined, CheckCircleOutlined, SettingOutlined,
} from "@ant-design/icons";
import Cookies from "js-cookie";
import GetItemAvailabilityStatusOptionAPI from "../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import GetItemStatusOptionAPI from "../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import { URL } from "../API/URL";
import "./StatusManagement.css";

const { Title, Text } = Typography;

// Helper to get auth token
const getToken = () => {
    try {
        return atob(Cookies.get("authToken"));
    } catch {
        return null;
    }
};

// Tab 1: Add Availability Status
const AddAvailabilityTab = () => {
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/option/item-availability", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: values.status,
            });

            const responseText = await response.text();
            if (response.ok) {
                message.success(responseText || "Availability status added", 1);
                form.resetFields();
            } else {
                message.error(responseText || "Failed to add status", 2);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="add-availability-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Add new availability status options for inventory items.
                </Text>
            </div>

            <Form.Item
                label="Availability Status Name"
                name="status"
                rules={[{ required: true, message: "Please enter a new availability status!" }]}
            >
                <Input
                    prefix={<CheckCircleOutlined className="input-icon" />}
                    placeholder="Enter new availability status"
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
                    Add Availability Status
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 2: Update Availability Status
const UpdateAvailabilityTab = () => {
    const [form] = Form.useForm();
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchOptions = async () => {
            setLoading(true);
            try {
                const data = await GetItemAvailabilityStatusOptionAPI();
                if (data) setOptions(data);
            } catch (error) {
                console.error("API Error:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchOptions();
    }, []);

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/option/item-availability", {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(values),
            });

            if (response.ok) {
                message.success("Updated successfully!", 1);
                form.resetFields();
                // Refresh options
                const data = await GetItemAvailabilityStatusOptionAPI();
                if (data) setOptions(data);
            } else {
                const errorText = await response.text();
                message.error(errorText || "Update failed", 2);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="update-availability-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Update existing availability status options.
                </Text>
            </div>

            <Form.Item
                label="Select Availability Status"
                name="existingOption"
                rules={[{ required: true, message: "Please select a status!" }]}
            >
                <Select
                    placeholder={loading ? "Loading..." : "Select status to update"}
                    loading={loading}
                    disabled={loading || options.length === 0}
                    size="large"
                    showSearch
                    optionFilterProp="children"
                    suffixIcon={<CheckCircleOutlined className="input-icon" />}
                >
                    {options.map((option) => (
                        <Select.Option key={option} value={option}>
                            {option}
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item
                label="New Status Name"
                name="newOption"
                rules={[{ required: true, message: "Please enter the new status name!" }]}
            >
                <Input
                    prefix={<EditOutlined className="input-icon" />}
                    placeholder="Enter new status name"
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
                    Update Availability Status
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 3: Add Item Status
const AddItemStatusTab = () => {
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/option/item-status", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: values.itemStatus,
            });

            const responseText = await response.text();
            if (response.ok) {
                message.success(responseText || "Item status added", 1);
                form.resetFields();
            } else {
                message.error(responseText || "Failed to add status", 2);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="add-item-status-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Add new item status options for inventory items.
                </Text>
            </div>

            <Form.Item
                label="Item Status Name"
                name="itemStatus"
                rules={[{ required: true, message: "Please enter a new item status!" }]}
            >
                <Input
                    prefix={<SettingOutlined className="input-icon" />}
                    placeholder="Enter new item status"
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
                    Add Item Status
                </Button>
            </Form.Item>
        </Form>
    );
};

// Tab 4: Update Item Status
const UpdateItemStatusTab = () => {
    const [form] = Form.useForm();
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchOptions = async () => {
            setLoading(true);
            try {
                const data = await GetItemStatusOptionAPI();
                if (data) setOptions(data);
            } catch (error) {
                console.error("API Error:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchOptions();
    }, []);

    const onFinish = async (values) => {
        const token = getToken();
        if (!token) {
            message.error("Authentication required");
            return;
        }

        setSubmitting(true);
        try {
            const response = await fetch(URL + "/option/item-status", {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(values),
            });

            if (response.ok) {
                message.success("Updated successfully!", 1);
                form.resetFields();
                // Refresh options
                const data = await GetItemStatusOptionAPI();
                if (data) setOptions(data);
            } else {
                const errorText = await response.text();
                message.error(errorText || "Update failed", 2);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("An error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false} name="update-item-status-form">
            <div className="tab-description">
                <Text className="tab-desc-text">
                    Update existing item status options.
                </Text>
            </div>

            <Form.Item
                label="Select Item Status"
                name="oldStatus"
                rules={[{ required: true, message: "Please select a status!" }]}
            >
                <Select
                    placeholder={loading ? "Loading..." : "Select status to update"}
                    loading={loading}
                    disabled={loading || options.length === 0}
                    size="large"
                    showSearch
                    optionFilterProp="children"
                    suffixIcon={<SettingOutlined className="input-icon" />}
                >
                    {options.map((option) => (
                        <Select.Option key={option} value={option}>
                            {option}
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item
                label="New Status Name"
                name="newStatus"
                rules={[{ required: true, message: "Please enter the new status name!" }]}
            >
                <Input
                    prefix={<EditOutlined className="input-icon" />}
                    placeholder="Enter new status name"
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
                    Update Item Status
                </Button>
            </Form.Item>
        </Form>
    );
};

// Main Component
export default function StatusManagement() {
    const tabItems = [
        {
            key: "addAvailability",
            label: (
                <span className="tab-label">
                    <PlusOutlined />
                    <span>Add Availability</span>
                </span>
            ),
            children: <AddAvailabilityTab />,
        },
        {
            key: "updateAvailability",
            label: (
                <span className="tab-label">
                    <EditOutlined />
                    <span>Update Availability</span>
                </span>
            ),
            children: <UpdateAvailabilityTab />,
        },
        {
            key: "addItemStatus",
            label: (
                <span className="tab-label">
                    <PlusOutlined />
                    <span>Add Item Status</span>
                </span>
            ),
            children: <AddItemStatusTab />,
        },
        {
            key: "updateItemStatus",
            label: (
                <span className="tab-label">
                    <EditOutlined />
                    <span>Update Item Status</span>
                </span>
            ),
            children: <UpdateItemStatusTab />,
        },
    ];

    return (
        <div className="status-management-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <SettingOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Status Management</Title>
                        <Text className="header-subtitle">
                            Manage availability and item status options
                        </Text>
                    </div>
                </div>
            </div>

            {/* Main Card with Tabs */}
            <Card className="management-card" bordered={false}>
                <Tabs
                    defaultActiveKey="addAvailability"
                    items={tabItems}
                    className="management-tabs"
                />
            </Card>
        </div>
    );
}
