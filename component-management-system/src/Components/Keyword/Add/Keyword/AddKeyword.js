import React, { useState } from "react";
import { Form, Input, Button, Space, Card } from "antd";
import AddKeywordAPI from "../../../API/Keyword/AddKeyword/AddKeywordAPI";
import "./AddKeyword.css";

const AddKeyword = () => {
  const [form] = Form.useForm();
  const [subKeywords, setSubKeywords] = useState([{ subKeyword: "" }]);

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

    // Send the data to your API here
    await AddKeywordAPI(data);
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
      <title>Add Keyword Name</title>
      <Card>
        <h4
          style={{
            fontSize: "20px",
            color: "orange",
            textAlign: "center",
            padding: "2px",
            marginBottom: "20px",
          }}
        >
          Add Keyword Name
        </h4>
        <Form
          style={{ display: "flex", flexDirection: "column" }}
          form={form}
          onFinish={onFinish}
        >
          <Form.Item
            label="Keyword"
            name="keyword"
            rules={[{ required: true, message: "Please enter a keyword!" }]}
          >
            <Input />
          </Form.Item>

          <h3>Subkeywords</h3>
          {subKeywords.map((subKeyword, index) => (
            <Space key={index} align="baseline">
              <Form.Item
                name={["subKeywordList", index, "subKeyword"]}
                rules={[
                  { required: false, message: "Please enter a subkeyword!" },
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

export default AddKeyword;
