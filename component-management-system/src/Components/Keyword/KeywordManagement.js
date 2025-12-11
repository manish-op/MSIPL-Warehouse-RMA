// src/Components/Keyword/KeywordManagement.js
import React, { useState, useEffect } from "react";
import {
    Form,
    Input,
    Button,
    Select,
    Card,
    Tabs,
    Space,
    Typography,
    Divider,
    Row,
    Col,
    message,
} from "antd";
import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    TagOutlined,
    TagsOutlined,
} from "@ant-design/icons";
import GetKeywordAPI from "../API/Keyword/GetKeyword/GetKeywordAPI";
import AddKeywordAPI from "../API/Keyword/AddKeyword/AddKeywordAPI";
import AddSubKeywordAPI from "../API/Keyword/SubKeyword/AddSubKeyword/AddSubKeywordAPI";
import UpdateKeywordAPI from "../API/Keyword/UpdateKeyword/UpdateKeywordAPI";
import GetSubKeywordAPI from "../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import UpdateSubKeywordAPI from "../API/Keyword/SubKeyword/UpdateSubKeyword/UpdateSubKeywordAPI";
import "./KeywordManagement.css";

const { Title, Text } = Typography;

const KeywordManagement = () => {
    const [keywords, setKeywords] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState("1");

    // Forms for each tab
    const [addKeywordForm] = Form.useForm();
    const [addSubKeywordForm] = Form.useForm();
    const [updateKeywordForm] = Form.useForm();
    const [updateSubKeywordForm] = Form.useForm();

    // State for dynamic subkeyword fields
    const [addKeywordSubs, setAddKeywordSubs] = useState([{ subKeyword: "" }]);
    const [addSubKeywordSubs, setAddSubKeywordSubs] = useState([{ subKeyword: "" }]);

    // State for update subkeyword
    // eslint-disable-next-line no-unused-vars
    const [selectedKeyword, setSelectedKeyword] = useState("");
    const [subkeywords, setSubkeywords] = useState([]);

    // Fetch keywords on load
    useEffect(() => {
        fetchKeywords();
    }, []);

    const fetchKeywords = async () => {
        setLoading(true);
        try {
            const data = await GetKeywordAPI();
            if (data) {
                setKeywords(data);
            }
        } catch (error) {
            console.error("Failed to fetch keywords:", error);
        }
        setLoading(false);
    };

    // Fetch subkeywords when keyword is selected
    const fetchSubKeywords = async (keywordName) => {
        try {
            const data = await GetSubKeywordAPI(keywordName);
            if (data && data.subKeywordList) {
                const subKeywordValues = data.subKeywordList.map((item) => item.subKeyword);
                setSubkeywords(subKeywordValues);
            }
        } catch (error) {
            console.error("Failed to fetch subkeywords:", error);
        }
    };

    // ============ ADD KEYWORD ============
    const handleAddKeyword = async (values) => {
        const data = {
            keyword: values.keyword,
            subKeywordList: values.subKeywordList
                ? values.subKeywordList.map((sub) => ({ subKeyword: sub.subKeyword }))
                : [],
        };
        const result = await AddKeywordAPI(data);
        if (result) {
            message.success("Keyword added successfully!");
            addKeywordForm.resetFields();
            setAddKeywordSubs([{ subKeyword: "" }]);
            fetchKeywords();
        }
    };

    // ============ ADD SUBKEYWORD ============
    const handleAddSubKeyword = async (values) => {
        const data = {
            keyword: values.keyword,
            subKeywordList: values.subKeywordList
                ? values.subKeywordList.map((sub) => ({ subKeyword: sub.subKeyword }))
                : [],
        };
        const result = await AddSubKeywordAPI(data);
        if (result) {
            message.success("Subkeyword(s) added successfully!");
            addSubKeywordForm.resetFields();
            setAddSubKeywordSubs([{ subKeyword: "" }]);
        }
    };

    // ============ UPDATE KEYWORD ============
    const handleUpdateKeyword = async (values) => {
        const data = {
            oldKeyword: values.oldKeyword,
            newKeyword: values.newKeyword,
        };
        const result = await UpdateKeywordAPI(data);
        if (result) {
            message.success("Keyword updated successfully!");
            updateKeywordForm.resetFields();
            fetchKeywords();
        }
    };

    // ============ UPDATE SUBKEYWORD ============
    const handleKeywordChange = (value) => {
        setSelectedKeyword(value);
        setSubkeywords([]);
        updateSubKeywordForm.setFieldsValue({ oldSubKeyword: null });
        if (value) {
            fetchSubKeywords(value);
        }
    };

    const handleUpdateSubKeyword = async (values) => {
        const data = {
            keywordName: values.keywordName,
            oldSubKeyword: values.oldSubKeyword,
            updateSubKeyword: values.updateSubKeyword,
        };
        const result = await UpdateSubKeywordAPI(data);
        if (result) {
            message.success("Subkeyword updated successfully!");
            updateSubKeywordForm.resetFields();
            setSubkeywords([]);
            setSelectedKeyword("");
        }
    };

    // Dynamic subkeyword field helpers
    const addSubField = (type) => {
        if (type === "addKeyword") {
            setAddKeywordSubs([...addKeywordSubs, { subKeyword: "" }]);
        } else {
            setAddSubKeywordSubs([...addSubKeywordSubs, { subKeyword: "" }]);
        }
    };

    const removeSubField = (index, type) => {
        if (type === "addKeyword") {
            const updated = [...addKeywordSubs];
            updated.splice(index, 1);
            setAddKeywordSubs(updated);
        } else {
            const updated = [...addSubKeywordSubs];
            updated.splice(index, 1);
            setAddSubKeywordSubs(updated);
        }
    };

    return (
        <div className="keyword-management-container">
            <div className="keyword-management-header">
                <TagsOutlined className="header-icon" />
                <div>
                    <Title level={2} style={{ margin: 0, color: "#fff" }}>
                        Keyword Management
                    </Title>
                    <Text style={{ color: "rgba(255,255,255,0.8)" }}>
                        Manage keywords and subkeywords for your inventory
                    </Text>
                </div>
            </div>

            <Card className="keyword-management-card">
                <Tabs
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    type="card"
                    size="large"
                    items={[
                        {
                            key: "1",
                            label: (
                                <span>
                                    <PlusOutlined /> Add Keyword
                                </span>
                            ),
                            children: (
                                <div className="tab-content">
                                    <Form
                                        form={addKeywordForm}
                                        layout="vertical"
                                        onFinish={handleAddKeyword}
                                    >
                                        <Form.Item
                                            label="Keyword Name"
                                            name="keyword"
                                            rules={[{ required: true, message: "Please enter a keyword!" }]}
                                        >
                                            <Input
                                                placeholder="Enter keyword name"
                                                size="large"
                                                prefix={<TagOutlined />}
                                            />
                                        </Form.Item>

                                        <Divider orientation="left">Subkeywords (Optional)</Divider>

                                        {addKeywordSubs.map((_, index) => (
                                            <Space key={index} align="baseline" style={{ display: "flex", marginBottom: 8 }}>
                                                <Form.Item
                                                    name={["subKeywordList", index, "subKeyword"]}
                                                    style={{ marginBottom: 0, width: 300 }}
                                                >
                                                    <Input placeholder={`Subkeyword ${index + 1}`} />
                                                </Form.Item>
                                                {addKeywordSubs.length > 1 && (
                                                    <Button
                                                        danger
                                                        icon={<DeleteOutlined />}
                                                        onClick={() => removeSubField(index, "addKeyword")}
                                                    />
                                                )}
                                            </Space>
                                        ))}

                                        <Button
                                            type="dashed"
                                            onClick={() => addSubField("addKeyword")}
                                            icon={<PlusOutlined />}
                                            style={{ marginBottom: 16 }}
                                        >
                                            Add Subkeyword
                                        </Button>

                                        <Form.Item>
                                            <Button type="primary" htmlType="submit" size="large" block>
                                                Create Keyword
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </div>
                            ),
                        },
                        {
                            key: "2",
                            label: (
                                <span>
                                    <PlusOutlined /> Add SubKeyword
                                </span>
                            ),
                            children: (
                                <div className="tab-content">
                                    <Form
                                        form={addSubKeywordForm}
                                        layout="vertical"
                                        onFinish={handleAddSubKeyword}
                                    >
                                        <Form.Item
                                            label="Select Keyword"
                                            name="keyword"
                                            rules={[{ required: true, message: "Please select a keyword!" }]}
                                        >
                                            <Select
                                                placeholder="Select keyword"
                                                size="large"
                                                showSearch
                                                loading={loading}
                                                optionFilterProp="children"
                                                filterOption={(input, option) =>
                                                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                                                }
                                            >
                                                {keywords.map((kw) => (
                                                    <Select.Option key={kw} value={kw}>
                                                        {kw}
                                                    </Select.Option>
                                                ))}
                                            </Select>
                                        </Form.Item>

                                        <Divider orientation="left">New Subkeywords</Divider>

                                        {addSubKeywordSubs.map((_, index) => (
                                            <Space key={index} align="baseline" style={{ display: "flex", marginBottom: 8 }}>
                                                <Form.Item
                                                    name={["subKeywordList", index, "subKeyword"]}
                                                    rules={[{ required: true, message: "Enter subkeyword" }]}
                                                    style={{ marginBottom: 0, width: 300 }}
                                                >
                                                    <Input placeholder={`Subkeyword ${index + 1}`} />
                                                </Form.Item>
                                                {addSubKeywordSubs.length > 1 && (
                                                    <Button
                                                        danger
                                                        icon={<DeleteOutlined />}
                                                        onClick={() => removeSubField(index, "addSubKeyword")}
                                                    />
                                                )}
                                            </Space>
                                        ))}

                                        <Button
                                            type="dashed"
                                            onClick={() => addSubField("addSubKeyword")}
                                            icon={<PlusOutlined />}
                                            style={{ marginBottom: 16 }}
                                        >
                                            Add More Subkeywords
                                        </Button>

                                        <Form.Item>
                                            <Button type="primary" htmlType="submit" size="large" block>
                                                Add Subkeywords
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </div>
                            ),
                        },
                        {
                            key: "3",
                            label: (
                                <span>
                                    <EditOutlined /> Update Keyword
                                </span>
                            ),
                            children: (
                                <div className="tab-content">
                                    <Form
                                        form={updateKeywordForm}
                                        layout="vertical"
                                        onFinish={handleUpdateKeyword}
                                    >
                                        <Row gutter={24}>
                                            <Col xs={24} md={12}>
                                                <Form.Item
                                                    label="Select Keyword to Update"
                                                    name="oldKeyword"
                                                    rules={[{ required: true, message: "Select a keyword!" }]}
                                                >
                                                    <Select
                                                        placeholder="Select keyword"
                                                        size="large"
                                                        showSearch
                                                        loading={loading}
                                                        optionFilterProp="children"
                                                        filterOption={(input, option) =>
                                                            (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                                                        }
                                                    >
                                                        {keywords.map((kw) => (
                                                            <Select.Option key={kw} value={kw}>
                                                                {kw}
                                                            </Select.Option>
                                                        ))}
                                                    </Select>
                                                </Form.Item>
                                            </Col>
                                            <Col xs={24} md={12}>
                                                <Form.Item
                                                    label="New Keyword Name"
                                                    name="newKeyword"
                                                    rules={[{ required: true, message: "Enter new name!" }]}
                                                >
                                                    <Input placeholder="New keyword name" size="large" />
                                                </Form.Item>
                                            </Col>
                                        </Row>

                                        <Form.Item>
                                            <Button type="primary" htmlType="submit" size="large" block>
                                                Update Keyword
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </div>
                            ),
                        },
                        {
                            key: "4",
                            label: (
                                <span>
                                    <EditOutlined /> Update SubKeyword
                                </span>
                            ),
                            children: (
                                <div className="tab-content">
                                    <Form
                                        form={updateSubKeywordForm}
                                        layout="vertical"
                                        onFinish={handleUpdateSubKeyword}
                                    >
                                        <Form.Item
                                            label="Select Keyword"
                                            name="keywordName"
                                            rules={[{ required: true, message: "Select a keyword!" }]}
                                        >
                                            <Select
                                                placeholder="Select keyword"
                                                size="large"
                                                showSearch
                                                loading={loading}
                                                onChange={handleKeywordChange}
                                                optionFilterProp="children"
                                                filterOption={(input, option) =>
                                                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                                                }
                                            >
                                                {keywords.map((kw) => (
                                                    <Select.Option key={kw} value={kw}>
                                                        {kw}
                                                    </Select.Option>
                                                ))}
                                            </Select>
                                        </Form.Item>

                                        {subkeywords.length > 0 && (
                                            <>
                                                <Row gutter={24}>
                                                    <Col xs={24} md={12}>
                                                        <Form.Item
                                                            label="Select SubKeyword to Update"
                                                            name="oldSubKeyword"
                                                            rules={[{ required: true, message: "Select subkeyword!" }]}
                                                        >
                                                            <Select
                                                                placeholder="Select subkeyword"
                                                                size="large"
                                                                showSearch
                                                                optionFilterProp="children"
                                                                filterOption={(input, option) =>
                                                                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                                                                }
                                                            >
                                                                {subkeywords.map((sk) => (
                                                                    <Select.Option key={sk} value={sk}>
                                                                        {sk}
                                                                    </Select.Option>
                                                                ))}
                                                            </Select>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col xs={24} md={12}>
                                                        <Form.Item
                                                            label="New SubKeyword Name"
                                                            name="updateSubKeyword"
                                                            rules={[{ required: true, message: "Enter new name!" }]}
                                                        >
                                                            <Input placeholder="New subkeyword name" size="large" />
                                                        </Form.Item>
                                                    </Col>
                                                </Row>
                                            </>
                                        )}

                                        <Form.Item>
                                            <Button
                                                type="primary"
                                                htmlType="submit"
                                                size="large"
                                                block
                                                disabled={subkeywords.length === 0}
                                            >
                                                Update SubKeyword
                                            </Button>
                                        </Form.Item>
                                    </Form>
                                </div>
                            ),
                        },
                    ]}
                />
            </Card>
        </div>
    );
};

export default KeywordManagement;
