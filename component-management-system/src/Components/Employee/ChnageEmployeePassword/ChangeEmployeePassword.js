import { Card, Form, Button, Input } from "antd";
import { useState } from "react";
import ChangeEmployeePasswordAPI from "../../API/ChangeEmployeePassword/ChangeEmployeePasswordAPI";

const ChangeEmployeePassword = () => {
  const [form] = Form.useForm();

  const [changeEmpPassword, setchangeEmpPassword] = useState({
    empEmail: "",
    newPassword: "",
  });

  const handleChange = (event, field) => {
    let actualValue = event.target.value;
    setchangeEmpPassword({
      ...changeEmpPassword,
      [field]: actualValue,
    });
  };

  const onFinish = async (event) => {
    //event.preventDefault();
    if (
      changeEmpPassword.empEmail.trim() === "" ||
      changeEmpPassword.newPassword.trim() === ""
    ) {
      console.log("employee Email and Password both required");
      return;
    } else {
     await ChangeEmployeePasswordAPI(changeEmpPassword);
    }
  };

  return (
    <Card style={{ background: "var(--component-background)"}}>
      <Form
        form={form}
        name="change-employee-password"
        onFinish={onFinish}
        //... any layout or styling props...
      >
        <h4
          style={{
            fontSize: "20px",
            color: "orange",
            textAlign: "center",
            padding: "2px",
            marginBottom: "20px",
          }}
        >
          Change Employee Password
        </h4>
        <Form.Item 
          label="Employee Email"
          name="email"
          rules={[
            {
              required: true,
              type: "email",
              message: "Please enter a valid email address!",
            },
          ]}
        >
          <Input
            value={changeEmpPassword.empEmail}
            onChange={(e) => handleChange(e, "empEmail")}
          />
        </Form.Item>

        <Form.Item
          label="New Password"
          name="newPassword"
          rules={[{ required: true, message: "Enter new password!" }]}
        >
          <Input.Password
            value={changeEmpPassword.newPassword}
            onChange={(e) => handleChange(e, "newPassword")}
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
                  new Error("The two passwords that you entered do not match!")
                );
              },
            }),
          ]}
        >
          <Input.Password />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Change Password
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default ChangeEmployeePassword;
