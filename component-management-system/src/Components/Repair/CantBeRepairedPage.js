import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Spin,
    Card,
    Row,
    Col,
    Badge,
    Empty,
    Divider,
    Button,
} from "antd";
import {
    WarningOutlined,
    ExclamationCircleOutlined,
    SwapOutlined,
    ReloadOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

export default function CantBeRepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getCantBeRepairedItems();
        if (result.success) {
            setItems(result.data || []);
        } else {
            message.error("Failed to load items");
        }
        setLoading(false);
    };

    useEffect(() => {
        loadItems();
    }, []);

    // Group items by RMA number
    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) {
            acc[rmaNo] = [];
        }
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRmaRequests = Object.keys(groupedItems).length;
    const totalItems = items.length;

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section - Red Theme for Can't Be Repaired */}
                <div className="unrepaired-header" style={{ background: "linear-gradient(135deg, #f5222d 0%, #cf1322 100%)", boxShadow: "0 4px 20px rgba(245, 34, 45, 0.3)" }}>
                    <div className="header-content">
                        <div className="header-title">
                            <WarningOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Can't Be Repaired
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items requiring replacement
                                </Text>
                            </div>
                        </div>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={loadItems}
                            loading={loading}
                            className="refresh-btn"
                        >
                            Refresh
                        </Button>
                    </div>

                    {/* Stats Row */}
                    <Row gutter={16} className="stats-row">
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <ExclamationCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">Affected RMAs</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <SwapOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Need Replacement</div>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </div>

                {/* Content Section */}
                <div className="unrepaired-content">
                    {loading ? (
                        <div className="loading-container">
                            <Spin size="large" />
                            <Text style={{ marginTop: 16 }}>Loading items...</Text>
                        </div>
                    ) : totalRmaRequests === 0 ? (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="No items requiring replacement"
                            className="empty-state"
                        />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card
                                    key={rmaNo}
                                    className="rma-group-card"
                                    style={{ borderLeft: "4px solid #f5222d" }}
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number">
                                                <Tag color="#f5222d" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                            </span>
                                            <Badge
                                                count={rmaItems.length}
                                                style={{ backgroundColor: "#f5222d" }}
                                                overflowCount={99}
                                            />
                                        </div>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card
                                                    className="item-card"
                                                    size="small"
                                                    hoverable
                                                    style={{ borderTop: "3px solid #f5222d" }}
                                                    actions={[
                                                        <Button
                                                            type="primary"
                                                            icon={<SwapOutlined />}
                                                            style={{ backgroundColor: "#722ed1", borderColor: "#722ed1" }}
                                                            className="assign-btn"
                                                        >
                                                            Process Replacement
                                                        </Button>
                                                    ]}
                                                >
                                                    <div className="item-content">
                                                        <div className="item-row">
                                                            <Text type="secondary">Product</Text>
                                                            <Text strong>{item.product || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Serial No.</Text>
                                                            <Text code>{item.serialNo || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Model</Text>
                                                            <Text>{item.model || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Status</Text>
                                                            <Tag color="red" icon={<WarningOutlined />}>
                                                                CAN'T BE REPAIRED
                                                            </Tag>
                                                        </div>
                                                        {item.itemRmaNo && (
                                                            <div className="item-row">
                                                                <Text type="secondary">RMA Number</Text>
                                                                <Tag color="green">{item.itemRmaNo}</Tag>
                                                            </div>
                                                        )}
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section">
                                                            <Text type="secondary">Remarks</Text>
                                                            <Paragraph
                                                                ellipsis={{ rows: 2, expandable: true }}
                                                                className="fault-text"
                                                            >
                                                                {item.repairRemarks || "No remarks provided"}
                                                            </Paragraph>
                                                        </div>
                                                    </div>
                                                </Card>
                                            </Col>
                                        ))}
                                    </Row>
                                </Card>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </RmaLayout>
    );
}
