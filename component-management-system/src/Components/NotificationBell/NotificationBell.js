import React, { useState, useEffect } from 'react';
import { Badge, Popover, Spin, Empty, Button } from 'antd';
import {
  BellOutlined,
  WarningOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  RightOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { AlertApi } from '../API/AlertApi/AlertApi';
import './NotificationBell.css';

const NotificationBell = () => {
  const navigate = useNavigate();
  const [alertMessages, setAlertMessages] = useState([]);
  const [alertCount, setAlertCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [hasNewAlerts, setHasNewAlerts] = useState(false);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const data = await AlertApi.getActiveAlerts();
      if (data) {
        // Check if there are new alerts
        if (data.count > alertCount && alertCount > 0) {
          setHasNewAlerts(true);
          setTimeout(() => setHasNewAlerts(false), 5000);
        }
        setAlertMessages(data.messages || []);
        setAlertCount(data.count || 0);
      }
    } catch (error) {
      console.error("Failed to fetch alerts:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
    const interval = setInterval(fetchAlerts, 60000);
    return () => clearInterval(interval);
  }, []);

  // Determine severity based on message content
  const getSeverity = (message) => {
    const lowerMsg = message.toLowerCase();
    if (lowerMsg.includes('critical') || lowerMsg.includes('urgent') || lowerMsg.includes('0 left')) {
      return 'critical';
    } else if (lowerMsg.includes('low') || lowerMsg.includes('below')) {
      return 'warning';
    }
    return 'info';
  };

  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'critical':
        return <ExclamationCircleOutlined className="alert-icon critical" />;
      case 'warning':
        return <WarningOutlined className="alert-icon warning" />;
      default:
        return <CheckCircleOutlined className="alert-icon info" />;
    }
  };

  const handleViewAll = () => {
    setIsOpen(false);
    navigate('alerts/active');
  };

  const content = (
    <div className="notification-content">
      {/* Header */}
      <div className="notification-header">
        <span className="notification-title">
          <BellOutlined /> Alerts
        </span>
        <span className="notification-count">
          {alertCount} active
        </span>
      </div>

      {/* Content */}
      <div className="notification-body">
        {loading && (
          <div className="notification-loading">
            <Spin size="small" />
            <span>Loading alerts...</span>
          </div>
        )}

        {!loading && alertCount === 0 && (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="No active alerts"
            className="notification-empty"
          />
        )}

        {!loading && alertMessages.map((msg, index) => {
          const severity = getSeverity(msg);
          return (
            <div key={index} className={`notification-item ${severity}`}>
              <div className="notification-item-icon">
                {getSeverityIcon(severity)}
              </div>
              <div className="notification-item-content">
                <p className="notification-message">{msg}</p>
                <span className="notification-time">
                  <ClockCircleOutlined /> Just now
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Footer */}
      {alertCount > 0 && (
        <div className="notification-footer">
          <Button
            type="link"
            onClick={handleViewAll}
            className="view-all-btn"
          >
            View All Alerts <RightOutlined />
          </Button>
        </div>
      )}
    </div>
  );

  return (
    <Popover
      content={content}
      trigger="click"
      open={isOpen}
      onOpenChange={setIsOpen}
      placement="bottomRight"
      overlayClassName="notification-popover"
      arrow={false}
    >
      <span className={`notification-bell ${alertCount > 0 ? 'has-alerts' : ''} ${hasNewAlerts ? 'pulse' : ''}`}>
        <Badge
          count={alertCount}
          overflowCount={99}
          className={alertCount > 0 ? 'badge-active' : ''}
        >
          <BellOutlined className="bell-icon" />
        </Badge>
      </span>
    </Popover>
  );
};

export default NotificationBell;