import React, { useEffect, useState } from "react";
import { Form, Select, Input, Button, Card } from "antd";
import GetKeywordAPI from "../../../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../../../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import UpdateSubKeywordAPI from "../../../API/Keyword/SubKeyword/UpdateSubKeyword/UpdateSubKeywordAPI";

function UpdateSubKeyword() {
  const [form] = Form.useForm();
  const [keywords, setKeywords] = useState();
  const [subkeywords, setsubKeywords] = useState();
  const [keywordSelection, setkeywordSelection] = useState({
    keywordName: "",
  });

  useEffect(() => {
    const fetchKeyword = async () => {
      try {
        const data = await GetKeywordAPI(); // Use await to get the data
        if (data) {
          setKeywords(data);
        } else {
          // Handle the API error (e.g., display an error message)
          // alert("Failed to fetch Keyword");
        }
      } catch (error) {
        // alert("API Error:", error.message);
      }
    };

    fetchKeyword(); // Call the async function
  }, []);

  //Api calling for getting sub/keyword
  useEffect(() => {
    const fetchSubKeyword = async (keywordSelection) => {
      // Change parameter to keywordName
      try {
        // Log the keywordName string
        const data = await GetSubKeywordAPI(keywordSelection); // Pass keywordName to the API
        if (data) {
          const subKeywordValues = data.subKeywordList.map(
            (item) => item.subKeyword
          );
          setsubKeywords(subKeywordValues);
        } else {
          //alert('Failed to fetch subkeywords');
        }
      } catch (error) {
        //console.error('API Error:', error);
      }
    };

    if (keywordSelection.keywordName) {
      // Prevent API call on initial render if keywordName is empty
      fetchSubKeyword(keywordSelection.keywordName); // Pass keywordName
    }
  }, [keywordSelection.keywordName]);

  const handleKeywordChange = (selectedKeyword) => {
    setkeywordSelection({
      ...keywordSelection,
      keywordName: selectedKeyword,
    });
    setsubKeywords(null);
    form.setFieldsValue({ oldSubKeyword: null });
  };

  const onFinish = async (values) => {
    const data = {
      keywordName: values.keywordName,
      updateSubKeyword: values.updateSubKeyword,
      oldSubKeyword: values.oldSubKeyword,
    };   
    await UpdateSubKeywordAPI(data);
  };

  return (
    <div style={{display:'flex', flex:'1', justifyContent:'center', alignItems:'center'}}>
      <title>Sub Keyword  Update</title>
      <Card>
      <h2 style={{ fontSize: "20px", color: "orange", textAlign:'center', padding:'2px', marginBottom:'20px'}}>Sub Keyword Name Update </h2>
      <Form
        style={{ display: "flex", flexDirection: "column" }}
        form={form}
        onFinish={onFinish}
      >
        <Form.Item
          label="Keyword"
          name="keywordName"
          rules={[{ required: true, message: "Please select a keyword!" }]}
        >
          {keywords ? ( // Check if regions is defined
            <Select
              onChange={handleKeywordChange}
              dropdownStyle={{ minWidth: "100px" }
            }

            showSearch
                  allowClear
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                  }
                  filterSort={(optionA, optionB) =>
                    (optionA?.children ?? "").toLowerCase().localeCompare((optionB?.children ?? "").toLowerCase())
                  }
            >
              {keywords.map((keyword) => (
                <Select.Option key={keyword} value={keyword}>
                  {keyword}
                </Select.Option>
              ))}
            </Select>
          ) : (
            <div>
              <Select value={null}></Select>
            </div> // Or a loading indicator
          )}
        </Form.Item>

        {subkeywords ? (
          <Form.Item
            label="Sub Keyword"
            name="oldSubKeyword"
            rules={[{ required: true, message: "Please select a keyword!" }]}
          >
              <Select
                dropdownStyle={{
                  minWidth: "100px", 
                }}
                showSearch
                  allowClear
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    (option?.children ?? "").toLowerCase().includes(input.toLowerCase())
                  }
                  filterSort={(optionA, optionB) =>
                    (optionA?.children ?? "").toLowerCase().localeCompare((optionB?.children ?? "").toLowerCase())
                  }
              >
                {subkeywords.map((subkeyword) => (
                  <Select.Option key={subkeyword} value={subkeyword}>
                    {subkeyword}
                  </Select.Option>
                ))}
              </Select>
          </Form.Item>
        ) : null}

        {subkeywords ? (
        <Form.Item
          label="Updated SubKeyword"
          name="updateSubKeyword"
          rules={[
            {
              required: true,
              message: "Please enter a Updated SubKeyword Name!",
            },
          ]}
        >
          <Input />
        </Form.Item>
      ):null}

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Update Keyword
          </Button>
        </Form.Item>
      </Form>
      </Card>
    </div>
  );
}
export default UpdateSubKeyword;
