import React, { useState } from 'react';
import { Form, Input, Button, DatePicker, Card, Row, Col, notification, Divider, InputNumber } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { CustomerSlaApi } from '../API/CustomerSla/CustomerSlaApi';
import RmaLayout from "../RMA/RmaLayout";

const AddCustomerSLA = () => {
    const [form] = Form.useForm();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const onFinish = async (values) => {
        setLoading(true);
        try {
            // Structure the data to match ProjectCore entity
            const payload = {
                systemId: values.systemId,
                projectName: values.projectName,
                region: values.region,
                category: values.category,
                partnerName: values.partnerName,
                customerNumber: values.customerNumber,
                sfdcNumber: values.sfdcNumber,
                poValue: values.poValue,
                contractStartDate: values.contractStartDate ? values.contractStartDate.format('YYYY-MM-DD') : null,
                contractEndDate: values.contractEndDate ? values.contractEndDate.format('YYYY-MM-DD') : null,
                amcStatus: values.amcStatus,
                serviceSlaDetails: {
                    systemVersion: values.systemVersion,
                    frequencyBand: values.frequencyBand,
                    encryptionType: values.encryptionType,
                    spocContact: values.spocContact,
                    invoiceBillingCycle: values.invoiceBillingCycle,
                    slaScope: values.slaScope,
                    repairTat: values.repairTat,
                    pmiSchedule: values.pmiSchedule,
                    serviceReportFrequency: values.serviceReportFrequency,
                    dlpWarrantyStartDate: values.dlpWarrantyStartDate ? values.dlpWarrantyStartDate.format('YYYY-MM-DD') : null,
                    dlpWarrantyEndDate: values.dlpWarrantyEndDate ? values.dlpWarrantyEndDate.format('YYYY-MM-DD') : null,
                }
            };

            const response = await CustomerSlaApi.createCustomerSla(payload);

            if (response) {
                notification.success({
                    message: 'Success',
                    description: 'Customer SLA created successfully',
                });
                form.resetFields();
                navigate('/customer-sla/manage');
            }
        } catch (error) {
            console.error(error);
            // Error handling is mostly done in CustomerSlaApi
            notification.error({
                message: 'Error',
                description: 'Failed to create Customer SLA',
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <RmaLayout sidebarType="customerSla">
            <div style={{ padding: '24px' }}>
            <div style={{ marginBottom: '16px' }}>
                <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
                    Back
                </Button>
            </div>
            <Card title="Add Customer SLA" bordered={false}>
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={onFinish}
                >
                    <Divider orientation="left">Project Core Details</Divider>
                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item name="systemId" label="System ID" rules={[{ required: true, message: 'Please enter System ID' }]}>
                                <Input placeholder="Enter System ID" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="projectName" label="Project Name">
                                <Input placeholder="Enter Project Name" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="region" label="Region">
                                <Input placeholder="Enter Region" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="category" label="Category">
                                <Input placeholder="Enter Category" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="partnerName" label="Partner Name">
                                <Input placeholder="Enter Partner Name" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="customerNumber" label="Customer Number">
                                <Input placeholder="Enter Customer Number" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="sfdcNumber" label="SFDC Number">
                                <Input placeholder="Enter SFDC Number" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="poValue" label="PO Value">
                                <InputNumber style={{ width: '100%' }} placeholder="Enter PO Value" precision={2} />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="amcStatus" label="AMC Status">
                                <Input placeholder="Enter AMC Status" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="contractStartDate" label="Contract Start Date">
                                <DatePicker style={{ width: '100%' }} />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="contractEndDate" label="Contract End Date">
                                <DatePicker style={{ width: '100%' }} />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Divider orientation="left">Service SLA Details</Divider>
                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item name="systemVersion" label="System Version">
                                <Input placeholder="Enter System Version" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="frequencyBand" label="Frequency Band">
                                <Input placeholder="Enter Frequency Band" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="encryptionType" label="Encryption Type">
                                <Input placeholder="Enter Encryption Type" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="spocContact" label="SPOC Contact">
                                <Input placeholder="Enter SPOC Contact" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="invoiceBillingCycle" label="Invoice Billing Cycle">
                                <Input placeholder="Enter Invoice Billing Cycle" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="slaScope" label="SLA Scope">
                                <Input placeholder="Enter SLA Scope" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="repairTat" label="Repair TAT">
                                <Input placeholder="Enter Repair TAT" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="pmiSchedule" label="PMI Schedule">
                                <Input placeholder="Enter PMI Schedule" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="serviceReportFrequency" label="Service Report Frequency">
                                <Input placeholder="Enter Service Report Frequency" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="dlpWarrantyStartDate" label="DLP/Warranty Start Date">
                                <DatePicker style={{ width: '100%' }} />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="dlpWarrantyEndDate" label="DLP/Warranty End Date">
                                <DatePicker style={{ width: '100%' }} />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row>
                        <Col span={24} style={{ textAlign: 'right' }}>
                            <Button type="primary" htmlType="submit" loading={loading}>
                                Submit
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card>
        </div>
        </RmaLayout>
    );
};

export default AddCustomerSLA;
