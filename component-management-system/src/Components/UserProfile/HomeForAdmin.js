import React from "react";
import { Card, Avatar, Row, Col } from "antd";
import { UserOutlined, } from "@ant-design/icons";
import "./HomeForAdmin.css";

function HomeForAdmin() {
  return (
    <div className="admin-dashboard">
      <h1 className="dashboard-title">Admin Dashboard</h1>

      <Row gutter={[16, 16]} justify="center">
        {/* User Profile Section - spans more columns on small screens */}
        <Col xs={24} sm={24} md={12} lg={10} xl={8}>
          <Card className="user-profile-card">
            <div className="profile-header">
              <Avatar className="profile-avatar" size={100} icon={<UserOutlined />} />
              <h2 className="user-name">{`${(() => {
                const hour = new Date().getHours();
                if (hour < 12) return "Good morning ";
                if (hour < 18) return "Good afternoon ";
                return "Good evening ";
              })()}, ${localStorage.getItem("name") || "Admin User"}`}</h2>
            </div>
            <div className="user-details-list">
              <p>
                <strong>Email:</strong> {localStorage.getItem("email") || "admin@example.com"}
              </p>
              <p>
                <strong>Mobile No:</strong> {localStorage.getItem("mobile") || "+1 234 567 890"}
              </p>
              <p>
                <strong>User Role:</strong>{' '}
                {(localStorage.getItem('_User_role_for_MSIPL') || 'Administrator').toUpperCase()}
              </p>
            </div>
          </Card>
        </Col>

      </Row>
    </div>
  );
}

export default HomeForAdmin;