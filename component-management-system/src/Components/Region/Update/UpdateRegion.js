import { Card, Form, Button, Input, Select, message} from "antd";
import { useState } from "react";
import Cookies from "js-cookie";
import { useEffect } from "react";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import { URL } from "../../API/URL";

const UpdateRegion = () => {
  const [form] = Form.useForm();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const token = atob(Cookies.get("authToken"));
  const [regions, setRegions] = useState();

  //Api is calling for getting region from Database
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI(); // Use await to get the data
        if (data) {
          setRegions(data);
        } else {
          // Handle the API error (e.g., display an error message)
          message.warning("Failed to fetch regions");
        }
      } catch (error) {
        console.error("API Error:", error);
        message.error('An error occurred, while loading region.');
      }
    };

    fetchRegions(); // Call the async function
  }, []);

  const onFinish = async (values) => {
    try {
      console.log(values);
      const response = await fetch(URL+"/region", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(values),
      });
      if (!response.ok) {
        const mess= await response.text();
        if (response.status === 404) {
         return message.warning(mess, 2);
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
        // Handle error (e.g., display error message)
        if(response.status===200){
        window.location.reload();
        return message.success("Updated Successfully", 2);
        }else{
          const mess2= await response.text();
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
      <title>Update Region Name</title>
    <Card>
      <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Update Region Name</h2>
      <Form
        form={form}
        name="Update-region"
        onFinish={onFinish}
        //... any layout or styling props...
      >
        {role === "admin" && (
          <Form.Item
            label="Region"
            name="oldRegion"
            rules={[{ required: true, message: "Please select a region!" }]}
          >
            {regions ? ( // Check if regions is defined
              <Select showSearch
                  allowClear
                  optionFilterProp="children"
                  placeholder="Select region"
                  filterOption={(input, option) =>
                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                  }
                  filterSort={(optionA, optionB) =>
                    (optionA?.children ?? "").toLowerCase().localeCompare((optionB?.children ?? "").toLowerCase())
                  }
                dropdownStyle={{
                  minWidth: "100px", // Adjust the value as needed
                }}
              >
                {regions.map((region) => (
                  <Select.Option key={region} value={region}>
                    {region}
                  </Select.Option>
                ))}
              </Select>
            ) : (
              <div>
                <Select></Select>
              </div> // Or a loading indicator
            )}
          </Form.Item>
        )}

        <Form.Item
          label="New Region"
          name="updatedRegion"
          rules={[{ required: true, message: "Please enter your new Region!" }]}
        >
          <Input />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Change Region
          </Button>
        </Form.Item>
      </Form>
    </Card>
    </div>
  );
};

export default UpdateRegion;
