import {
  Form,
  Row,
  Col,
  Input,
  Button,
  Card,
  Select,
  Divider,
} from "antd";
import { useEffect, useState, useMemo } from "react";
import { useItemDetails } from "../../Items/UpdateItem/ItemContext";
import UtcToISO from "../../UtcToISO";
import WarrantyOptionAPI from "../../API/RepairingOption/WarrantyOptionAPI";
import AfterInwardPassFruMakingAPI from "../../API/FRU/AfterInwardPassFruMakingAPI";

const { Option } = Select;
const { TextArea } = Input;

//changing time zone to india function
const changeTimeZone = (date) => {
  return UtcToISO(date);
};

// --- Display-Only Section ---
// This component renders all the disabled item detail fields.
const ItemDisplaySection = () => (
  <Row gutter={16}>
    <Col span={8}>
      <Form.Item label="Serial No." name="serialNo" style={{ margin: "2px" }}>
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Added By" name="addedByEmail">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Item Added Date" name="addingDate">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Updated By" name="empEmail">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Item Updated Date" name="updateDate">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Box No." name="boxNo">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Part No." name="partNo">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Model No." name="modelNo">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Rack No." name="rackNo">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Spare Location" name="spareLocation">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Item Name" name="system">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Module For" name="moduleFor">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="module Version" name="systemVersion">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Item Status" name="itemStatus">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Region" name="region">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Keyword" name="keyword" style={{ margin: "2px" }}>
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Sub Keyword" name="subKeyword">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Available Status" name="itemAvailability">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Party Name" name="partyName">
        <Input disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Remark" name="remark">
        <TextArea disabled />
      </Form.Item>
    </Col>
    <Col span={8}>
      <Form.Item label="Item Description" name="itemDescription">
        <TextArea disabled />
      </Form.Item>
    </Col>
  </Row>
);

// --- Repair Input Section ---
// This component renders the active fields for repair submission.
const RepairInputSection = ({ warrantyOption }) => (
  <>
    <Divider>Enter Repair Details</Divider>
    <Row gutter={16}>
      <Col span={12}>
        <Form.Item
          label="RMA NO."
          name="rmaNo"
          rules={[{ required: true, message: "Please enter the RMA No.!" }]}
        >
          <Input />
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          name="warrantyDetails"
          label="Warranty Details"
          rules={[{ required: true, message: "Please enter Warranty Details!" }]}
        >
          <Select
            styles={{
              root: { minWidth: "100px", textAlign: "left" },
              popup: { root: { minWidth: "120px" } },
            }}
          >
            {warrantyOption?.map((warrantyStatus) => (
              <Option key={warrantyStatus} value={warrantyStatus}>
                {warrantyStatus}
              </Option>
            ))}
          </Select>
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          label="Fault Detail"
          name="faultDetails"
          rules={[{ required: false, message: "Please explain the item fault" }]}
        >
          <Input />
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          label="Fault Remark"
          name="faultRemark"
          rules={[{ required: false, message: "Please explain the item fault" }]}
        >
          <Input />
        </Form.Item>
      </Col>
    </Row>
  </>
);

// --- Main Page Component ---
function RepairingPage() {
  const [form] = Form.useForm();
  const { itemDetails } = useItemDetails();
  const [warrantyOption, setWarrantyOption] = useState();

  // Prepare initialValues with formatted dates using useMemo
  const formattedItemDetails = useMemo(() => {
    if (!itemDetails) return {};
    return {
      ...itemDetails,
      addingDate: itemDetails.addingDate
        ? changeTimeZone(itemDetails.addingDate)
        : "",
      updateDate: itemDetails.updateDate
        ? changeTimeZone(itemDetails.updateDate)
        : "",
    };
  }, [itemDetails]);

  // API call for getting item warranty list
  useEffect(() => {
    const fetchwarrantyOptionStatus = async () => {
      try {
        const data = await WarrantyOptionAPI();
        if (data) {
          setWarrantyOption(data);
        }
      } catch (error) {
        console.error("API Error:", error);
      }
    };

    fetchwarrantyOptionStatus();
  }, []);

  // API call for update data
  const onFinish = async (values) => {
    const data = {
      fruSerialNo: values.serialNo,
      rmaNo: values.rmaNo,
      warrantyDetails: values.warrantyDetails,
      faultDescription: values.faultDetails,
      faultRemark: values.faultRemark,
    };
    console.log(data);
    await AfterInwardPassFruMakingAPI(data);
  };

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
            Repairing Item Details
          </h4>
          <Form
            form={form}
            name="spare-part-form"
            layout="vertical"
            onFinish={onFinish}
            initialValues={formattedItemDetails}
          >
            <ItemDisplaySection />
            <RepairInputSection warrantyOption={warrantyOption} />

            <Form.Item style={{ marginTop: "20px" }}>
              <Button type="primary" htmlType="submit">
                Submit Repair
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </>
  );
}

export default RepairingPage;