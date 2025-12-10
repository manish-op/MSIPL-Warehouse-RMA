import React from "react";
import { Form, Input, Button, Card } from "antd";
import { useState } from "react";
import UserChangePasswordAPI from "../API/User/UserChangePassword/UserChangePasswordAPI";

const ChangePassword = () => {
  const [form] = Form.useForm();

  const [changePass, setchangePass] = useState({
    oldPassword: "",
    newPassword: "",
  });

  const handleChange = (event, field) => {
    let actualValue = event.target.value;
    setchangePass({
      ...changePass,
      [field]: actualValue,
    });
  };
  const onFinish = async (event) => {
    //event.preventDefault();
    console.log(changePass);
    if (
      changePass.oldPassword.trim() === "" ||
      changePass.newPassword.trim() === ""
    ) {
      console.log("username and password cant be blank");
      return;
    } else {
     await UserChangePasswordAPI(changePass);
    }
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
    <Card>
      <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Change Password</h2>
      <Form
        form={form}
        name="change-password"
        //onSubmit={handleFormSubmit}
        //onFinish={handleFormSubmit}
        onFinish={onFinish}
        //validateTrigger={['onSubmit']}
        //... any layout or styling props...
      >
        <Form.Item
          style={{ marginTop: "50px" }}
          label="Old Password"
          name="oldPassword"
          rules={[
            { required: true, message: "Please enter your old password!" },
          ]}
        >
          <Input.Password
            value={changePass.oldPasswordpassword}
            onChange={(e) => handleChange(e, "oldPassword")}
          />
        </Form.Item>

        <Form.Item
          label="New Password"
          name="newPassword"
          rules={[
            { required: true, message: "Please enter your new password!" },
          ]}
        >
          <Input.Password
            value={changePass.newPasswordPasswordpassword}
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
    </div>
  );
};

export default ChangePassword;
