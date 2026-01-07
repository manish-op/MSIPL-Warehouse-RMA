import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { Card, Typography, Input, Button, Empty, Alert, Spin, Badge } from 'antd';
import {
    SendOutlined,
    MessageOutlined,
    WifiOutlined,
    DisconnectOutlined,
    ReloadOutlined,
    UserAddOutlined,
    UserDeleteOutlined,
} from '@ant-design/icons';
import "../../App.css";
import "./ChatComponent.css";

const { Title, Text } = Typography;

// Constants
const CHAT_STORAGE_KEY = 'chat_messages';
const MAX_MESSAGES = 100; // Limit stored messages to prevent memory issues
const MAX_MESSAGE_LENGTH = 1000; // Prevent excessively long messages
const MAX_RETRY_DELAY = 30000;
const BASE_RETRY_DELAY = 5000;

// Load messages from session storage with validation
const loadMessagesFromStorage = () => {
    try {
        const storedMessages = sessionStorage.getItem(CHAT_STORAGE_KEY);
        if (storedMessages) {
            const parsed = JSON.parse(storedMessages);
            // Validate it's an array
            if (Array.isArray(parsed)) {
                return parsed.slice(-MAX_MESSAGES); // Keep only last N messages
            }
        }
    } catch {
        // Silent fail - don't expose storage errors
        sessionStorage.removeItem(CHAT_STORAGE_KEY);
    }
    return [];
};

// Sanitize message content to prevent XSS
const sanitizeMessage = (content) => {
    if (typeof content !== 'string') return '';
    return content
        .slice(0, MAX_MESSAGE_LENGTH)
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .trim();
};

// Sanitize username
const sanitizeUsername = (name) => {
    if (typeof name !== 'string') return 'Guest';
    return name.slice(0, 50).replace(/[<>]/g, '').trim() || 'Guest';
};

const ChatComponent = () => {
    const [messages, setMessages] = useState(() => loadMessagesFromStorage());
    const [messageInput, setMessageInput] = useState('');
    const stompClientRef = useRef(null);
    const [connected, setConnected] = useState(false);
    const [connecting, setConnecting] = useState(true);
    const [error, setError] = useState(null);
    const retryCountRef = useRef(0);
    const subscriptionRef = useRef(null);
    const messagesEndRef = useRef(null);
    const retryTimeoutRef = useRef(null);
    const isMountedRef = useRef(true);

    // Memoize username to prevent re-renders
    const username = useMemo(() => {
        return sanitizeUsername(localStorage.getItem("name"));
    }, []);

    // Scroll to bottom when messages change
    const scrollToBottom = useCallback(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, []);

    useEffect(scrollToBottom, [messages, scrollToBottom]);

    // Persist messages to session storage (with limit)
    useEffect(() => {
        const messagesToStore = messages.slice(-MAX_MESSAGES);
        sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(messagesToStore));
    }, [messages]);

    // Cleanup function
    const cleanup = useCallback(() => {
        if (retryTimeoutRef.current) {
            clearTimeout(retryTimeoutRef.current);
            retryTimeoutRef.current = null;
        }

        if (subscriptionRef.current) {
            try {
                subscriptionRef.current.unsubscribe();
            } catch {
                // Silent fail
            }
            subscriptionRef.current = null;
        }

        if (stompClientRef.current) {
            try {
                if (stompClientRef.current.connected) {
                    stompClientRef.current.send(
                        "/app/chat.sendMessage",
                        {},
                        JSON.stringify({ sender: username, type: 'LEAVE' })
                    );
                }
                stompClientRef.current.disconnect();
            } catch {
                // Silent fail
            }
            stompClientRef.current = null;
        }
    }, [username]);

    // Connect to WebSocket
    const connect = useCallback(() => {
        // Check if component is still mounted
        if (!isMountedRef.current) return;

        // Check if libraries are loaded
        if (typeof window.SockJS === 'undefined' || typeof window.Stomp === 'undefined') {
            setError("Chat libraries failed to load. Please refresh the page.");
            setConnecting(false);
            return;
        }

        setConnecting(true);
        setError(null);

        const Stomp = window.Stomp;
        const SockJS = window.SockJS;

        try {
            const socket = new SockJS('/ws');
            const client = Stomp.over(socket);
            stompClientRef.current = client;

            // Disable debug logging
            client.debug = () => { };

            const onConnect = () => {
                if (!isMountedRef.current) return;

                setConnected(true);
                setConnecting(false);
                setError(null);
                retryCountRef.current = 0;

                subscriptionRef.current = client.subscribe('/topic/public', (message) => {
                    if (!isMountedRef.current) return;

                    try {
                        const chatMessage = JSON.parse(message.body);
                        // Validate message structure
                        if (chatMessage && typeof chatMessage === 'object') {
                            const sanitizedMessage = {
                                ...chatMessage,
                                sender: sanitizeUsername(chatMessage.sender),
                                content: chatMessage.content ? sanitizeMessage(chatMessage.content) : undefined,
                            };
                            setMessages((prev) => [...prev.slice(-(MAX_MESSAGES - 1)), sanitizedMessage]);
                        }
                    } catch {
                        // Invalid message format - ignore
                    }
                });

                client.send(
                    "/app/chat.addUser",
                    {},
                    JSON.stringify({ sender: username, type: 'JOIN' })
                );
            };

            const onError = () => {
                if (!isMountedRef.current) return;

                setConnected(false);
                setConnecting(false);
                setError("Connection to chat server lost.");

                retryCountRef.current += 1;

                // Exponential backoff with max delay
                const retryDelay = Math.min(
                    BASE_RETRY_DELAY * Math.pow(1.5, retryCountRef.current - 1),
                    MAX_RETRY_DELAY
                );

                retryTimeoutRef.current = setTimeout(() => {
                    if (isMountedRef.current) {
                        connect();
                    }
                }, retryDelay);
            };

            client.connect({}, onConnect, onError);
        } catch {
            setError("Failed to initialize chat connection.");
            setConnecting(false);
        }
    }, [username]);

    // Initialize connection on mount
    useEffect(() => {
        isMountedRef.current = true;
        connect();

        return () => {
            isMountedRef.current = false;
            cleanup();
            setConnected(false);
        };
    }, [connect, cleanup]);

    // Send message handler
    const sendMessage = useCallback(() => {
        const trimmedMessage = messageInput.trim();

        if (!trimmedMessage || !stompClientRef.current || !connected) {
            return;
        }

        // Validate message length
        if (trimmedMessage.length > MAX_MESSAGE_LENGTH) {
            return;
        }

        const chatMessage = {
            sender: username,
            content: sanitizeMessage(trimmedMessage),
            type: 'CHAT'
        };

        try {
            stompClientRef.current.send(
                "/app/chat.sendMessage",
                {},
                JSON.stringify(chatMessage)
            );
            setMessageInput('');
        } catch {
            setError("Failed to send message. Please try again.");
        }
    }, [messageInput, connected, username]);

    // Manual retry handler
    const handleRetry = useCallback(() => {
        if (retryTimeoutRef.current) {
            clearTimeout(retryTimeoutRef.current);
        }
        retryCountRef.current = 0;
        connect();
    }, [connect]);

    // Handle input change with length limit
    const handleInputChange = useCallback((e) => {
        const value = e.target.value;
        if (value.length <= MAX_MESSAGE_LENGTH) {
            setMessageInput(value);
        }
    }, []);

    return (
        <div className="chat-page">
            {/* Page Header */}
            <div className="page-header">
                <div className="header-content">
                    <div className="header-icon">
                        <MessageOutlined />
                    </div>
                    <div className="header-text">
                        <Title level={3} className="header-title">Team Chat</Title>
                        <Text className="header-subtitle">
                            Real-time communication with your team
                        </Text>
                    </div>
                </div>

                <div className="connection-status">
                    {connecting ? (
                        <Badge status="processing" text={<Text className="status-text">Connecting...</Text>} />
                    ) : connected ? (
                        <Badge status="success" text={<Text className="status-text"><WifiOutlined /> Connected</Text>} />
                    ) : (
                        <Badge status="error" text={<Text className="status-text"><DisconnectOutlined /> Disconnected</Text>} />
                    )}
                </div>
            </div>

            {/* Error Alert */}
            {error && !connecting && (
                <Alert
                    message="Connection Error"
                    description={
                        <div className="error-content">
                            <span>{error}</span>
                            <Button
                                icon={<ReloadOutlined />}
                                onClick={handleRetry}
                                size="small"
                                type="primary"
                            >
                                Retry Now
                            </Button>
                        </div>
                    }
                    type="warning"
                    showIcon
                    className="connection-error"
                />
            )}

            {/* Chat Card */}
            <Card className="chat-card" bordered={false}>
                {/* Messages Area */}
                <div className="message-area">
                    {connecting && messages.length === 0 ? (
                        <div className="loading-container">
                            <Spin size="large" />
                            <Text className="loading-text">Connecting to chat server...</Text>
                        </div>
                    ) : messages.length === 0 ? (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="No messages yet. Start the conversation!"
                            className="empty-messages"
                        />
                    ) : (
                        messages.map((msg, index) => (
                            <div key={`${msg.sender}-${index}`} className="message-container">
                                {msg.type === 'JOIN' || msg.type === 'LEAVE' ? (
                                    <div className="system-message">
                                        <span className={`system-message-pill ${msg.type === 'JOIN' ? 'join' : 'leave'}`}>
                                            {msg.type === 'JOIN' ? <UserAddOutlined /> : <UserDeleteOutlined />}
                                            {msg.sender} {msg.type === 'JOIN' ? 'joined the chat' : 'left the chat'}
                                        </span>
                                        {msg.timestamp && (
                                            <div className="system-message-timestamp">{msg.timestamp}</div>
                                        )}
                                    </div>
                                ) : (
                                    <div className={`chat-message ${msg.sender === username ? 'own-message' : ''}`}>
                                        <span className="sender-name">{msg.sender}</span>
                                        <span className="message-bubble">{msg.content}</span>
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                    <div ref={messagesEndRef} />
                </div>

                {/* Input Area */}
                <div className="input-area">
                    <Input
                        value={messageInput}
                        onChange={handleInputChange}
                        onPressEnter={sendMessage}
                        placeholder={connected ? "Type a message..." : "Waiting for connection..."}
                        disabled={!connected}
                        size="large"
                        className="chat-input"
                        maxLength={MAX_MESSAGE_LENGTH}
                    />
                    <Button
                        type="primary"
                        icon={<SendOutlined />}
                        onClick={sendMessage}
                        disabled={!connected || !messageInput.trim()}
                        size="large"
                        className="send-button"
                    >
                        Send
                    </Button>
                </div>
            </Card>
        </div>
    );
};

export default ChatComponent;