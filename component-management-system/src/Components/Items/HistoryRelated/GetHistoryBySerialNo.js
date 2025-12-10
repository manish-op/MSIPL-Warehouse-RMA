import React from "react";
import { Form, Button, Row, Col, Card, Input } from "antd";
import GetItemHistoryBySerialNoAPI from "../../API/ItemRelatedApi/ItemHistory/GetItemHistoryBySerialNoAPI";
import { useItemDetails } from "../UpdateItem/ItemContext";
import { useNavigate } from "react-router-dom";

function GetHistoryBySerialNo() {
  const [form] = Form.useForm();
  const { setItemHistory } = useItemDetails();
  const navigate = useNavigate();

  const onFinish = async (values) => {
    await GetItemHistoryBySerialNoAPI(values, setItemHistory, navigate);
  };

  return (
    <>
      <title>HistorySearchBySerailno</title>
      <div
        style={{
          display: "flex",
          flex: "1",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Card>
          <h2
            style={{
              fontSize: "20px",
              color: "orange",
              textAlign: "center",
              padding: "2px",
              marginBottom: "20px",
            }}
          >
            History Search by Serial no.
          </h2>
          <Form
            form={form}
            name="check-item-form"
            onFinish={onFinish}
            style={{
              display: "flex",
              justifyContent: "center",
              marginTop: "20px",
            }}
          >
            <Row gutter={12}>
              <Col span={12}>
                <Form.Item
                  label="Serial No."
                  name="serialNo"
                  rules={[
                    {
                      required: true,
                      message: "Please enter the serial number!",
                    },
                  ]}
                  htmlFor="serialNoInput"
                >
                  <Input id="serialNoInput" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item>
                  <Button type="primary" htmlType="submit">
                    Search Item
                  </Button>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </Card>
      </div>
    </>
  );
}

export default GetHistoryBySerialNo;
