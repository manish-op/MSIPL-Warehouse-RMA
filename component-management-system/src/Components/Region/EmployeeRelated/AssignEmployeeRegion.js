import { Card, Form, Button, Input, Select, message } from "antd";
import { useState, useEffect } from "react";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import ChangeEmployeeRegionAPI from "../../API/Region/ChangeEmployeeRegion/ChangeEmployeeRegionAPI";

const AssignEmployeeRegion = () => {
  const [form] = Form.useForm();
  let role = localStorage.getItem("_User_role_for_MSIPL");
  const [empChangeRegion, setempChangeRegion] = useState({
    empEmail: "",
    region: "",
  });
  const [regions, setRegions] = useState();

  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI(); // Use await to get the data
        if (data) {
          setRegions(data);
        } else {
          // Handle the API error (e.g., display an error message)
          console.error("Failed to fetch regions");
        }
      } catch (error) {
        console.error("API Error:", error);
      }
    };

    fetchRegions(); // Call the async function
  }, []);

  const handleChange = (event, field) => {
    let actualValue = event.target.value;
    setempChangeRegion({
      ...empChangeRegion,
      [field]: actualValue,
    });
  };

  const handleRegionChange = (value, field) => {
    setempChangeRegion({
      ...empChangeRegion,
      [field]: value,
    });
  };

  const onFinish = async (event) => {
    //event.preventDefault();
    console.log(empChangeRegion);
    if (
      empChangeRegion.empEmail.trim() === "" ||
      empChangeRegion.region.trim() === ""
    ) {
      message.warning("username and password cant be blank");
    } else {
      if (role === "admin") {
       await ChangeEmployeeRegionAPI(empChangeRegion);
      }
    }
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
      <title>Assign employeeRegion</title>
    <Card>
      <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Change/Assign Employee Region</h2>
      <Form
        form={form}
        name="assign-employee-region"
        onFinish={onFinish}
        //... any layout or styling props...
      >
        <Form.Item
          style={{ marginTop: "50px" }}
          label="Employee Email"
          name="empEmail"
          rules={[
            { required: true, message: "Please enter employee email" },
            { type: "email", message: "Please enter a valid email address!" },
          ]}
        >
          <Input
            value={empChangeRegion.empEmail}
            onChange={(e) => handleChange(e, "empEmail")}
          />
        </Form.Item>

        <Form.Item
          label="Region"
          name="region"
          rules={[{ required: true, message: "Please select a region!" }]}
        >
          {regions ? ( // Check if regions is defined
            <Select onChange={(value) => handleRegionChange(value, "region")}>
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

export default AssignEmployeeRegion;
