import React, { useEffect, useState } from "react";
import { Form, Select, Input, Button, Card } from "antd";
import GetKeywordAPI from "../../../API/Keyword/GetKeyword/GetKeywordAPI";
import UpdateKeywordAPI from "../../../API/Keyword/UpdateKeyword/UpdateKeywordAPI";

function UpdateKeyword() {
  const [form] = Form.useForm();
  const [keywords, setKeywords] = useState();

  useEffect(() => {
    const fetchKeyword = async () => {
      try {
        const data = await GetKeywordAPI(); // Use await to get the data
        if (data) {
          setKeywords(data);
        } else {
          // Handle the API error (e.g., display an error message)
          alert("Failed to fetch Keyword");
        }
      } catch (error) {
        alert("API Error:", error.message);
      }
    };

    fetchKeyword(); // Call the async function
  }, []);

  const onFinish = async (values) => {
    const data = {
      oldKeyword: values.oldKeyword,
      newKeyword: values.newKeyword,
    };

    // Send the data to your API here
    await UpdateKeywordAPI(data);
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
      <title>Keyword Update</title>
      <Card>
      <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Keyword Name Update </h2>
      <Form
        style={{ display: "flex", flexDirection: "column" }}
        form={form}
        onFinish={onFinish}
      >
        <Form.Item
          label="Old Keyword"
          name="oldKeyword"
          rules={[{ required: true, message: "Please select a keyword!" }]}
        >
          {keywords ? ( // Check if regions is defined
            <Select showSearch
                  allowClear
                  optionFilterProp="children"
                  placeholder="Select Keyword"
                  filterOption={(input, option) =>
                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                  }
                  filterSort={(optionA, optionB) =>
                    (optionA?.children ?? "").toLowerCase().localeCompare((optionB?.children ?? "").toLowerCase())
                  }>
              {keywords.map((keyword) => (
                <Select.Option key={keyword} value={keyword}>
                  {keyword}
                </Select.Option>
              ))}
            </Select>
          ) : (
            <div>
              <Select></Select>
            </div> // Or a loading indicator
          )}
        </Form.Item>

        <Form.Item
          label="New Keyword"
          name="newKeyword"
          rules={[{ required: true, message: "Please enter a keyword!" }]}
        >
          <Input />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Update 
          </Button>
        </Form.Item>
      </Form>
      </Card>
    </div>
  );
}
export default UpdateKeyword;
