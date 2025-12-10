import { Card, Form, Button, Input, Select, message } from "antd";
import { useState } from "react";
import Cookies from "js-cookie";
import { useEffect } from "react";
import GetItemAvailabilityStatusOptionAPI from "../../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import { URL } from "../../API/URL";

const UpdateAvailabilityStatus = () => {
  const [form] = Form.useForm();
  // const role=localStorage.getItem('role');
  const token = atob(Cookies.get("authToken"));
  const [itemAvailabilityOption, setitemAvailabilityOption] = useState();

  //Api is calling for getting item availability status option
  useEffect(() => {
    const fetchAvailabilityStatus = async () => {
      try {
        const data = await GetItemAvailabilityStatusOptionAPI(); // Use await to get the data
        if (data) {
          setitemAvailabilityOption(data);
        } else {
          // Handle the API error (e.g., display an error message)
          //alert('Failed to fetch Availability Status');
        }
      } catch (error) {
        //alert('API Error:', error.message);
      }
    };
    fetchAvailabilityStatus(); // Call the async function
  }, []);

  const onFinish = async (values) => {
    try {
      console.log(values);
      const response = await fetch(URL + "/option/item-availability", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(values),
      });

      if (!response.ok) {
        const mess = await response.text();
        if (response.status === 404) {
          return message.error(mess, 2);
        }
        if (response.status === 400) {
          return message.warning(mess, 2);
        }
        if (response.status === 401) {
          return message.error(mess, 2);
        }
        //form.resetFields(); // Clear the form
        return message.error(mess, 2);
      } else {
        const mess2 = await response.text();
        if (response.status === 200) {
          // Handle error (e.g., display error message)
          message.success("Update successful!", 1);
          return window.location.reload();
        }
        // Handle error (e.g., display error message)
        return message.warning(mess2, 3);
      }
    } catch (error) {
      // Handle network or other errors
      console.error("Error:", error);
      message.error("An error occurred.");
    }
  };

  return (
    <div
      style={{
        display: "flex",
        flex: "1",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <title>Update Availability Option</title>
      <Card style={{
         border: "2px solid black"          
        }}>
        <h2
          style={{
            fontSize: "20px",
            color: "orange",
            textAlign: "center",
            padding: "2px",
            marginBottom: "20px",
          }}
        >
          Update Availability Option
        </h2>
        <Form
          form={form}
          name="Update-availability-option"
          onFinish={onFinish}
          //... any layout or styling props...
        >
          <Form.Item
            label="Available Status"
            name="existingOption"
            rules={[
              { required: true, message: "Please select Availability Status!" },
            ]}
          >
            {itemAvailabilityOption ? ( // Check if regions is defined
              <Select dropdownStyle={{ minWidth: "100px" }}>
                {itemAvailabilityOption.map((itemAvailStatus) => (
                  <Select.Option key={itemAvailStatus} value={itemAvailStatus}>
                    {itemAvailStatus}
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
            label="update Availability Status Option"
            name="newOption"
            rules={[
              { required: true, message: "Please enter your new Region!" },
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item style={{ textAlign: "center", marginTop: "15px" }}>
            <Button type="primary" htmlType="submit">
              Update Option
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UpdateAvailabilityStatus;
