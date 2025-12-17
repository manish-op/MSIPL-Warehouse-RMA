import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Spin,
    Card,
    Row,
    Col,
    Badge,
    Empty,
    Divider,
    Button,
    Modal,
    Table,
    Form,
    Input,
    Select,
    DatePicker,
    Space,
} from "antd";
import {
    CheckCircleOutlined,
    TrophyOutlined,
    ReloadOutlined,
    EyeOutlined,
    DownloadOutlined,
    FilePdfOutlined,
    SendOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

const PREDEFINED_TRANSPORTERS = {
    "BlueDart": "BD001",
    "DTDC": "DT001",
    "Professional Couriers": "PC001",
    "Trackon": "TR001",
    "Delhivery": "DL001"
};

export default function RepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [generatingGatepass, setGeneratingGatepass] = useState(null);
    const [previewVisible, setPreviewVisible] = useState(false);
    const [previewData, setPreviewData] = useState({ rmaNo: "", items: [] });
    // DC Modal State
    const [dcModalVisible, setDcModalVisible] = useState(false);
    const [dcSubmitting, setDcSubmitting] = useState(false);
    const [dcTableData, setDcTableData] = useState([]);
    const [transporters, setTransporters] = useState([]);
    const [isNewTransporter, setIsNewTransporter] = useState(false);
    const [selectedRmaForDc, setSelectedRmaForDc] = useState(null);
    const [dcForm] = Form.useForm();
    // Dispatch Modal State
    const [dispatchModalVisible, setDispatchModalVisible] = useState(false);
    const [dispatchSubmitting, setDispatchSubmitting] = useState(false);
    const [selectedRmaNo, setSelectedRmaNo] = useState(null);
    const [selectedRmaItems, setSelectedRmaItems] = useState([]);
    const [dispatchForm] = Form.useForm();

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getRepairedItems();
        if (result.success) {
            // Filter out Depot items, show everything else (Local, legacy nulls, etc.)
            const localItems = (result.data || []).filter(item => item.repairType !== 'DEPOT');
            setItems(localItems);
        } else {
            message.error("Failed to load repaired items");
        }
        setLoading(false);
    };

    const fetchTransporters = async () => {
        const result = await RmaApi.getAllTransporters();
        if (result.success) {
            setTransporters(result.data);
        }
    };

    useEffect(() => {
        loadItems();
        fetchTransporters();
    }, []);

    // Open DC Modal
    const openDcModal = (rmaNo, rmaItems) => {
        // Validation: Ensure all items have an RMA Number assigned
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before generating Delivery Challan");
            return;
        }
        setSelectedRmaForDc(rmaNo);
        setDcTableData(rmaItems.map((item, index) => ({ ...item, slNo: index + 1, qty: 1 })));

        dcForm.resetFields();
        // Pre-fill Customer Details
        const customerDetails = rmaItems[0]?.rmaRequest;
        dcForm.setFieldsValue({
            modeOfShipment: "ROAD",
            boxes: "1",
            consigneeName: customerDetails?.companyName || "",
            consigneeAddress: customerDetails?.returnAddress || "",
        });
        setDcModalVisible(true);
    };

    // Handle Generate DC
    const handleGenerateDC = async (values) => {
        try {
            setDcSubmitting(true);
            const {
                consigneeName, consigneeAddress, gstIn,
                boxes, dimensions, weight, modeOfShipment,
                transporterName, transporterId,
                items: formItems // captured from table inputs
            } = values;

            const formattedItems = dcTableData.map((item, index) => ({
                slNo: index + 1,
                product: item.product,
                model: item.model,
                serialNo: item.serialNo,
                rate: (formItems?.[index]?.rate || 0).toString(),
                itemId: item.id
            }));

            const payload = {
                rmaNo: selectedRmaForDc,
                consigneeName,
                consigneeAddress,
                gstIn,
                boxes: parseInt(boxes),
                dimensions,
                weight,
                modeOfShipment,
                transporterName: Array.isArray(transporterName) ? transporterName[transporterName.length - 1] : transporterName,
                transporterId,
                items: formattedItems
            };

            const result = await RmaApi.generateDeliveryChallan(payload);
            if (result.success) {
                message.success("Delivery Challan Generated Successfully");
                setDcModalVisible(false);
                fetchTransporters();
            } else {
                message.error("Failed to Generate DC");
            }

        } catch (error) {
            console.error("DC Gen Error:", error);
            message.error("An error occurred");
        } finally {
            setDcSubmitting(false);
        }
    };

    // Open Preview Modal
    const openPreview = (rmaItems, rmaNo) => {
        setPreviewData({ rmaNo, items: rmaItems });
        setPreviewVisible(true);
    };

    // Open Dispatch Modal
    const openDispatchModal = (rmaNo, rmaItems) => {
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before dispatching");
            return;
        }
        setSelectedRmaNo(rmaNo);
        setSelectedRmaItems(rmaItems);
        dispatchForm.resetFields();
        const customerDetails = rmaItems[0]?.rmaRequest;
        dispatchForm.setFieldsValue({
            dispatchDate: null,
            courierName: "",
            trackingNo: "",
            dcNo: "",
            ewayBillNo: "",
            remarks: `Dispatch to Customer: ${customerDetails?.companyName || 'N/A'}`,
        });
        setDispatchModalVisible(true);
    };

    // Handle Confirm Dispatch to Customer
    const handleConfirmDispatch = async () => {
        try {
            const values = await dispatchForm.validateFields();
            setDispatchSubmitting(true);

            // Update each item status to DISPATCHED
            let successCount = 0;
            for (const item of selectedRmaItems) {
                const result = await RmaApi.updateItemStatus(
                    item.id,
                    'DISPATCHED',
                    values.remarks || 'Dispatched to Customer',
                    true
                );
                if (result.success) successCount++;
            }

            if (successCount === selectedRmaItems.length) {
                message.success(`${successCount} item(s) dispatched to customer successfully!`);
                setDispatchModalVisible(false);
                loadItems();
            } else {
                message.warning(`${successCount} of ${selectedRmaItems.length} items dispatched.`);
            }
        } catch (error) {
            console.error("Dispatch error:", error);
            message.error("Failed to dispatch items");
        } finally {
            setDispatchSubmitting(false);
        }
    };

    // Confirm and Generate Outward Gatepass PDF
    const confirmGenerateGatepass = async () => {
        const { rmaNo } = previewData;
        if (!rmaNo || rmaNo === "Unknown") {
            message.error("Cannot generate Gatepass: Invalid RMA Number.");
            return;
        }
        setGeneratingGatepass(rmaNo);
        const result = await RmaApi.generateOutwardGatepass(rmaNo);
        if (result.success && result.blob) {
            // Create download link
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `OutwardGatepass_${rmaNo}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('Outward Gatepass generated and downloaded!');
            setPreviewVisible(false); // Close modal on success
        } else {
            message.error(result.error || 'Failed to generate gatepass');
        }
        setGeneratingGatepass(null);
    };

    // Group items by RMA number
    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) {
            acc[rmaNo] = [];
        }
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRmaRequests = Object.keys(groupedItems).length;
    const totalItems = items.length;

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header Section - Green Theme for Repaired */}
                <div className="unrepaired-header" style={{ background: "linear-gradient(135deg, #52c41a 0%, #389e0d 100%)", boxShadow: "0 4px 20px rgba(82, 196, 26, 0.3)" }}>
                    <div className="header-content">
                        <div className="header-title">
                            <CheckCircleOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Local Repaired Items
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items repaired locally and ready for dispatch
                                </Text>
                            </div>
                        </div>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={loadItems}
                            loading={loading}
                            className="refresh-btn"
                        >
                            Refresh
                        </Button>
                    </div>

                    {/* Stats Row */}
                    <Row gutter={16} className="stats-row">
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <TrophyOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">Pending Dispatch</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <CheckCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Ready Items</div>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </div>

                {/* Content Section */}
                <div className="unrepaired-content">
                    {loading ? (
                        <div className="loading-container">
                            <Spin size="large" />
                            <Text style={{ marginTop: 16 }}>Loading items...</Text>
                        </div>
                    ) : totalRmaRequests === 0 ? (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="No local repaired items found"
                            className="empty-state"
                        />
                    ) : (
                        <div className="rma-groups">
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                <Card
                                    key={rmaNo}
                                    className="rma-group-card"
                                    style={{ borderLeft: "4px solid #52c41a" }}
                                    title={
                                        <div className="rma-card-header">
                                            <span className="rma-number">
                                                <Tag color="#52c41a" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaNo}
                                                </Tag>
                                            </span>
                                            <Badge
                                                count={rmaItems.length}
                                                style={{ backgroundColor: "#52c41a" }}
                                                overflowCount={99}
                                            />
                                        </div>
                                    }
                                    extra={
                                        <Space>
                                            <Button
                                                className="btn-generate-dc"
                                                icon={<FilePdfOutlined />}
                                                onClick={() => openDcModal(rmaItems[0].rmaRequest?.requestNumber || rmaNo, rmaItems)}
                                            >
                                                Generate DC
                                            </Button>
                                            <Button
                                                type="primary"
                                                icon={<SendOutlined />}
                                                onClick={() => openDispatchModal(rmaItems[0].rmaRequest?.requestNumber || rmaNo, rmaItems)}
                                            >
                                                Dispatch to Customer
                                            </Button>
                                            <Button
                                                onClick={() => openPreview(rmaItems, rmaItems[0].rmaRequest?.requestNumber || rmaNo)}
                                                style={{ backgroundColor: "#135200", borderColor: "#135200", color: "#fff" }}
                                            >
                                                Generate Out Gatepass
                                            </Button>
                                        </Space>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card
                                                    className="item-card"
                                                    size="small"
                                                    hoverable
                                                    style={{ borderTop: "3px solid #52c41a" }}
                                                >
                                                    <div className="item-content">
                                                        <div className="item-row">
                                                            <Text type="secondary">Product</Text>
                                                            <Text strong>{item.product || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Serial No.</Text>
                                                            <Text code>{item.serialNo || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Repaired By</Text>
                                                            <Text>{item.repairedByName || "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Repair Date</Text>
                                                            <Text>{item.repairedDate ? new Date(item.repairedDate).toLocaleDateString() : "N/A"}</Text>
                                                        </div>
                                                        <div className="item-row">
                                                            <Text type="secondary">Status</Text>
                                                            <Tag color="green" icon={<CheckCircleOutlined />}>
                                                                REPAIRED
                                                            </Tag>
                                                        </div>
                                                        {item.itemRmaNo && (
                                                            <div className="item-row">
                                                                <Text type="secondary">RMA Number</Text>
                                                                <Tag color="green">{item.itemRmaNo}</Tag>
                                                            </div>
                                                        )}
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section">
                                                            <Text type="secondary">Repair Remarks</Text>
                                                            <Paragraph
                                                                ellipsis={{ rows: 2, expandable: true }}
                                                                className="fault-text"
                                                            >
                                                                {item.repairRemarks || "No remarks"}
                                                            </Paragraph>
                                                        </div>
                                                    </div>
                                                </Card>
                                            </Col>
                                        ))}
                                    </Row>
                                </Card>
                            ))}
                        </div>
                    )}
                </div>

                {/* Dispatch to Customer Modal */}
                <Modal
                    title={`Dispatch to Customer - ${selectedRmaNo || ""}`}
                    open={dispatchModalVisible}
                    onCancel={() => setDispatchModalVisible(false)}
                    confirmLoading={dispatchSubmitting}
                    onOk={handleConfirmDispatch}
                    okText="Confirm Dispatch"
                    width={700}
                >
                    <Form form={dispatchForm} layout="vertical">
                        <Row gutter={16}>
                            <Col span={12}>
                                <Form.Item
                                    name="dispatchDate"
                                    label="Dispatch Date"
                                    rules={[{ required: true, message: "Required" }]}
                                >
                                    <DatePicker style={{ width: "100%" }} />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item name="courierName" label="Courier Company">
                                    <Input placeholder="e.g., BlueDart, DTDC" />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Row gutter={16}>
                            <Col span={12}>
                                <Form.Item name="trackingNo" label="Tracking / AWB No.">
                                    <Input placeholder="Tracking number" />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item name="dcNo" label="Delivery Challan No.">
                                    <Input placeholder="DC number" />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Row gutter={16}>
                            <Col span={12}>
                                <Form.Item name="ewayBillNo" label="E-Way Bill No.">
                                    <Input placeholder="E-way bill number" />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Form.Item name="remarks" label="Remarks">
                            <Input.TextArea rows={3} placeholder="Any dispatch remarks" />
                        </Form.Item>

                        <Divider />
                        <Space direction="vertical" style={{ width: "100%" }}>
                            <Text type="secondary">
                                You are dispatching <strong>{selectedRmaItems.length}</strong> item(s) to the customer.
                            </Text>
                        </Space>
                    </Form>
                </Modal>

                {/* Delivery Challan Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <FilePdfOutlined style={{ color: "#faad14" }} />
                            <span>Generate Delivery Challan</span>
                        </div>
                    }
                    open={dcModalVisible}
                    onCancel={() => setDcModalVisible(false)}
                    width={1000}
                    className="dc-modal"
                    footer={[
                        <Button key="cancel" onClick={() => setDcModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="generate"
                            type="primary"
                            icon={<FilePdfOutlined />}
                            onClick={() => dcForm.submit()}
                            loading={dcSubmitting}
                            style={{ background: "#faad14", borderColor: "#faad14" }}
                        >
                            Generate DC
                        </Button>
                    ]}
                >
                    <Form
                        form={dcForm}
                        layout="vertical"
                        onFinish={handleGenerateDC}
                        initialValues={{
                            modeOfShipment: "ROAD",
                            boxes: "1"
                        }}
                    >
                        <Row gutter={16}>
                            {/* Consignor (Fixed for now, or display only) */}
                            <Col span={12}>
                                <Card title="Consignor Details" size="small" style={{ background: '#f9f9f9' }}>
                                    <p><strong>Motorola Solutions India</strong></p>
                                    <p>A, Building 8, DLF</p>
                                    <p>Gurgaon, Haryana, India</p>
                                </Card>
                            </Col>
                            {/* Consignee */}
                            <Col span={12}>
                                <Card title="Consignee Details" size="small">
                                    <Form.Item name="consigneeName" label="Name">
                                        <Input />
                                    </Form.Item>
                                    <Form.Item name="consigneeAddress" label="Address">
                                        <Input.TextArea rows={2} />
                                    </Form.Item>
                                    <Form.Item name="gstIn" label="GST IN">
                                        <Input placeholder="Enter GST IN" />
                                    </Form.Item>
                                </Card>
                            </Col>
                        </Row>

                        <Divider orientation="left">Item Details</Divider>

                        <Table
                            dataSource={dcTableData}
                            pagination={false}
                            size="small"
                            columns={[
                                { title: 'Sr No', dataIndex: 'slNo', key: 'slNo', width: 60 },
                                { title: 'Material Code', dataIndex: 'serialNo', key: 'serialNo' },
                                {
                                    title: 'Description',
                                    key: 'product',
                                    render: (_, record) => `${record.product || ''}${record.model ? ' - ' + record.model : ''}`
                                },
                                { title: 'Qty', dataIndex: 'qty', key: 'qty', render: () => 1 }, // Always 1 per item row
                                {
                                    title: 'Rate (Value)',
                                    key: 'rate',
                                    render: (_, record, index) => (
                                        <Form.Item
                                            name={['items', index, 'rate']}
                                            rules={[{ required: true, message: 'Required' }]}
                                            style={{ margin: 0 }}
                                        >
                                            <Input prefix="₹" type="number" placeholder="Value" />
                                        </Form.Item>
                                    )
                                }
                            ]}
                        />

                        <Divider orientation="left">Shipment Details</Divider>
                        <Row gutter={16}>
                            <Col span={6}>
                                <Form.Item name="boxes" label="No of Boxes" rules={[{ required: true }]}>
                                    <Input type="number" />
                                </Form.Item>
                            </Col>
                            <Col span={6}>
                                <Form.Item name="dimensions" label="Dimensions" rules={[{ required: true }]}>
                                    <Input placeholder="e.g. 10x10x10" />
                                </Form.Item>
                            </Col>
                            <Col span={6}>
                                <Form.Item name="weight" label="Weight (kg)" rules={[{ required: true }]}>
                                    <Input placeholder="e.g. 5kg" />
                                </Form.Item>
                            </Col>
                            <Col span={6}>
                                <Form.Item name="modeOfShipment" label="Mode of Shipment">
                                    <Select>
                                        <Select.Option value="ROAD">ROAD</Select.Option>
                                        <Select.Option value="AIR">AIR</Select.Option>
                                        <Select.Option value="HAND_CARRY">HAND CARRY</Select.Option>
                                    </Select>
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={16}>
                            <Col span={12}>
                                <Form.Item name="transporterName" label="Transporter Name">
                                    <Select
                                        showSearch
                                        placeholder="Select or Type New Transporter"
                                        optionFilterProp="children"
                                        onSelect={(value) => {
                                            const t = transporters.find(tr => tr.name === value);
                                            if (t) {
                                                dcForm.setFieldsValue({ transporterId: t.transporterId });
                                                setIsNewTransporter(false);
                                            }
                                        }}
                                        onSearch={(val) => {
                                            const exists = transporters.some(t => t.name.toLowerCase() === val.toLowerCase());
                                            if (!exists) {
                                                dcForm.setFieldsValue({ transporterId: "" });
                                                setIsNewTransporter(true);
                                            }
                                        }}
                                        mode="tags"
                                        maxTagCount={1}
                                    >
                                        {[
                                            ...transporters,
                                            ...Object.keys(PREDEFINED_TRANSPORTERS)
                                                .filter(name => !transporters.some(t => t.name === name))
                                                .map(name => ({ id: `pre-${name}`, name: name }))
                                        ].map(t => (
                                            <Select.Option key={t.id} value={t.name}>{t.name}</Select.Option>
                                        ))}
                                    </Select>
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="transporterId"
                                    label="Transporter ID"
                                    rules={[{ required: true, message: 'Please enter Transporter ID' }]}
                                    help={isNewTransporter ? "Enter ID for new transporter to save it" : ""}
                                >
                                    <Input placeholder="Auto-filled or Enter New ID" />
                                </Form.Item>
                            </Col>
                        </Row>

                    </Form>
                </Modal>

                {/* Outward Gatepass Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <EyeOutlined style={{ color: "#1890ff" }} />
                            <span>Outward Gatepass Preview - {previewData.rmaNo}</span>
                        </div>
                    }
                    open={previewVisible}
                    onCancel={() => setPreviewVisible(false)}
                    width={900}
                    footer={[
                        <Button key="cancel" onClick={() => setPreviewVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="download"
                            type="primary"
                            icon={<DownloadOutlined />}
                            onClick={confirmGenerateGatepass}
                            loading={generatingGatepass === previewData.rmaNo}
                            style={{ backgroundColor: "#135200", borderColor: "#135200" }}
                        >
                            Confirm & Generate PDF
                        </Button>
                    ]}
                    className="gatepass-modal"
                >
                    <div className="gatepass-preview-container">
                        {/* Header Info */}
                        <Card size="small" className="gatepass-info-card" style={{ marginBottom: 16 }}>
                            <Row gutter={16}>
                                <Col span={8}>
                                    <Text type="secondary">RMA Number</Text>
                                    <div>
                                        <Tag color="green" style={{ fontSize: 14 }}>
                                            {previewData.rmaNo}
                                        </Tag>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Consignee (Customer)</Text>
                                    <div>
                                        <Text strong>{previewData.items[0]?.rmaRequest?.companyName || previewData.items[0]?.companyName || 'N/A'}</Text>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Total Items</Text>
                                    <div>
                                        <Badge
                                            count={previewData.items.length}
                                            style={{ backgroundColor: "#52c41a" }}
                                            showZero
                                        />
                                    </div>
                                </Col>
                            </Row>
                        </Card>

                        {/* Items Table */}
                        <Table
                            dataSource={previewData.items.map((item, index) => ({ ...item, key: item.id || index, slNo: index + 1 }))}
                            columns={[
                                {
                                    title: 'Sl.No',
                                    dataIndex: 'slNo',
                                    key: 'slNo',
                                    width: 80,
                                    align: 'center',
                                },
                                {
                                    title: 'Product',
                                    dataIndex: 'product',
                                    key: 'product',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Serial No',
                                    dataIndex: 'serialNo',
                                    key: 'serialNo',
                                    render: (text) => <Text code>{text || 'N/A'}</Text>,
                                },
                                {
                                    title: 'Model',
                                    dataIndex: 'model',
                                    key: 'model',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Repair Remarks',
                                    dataIndex: 'repairRemarks',
                                    key: 'repairRemarks',
                                    ellipsis: true,
                                    render: (text) => text || 'N/A',
                                },
                            ]}
                            pagination={false}
                            size="small"
                            bordered
                        />
                    </div>
                </Modal>
            </div>
        </RmaLayout>
    );
}