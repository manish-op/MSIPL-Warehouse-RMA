import { Card, Form, Button, Input, message} from "antd";
import Cookies from "js-cookie";
import { URL } from "../../API/URL";

const AddRegion = () => {
  const [form] = Form.useForm();
  const token = atob(Cookies.get("authToken"));

  const onFinish = async (values) => {
    try {
      const response = await fetch(URL+"/region", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: values.region,
      });

      if (!response.ok) {
        const mess= await response.text();
        if (response.status === 400) {
          return message.warning(mess, 2);
        }
        if (response.status === 401) {
          message.alert(mess, 2);
          return null;
        }
        //form.resetFields(); // Clear the form
        message.warning(mess, 2);
        return null;
      } else {
        const mess2= await response.text();
        // Handle error (e.g., display error message)
        if(response.status===200){
        message.success('Region added Successfully',1);
        return null;
        }
        message.warning(mess2, 3)
        return null;
      }
    } catch (error) {
      // Handle network or other errors
      console.error("Error:", error);
      message.error('An error occurred.');
    }
  };

  return (
    <div
    style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}
    >
      <title>Add Region</title>
      <Card>
        <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Add New Region</h2>
        <Form
          form={form}
          name="add-region"
          onFinish={onFinish}
          //... any layout or styling props...
        >
          <Form.Item
            style={{ marginTop: "50px" }}
            label="New Region"
            name="region"
            rules={[
              { required: true, message: "Please enter your new Region!" },
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item style={{ textAlign: "center", marginTop: "15px" }}>
            <Button type="primary" htmlType="submit">
              Add Region
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default AddRegion;
