import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Cookies from "js-cookie";
import { message, Card, Input, Button, Typography, Segmented } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";

import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import LoginApiCall from "../API/User/LoginPageApi/LoginApiCall";
import logo from "../../images/images.png";
import "./LoginForm.css";

const { Title, Text } = Typography;

function LoginForm() {
  const navigate = useNavigate();
  const [loginDetails, setLoginDetails] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [remember, setRemember] = useState(false);

  const [mode, setMode] = useState("warehouse");

  useEffect(() => {
    const token = Cookies.get("authToken");
    if (token) navigate("/dashboard/profile");

    // load saved email if "remember me" used previously
    const savedEmail = localStorage.getItem("msipl_remember_email");
    if (savedEmail) {
      setLoginDetails((s) => ({ ...s, email: savedEmail }));
      setRemember(true);
    }
  }, [navigate]);

  const validateEmail = (email) => {
    // simple email regex
    return /^\S+@\S+\.\S+$/.test(email);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();

    const email = (loginDetails.email || "").trim();
    const pwd = (loginDetails.password || "").trim();

    if (!email || !pwd) {
      message.error("Email and password cannot be blank.", 2);
      return;
    }

    if (!validateEmail(email)) {
      message.error("Please enter a valid email address.", 2);
      return;
    }

    try {
      setLoading(true);
      // remember email locally if user asked
      if (remember) localStorage.setItem("msipl_remember_email", email);
      else localStorage.removeItem("msipl_remember_email");

      await LoginApiCall({ email, password: pwd }, navigate);

      // Save the selected mode to sessionStorage for navigation protection
      sessionStorage.setItem("msipl_service_mode", mode);

      // LoginApiCall should set cookies / navigate on success. If it doesn't, you can handle here.
      if (mode === "rma") {
        navigate("/rma-dashboard");
      }
      else {
        navigate("/dashboard/profile")
      }

    } catch (err) {
      console.error("Login failed:", err);
      message.error(err?.message || "Login failed. Please try again.", 3);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />

      <div className="login-wrapper">
        <Card className="login-card" variant="borderless">
          <div className="login-header">
            <img src={logo} alt="Company Logo" className="login-logo" />
            <Title level={4} style={{ margin: "10px 0 0 0", color: "var(--text-color)" }}>
              Motorola Solutions India Pvt. Ltd.
            </Title>
            <Text type="secondary" style={{ color: "var(--text color)" }}>
              {mode === "warehouse"
                ? "Warehouse Management System"
                : "RMA Request Portal"}
            </Text>
          </div>

          {/*Toggle button for switching from warehouse to RMA*/}
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              marginTop: 16,
              marginBottom: 8
            }}>
            <Segmented
              value={mode}
              onChange={setMode}
              options={[
                { label: "Warehouse", value: "warehouse" },
                { label: "RMA Request", value: "rma" }
              ]}>
            </Segmented>
          </div>

          <form onSubmit={handleFormSubmit} className="login-form" aria-label="Login form">

            {/*small helper text under toggle button*/}
            <div style={{
              marginBottom: 8,
              textAlign: "center",
            }}>
              <Text type="secondary"
                style={{ fontSize: 12 }}>
                {mode === "warehouse"
                  ? "Login to access Warehouse Management System"
                  : "Login to access RMA Request Portal."}
              </Text>
            </div>

            <label className="visually-hidden" htmlFor="login-email">Email</label>
            <Input
              id="login-email"
              size="large"
              placeholder="Email"
              prefix={<UserOutlined />}
              value={loginDetails.email}
              onChange={(e) => setLoginDetails({ ...loginDetails, email: e.target.value })}
              style={{ marginBottom: "12px" }}
              autoComplete="username"
            />

            <label className="visually-hidden" htmlFor="login-password">Password</label>
            <Input.Password
              id="login-password"
              size="large"
              placeholder="Password"
              prefix={<LockOutlined />}
              value={loginDetails.password}
              onChange={(e) => setLoginDetails({ ...loginDetails, password: e.target.value })}
              style={{ marginBottom: "10px" }}
              autoComplete="current-password"
            />



            <Button
              type="primary"
              htmlType="submit"
              size="large"
              block
              className="login-btn"
              loading={loading}
              disabled={loading}
            >
              {loading ? "Signing in…" : "Login"}
            </Button>
          </form>

          <div className="back-home">
            <Link to="/" className="back-link">← Back to Home</Link>
          </div>
        </Card>
      </div>

      <Footer />
    </>
  );
}

export default LoginForm;
