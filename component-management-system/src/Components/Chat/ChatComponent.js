import React, { useState, useEffect, useRef } from 'react';
 import "../../App.css";
 import "./ChatComponent.css"; 


const CHAT_STORAGE_KEY = 'chat_messages';
const loadMessagesFromStorage = () => {
    try {
        const storedMessages = sessionStorage.getItem(CHAT_STORAGE_KEY);
        if (storedMessages) { return JSON.parse(storedMessages); }
    } catch (e) { console.error("Failed to parse messages from sessionStorage", e); }
    return [];
};

// --- No jwtToken prop needed ---
const ChatComponent = () => {
    const [messages, setMessages] = useState(() => loadMessagesFromStorage());
    const [messageInput, setMessageInput] = useState('');
    const stompClientRef = useRef(null);
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const subscriptionRef = useRef(null);
    const messagesEndRef = useRef(null);

   
    const [username] = useState(() => localStorage.getItem("name") || "Guest");
    
    const scrollToBottom = () => { messagesEndRef.current?.scrollIntoView({ behavior: "smooth" }); };
    useEffect(scrollToBottom, [messages]);
    useEffect(() => {
        sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(messages));
    }, [messages]);


    useEffect(() => {
       
        if (typeof window.SockJS === 'undefined' || typeof window.Stomp === 'undefined') {
            console.error("ChatComponent: SockJS or StompJS not loaded.");
            setError("Chat libraries failed to load. Please refresh.");
            return;
        }

        const connect = () => {
            const Stomp = window.Stomp;
            const SockJS = window.SockJS;

            const socket = new SockJS('/ws');
            const client = Stomp.over(socket);
            stompClientRef.current = client;
            client.debug = null;

            const onConnect = (frame) => {
                console.log('STOMP Connected (using localStorage name: ' + username + '): ' + frame);
                setConnected(true);
                setError(null);

                subscriptionRef.current = client.subscribe('/topic/public', (message) => {
                    const chatMessage = JSON.parse(message.body);
                    setMessages((prevMessages) => [...prevMessages, chatMessage]);
                });

                // --- Send JOIN with username from localStorage ---
                client.send(
                    "/app/chat.addUser",
                    {},
                    JSON.stringify({ sender: username, type: 'JOIN' })
                );
            };

            const onError = (error) => {
                console.error("STOMP Error: ", error);
                let errorMessage = "Connection error. Retrying...";
                if (typeof error === 'string') {
                    if (error.includes("Whoops! Lost connection")) {
                        errorMessage = "Lost connection to server. Retrying...";
                    } else if (error.includes("Connection closed")) {
                        errorMessage = "Connection closed. Retrying...";
                    }
                }
                setError(errorMessage);
                // Simple retry logic
                setTimeout(connect, 5000); 
            };

            // --- Connect with no headers ---
            client.connect({}, onConnect, onError);
        };

        connect();

        return () => {
           
            console.log("Cleaning up chat component...");
            if (subscriptionRef.current) subscriptionRef.current.unsubscribe();
            if (stompClientRef.current) {
                if (stompClientRef.current.connected) {
                    // --- Send LEAVE message with username ---
                     stompClientRef.current.send(
                         "/app/chat.sendMessage",
                         {},
                         JSON.stringify({ sender: username, type: 'LEAVE' })
                     );
                }
                stompClientRef.current.disconnect(() => { console.log("STOMP Disconnected"); });
            }
            setConnected(false);
        };

    }, [username]);

    const sendMessage = () => {
        if (messageInput && stompClientRef.current && connected) {
           
            const chatMessage = {
                sender: username,
                content: messageInput,
                type: 'CHAT'
            };
        
            stompClientRef.current.send(
                "/app/chat.sendMessage",
                {}, // headers
                JSON.stringify(chatMessage) // body
            );
            setMessageInput('');
        }
    };


    return (
        <div className="chat-container">
            <h2 className="chat-header">
                Active Users
            </h2>

            {error && (
                <div className="chat-error-bar">
                    Error: {error}
                </div>
            )}

       
            <div className="message-area">
                {messages.map((msg, index) => (
                    <div key={index} className="message-container">
                        {msg.type === 'JOIN' || msg.type === 'LEAVE' ? (
                            <div className="system-message">
                                <span
                                  className={`system-message-pill ${msg.type === 'JOIN' ? 'join' : 'leave'}`}
                                >
                                    {msg.sender} {msg.type === 'JOIN' ? 'joined' : 'left'}
                                </span>

                     
                                {msg.timestamp && ( 
                                    <div className="system-message-timestamp">
                                        {msg.timestamp}
                                    </div>
                                )}

                            </div>
                        ) : (
                            <div className="chat-message">
                                <span className="sender-name">
                                    {msg.sender}
                                </span>
                                <span className="message-bubble">
                                    {msg.content}
                                    
                                </span>
                                
                            </div>
                        )}
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>

        
            <div className="input-area">
                <input
                    type="text"
                    className="chat-input"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                    placeholder="Type a message..."
                    disabled={!connected || !!error}
                />
                <button
                    className="send-button"
                    style={{
                        background: (connected ? 'var(--primary-color)' : '#ccc'),
                        cursor: (connected ? 'pointer' : 'not-allowed')
                    }}
                    onClick={sendMessage}
                    disabled={!connected || !!error}
                >
                    {connected ? 'Send' : '...'}
                </button>
            </div>
            
        </div>
    );
};

export default ChatComponent;