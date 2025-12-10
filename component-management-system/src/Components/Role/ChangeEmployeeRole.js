import { Card, Form, Button, Input, Select } from "antd";
import { useState, useEffect } from "react";
import GetRoleAPI from "../API/Role/GetRoleAPI";
import ChangeEmployeeRoleAPI from "../API/Role/ChanageEmployeeRole/ChangeEmployeeRoleAPI";

const { Option } = Select;

const ChangeEmployeeRole = () => {
  const [form] = Form.useForm();
  const [empRoles, setEmpRoles] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchRoles = async () => {
      setIsLoading(true);
      try {
        const data = await GetRoleAPI();
        if (data) {
          setEmpRoles(data);
        } else {
          console.error("Failed to fetch roles from the server.");
        }
      } catch (error) {
        console.error("API Error:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRoles();
  }, []);

  const onFinish = async (values) => {
    const currentUserRole = localStorage.getItem("_User_role_for_MSIPL");

    if (currentUserRole !== "admin") {
      console.warn("Only an admin can perform this task.");
      return;
    }

    try {
      const payload = {
        empEmail: values.email,
        role: values.role,
      };
      await ChangeEmployeeRoleAPI(payload);
      //console.log("Employee role changed successfully!");
      form.resetFields();
    } catch (error) {
      console.error("Failed to change employee role:", error);
    }
  };

  return (
    <div style={{ display: 'flex', flex: '1', justifyContent: 'center', alignItems: 'center' }}>
      <Card style={{ width: 400 }}>
        <h2 style={{ fontSize: "20px", color: "var(--primary-accent-color)", textAlign: 'center', marginBottom: '20px' }}>
          Change Employee Role
        </h2>
        <Form 
          form={form}
          name="change-employee-role"
          onFinish={onFinish}
          layout="vertical"
          autoComplete="off"
        >
          <Form.Item
            label="Employee Email"
            name="email"
            rules={[
              { required: true, message: "Please enter the employee's email!" },
              { type: "email", message: "Please enter a valid email address!" },
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            label="New Role"
            name="role"
            rules={[{ required: true, message: "Please select a role!" }]}
          >
            <Select loading={isLoading} placeholder="Select a role">
              {empRoles.map((role) => (
                <Option key={role} value={role}>
                  {role}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }}>
              Change Role
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default ChangeEmployeeRole;