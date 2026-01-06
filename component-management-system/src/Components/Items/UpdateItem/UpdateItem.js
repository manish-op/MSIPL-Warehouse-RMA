import { Form, Row, Col, Input, Select, Button, Card } from "antd";
import { useState, useEffect } from "react";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import GetKeywordAPI from "../../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import GetItemAvailabilityStatusOptionAPI from "../../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import GetItemStatusOptionAPI from "../../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import { useItemDetails } from "./ItemContext";
import UpdateItemAPI from "../../API/ItemRelatedApi/UpdateItem/UpdateItemAPI";
import UtcToISO from "../../UtcToISO";
import { Link, useLocation } from "react-router-dom";
import Cookies from "js-cookie";
import { message } from "antd";
import { URL } from "../../API/URL";

function UpdateItem() {
  const [form] = Form.useForm();
  const location = useLocation(); // Hook to access navigation state
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [regions, setRegions] = useState();
  const [keywords, setKeywords] = useState();
  const [subkeywords, setsubKeywords] = useState();
  const [itemAvailabilityOption, setitemAvailabilityOption] = useState();
  const [itemStatusOption, setitemStatusOption] = useState();
  const [keywordSelection, setkeywordSelection] = useState({ keywordName: "" });
  const { itemDetails, setItemDetails, setItemHistory } = useItemDetails();

  // ... (All your useEffects and handler functions remain exactly the same) ...

  // Search function to find item details
  const fetchItemDetails = async (serialNo) => {
    const token = atob(Cookies.get("authToken") || "");
    if (!token) {
      message.error("User not authenticated");
      return;
    }

    // Clear previous history to avoid showing stale data while loading
    setItemHistory([]);

    try {
      const response = await fetch(`${URL}/componentDetails/serialno`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: serialNo,
      });

      if (!response.ok) {
        const mess = await response.text();
        message.warning(mess, 2);
      } else {
        const data = await response.json();
        if (data) {
          setItemDetails(data);
          form.setFieldsValue({
            ...data,
            // Ensure fields match form names if different
          });
          message.success("Item details fetched successfully");

          // Fetch History
          try {
            fetch(`${URL}/componentDetails/history`, {
              method: "POST",
              headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
              },
              body: JSON.stringify(serialNo),
              // In GetItemHistoryBySerialNoAPI.js: body: JSON.stringify(values.serialNo)
              // But wait, values.serialNo is a string. So it sends "serial".
              // Let's safe bet: send serialNo as raw string if the header is not application/json, or json string if it is.
            })
              .then(async (histRes) => {
                if (histRes.ok) {
                  const histData = await histRes.json();
                  if (histData && Array.isArray(histData)) {
                    setItemHistory(histData);
                  }
                }
              });
          } catch (err) {
            console.error("Failed to fetch history", err);
          }
        }
      }
    } catch (error) {
      console.error(error);
      message.error("Failed to fetch item details");
    }
  };

  const onSearch = async (value) => {
    if (value) {
      await fetchItemDetails(value);
    }
  };
  // Pre-fill serial number if coming from Alerts
  useEffect(() => {
    if (location.state && location.state.serialNo) {
      const serial = location.state.serialNo;
      form.setFieldsValue({ serialNo: serial }); // Use correct field name 'serialNo' matching the form item
      fetchItemDetails(serial);
    }
  }, [location.state, form]);


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
        //console.error('API Error:', error);
      }
    };

    fetchAvailabilityStatus(); // Call the async function
  }, []);

  //Api is calling for getting item option status
  useEffect(() => {
    const fetchitemOptionStatus = async () => {
      try {
        const data = await GetItemStatusOptionAPI(); // Use await to get the data
        if (data) {
          setitemStatusOption(data);
        } else {
          // Handle the API error (e.g., display an error message)
          //console.error('Failed to fetch Item Status Option');
        }
      } catch (error) {
        //console.error('API Error:', error);
      }
    };

    fetchitemOptionStatus(); // Call the async function
  }, []);

  //Api is calling for getting region from Database
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI(); // Use await to get the data
        if (data) {
          setRegions(data);
        } else {
          // Handle the API error (e.g., display an error message)
          //console.error('Failed to fetch regions');
        }
      } catch (error) {
        // console.error('API Error:', error);
      }
    };

    fetchRegions(); // Call the async function
  }, []);

  //Api is calling for getting Keyword
  useEffect(() => {
    const fetchKeyword = async () => {
      try {
        const data = await GetKeywordAPI(); // Use await to get the data
        if (data) {
          setKeywords(data);
        } else {
          // Handle the API error (e.g., display an error message)
          //console.error('Failed to fetch regions');
        }
      } catch (error) {
        //console.error('API Error:', error);
      }
    };

    fetchKeyword(); // Call the async function
  }, []);

  //Api calling for getting sub/keyword
  useEffect(() => {
    const fetchSubKeyword = async (keywordSelection) => {
      // Change parameter to keywordName
      try {
        const data = await GetSubKeywordAPI(keywordSelection); // Pass keywordName to the API
        if (data) {
          const subKeywordValues = data.subKeywordList.map(
            (item) => item.subKeyword
          );
          setsubKeywords(subKeywordValues);
        } else {
          //console.error('Failed to fetch subkeywords');
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

  //hande keyword changes for calling subkeyword
  const handleKeywordChange = (selectedKeyword) => {
    setkeywordSelection({
      ...keywordSelection,
      keywordName: selectedKeyword,
    });
    setsubKeywords(null);
    form.setFieldsValue({ subKeyword: null });
  };

  // calling api for update data
  const onFinish = async (values) => {
    await UpdateItemAPI(values, setItemDetails);
  };

  //changing time zone to india function calling
  const changeTimeZone = (date) => {
    return UtcToISO(date);
  };

  //changing update and assign time formate to india
  useEffect(() => {
    if (itemDetails && itemDetails.updateDate) {
      form.setFieldsValue({
        updateDate: changeTimeZone(itemDetails?.updateDate),
      });
    } else {
      form.setFieldsValue({
        updateDate: "",
      });
    }

    if (itemDetails && itemDetails.addingDate) {
      form.setFieldsValue({
        addingDate: changeTimeZone(itemDetails?.addingDate),
      });
    } else {
      form.setFieldsValue({
        addingDate: "",
      });
    }
  }, [form, itemDetails]);


  return (
    <>
      <title>Item Details</title>
      <div id="printPage">
        <Card style={{ margin: "20px", padding: "10px" }}>
          <h4
            style={{
              fontSize: "20px",
              color: "orange",
              textAlign: "center",
              padding: "2px",
              marginBottom: "20px",
            }}
          >
            Update Item Details
          </h4>

          {/* CHANGE 1: 
            - Add 'layout="vertical"' to put labels above inputs.
            - Remove the inline style prop.
          */}
          <Form
            layout="vertical"
            form={form}
            name="spare-part-form"
            onFinish={onFinish}
            initialValues={itemDetails}
          >
            <Row gutter={16}>
              {/* CHANGE 2: 
                - All <Col> are now 'span={8}' for a uniform 3-column grid.
                - All 'style={{ margin: "2px" }}' are removed from <Form.Item>.
              */}
              <Col span={8}>
                <Form.Item
                  label="Serial No."
                  name="serialNo"
                  rules={[{ required: false }]}
                >
                  <Input disabled />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Added By"
                  name="addedByEmail"
                  rules={[{ required: false }]}
                >
                  <Input disabled />
                </Form.Item>
              </Col>

              <Col span={8}>
                <Form.Item
                  label="Item Added Date"
                  name="addingDate"
                  rules={[{ required: false }]}
                >
                  <Input disabled />
                </Form.Item>
              </Col>

              <Col span={8}>
                <Form.Item
                  label="Updated By"
                  name="empEmail"
                  rules={[{ required: false }]}
                >
                  <Input disabled />
                </Form.Item>
              </Col>

              <Col span={8}>
                <Form.Item
                  label="Item Updated Date"
                  name="updateDate"
                  rules={[{ required: false }]}
                >
                  <Input disabled />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Box No."
                  name="boxNo"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the box number!",
                    },
                  ]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Part No."
                  name="partNo"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the part number!",
                    },
                  ]}
                >
                  <Input disabled={role === "employee"} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Model No."
                  name="modelNo"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the model number!",
                    },
                  ]}
                >
                  <Input disabled={role === "employee"} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Rack No."
                  name="rackNo"
                  rules={[
                    {
                      required: true,
                      message: "Please enter the rack number!",
                    },
                  ]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Spare Location"
                  name="spareLocation"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the spare location!",
                    },
                  ]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Item Name"
                  name="system"
                  rules={[
                    { required: false, message: "Please enter the system!" },
                  ]}
                >
                  <Input disabled={role === "employee"} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Module For"
                  name="moduleFor"
                  rules={[
                    { required: false, message: "Please enter the module!" },
                  ]}
                >
                  <Input disabled={role === "employee"} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="module Version"
                  name="systemVersion"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the system version!",
                    },
                  ]}
                >
                  <Input disabled={role === "employee"} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Item Status"
                  name="itemStatus"
                  rules={[
                    {
                      required: true,
                      message: "Please select the item status!",
                    },
                  ]}
                >
                  {itemStatusOption ? (
                    <Select
                      dropdownStyle={{ minWidth: "100px" }}
                      style={{ width: "AUTO" }}
                    >
                      {itemStatusOption.map((itemStatus) => (
                        <Select.Option key={itemStatus} value={itemStatus}>
                          {itemStatus}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select value={null} style={{ width: "AUTO" }}></Select> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Region"
                  name="region"
                  rules={[
                    { required: true, message: "Please select a region!" },
                  ]}
                >
                  {regions ? ( // Check if regions is defined
                    <Select
                      disabled={role === "employee" || role === "manager"}
                      dropdownStyle={{
                        minWidth: "100px",
                        // Adjust the value as needed
                      }}
                      style={{ width: "AUTO" }}
                    >
                      {regions.map((region) => (
                        <Select.Option key={region} value={region}>
                          {region}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select value={null} style={{ width: "AUTO" }}></Select> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Keyword"
                  name="keyword"
                  rules={[
                    { required: true, message: "Please select a keyword!" },
                  ]}
                >
                  {keywords ? (
                    <Select
                      disabled={role === "employee"}
                      onChange={handleKeywordChange}
                      dropdownStyle={{ minWidth: "200px" }}
                      style={{ width: "AUTO" }}
                    >
                      {keywords.map((keyword) => (
                        <Select.Option key={keyword} value={keyword}>
                          {keyword}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select
                      disabled={role === "employee"}
                      value={null}
                      dropdownStyle={{ minWidth: "100px" }}
                      style={{ width: "AUTO" }}
                    ></Select>
                  )}
                </Form.Item>
              </Col>

              <Col span={8}>
                <Form.Item
                  label="Sub Keyword"
                  name="subKeyword"
                  rules={[
                    { required: false, message: "Please select a keyword!" },
                  ]}
                >
                  {subkeywords ? (
                    <Select
                      disabled={role === "employee"}
                      dropdownStyle={{
                        minWidth: "100px",
                      }}
                      style={{ width: "AUTO" }}
                    >
                      <Select.Option value={""}>Select</Select.Option>
                      {subkeywords.map((subkeyword) => (
                        <Select.Option key={subkeyword} value={subkeyword}>
                          {subkeyword}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select
                      disabled={role === "employee"}
                      value={null}
                      dropdownStyle={{ width: "100px" }}
                      style={{ width: "AUTO" }}
                    ></Select> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Available Status"
                  name="itemAvailability"
                  rules={[
                    {
                      required: true,
                      message: "Please select Availability Status!",
                    },
                  ]}
                >
                  {itemAvailabilityOption ? ( // Check if regions is defined
                    <Select dropdownStyle={{ minWidth: "100px" }}>
                      {itemAvailabilityOption.map((itemAvailStatus) => (
                        <Select.Option
                          key={itemAvailStatus}
                          value={itemAvailStatus}
                        >
                          {itemAvailStatus}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select value={null}></Select> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Return Duration (Days)"
                  name="returnDuration"
                  rules={[
                    {
                      required: false,
                      message: "Please enter return duration in days!",
                    },
                  ]}
                >
                  <Input type="number" placeholder="Enter days (e.g., 7)" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Loaned To"
                  name="partyName"
                  rules={[
                    { required: true, message: "Please add Loaned To Name!" },
                  ]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Remark"
                  name="remark"
                  rules={[
                    { required: false, message: "Add Remark if required!" },
                  ]}
                >
                  <Input.TextArea />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="Item Description"
                  name="itemDescription"
                  rules={[
                    {
                      required: false,
                      message: "Please enter the item description!",
                    },
                  ]}
                >
                  <Input.TextArea />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item style={{ textAlign: "center", marginTop: "20px" }}>
              <Button
                type="primary"
                htmlType="submit"
              >
                Update
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: "center" }}>
            <Link to="/dashboard/itemRepairing">
              <Button type="default">
                Repairing
              </Button>
            </Link>
          </div>
        </Card>
      </div>
    </>
  );
}

export default UpdateItem;