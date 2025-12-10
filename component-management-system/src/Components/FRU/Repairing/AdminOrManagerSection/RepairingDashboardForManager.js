import { Button, Col, DatePicker, Form, Row, Select } from "antd";
import React, { useEffect, useState } from "react";
import ItemRepairingStatusAPI from "../../../API/RepairingOption/ItemRepairingStatusAPI";
import TechnicianOptionAPI from "../../../API/RepairingOption/TechnicianStatusOptionAPI";
import GetRegionAPI from "../../../API/Region/GetRegion/GetRegionAPI";
import RepairingDashboardForManagerAPI from "../../../API/FRU/GetTicketDetails/RepairingDashboardForManagerAPI";
import TableFormateFormAdminRepairingSearch from "./TableFormateFormAdminRepairingSearch";
//import { useTicketDetails } from "../../RepairingContext/RepairingContext";
import UpdateSectionOfTicket from "./UpdateSectionOfTicket";
import dayjs from "dayjs";

function RepairingDashboardForManager() {
  const [form] = Form.useForm();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [repairingStatus, setRepairingStatus] = useState();
  const [technicianStatus, setTechnicianStatus] = useState();
  const [region, setRegion] = useState();
  const [ticketDetails, setTicketDetails] = useState(
    JSON.parse(localStorage.getItem("ticketDetails")) || []
  );

  //api calling for get region
  useEffect(() => {
    const fetchRegion = async () => {
      try {
        const data = await GetRegionAPI();
        if (data) {
          setRegion(data);
        }
      } catch (error) {}
    };
    fetchRegion();
  }, []);
  
  //api calling for get repairingStatus
  useEffect(() => {
    const fetchRepairingStatus = async () => {
      try {
        const data = await ItemRepairingStatusAPI();
        if (data) {
          setRepairingStatus(data);
        } else {
        }
      } catch (error) {}
    };
    fetchRepairingStatus();
  }, []);

  //api calling for technicianStatus
  useEffect(() => {
    const fetchTechnicianStatus = async () => {
      try {
        const data = await TechnicianOptionAPI();
        if (data) {
          setTechnicianStatus(data);
        }
      } catch (error) {}
    };
    fetchTechnicianStatus();
  }, []);

  const onFinish = async (values) => {
    await RepairingDashboardForManagerAPI(values, setTicketDetails);
    // Here you can send the 'values' object to your API
  };

  return (
    <>
      <title>Ticket Details Dashboard</title>
      <div style={{ maxWidth:"100%",minWidth:"100%", minHeight:"100%", maxHeight:"100%",}}>
        <div
          style={{
            display: "flex",
            flexDirection: "row",
            paddingLeft: "10px",
            paddingTop: "5px",
            overflowY:"auto",
            overflowX:"auto",
            position:"sticky",
            minHeight:"20%",
            maxHeight:"20%",
            minWidth:"100%",
            maxWidth:"100%",
          }}
          
        >
          <Form
            style={{
              display: "flex",
              flexDirection: "column",
              width: "100%",
              wordWrap: "normal",
              textAlign: "center",
              padding: "5px",
              overflow:'auto'
            }}
            form={form}
            name="TicketDetailSEO"
            onFinish={onFinish}
          >
            <Row>
              <Col>
                <Form.Item
                  label="FROM: "
                  name="startingDate"
                  rules={[{ required: true }]}
                  style={{ margin: "4px" }}
                >
                  <DatePicker
                    size="middle"
                    style={{ width: 130 }}
                    disabledDate={(current) => {
                      return current && current > dayjs().endOf("day");
                    }}
                  />
                </Form.Item>
              </Col>

              <Col>
                <Form.Item
                  label="TO: "
                  name="endDate"
                  rules={[{ required: false }]}
                  style={{ margin: "4px" }}
                >
                  <DatePicker
                    size="middle"
                    style={{ width: 130 }}
                    disabledDate={(current) => {
                      return current && current > dayjs().endOf("day");
                    }}
                  />
                </Form.Item>
              </Col>

              {role === "admin" && (
                <Col>
                  <Form.Item
                    label="Region"
                    name="region"
                    rules={[
                      { required: true, message: "Please select a region!" },
                    ]}
                  >
                    {region ? ( // Check if regions is defined
                      <Select
                        styles={{
                          root: {
                            minWidth: "100px",                          
                            textAlign: 'left',
                          },
                          popup: {
                            root: {
                              minWidth: "120px",
                            },
                          },
                        }}
                        size="middle"
                        style={{ width: 120 }}
                      >
                        {region.map((regionName) => (
                          <Select.Option key={regionName} value={regionName}>
                            {regionName}
                          </Select.Option>
                        ))}
                      </Select>
                    ) : (
                      <Select
                        styles={{
                          root: {
                            minWidth: "100px",
                          },
                          popup: {
                            root: {
                              minWidth: "150px",
                            },
                          },
                        }}
                        size="middle"
                        value={""}
                      ></Select>
                    )}
                  </Form.Item>
                </Col>
              )}

              <Col>
                <Form.Item
                  label="Technician Status"
                  name="technicianStatus"
                  rules={[{ required: false }]}
                >
                  {technicianStatus ? ( // Check if regions is defined
                    <Select
                      styles={{
                        root: {
                          minWidth: "100px",
                        },
                        popup: {
                          root: {
                            minWidth: "150px",
                          },
                        },
                      }}
                      size="middle"
                    >
                      <Select.Option key="select" value={""}>Select</Select.Option>
                      {technicianStatus.map((techStatus) => (              
                        <Select.Option key={techStatus} value={techStatus}>
                          {techStatus}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <Select
                      styles={{
                        root: {
                          minWidth: "100px",
                        },
                        popup: {
                          root: {
                            minWidth: "150px",
                          },
                        },
                      }}
                      size="middle"
                      value={""}
                    ></Select> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>

              <Col>
                <Form.Item
                  label="Repair Status"
                  name="repairStatus"
                  rules={[{ required: false }]}
                >
                  {repairingStatus ? ( // Check if regions is defined
                    <Select
                      styles={{marginLeft: "2px",
                        root: {
                          minWidth: "100px",
                        },
                        popup: {
                          root: {
                            minWidth: "150px",
                          },
                        },
                      }}
                      size="middle"
                    >
                  <Select.Option key="select" value={""}>Select</Select.Option>
                      {repairingStatus.map((repairStatusName) => (
                        <Select.Option
                          key={repairStatusName}
                          value={repairStatusName}
                        >
                          {repairStatusName}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : (
                    <div>
                      <Select
                        styles={{marginLeft: "2px",
                          root: {
                            minWidth: "100px",
                          },
                          popup: {
                            root: {
                              minWidth: "150px",
                            },
                          },
                        }}
                        size="middle"
                        value={""}
                      ></Select>
                    </div> // Or a loading indicator
                  )}
                </Form.Item>
              </Col>

              <Col>
                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    style={{
                      marginLeft: "2px",
                    }}
                    size="middle"
                  >
                    Search
                  </Button>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </div>
        <div
          style={{
            display: "flex",
            minHeight: "80%",
            maxHeight:"80%",
            flexDirection: "row",
            minWidth: "100%",
            maxWidth: "100%",
          }}
        >
          {/* ticket list section div   */}
          <div
            style={{
              maxWidth: "60%",
              minWidth: "60%",
              minHeight:"100%",
              maxHeight:"100%",
              overflow: "auto",
              border: "solid 2px black",
            }}
          >
            <title>Get Ticket Details</title>
            {TableFormateFormAdminRepairingSearch(ticketDetails)}
          </div>
          {/* update ticket section div */}
          <div
            style={{
              maxWidth: "40%",
              minWidth: "40%",
              overflow: "auto",
              minHeight:"100%",
              maxHeight:"100%",
              marginLeft: "2px",
              border: "solid 2px black",
            }}
          >
            
            <>
            {UpdateSectionOfTicket()}
            </>
          </div>
        </div>
        {/* content parent section div closed*/}

      </div>
    </>
  );
}

export default RepairingDashboardForManager;
