import { Card, Form, Button, Input, Select, message} from "antd";
import { useState } from "react";
import Cookies from "js-cookie";
import { useEffect } from "react";
import GetItemStatusOptionAPI from "../../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import { URL } from "../../API/URL";

const UpdateItemStatusOption = () => {
  const [form] = Form.useForm();
  // const role=localStorage.getItem('role');
  const token = atob(Cookies.get("authToken"));
  const [itemStatusOption, setitemStatusOption] = useState();

  //Api is calling for getting item option status
  useEffect(() => {
    const fetchitemOptionStatus = async () => {
      try {
        const data = await GetItemStatusOptionAPI(); // Use await to get the data
        if (data) {
          setitemStatusOption(data);
        } else {
          // Handle the API error (e.g., display an error message)
          // alert('Failed to fetch Item Status Option');
        }
      } catch (error) {
        //console.error('API Error:', error);
      }
    };

    fetchitemOptionStatus(); // Call the async function
  }, []);

  const onFinish = async (values) => {
    try {
      console.log(values);
      const response = await fetch(
        URL+"/option/item-status",
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(values),
        }
      );

      if (!response.ok) {
        const mess = await response.text();
        if (response.status === 404) {
        return  message.warning(mess, 2);
        }
        if (response.status === 400) {
          return message.warning(mess, 2);
        }
        if (response.status === 401) {
          return message.warning(mess, 2);
        }
        //form.resetFields(); // Clear the form
        return message.warning(mess, 2);
      } else {
        
        if (response.status === 200) {
          const successMess = await response.text();
          // Handle error (e.g., display error message)
          message.success(successMess, 1);
          return window.location.reload();
        }else{
          const mess2 = await response.text();
          return message.warning(mess2, 3);
        }        
      }
    } catch (error) {
      // Handle network or other errors
      console.error("Error:", error);
      message.error('An error occurred.');
    }
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
      <title>Update Item Status Option</title>
      <Card style={{
         border: "2px solid black"          
        }}>
        <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Update Item Status Option</h2>
        <Form
          form={form}
          name="Update-item-status-option"
          onFinish={onFinish}
          //... any layout or styling props...
        >
          <Form.Item
            label="Item Status"
            name="oldStatus"
            rules={[
              { required: true, message: "Please select the item status!" },
            ]}
          >
            {itemStatusOption ? ( // Check if regions is defined
              <Select dropdownStyle={{ minWidth: "100px" }}>
                {itemStatusOption.map((itemStatus) => (
                  <Select.Option key={itemStatus} value={itemStatus}>
                    {itemStatus}
                  </Select.Option>
                ))}
              </Select>
            ) : (
              <div>
                <Select value={null}></Select>
              </div> // Or a loading indicator
            )}
          </Form.Item>
          <Form.Item
            label="Update Item Status"
            name="newStatus"
            rules={[
              {
                required: true,
                message: "Please enter your updated Item status option!",
              },
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item style={{ textAlign: "center", marginTop: "15px" }}>
            <Button type="primary" htmlType="submit" primary>
              Update Item Status Option
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UpdateItemStatusOption;
