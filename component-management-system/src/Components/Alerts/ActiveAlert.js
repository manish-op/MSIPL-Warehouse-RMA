import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Typography, Empty, Spin, Badge, Button, message } from 'antd';
import {
    AlertOutlined,
    WarningOutlined,
    ExclamationCircleOutlined,
    CheckCircleOutlined,
    ReloadOutlined,
    ClockCircleOutlined,
} from '@ant-design/icons';
import { AlertApi } from '../API/AlertApi/AlertApi';
import './ActiveAlert.css';

const { Title, Text } = Typography;

function ActiveAlerts() {
    const [alerts, setAlerts] = useState([]);
    const [alertCount, setAlertCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const navigate = useNavigate();

    const fetchAlerts = async (showMessage = false) => {
        if (showMessage) setRefreshing(true);
        else setLoading(true);

        try {
            const data = await AlertApi.getActiveAlerts();
            if (data) {
                setAlerts(data.messages || []);
                setAlertCount(data.count || 0);
                if (showMessage) message.success('Alerts refreshed');
            }
        } catch (error) {
            console.error('Failed to fetch alerts:', error);
            if (showMessage) message.error('Failed to refresh alerts');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchAlerts();
        const interval = setInterval(() => fetchAlerts(), 60000);
        return () => clearInterval(interval);
    }, []);

    const getSeverity = (alert) => {
        // Handle both string (old format fallback) and object (new format)
        if (typeof alert === 'string') {
            const lowerMsg = alert.toLowerCase();
            if (lowerMsg.includes('critical') || lowerMsg.includes('urgent') || lowerMsg.includes('0 left')) {
                return 'critical';
            } else if (lowerMsg.includes('low') || lowerMsg.includes('below')) {
                return 'warning';
            }
            return 'info';
        }
        return alert.severity ? alert.severity.toLowerCase() : 'info';
    };

    const getSeverityIcon = (severity) => {
        switch (severity) {
            case 'critical':
                return <ExclamationCircleOutlined className="alert-severity-icon critical" />;
            case 'warning':
                return <WarningOutlined className="alert-severity-icon warning" />;
            default:
                return <CheckCircleOutlined className="alert-severity-icon info" />;
        }
    };

    const getSeverityLabel = (severity) => {
        switch (severity) {
            case 'critical':
                return <Badge color="#ff4d4f" text="Critical" />;
            case 'warning':
                return <Badge color="#faad14" text="Warning" />;
            default:
                return <Badge color="#1890ff" text="Info" />;
        }
    };

    // Group alerts by severity
    const groupedAlerts = alerts.reduce((acc, alert) => {
        const severity = getSeverity(alert);
        if (!acc[severity]) acc[severity] = [];
        acc[severity].push(alert);
        return acc;
    }, {});

    const criticalCount = groupedAlerts.critical?.length || 0;
    const warningCount = groupedAlerts.warning?.length || 0;
    const infoCount = groupedAlerts.info?.length || 0;

    return (
        <div className="active-alerts-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <AlertOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Active Alerts</Title>
                        <Text className="header-subtitle">
                            Monitor inventory threshold alerts and notifications
                        </Text>
                    </div>
                </div>
                <Button
                    icon={<ReloadOutlined spin={refreshing} />}
                    onClick={() => fetchAlerts(true)}
                    loading={refreshing}
                    className="refresh-btn"
                >
                    Refresh
                </Button>
            </div>

            {/* Stats Cards */}
            <div className="alert-stats">
                <div className="stat-card total">
                    <div className="stat-icon">
                        <AlertOutlined />
                    </div>
                    <div className="stat-info">
                        <span className="stat-value">{alertCount}</span>
                        <span className="stat-label">Total Alerts</span>
                    </div>
                </div>

                <div className="stat-card critical">
                    <div className="stat-icon">
                        <ExclamationCircleOutlined />
                    </div>
                    <div className="stat-info">
                        <span className="stat-value">{criticalCount}</span>
                        <span className="stat-label">Critical</span>
                    </div>
                </div>

                <div className="stat-card warning">
                    <div className="stat-icon">
                        <WarningOutlined />
                    </div>
                    <div className="stat-info">
                        <span className="stat-value">{warningCount}</span>
                        <span className="stat-label">Warnings</span>
                    </div>
                </div>

                <div className="stat-card info">
                    <div className="stat-icon">
                        <CheckCircleOutlined />
                    </div>
                    <div className="stat-info">
                        <span className="stat-value">{infoCount}</span>
                        <span className="stat-label">Info</span>
                    </div>
                </div>
            </div>

            {/* Alerts List */}
            <Card className="alerts-card" bordered={false}>
                {loading ? (
                    <div className="alerts-loading">
                        <Spin size="large" />
                        <Text className="loading-text">Loading alerts...</Text>
                    </div>
                ) : alertCount === 0 ? (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description={
                            <span className="empty-text">
                                No active alerts. All inventory levels are healthy!
                            </span>
                        }
                        className="alerts-empty"
                    />
                ) : (
                    <div className="alerts-list">
                        {alerts.map((alert, index) => {
                            const severity = getSeverity(alert);
                            const msgText = typeof alert === 'string' ? alert : alert.message;
                            const serialNo = typeof alert === 'object' ? alert.serialNo : null;

                            return (
                                <div key={index} className={`alert-item ${severity}`}>
                                    <div className="alert-item-left">
                                        <div className="alert-icon-wrapper">
                                            {getSeverityIcon(severity)}
                                        </div>
                                        <div className="alert-content">
                                            <p className="alert-message">{msgText}</p>
                                            <div className="alert-meta">
                                                <span className="alert-time">
                                                    <ClockCircleOutlined /> Active now
                                                </span>
                                                {getSeverityLabel(severity)}
                                            </div>
                                        </div>
                                    </div>
                                    {serialNo && (
                                        <Button
                                            type="primary"
                                            size="small"
                                            className="resolve-btn"
                                            onClick={() => navigate('/dashboard/updateItem', { state: { serialNo } })}
                                        >
                                            Resolve
                                        </Button>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )}
            </Card>

            {/* Auto-refresh Indicator */}
            <div className="auto-refresh-note">
                <ClockCircleOutlined /> Alerts refresh automatically every 60 seconds
            </div>
        </div>
    );
}

export default ActiveAlerts;
