import { Card, Form, Button, Input, message } from "antd";
import Cookies from "js-cookie";
import { URL } from "../../API/URL";

const AddAvailabilityStatus = () => {

  const [form] = Form.useForm();
  const token = atob(Cookies.get("authToken"));

  const onFinish = async (values) => {
    try {
      console.log(values);
      const response = await fetch(URL + "/option/item-availability", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: values.status,
      });

      if (!response.ok) {
        const mess = await response.text();
        if (response.status === 400) {
          return message.error(mess, 2);
        } else if (response.status === 401) {
          alert(mess);
          return message.error(mess, 2);
        } else {
          //form.resetFields(); // Clear the form
          // alert(mess);
          return message.error(mess, 2);
        }
      } else {
        const mess2 = await response.text();
        if (response.status === 200) {
          // Handle error (e.g., display error message)
          return message.success(mess2, 1);
        } else {
          // Handle error (e.g., display error message)
          return message.warning(mess2, 3);
        }
      }
    } catch (error) {
      // Handle network or other errors
      console.error("Error:", error);
      message.error("An error occurred.", 2);
    }
  };

  return (
    <>
      <title>Add Availability Option</title>
      <div
        style={{
          display: "flex",
          flex: "1",
          justifyContent: "center",
          alignItems: "center",
          border: "none"
        }}
      >
        <Card style={{
         border: "2px solid black"          
        }}>
          <h2
            style={{
              fontSize: "20px",
              color: " #1f3b57",
              textAlign: "center",
              padding: "2px",
              marginBottom: "20px",
            }}
          >
            Add Availability Option
          </h2>
          <Form
            form={form}
            name="add-region"
            onFinish={onFinish}
          //... any layout or styling props...
          >
            <Form.Item
              style={{ marginTop: "50px" }}
              label="New Available Status Name"
              name="status"
              rules={[
                { required: true, message: "Please enter your new status option!" },
              ]}
            >
              <Input />
            </Form.Item>

            <Form.Item style={{ textAlign: "center", marginTop: "15px" }}>
              <Button type="primary" htmlType="submit">
                Add New Option
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </>
  );
};

export default AddAvailabilityStatus;
