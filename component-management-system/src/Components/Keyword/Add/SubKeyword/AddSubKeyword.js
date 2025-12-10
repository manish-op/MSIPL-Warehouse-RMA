import React, { useState, useEffect } from "react";
import { Form, Input, Button, Space, Select, Card } from "antd";
import GetKeywordAPI from "../../../API/Keyword/GetKeyword/GetKeywordAPI";
import AddSubKeywordAPI from "../../../API/Keyword/SubKeyword/AddSubKeyword/AddSubKeywordAPI";
import "./AddSubKeyword.css";

const AddSubKeyword = () => {
  const [form] = Form.useForm();
  const [subKeywords, setSubKeywords] = useState([{ subKeyword: "" }]);
  const [keywords, setKeywords] = useState();

  useEffect(() => {
    const fetchKeyword = async () => {
      try {
        const data = await GetKeywordAPI(); // Use await to get the data
        if (data) {
          setKeywords(data);
        } else {
          // Handle the API error (e.g., display an error message)
          
        }
      } catch (error) {
        
      }
    };

    fetchKeyword(); // Call the async function
  }, []);

  const addSubKeyword = () => {
    setSubKeywords([...subKeywords, { subKeyword: "" }]);
  };

  const removeSubKeyword = (index) => {
    const updatedSubKeywords = [...subKeywords];
    updatedSubKeywords.splice(index, 1);
    setSubKeywords(updatedSubKeywords);
  };

  const onFinish = async (values) => {
    const data = {
      keyword: values.keyword,
      subKeywordList: values.subKeywordList.map((subKeyword) => ({
        subKeyword: subKeyword.subKeyword,
      })),
    };

    // console.log('Data:', JSON.stringify(data));
    // Send the data to your API here
    await AddSubKeywordAPI(data);
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
      <title>Add SubKeyword Name</title>
      <Card>
      <h4 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Add Subkeywords</h4>
      <Form
        style={{ display: "flex", flexDirection: "column", marginTop: "5px" }}
        form={form}
        name="add-sub-keyword"
        onFinish={onFinish}
      >
        <Form.Item
          label="Keyword"
          name="keyword"
          rules={[{ required: true, message: "Please select a keyword!" }]}
        >
          {keywords ? ( // Check if regions is defined
            <Select  showSearch
                  allowClear
                  optionFilterProp="children"

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

        <h3 style={{ fontSize: "15px" }}>Subkeywords</h3>
        {subKeywords.map((subKeyword, index) => (
          <Space key={index} align="baseline">
            <Form.Item
              name={["subKeywordList", index, "subKeyword"]}
              rules={[
                { required: true, message: "Please enter a subkeyword!" },
              ]}
            >
              <Input placeholder="Subkeyword" />
            </Form.Item>
            <Button onClick={() => removeSubKeyword(index)} danger>
              Remove
            </Button>
          </Space>
        ))}

        <Form.Item>
          <Button type="dashed" onClick={addSubKeyword} block>
            Add Subkeyword
          </Button>
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Submit
          </Button>
        </Form.Item>
      </Form>
      </Card>
    </div>
  );
};

export default AddSubKeyword;
