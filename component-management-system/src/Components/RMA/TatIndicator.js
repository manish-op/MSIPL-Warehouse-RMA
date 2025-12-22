// TatIndicator.js - Reusable TAT status indicator component
import React from 'react';
import { Tag, Tooltip, Badge, Alert } from 'antd';
import {
    CheckCircleOutlined,
    WarningOutlined,
    CloseCircleOutlined,
    ClockCircleOutlined,
    AlertOutlined,
    FieldTimeOutlined,
} from '@ant-design/icons';

/**
 * TAT Status Indicator Component
 * Shows color-coded status based on remaining days until due date
 * 
 * Status:
 * - ON_TRACK (Green): > 50% of TAT remaining
 * - AT_RISK (Yellow): ≤ 50% of TAT remaining but not overdue
 * - BREACHED (Red): Past due date
 */

// Compact Tag-based indicator (for tables/lists)
function TatIndicator({ dueDate, tat }) {
    if (!dueDate || !tat) {
        return <Tag color="default">No TAT</Tag>;
    }

    const { daysRemaining, status, color, icon, label } = calculateTatStatus(dueDate, tat);
    const tooltipText = `TAT: ${tat} days | Due: ${new Date(dueDate).toLocaleDateString()}`;

    return (
        <Tooltip title={tooltipText}>
            <Tag color={color} icon={icon}>
                {label}
            </Tag>
        </Tooltip>
    );
}

// Alert-style Banner indicator (for headers/prominent display)
export function TatAlertBanner({ dueDate, tat, showIfOnTrack = false }) {
    if (!dueDate || !tat) {
        return null;
    }

    const { daysRemaining, status, alertType, icon, label } = calculateTatStatus(dueDate, tat);

    // Don't show banner for on-track items unless explicitly requested
    if (status === 'ON_TRACK' && !showIfOnTrack) {
        return null;
    }

    const dueFormatted = new Date(dueDate).toLocaleDateString();
    const message = status === 'BREACHED'
        ? `TAT Breached! Due date was ${dueFormatted}`
        : status === 'AT_RISK'
            ? `TAT Warning: Only ${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} remaining (Due: ${dueFormatted})`
            : `On Track: ${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} remaining`;

    return (
        <Alert
            message={message}
            type={alertType}
            showIcon
            icon={icon}
            style={{ marginBottom: 16 }}
            banner
        />
    );
}

// Icon-only indicator with tooltip (for compact spaces)
export function TatIconIndicator({ dueDate, tat, size = 20 }) {
    if (!dueDate || !tat) {
        return <ClockCircleOutlined style={{ color: '#d9d9d9', fontSize: size }} />;
    }

    const { status, iconColor, icon, label, daysRemaining } = calculateTatStatus(dueDate, tat);
    const dueFormatted = new Date(dueDate).toLocaleDateString();

    const tooltipContent = (
        <div>
            <div><strong>TAT Status: {status.replace('_', ' ')}</strong></div>
            <div>Due Date: {dueFormatted}</div>
            <div>Days Remaining: {daysRemaining}</div>
        </div>
    );

    // Use Badge with pulsing effect for breached items
    if (status === 'BREACHED') {
        return (
            <Tooltip title={tooltipContent}>
                <Badge dot status="error">
                    <AlertOutlined
                        style={{
                            color: iconColor,
                            fontSize: size,
                            animation: 'pulse 1.5s infinite'
                        }}
                    />
                </Badge>
            </Tooltip>
        );
    }

    if (status === 'AT_RISK') {
        return (
            <Tooltip title={tooltipContent}>
                <Badge dot status="warning">
                    <WarningOutlined style={{ color: iconColor, fontSize: size }} />
                </Badge>
            </Tooltip>
        );
    }

    return (
        <Tooltip title={tooltipContent}>
            <CheckCircleOutlined style={{ color: iconColor, fontSize: size }} />
        </Tooltip>
    );
}

// Helper function to calculate TAT status
function calculateTatStatus(dueDate, tat) {
    const now = new Date();
    const due = new Date(dueDate);
    const diffTime = due.getTime() - now.getTime();
    const daysRemaining = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    const halfTat = Math.floor(tat / 2);

    let status, color, alertType, iconColor, icon, label;

    if (daysRemaining < 0) {
        status = 'BREACHED';
        color = 'error';
        alertType = 'error';
        iconColor = '#f5222d';
        icon = <CloseCircleOutlined />;
        label = `${Math.abs(daysRemaining)} day${Math.abs(daysRemaining) !== 1 ? 's' : ''} overdue`;
    } else if (daysRemaining <= halfTat) {
        status = 'AT_RISK';
        color = 'warning';
        alertType = 'warning';
        iconColor = '#faad14';
        icon = <WarningOutlined />;
        label = `${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} left`;
    } else {
        status = 'ON_TRACK';
        color = 'success';
        alertType = 'success';
        iconColor = '#52c41a';
        icon = <CheckCircleOutlined />;
        label = `${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} left`;
    }

    return { daysRemaining, status, color, alertType, iconColor, icon, label };
}

/**
 * Helper function to get TAT status color
 */
export function getTatStatusColor(status) {
    switch (status) {
        case 'ON_TRACK':
            return '#52c41a'; // Green
        case 'AT_RISK':
            return '#faad14'; // Yellow/Orange
        case 'BREACHED':
            return '#f5222d'; // Red
        default:
            return '#d9d9d9'; // Gray
    }
}

export default TatIndicator;
