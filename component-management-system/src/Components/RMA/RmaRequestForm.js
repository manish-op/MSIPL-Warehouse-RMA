// src/Components/RMA/RmaRequestForm.js
import React, { useState, useEffect } from "react";
import {
  Form,
  Input,
  Select,
  Button,
  Row,
  Col,
  Typography,
  Steps,
  Card,
  Divider,
  Space,
  Tag,
  message,
  Result,
  AutoComplete,
  Modal,
  Radio,
} from "antd";
import {
  PlusOutlined,
  MinusCircleOutlined,
  ArrowLeftOutlined,
  ArrowRightOutlined,
  SendOutlined,
  CopyOutlined,
  UserOutlined,
  CarOutlined,
  InboxOutlined,
  FileSearchOutlined,
} from "@ant-design/icons";
import RmaLayout from "./RmaLayout";
import "./RmaRequestForm.css";
import { useNavigate } from "react-router-dom";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import { exportRmaToExcel } from "./RmaExcelExport";

const { TextArea } = Input;
const { Option } = Select;
const { Title, Text, Paragraph } = Typography;

// Fallback product catalog (used if API fails)
const DEFAULT_PRODUCT_CATALOG = [
  { name: "Other", model: "", partNo: "" },
];

function RmaRequestForm() {
  const [form] = Form.useForm();
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [rmaNumbers, setRmaNumbers] = useState([]);
  const [rmaRequestNumber, setRmaRequestNumber] = useState(""); // Store the RMA request number
  const [sameAsReturn, setSameAsReturn] = useState(false);
  const [productCatalog, setProductCatalog] = useState(DEFAULT_PRODUCT_CATALOG);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [submittedFormData, setSubmittedFormData] = useState(null); // Store form data for Excel export
  const [repairType, setRepairType] = useState(null);
  const [previewData, setPreviewData] = useState(null);
  const [conformVisible, setConfrimVisible] = useState(false);
  const [finalSubmitting, setFinalSubmitting] = useState(false);

  // Customer auto-complete state
  const [customerOptions, setCustomerOptions] = useState([]);
  const [searchingCustomers, setSearchingCustomers] = useState(false);

  const navigate = useNavigate();

  // Fetch product catalog from backend
  useEffect(() => {
    const fetchProductCatalog = async () => {
      setLoadingProducts(true);
      const result = await RmaApi.getProductCatalog();
      if (result.success && Array.isArray(result.data) && result.data.length > 0) {
        setProductCatalog(result.data);
      } else {
        console.warn("Could not fetch product catalog, using defaults");
      }
      setLoadingProducts(false);
    };
    fetchProductCatalog();
  }, []);

  // Load saved data from localStorage on mount
  useEffect(() => {
    const savedData = localStorage.getItem("rmaFormData");
    if (savedData) {
      try {
        const parsedData = JSON.parse(savedData);
        form.setFieldsValue(parsedData);
        if (parsedData.sameAsReturn) {
          setSameAsReturn(true);
        }
      } catch (error) {
        console.error("Failed to parse saved form data:", error);
      }
    }
  }, [form]);

  // Auto-fill user info from localStorage
  useEffect(() => {
    const userName = localStorage.getItem("userName");
    const userEmail = localStorage.getItem("userEmail");
    if (userName && !form.getFieldValue("contactName")) {
      form.setFieldsValue({ contactName: userName });
    }
    if (userEmail && !form.getFieldValue("email")) {
      form.setFieldsValue({ email: userEmail });
    }
  }, [form]);

  // Save form data to localStorage whenever it changes
  const handleFormChange = (changedValues, allValues) => {
    localStorage.setItem("rmaFormData", JSON.stringify({ ...allValues, sameAsReturn }));
  };

  const handleReset = () => {
    form.resetFields();
    localStorage.removeItem("rmaFormData");
    setCurrentStep(0);
    setSameAsReturn(false);
  };

  // Copy return address to invoice address
  const copyToInvoice = () => {
    const values = form.getFieldsValue();
    form.setFieldsValue({
      invoiceCompanyName: values.companyName,
      invoiceEmail: values.email,
      invoiceContactName: values.contactName,
      invoiceTelephone: values.telephone,
      invoiceMobile: values.mobile,
      invoiceAddress: values.returnAddress,
    });
    setSameAsReturn(true);
    message.success("Copied return address to invoice address");
  };

  // Customer search handler for auto-complete
  const handleCustomerSearch = async (searchText) => {
    if (!searchText || searchText.length < 2) {
      setCustomerOptions([]);
      return;
    }

    setSearchingCustomers(true);
    try {
      const result = await RmaApi.searchCustomers(searchText);
      if (result.success && Array.isArray(result.data)) {
        const options = result.data.map(customer => ({
          value: customer.companyName,
          label: (
            <div>
              <strong>{customer.companyName}</strong>
              <br />
              <span style={{ color: '#666', fontSize: '12px' }}>
                {customer.email} | {customer.contactName}
              </span>
            </div>
          ),
          customer: customer, // Store full customer data
        }));
        setCustomerOptions(options);
      }
    } catch (error) {
      console.error("Customer search failed:", error);
    } finally {
      setSearchingCustomers(false);
    }
  };

  // Auto-fill form when customer is selected from dropdown
  const handleCustomerSelect = (value, option) => {
    if (option && option.customer) {
      const customer = option.customer;
      form.setFieldsValue({
        companyName: customer.companyName,
        email: customer.email,
        contactName: customer.contactName,
        telephone: customer.telephone,
        mobile: customer.mobile,
        returnAddress: customer.address,
      });
      message.success(`Customer details loaded: ${customer.companyName}`);
    }
  };

  // Product selection handler - auto-fill model
  const handleProductSelect = (value, index) => {
    const product = productCatalog.find(p => p.name === value);
    if (product && (product.model || product.partNo)) {
      const items = form.getFieldValue("items") || [];
      items[index] = { ...items[index], product: value, partNo: product.model || product.partNo || "" };
      form.setFieldsValue({ items });
    }
  };

  // Step validation
  const validateStep = async (step) => {
    try {
      if (step === 0) {
        await form.validateFields(["companyName", "email", "contactName", "telephone", "mobile", "returnAddress"]);
      } else if (step === 1) {
        await form.validateFields(["modeOfTransport", "shippingMethod"]);
      } else if (step === 2) {
        const items = form.getFieldValue("items") || [];
        if (items.length === 0) {
          message.error("Please add at least one item");
          return false;
        }
        await form.validateFields();
      }
      return true;
    } catch {
      return false;
    }
  };

  const nextStep = async () => {
    const isValid = await validateStep(currentStep);
    if (isValid) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    setCurrentStep(currentStep - 1);
  };

  // Handle form submission
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setPreviewData(values);
      setRepairType(null); // Reset repair type
      setConfrimVisible(true);
    } catch (error) {
      console.error("Validation failed:", error);
      message.error("Please fill all required fields");
    }
  };

  const finalSubmit = async () => {
    if (!repairType) {
      message.warning("Please select Local Repair or Depot Repair");
      return;
    }
    setFinalSubmitting(true);
    setLoading(true);

    try {
      const values = await form.validateFields();

      const payload = {
        dplLicense: values.dplLicense || "",
        date: new Date().toISOString().split('T')[0],
        modeOfTransport: values.modeOfTransport,
        shippingMethod: values.shippingMethod,
        courierCompanyName: values.courierCompanyName || "",
        companyName: values.companyName,
        email: values.email,
        contactName: values.contactName,
        telephone: values.telephone,
        mobile: values.mobile,
        returnAddress: values.returnAddress,
        invoiceCompanyName: values.invoiceCompanyName || "",
        invoiceEmail: values.invoiceEmail || "",
        invoiceContactName: values.invoiceContactName || "",
        invoiceTelephone: values.invoiceTelephone || "",
        invoiceMobile: values.invoiceMobile || "",
        invoiceAddress: values.invoiceAddress || "",
        signature: values.signature || values.contactName,
        repairType: repairType,

        items: (values.items || []).map(item => ({
          product: item.product,
          model: item.partNo || "",
          serialNo: item.serialNo,
          faultDescription: item.faultDescription,
          codeplug: item.codeplugProgramming || "Default",
          flashCode: "",
          repairStatus: item.status || "",
          invoiceNo: item.invoiceNo || "",
          dateCode: "",
          fmUlatex: item.fmUlAtex || "N",
          encryption: item.encryption || "None",
          firmwareVersion: item.firmwareVersion || "Latest",
          lowerFirmwareVersion: item.lowerFirmwareVersion || "Follow Depot Mainboard Inventory Version",
          remarks: item.remarks || ""
        }))
      };

      const result = await RmaApi.createRmaRequest(payload);

      if (result.success) {
        const rmaItems = result.data?.items || [];
        const itemsWithRma = payload.items.map((item, index) => ({
          ...item,
          rmaNo: rmaItems[index]?.rmaNo || ""
        }));
        setSubmittedFormData({ formData: payload, items: itemsWithRma });
        setRmaNumbers(rmaItems);
        setRmaRequestNumber(result.data?.rmaNo || ""); // Store the RMA request number

        // Save company info for reuse in next request
        const companyInfo = {
          companyName: payload.companyName,
          email: payload.email,
          contactName: payload.contactName,
          telephone: payload.telephone,
          mobile: payload.mobile,
          returnAddress: payload.returnAddress,
          invoiceCompanyName: payload.invoiceCompanyName,
          invoiceEmail: payload.invoiceEmail,
          invoiceContactName: payload.invoiceContactName,
          invoiceTelephone: payload.invoiceTelephone,
          invoiceMobile: payload.invoiceMobile,
          invoiceAddress: payload.invoiceAddress,
        };
        localStorage.setItem("rmaLastCompanyInfo", JSON.stringify(companyInfo));

        setSubmitted(true);
        form.resetFields();
        localStorage.removeItem("rmaFormData");
        setConfrimVisible(false); // Close modal
      } else {
        message.error(result.error || "Failed to submit RMA request");
      }
    } catch (error) {
      console.error("Form submission failed:", error);
      message.error("Please fill all required fields");
    } finally {
      setLoading(false);
      setFinalSubmitting(false);
    }
  };


  // Steps configuration
  const steps = [
    { title: "Contact Info", icon: <UserOutlined /> },
    { title: "Shipment", icon: <CarOutlined /> },
    { title: "Items", icon: <InboxOutlined /> },
    { title: "Review", icon: <FileSearchOutlined /> },
  ];

  // Render success screen
  if (submitted) {
    return (
      <RmaLayout>
        <div className="rma-wizard-container">
          <Card className="success-card">
            <Result
              status="success"
              title="RMA Request Submitted Successfully!"
              subTitle="Your items have been registered for repair. Please note down the RMA numbers below."
              extra={[
                <Button
                  type="primary"
                  key="export"
                  style={{ backgroundColor: '#28a745', borderColor: '#28a745' }}
                  onClick={() => {
                    if (submittedFormData) {
                      exportRmaToExcel(submittedFormData.formData, submittedFormData.items);
                    }
                  }}
                >
                  📥 Export to Excel
                </Button>,
                <Button key="new" onClick={() => {
                  // Load saved company info for reuse
                  const savedCompanyInfo = localStorage.getItem("rmaLastCompanyInfo");
                  setSubmitted(false);
                  setCurrentStep(0);
                  setSubmittedFormData(null);
                  // Pre-fill company info after a brief delay to ensure form is ready
                  if (savedCompanyInfo) {
                    setTimeout(() => {
                      try {
                        const companyInfo = JSON.parse(savedCompanyInfo);
                        form.setFieldsValue(companyInfo);
                      } catch (e) {
                        console.error("Failed to load saved company info:", e);
                      }
                    }, 100);
                  }
                }}>
                  Create New Request
                </Button>,
                <Button key="dashboard" onClick={() => navigate("/rma-dashboard")}>
                  Go to Dashboard
                </Button>,
              ]}
            />

            {/* Display RMA Request Number prominently */}
            {rmaRequestNumber && (
              <div className="rma-request-number-display" style={{ textAlign: 'center', marginBottom: 24 }}>
                <Title level={5} style={{ marginBottom: 8 }}>RMA Request Number:</Title>
                <Tag color="blue" style={{ fontSize: 24, padding: "12px 24px", fontWeight: 'bold' }}>
                  {rmaRequestNumber}
                </Tag>
                <Paragraph type="secondary" style={{ marginTop: 8 }}>
                  Please save this number for tracking your request.
                </Paragraph>
              </div>
            )}

            {rmaNumbers.length > 0 && (
              <div className="rma-numbers-list">
                <Title level={5}>Items Submitted:</Title>
                {rmaNumbers.map((item, idx) => (
                  <Card key={idx} size="small" className="rma-number-card">
                    <Row justify="space-between" align="middle">
                      <Col>
                        <Text strong>{item.product}</Text>
                        <br />
                        <Text type="secondary">Serial: {item.serialNo}</Text>
                      </Col>
                      <Col>
                        <Tag color="green" style={{ fontSize: 16, padding: "4px 12px" }}>
                          {item.rmaNo}
                        </Tag>
                      </Col>
                    </Row>
                  </Card>
                ))}
              </div>
            )}
          </Card>
        </div>
      </RmaLayout>
    );
  }

  return (
    <RmaLayout>
      <div className="rma-wizard-container">
        {/* Progress Steps */}
        <Card className="steps-card">
          <Steps current={currentStep} className="wizard-steps">
            {steps.map((step, index) => (
              <Steps.Step
                key={index}
                title={step.title}
                icon={<span className="step-icon">{step.icon}</span>}
              />
            ))}
          </Steps>
        </Card>

        {/* Form Content */}
        <Card className="form-card">
          <Form
            form={form}
            layout="vertical"
            colon={false}
            onValuesChange={handleFormChange}
          >
            {/* Step 1: Contact Information */}
            <div className={`step-content ${currentStep === 0 ? 'active' : 'hidden'}`}>
              <Title level={4} className="step-title">
                <UserOutlined className="step-icon-inline" /> Contact & Return Address
              </Title>
              <Text type="secondary" className="step-description">
                Enter company and contact details for this RMA request
              </Text>

              <Row gutter={[24, 0]} style={{ marginTop: 24 }}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Company Name"
                    name="companyName"
                    rules={[{ required: true, message: "Company name is required" }]}
                  >
                    <AutoComplete
                      placeholder="Type to search saved customers..."
                      size="large"
                      options={customerOptions}
                      onSearch={handleCustomerSearch}
                      onSelect={handleCustomerSelect}
                      notFoundContent={searchingCustomers ? "Searching..." : null}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Email Address"
                    name="email"
                    rules={[
                      { required: true, message: "Email is required" },
                      { type: "email", message: "Enter a valid email" },
                    ]}
                  >
                    <Input placeholder="Enter email address" size="large" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={[24, 0]}>
                <Col xs={24} md={8}>
                  <Form.Item
                    label="Contact Name"
                    name="contactName"
                    rules={[{ required: true, message: "Contact name is required" }]}
                  >
                    <Input placeholder="Enter contact name" size="large" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    label="Telephone"
                    name="telephone"
                    rules={[{ required: true, message: "Telephone is required" }]}
                  >
                    <Input placeholder="Enter phone number" size="large" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    label="Mobile"
                    name="mobile"
                    rules={[{ required: true, message: "Mobile is required" }]}
                  >
                    <Input placeholder="Enter mobile number" size="large" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                label="Return Address"
                name="returnAddress"
                rules={[{ required: true, message: "Return address is required" }]}
              >
                <TextArea rows={3} placeholder="Enter full return address" size="large" />
              </Form.Item>

              <Divider />

              <div className="invoice-section">
                <div className="invoice-header">
                  <Title level={5} style={{ margin: 0 }}>Invoice Address</Title>
                  <Button
                    type="link"
                    icon={<CopyOutlined />}
                    onClick={copyToInvoice}
                  >
                    Same as Return Address
                  </Button>
                </div>

                <Divider orientation="left" style={{ margin: '8px 0' }}>Invoice Details (optional)</Divider>
                <Row gutter={[24, 0]}>
                  <Col xs={24} md={12}>
                    <Form.Item label="Company Name" name="invoiceCompanyName">
                      <Input placeholder="Invoice company name" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={12}>
                    <Form.Item label="Email" name="invoiceEmail">
                      <Input placeholder="Invoice email" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={[24, 0]}>
                  <Col xs={24} md={8}>
                    <Form.Item label="Contact Name" name="invoiceContactName">
                      <Input placeholder="Contact name" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <Form.Item label="Telephone" name="invoiceTelephone">
                      <Input placeholder="Telephone" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <Form.Item label="Mobile" name="invoiceMobile">
                      <Input placeholder="Mobile" />
                    </Form.Item>
                  </Col>
                </Row>
                <Form.Item label="Invoice Address" name="invoiceAddress">
                  <TextArea rows={2} placeholder="Invoice address" />
                </Form.Item>
              </div>
            </div>

            {/* Step 2: Shipment Details */}
            <div className={`step-content ${currentStep === 1 ? 'active' : 'hidden'}`}>
              <Title level={4} className="step-title">
                <CarOutlined className="step-icon-inline" /> Shipment Details
              </Title>
              <Text type="secondary" className="step-description">
                Select how items will be shipped
              </Text>

              <Row gutter={[24, 0]} style={{ marginTop: 24 }}>
                <Col xs={24} md={12}>
                  <Form.Item label="DPL License (optional)" name="dplLicense">
                    <Input placeholder="Enter DPL License if applicable" size="large" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Mode of Transport"
                    name="modeOfTransport"
                    rules={[{ required: true, message: "Required" }]}
                  >
                    <Select placeholder="Select transport mode" size="large">
                      <Option value="Air">Air</Option>
                      <Option value="Road">Road</Option>
                      <Option value="Courier">Courier</Option>
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={[24, 0]}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="Shipping Method"
                    name="shippingMethod"
                    rules={[{ required: true, message: "Required" }]}
                  >
                    <Select placeholder="Select shipping method" size="large">
                      <Option value="Motorola Courier Service">Motorola Courier Service</Option>
                      <Option value="Other Courier Service">Other Courier Service</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    noStyle
                    shouldUpdate={(prev, curr) => prev.shippingMethod !== curr.shippingMethod}
                  >
                    {({ getFieldValue }) => (
                      <Form.Item
                        label="Courier Company Name"
                        name="courierCompanyName"
                        rules={[{
                          required: getFieldValue("shippingMethod") === "Other Courier Service",
                          message: "Required when using other courier",
                        }]}
                      >
                        <Input
                          placeholder="Enter courier company name"
                          size="large"
                          disabled={getFieldValue("shippingMethod") !== "Other Courier Service"}
                        />
                      </Form.Item>
                    )}
                  </Form.Item>
                </Col>
              </Row>
            </div>

            {/* Step 3: Items */}
            <div className={`step-content ${currentStep === 2 ? 'active' : 'hidden'}`}>
              <Title level={4} className="step-title">
                <InboxOutlined className="step-icon-inline" /> Items for Repair
              </Title>
              <Text type="secondary" className="step-description">
                Add products that need to be repaired
              </Text>

              <Form.List name="items" initialValue={[{}]}>
                {(fields, { add, remove }) => (
                  <>
                    {fields.map(({ key, name, ...restField }, index) => (
                      <Card
                        key={key}
                        className="item-card"
                        title={
                          <div className="item-card-header">
                            <Tag color="blue">Item {index + 1}</Tag>
                            {fields.length > 1 && (
                              <Button
                                type="text"
                                danger
                                icon={<MinusCircleOutlined />}
                                onClick={() => remove(name)}
                              >
                                Remove
                              </Button>
                            )}
                          </div>
                        }
                      >
                        <Row gutter={[16, 0]}>
                          <Col xs={24} md={8}>
                            <Form.Item
                              {...restField}
                              label="Product"
                              name={[name, "product"]}
                              rules={[{ required: true, message: "Product is required" }]}
                            >
                              <AutoComplete
                                placeholder={loadingProducts ? "Loading products..." : "Enter or Select product"}
                                size="large"
                                options={productCatalog.map(p => ({
                                  value: p.name,
                                  label: `${p.name}${p.model ? ` - ${p.model}` : ""}`
                                }))}
                                filterOption={(inputValue, option) =>
                                  option.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1
                                }
                                onSelect={(val) => handleProductSelect(val, index)}
                              />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item
                              {...restField}
                              label="Model / Part No."
                              name={[name, "partNo"]}
                              rules={[{ required: true, message: "Required" }]}
                            >
                              <Input placeholder="Model number" size="large" />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item
                              {...restField}
                              label="Serial Number"
                              name={[name, "serialNo"]}
                              rules={[{ required: true, message: "Serial number is required" }]}
                            >
                              <Input placeholder="Enter serial number" size="large" />
                            </Form.Item>
                          </Col>
                        </Row>

                        <Form.Item
                          {...restField}
                          label="Fault Description"
                          name={[name, "faultDescription"]}
                          rules={[{ required: true, message: "Please describe the fault" }]}
                        >
                          <TextArea
                            rows={2}
                            placeholder="Describe the issue or fault clearly"
                            size="large"
                          />
                        </Form.Item>

                        <Row gutter={[16, 0]}>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Codeplug" name={[name, "codeplugProgramming"]}>
                              <Select placeholder="Select" defaultValue="Default">
                                <Option value="Default">Default</Option>
                                <Option value="Customer Codeplug">Customer Codeplug</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Status" name={[name, "status"]}>
                              <Select placeholder="Select warranty status">
                                <Option value="WARR">Warranty</Option>
                                <Option value="OOW">Out of Warranty</Option>
                                <Option value="AMC">AMC</Option>
                                <Option value="SFS">SFS</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="FM/UL/ATEX" name={[name, "fmUlAtex"]}>
                              <Select placeholder="Select" defaultValue="N">
                                <Option value="N">Non FM/UL/ATEX</Option>
                                <Option value="Y-FM">FM Certified</Option>
                                <Option value="Y-UL">UL Certified</Option>
                                <Option value="Y-ATEX">ATEX Certified</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                        </Row>
                        <Row gutter={[16, 0]}>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Encryption" name={[name, "encryption"]}>
                              <Select placeholder="Select" defaultValue="None">
                                <Option value="TETRA">Tetra</Option>
                                <Option value="ASTRO">Astro</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Firmware Version" name={[name, "firmwareVersion"]}>
                              <Select placeholder="Select">
                                <Option value="TETRA">Tetra</Option>
                                <Option value="ASTRO">Astro</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Invoice No." name={[name, "invoiceNo"]}>
                              <Input placeholder="For accessories" />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item {...restField} label="Lower Firmaware Version" name={[name, "lowerFirmwareVersion"]}>
                              <Select placeholder="Select">
                                <Option value="Follow Depot Mainboard Inventory Version">Follow Depot Mainboard Inventory Version</Option>
                                <Option value="Return Unrepaired">Return Unrepaired</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col xs={12} md={8}>
                            <Form.Item {...restField} label="Partial Shipment" name={[name, "partialshipment"]}>
                              <Select placeholder="Select">
                                <Option value="Y">Yes</Option>
                                <Option value="N">No</Option>
                              </Select>
                            </Form.Item>
                          </Col>
                        </Row>
                        <Form.Item {...restField} label="Remarks" name={[name, "remarks"]}>
                          <TextArea rows={2} placeholder="Additional remarks" />
                        </Form.Item>
                      </Card>
                    ))}

                    <Button
                      type="dashed"
                      onClick={() => add()}
                      block
                      icon={<PlusOutlined />}
                      className="add-item-btn"
                    >
                      Add Another Item
                    </Button>
                  </>
                )}
              </Form.List>
            </div>

            {/* Step 4: Review */}
            <div className={`step-content ${currentStep === 3 ? 'active' : 'hidden'}`}>
              <Title level={4} className="step-title">
                <FileSearchOutlined className="step-icon-inline" /> Review & Submit
              </Title>
              <Text type="secondary" className="step-description">
                Please review your request before submitting
              </Text>

              <div className="review-section">
                <Card size="small" title="Contact Information" className="review-card">
                  <Row gutter={16}>
                    <Col span={12}>
                      <Text type="secondary">Company:</Text>
                      <br />
                      <Text strong>{form.getFieldValue("companyName")}</Text>
                    </Col>
                    <Col span={12}>
                      <Text type="secondary">Email:</Text>
                      <br />
                      <Text strong>{form.getFieldValue("email")}</Text>
                    </Col>
                  </Row>
                  <Row gutter={16} style={{ marginTop: 12 }}>
                    <Col span={12}>
                      <Text type="secondary">Contact:</Text>
                      <br />
                      <Text strong>{form.getFieldValue("contactName")}</Text>
                    </Col>
                    <Col span={12}>
                      <Text type="secondary">Phone:</Text>
                      <br />
                      <Text>{form.getFieldValue("telephone")} / {form.getFieldValue("mobile")}</Text>
                    </Col>
                  </Row>
                </Card>

                <Card size="small" title="Shipment" className="review-card">
                  <Row gutter={16}>
                    <Col span={12}>
                      <Text type="secondary">Transport:</Text>
                      <br />
                      <Text strong>{form.getFieldValue("modeOfTransport")}</Text>
                    </Col>
                    <Col span={12}>
                      <Text type="secondary">Method:</Text>
                      <br />
                      <Text strong>{form.getFieldValue("shippingMethod")}</Text>
                    </Col>
                  </Row>
                </Card>

                <Card size="small" title={`Items (${form.getFieldValue("items")?.length || 0})`} className="review-card">
                  {(form.getFieldValue("items") || []).map((item, idx) => (
                    <div key={idx} className="review-item">
                      <Row justify="space-between" align="middle">
                        <Col>
                          <Tag color="blue">{idx + 1}</Tag>
                          <Text strong>{item?.product}</Text>
                          <Text type="secondary"> ({item?.serialNo})</Text>
                        </Col>
                      </Row>
                      <Paragraph type="secondary" style={{ margin: "4px 0 0 28px", fontSize: 12 }}>
                        {item?.faultDescription}
                      </Paragraph>
                    </div>
                  ))}
                </Card>

                {/* <Form.Item
                  label="Authorized Signature (Print Name)"
                  name="signature"
                  style={{ marginTop: 24 }}
                >
                  <Input
                    placeholder="Enter your name"
                    size="large"
                    defaultValue={form.getFieldValue("contactName")}
                  />

                </Form.Item> */}
              </div>
            </div>
          </Form>

          {/* Navigation Buttons */}
          <div className="wizard-navigation">
            <Button onClick={handleReset}>Reset</Button>

            <Space>
              {currentStep > 0 && (
                <Button icon={<ArrowLeftOutlined />} onClick={prevStep}>
                  Previous
                </Button>
              )}
              {currentStep < 3 ? (
                <Button type="primary" onClick={nextStep}>
                  Next <ArrowRightOutlined />
                </Button>
              ) : (
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  loading={loading}
                  onClick={handleSubmit}
                  className="submit-btn"
                >
                  Submit RMA Request
                </Button>
              )}
            </Space>
          </div>
        </Card>

        <Modal
          title="Confirm RMA Submission"
          open={conformVisible}
          onCancel={() => setConfrimVisible(false)}
          footer={[
            <Button key="back" onClick={() => setConfrimVisible(false)}>
              Cancel
            </Button>,
            <Button
              key="submit"
              type="primary"
              loading={finalSubmitting}
              onClick={finalSubmit}
            >
              Confirm Submit
            </Button>,
          ]}
          width={700}
        >
          <div style={{ padding: "10px 0" }}>
            <Title level={5}>Select Repair Type</Title>
            <Radio.Group
              onChange={(e) => setRepairType(e.target.value)}
              value={repairType}
              style={{ marginBottom: 20 }}
            >
              <Radio value="LOCAL">Local Repair</Radio>
              <Radio value="DEPOT">Depot Repair</Radio>
            </Radio.Group>

            {previewData && (
              <div style={{ background: "#f5f5f5", padding: 15, borderRadius: 8 }}>
                <Title level={5}>Request Preview</Title>
                <Row gutter={[16, 8]}>
                  <Col span={12}>
                    <Text type="secondary">Company:</Text> <Text strong>{previewData.companyName}</Text>
                  </Col>
                  <Col span={12}>
                    <Text type="secondary">Contact:</Text> <Text strong>{previewData.contactName}</Text>
                  </Col>
                  <Col span={12}>
                    <Text type="secondary">Transport:</Text> <Text strong>{previewData.modeOfTransport}</Text>
                  </Col>
                  <Col span={12}>
                    <Text type="secondary">Total Items:</Text> <Text strong>{previewData.items?.length || 0}</Text>
                  </Col>
                </Row>

                <Divider style={{ margin: "12px 0" }} />

                <div style={{ maxHeight: 200, overflowY: "auto" }}>
                  <Text strong>Items:</Text>
                  <ul style={{ paddingLeft: 20, margin: "5px 0" }}>
                    {previewData.items?.map((item, idx) => (
                      <li key={idx}>
                        <Text>{item.product} ({item.serialNo})</Text>
                        <br />
                        <Text type="secondary" style={{ fontSize: 12 }}>{item.faultDescription}</Text>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </Modal>
      </div>
    </RmaLayout>
  );
}
export default RmaRequestForm;