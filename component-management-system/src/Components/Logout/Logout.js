import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Cookies from "js-cookie";
import { message, Button, Modal, Card, Typography, Flex } from "antd";
import { LogoutOutlined, ExclamationCircleFilled } from "@ant-design/icons";

const { Title, Text } = Typography;
const { confirm } = Modal;

function LogoutPage() {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleLogout = () => {
    setIsLoading(true);


    Cookies.remove("authToken");
    Cookies.remove("isLogin");

    localStorage.removeItem("email");
    localStorage.removeItem("name");
    localStorage.removeItem("mobile");
    localStorage.removeItem("region");
    localStorage.removeItem("role");

    // Clear service mode from sessionStorage
    sessionStorage.removeItem("msipl_service_mode");

    message.success("You have been successfully logged out.", 2);


    setTimeout(() => {
      navigate("/", { replace: true });
      setIsLoading(false);
    }, 500);
  };

  const showConfirmModal = () => {
    confirm({
      title: "Are you sure you want to logout?",
      icon: <ExclamationCircleFilled />,
      content: "You will be returned to the login page.",
      okText: "Yes, Logout",
      okType: "danger",
      cancelText: "No, Cancel",
      onOk() {
        handleLogout();
      },
    });
  };

  return (
    <Flex align="center" justify="center" style={{ flex: 1 }}>
      <Card
        bordered={false}
        style={{ width: 340, textAlign: "center", boxShadow: "0 4px 12px rgba(0,0,0,0.1)" }}
      >
        <LogoutOutlined style={{ fontSize: "48px", color: "#ff4d4f", marginBottom: "16px" }} />
        <Title style={{ color: "--text-color-secondary" }} level={3}>Logout</Title>
        <Text type="secondary" style={{ display: 'block', marginBottom: '24px', color: "--text-color-secondary" }}>
          Click the button below to end your session.
        </Text>
        <Button
          type="primary"
          danger
          size="large"
          block
          icon={<LogoutOutlined />}
          loading={isLoading}
          onClick={showConfirmModal}
        >
          Logout
        </Button>
      </Card>
    </Flex>
  );
}

export default LogoutPage;