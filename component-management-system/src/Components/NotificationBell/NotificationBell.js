import React, { useState, useEffect } from 'react';
import { Badge, Dropdown, Menu, Spin } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { AlertApi } from '../API/AlertApi/AlertApi'; 
import './NotificationBell.css';

const NotificationBell = () => {
  const [alertMessages, setAlertMessages] = useState([]);
  const [alertCount, setAlertCount] = useState(0);
  const [loading, setLoading] = useState(false);

  
  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const data = await AlertApi.getActiveAlerts();
      if (data) {
        setAlertMessages(data.messages);
        setAlertCount(data.count);
      }
    } catch (error) {
      console.error("Failed to fetch alerts:", error);
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    fetchAlerts(); 

    const interval = setInterval(() => {
      fetchAlerts();
    }, 60000); 

    return () => clearInterval(interval);
  }, []);


  const menu = (
    <Menu>
      {loading && (
        <Menu.Item key="loading" disabled>
          <Spin size="small" style={{ margin: '10px 20px' }} />
        </Menu.Item>
      )}
      
      {!loading && alertCount === 0 && (
        <Menu.Item key="empty" disabled>
          No unread notifications
        </Menu.Item>
      )}

      {!loading && alertMessages.map((msg, index) => (
        <Menu.Item 
          key={index} 
          style={{ whiteSpace: 'normal', lineHeight: '1.4', padding: '10px 15px' }}
          disabled
        >
          {msg}
        </Menu.Item>
      ))}
    </Menu>
  );

  return (
    <Dropdown overlay={menu} trigger={['click']}>
      <span style={{ cursor: 'pointer', padding: '0 12px' }}>
        <Badge count={alertCount}>
          {/* You might need to change the color to match your navbar */}
          <BellOutlined style={{ fontSize: '20px', color: '#fff' }} /> 
        </Badge>
      </span>
    </Dropdown>
  );
};

export default NotificationBell;