import { Card, Form, Button, Input, message} from "antd";
import Cookies from "js-cookie";
import { URL } from "../../API/URL";

const AddItemStatusOption = () => {
  const [form] = Form.useForm();
  const token = atob(Cookies.get("authToken"));

  const onFinish = async (values) => {
    try {
      console.log(values);
      const response = await fetch(
        URL+"/option/item-status",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: values.itemStatus,
        }
      );

      if (!response.ok) {
        const mess = await response.text();
        if (response.status === 400) {
          return message.warning(mess, 2);
        }
        if (response.status === 401) {
          return message.warning(mess, 2);
        }
        //form.resetFields(); // Clear the form
        return message.warning('something went wrong',2);
      } else {
        const mess2 = await response.text();
        if (response.status === 200) {          
          // Handle error (e.g., display error message)
          return message.success(mess2, 1);
        }
        // Handle error (e.g., display error message)
        return message.warning(mess2, 3);
      }
    } catch (error) {
      // Handle network or other errors
      console.error("Error:", error);
      message.error('An error occurred.');
    }
  };

  return (
    <>
      <title>Add Item Status Option</title>
      <div
        style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}
      >
        <Card style={{
         border: "2px solid black"          
        }}>
          <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Add Item Status Option</h2>
          <Form
            form={form}
            name="add-item-status"
            onFinish={onFinish}
            //... any layout or styling props...
          >
            <Form.Item
              style={{ marginTop: "50px" }}
              label="Add Item Status"
              name="itemStatus"
              rules={[
                {
                  required: true,
                  message: "Please enter your new Item status option!",
                },
              ]}
            >
              <Input />
            </Form.Item>

            <Form.Item style={{ textAlign: "center", marginTop: "15px" }}>
              <Button type="primary" htmlType="submit" primary>
                Add New Item Status 
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </>
  );
};

export default AddItemStatusOption;
