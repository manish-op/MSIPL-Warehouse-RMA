import React, { useState } from 'react';
import { Button, Tooltip, Modal } from 'antd';
import { MessageOutlined, CloseOutlined } from '@ant-design/icons';
import './FeedbackButton.css';

// Google Form URL - use embedded format for iframe
const GOOGLE_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSc2dCKkebMCLBZwwowFSbqEm_P2rbXsTiyJ7y30luGikc3o0Q/viewform?embedded=true";


const FeedbackButton = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isMinimized, setIsMinimized] = useState(false);

    const handleOpenFeedback = () => {
        // If no form URL configured, open in new tab as fallback
        if (GOOGLE_FORM_URL === "YOUR_GOOGLE_FORM_URL_HERE") {
            Modal.info({
                title: 'Feedback Form Not Configured',
                content: 'Please update the GOOGLE_FORM_URL in FeedbackButton.js with your Google Form URL.',
            });
            return;
        }
        setIsModalOpen(true);
    };

    const handleClose = () => {
        setIsModalOpen(false);
    };

    // Don't show on login page
    if (window.location.pathname === '/login' || window.location.pathname === '/') {
        return null;
    }

    if (isMinimized) {
        return (
            <Tooltip title="Give Feedback" placement="left">
                <Button
                    type="primary"
                    shape="circle"
                    size="large"
                    className="feedback-button-minimized"
                    icon={<MessageOutlined />}
                    onClick={() => setIsMinimized(false)}
                />
            </Tooltip>
        );
    }

    return (
        <>
            {/* Floating Feedback Button */}
            <div className="feedback-button-container">
                <Tooltip title="We'd love your feedback!" placement="left">
                    <Button
                        type="primary"
                        size="large"
                        className="feedback-button"
                        icon={<MessageOutlined />}
                        onClick={handleOpenFeedback}
                    >
                        Feedback
                    </Button>
                </Tooltip>
                <Button
                    type="text"
                    size="small"
                    className="feedback-minimize-btn"
                    icon={<CloseOutlined />}
                    onClick={() => setIsMinimized(true)}
                />
            </div>

            {/* Feedback Modal with Embedded Form */}
            <Modal
                title={
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <MessageOutlined style={{ color: '#1890ff' }} />
                        <span>Share Your Feedback</span>
                    </div>
                }
                open={isModalOpen}
                onCancel={handleClose}
                footer={null}
                width={650}
                bodyStyle={{ padding: 0 }}
                centered
            >
                <div className="feedback-iframe-container">
                    <iframe
                        src={GOOGLE_FORM_URL}
                        title="Feedback Form"
                        width="100%"
                        height="500"
                        frameBorder="0"
                        marginHeight="0"
                        marginWidth="0"
                    >
                        Loading…
                    </iframe>
                </div>
            </Modal>
        </>
    );
};

export default FeedbackButton;
