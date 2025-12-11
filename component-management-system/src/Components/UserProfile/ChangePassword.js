import React, { useState } from "react";
import { Form, Input, Button, Modal, message } from "antd";
import { LockOutlined, KeyOutlined } from "@ant-design/icons";
import UserChangePasswordAPI from "../API/User/UserChangePassword/UserChangePasswordAPI";

/**
 * ChangePasswordModal - A reusable modal component for changing password
 * Can be opened from Profile page or anywhere else in the app
 */
const ChangePasswordModal = ({ visible, onClose }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values) => {
    if (values.oldPassword.trim() === "" || values.newPassword.trim() === "") {
      message.warning("Password fields cannot be empty");
      return;
    }

    setLoading(true);
    try {
      const changePass = {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      };
      await UserChangePasswordAPI(changePass);
      // API already shows success/error messages
      form.resetFields();
      onClose();
    } catch (error) {
      // API handles error messages
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title={
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <KeyOutlined style={{ color: "#1890ff" }} />
          <span>Change Password</span>
        </div>
      }
      open={visible}
      onCancel={handleCancel}
      footer={null}
      centered
      destroyOnClose
      width={400}
    >
      <Form
        form={form}
        name="change-password-modal"
        onFinish={handleSubmit}
        layout="vertical"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="Current Password"
          name="oldPassword"
          rules={[
            { required: true, message: "Please enter your current password!" },
          ]}
        >
          <Input.Password
            prefix={<LockOutlined style={{ color: "#bfbfbf" }} />}
            placeholder="Enter current password"
            size="large"
          />
        </Form.Item>

        <Form.Item
          label="New Password"
          name="newPassword"
          rules={[
            { required: true, message: "Please enter your new password!" },
            { min: 6, message: "Password must be at least 6 characters!" },
          ]}
        >
          <Input.Password
            prefix={<LockOutlined style={{ color: "#bfbfbf" }} />}
            placeholder="Enter new password"
            size="large"
          />
        </Form.Item>

        <Form.Item
          label="Confirm New Password"
          name="confirmNewPassword"
          dependencies={["newPassword"]}
          hasFeedback
          rules={[
            { required: true, message: "Please confirm your new password!" },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue("newPassword") === value) {
                  return Promise.resolve();
                }
                return Promise.reject(
                  new Error("The two passwords do not match!")
                );
              },
            }),
          ]}
        >
          <Input.Password
            prefix={<LockOutlined style={{ color: "#bfbfbf" }} />}
            placeholder="Confirm new password"
            size="large"
          />
        </Form.Item>

        <Form.Item style={{ marginBottom: 0, marginTop: 24 }}>
          <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
            <Button onClick={handleCancel}>
              Cancel
            </Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              Change Password
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ChangePasswordModal;
