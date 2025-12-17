import React, { useEffect, useState } from "react";
import {
    Typography,
    Card,
    Row,
    Col,
    Tag,
    Badge,
    Button,
    Spin,
    Empty,
    message,
    Modal,
    Form,
    Input,
    DatePicker,
    Space,
    Divider,
    Table,
    Select,
    Tabs,
} from "antd";
import {
    CarOutlined,
    ReloadOutlined,
    SendOutlined,
    FilePdfOutlined,
    CheckCircleOutlined,
    EditOutlined,
} from "@ant-design/icons";
import RmaLayout from "./RmaLayout";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import "./DepotDispatchPage.css";

const { Title, Text, Paragraph } = Typography;

// PREDEFINED_TRANSPORTERS removed in favor of backend API


export default function DepotDispatchPage() {
    const [items, setItems] = useState([]);
    const [inTransitItems, setInTransitItems] = useState([]); // New state for in-transit
    // console.log("DepotDispatchPage rendered. InTransit items:", inTransitItems.length); // Debug render - Removed
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState("1");

    // Edit RMA State
    const [editRmaModalVisible, setEditRmaModalVisible] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [newRmaNo, setNewRmaNo] = useState("");
    const [updatingRma, setUpdatingRma] = useState(false);

    const [dispatchModalVisible, setDispatchModalVisible] = useState(false);
    const [dispatchForm] = Form.useForm();
    const [selectedRmaNo, setSelectedRmaNo] = useState(null);
    const [selectedRmaItems, setSelectedRmaItems] = useState([]);
    const [dispatchSubmitting, setDispatchSubmitting] = useState(false);
    // const [receiveSubmitting, setReceiveSubmitting] = useState(false); // Unused
    const [receivingIds, setReceivingIds] = useState(new Set()); // Track IDs being received

    // DC Generation State
    const [dcModalVisible, setDcModalVisible] = useState(false);
    const [dcForm] = Form.useForm();
    const [dcSubmitting, setDcSubmitting] = useState(false);
    const [transporters, setTransporters] = useState([]);
    const [isNewTransporter, setIsNewTransporter] = useState(false);
    const [dcTableData, setDcTableData] = useState([]);

    const loadItems = async () => {
        setLoading(true);
        try {
            // Parallel fetch for both tabs
            const [dispatchedResult, inTransitResult] = await Promise.all([
                RmaApi.getDepotReadyToDispatch(),
                RmaApi.getDepotInTransit()
            ]);

            if (dispatchedResult.success) {
                setItems(dispatchedResult.data || []);
            } else {
                message.error("Failed to load dispatched items");
            }

            if (inTransitResult.success) {
                setInTransitItems(inTransitResult.data || []);
            } else {
                // It's okay if this fails initially or is empty, just log it
                console.log("In Transit load info:", inTransitResult.error);
            }

        } catch (error) {
            console.error("Load items error:", error);
            message.error("Failed to load items");
        } finally {
            setLoading(false);
        }
    };

    const fetchTransporters = async () => {
        try {
            const result = await RmaApi.getAllTransporters();
            if (result.success) {
                setTransporters(result.data || []);
            }
        } catch (error) {
            console.error("Failed to fetch transporters", error);
        }
    };

    useEffect(() => {
        loadItems();
        fetchTransporters();
    }, []);

    const openEditRmaModal = (item) => {
        setSelectedItem(item);
        // User requested NOT to pre-fill the RMA number
        setNewRmaNo("");
        setEditRmaModalVisible(true);
    };

    const handleUpdateRma = async () => {
        if (!newRmaNo.trim()) {
            message.warning("Please enter RMA Number");
            return;
        }

        setUpdatingRma(true);
        // Using the API to update the item's RMA number
        const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);

        if (result.success) {
            message.success("RMA Number updated successfully!");
            setEditRmaModalVisible(false);
            // Reload items to show the new RMA number
            await loadItems();
        } else {
            message.error(result.error || "Failed to update RMA Number");
        }
        setUpdatingRma(false);
    };

    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) acc[rmaNo] = [];
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const groupedInTransit = inTransitItems.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) acc[rmaNo] = [];
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRma = Object.keys(groupedItems).length;
    const totalItems = items.length;
    const totalInTransitRma = Object.keys(groupedInTransit).length;
    const totalInTransitItems = inTransitItems.length;

    const openDispatchModal = (rmaNo, rmaItems) => {
        // Validation: Ensure all items have an RMA Number assigned
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before dispatching");
            return;
        }
        setSelectedRmaNo(rmaNo);
        setSelectedRmaItems(rmaItems);
        dispatchForm.resetFields();
        dispatchForm.setFieldsValue({
            dispatchDate: null,
            courierName: "",
            trackingNo: "",
            dcNo: rmaItems[0]?.dcNo || "",
            ewayBillNo: rmaItems[0]?.ewayBillNo || "",
            remarks: "Dispatch to Bangalore depot for repair.",
        });
        setDispatchModalVisible(true);
    };

    const handleConfirmDispatch = async () => {
        try {
            const values = await dispatchForm.validateFields();
            setDispatchSubmitting(true);

            const payload = {
                rmaNo: selectedRmaNo,
                itemIds: selectedRmaItems.map((it) => it.id),
                dispatchDate: values.dispatchDate?.format("YYYY-MM-DD"),
                courierName: values.courierName,
                trackingNo: values.trackingNo,
                dcNo: values.dcNo,
                ewayBillNo: values.ewayBillNo,
                remarks: values.remarks,
            };

            const result = await RmaApi.dispatchToBangalore(payload);
            if (result.success) {
                message.success("Items dispatched to Bangalore");
                setDispatchModalVisible(false);
                loadItems();
            } else {
                message.error(result.error || "Failed to dispatch");
            }
        } catch {
            // validation errors shown by antd
        } finally {
            setDispatchSubmitting(false);
        }
    };

    const openDcModal = (rmaNo, rmaItems) => {
        // Validation: Ensure all items have an RMA Number assigned
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before generating Delivery Challan");
            return;
        }
        setSelectedRmaNo(rmaNo); // Reuse this state or create new if needed, currently reusing
        setDcTableData(rmaItems.map((item, index) => ({ ...item, slNo: index + 1, qty: 1 })));

        dcForm.resetFields();
        // Pre-fill some defaults if needed
        dcForm.setFieldsValue({
            modeOfShipment: "ROAD",
            boxes: "1",
            consigneeName: "Motorola Solutions India Pvt Ltd", // Default or clear
            consigneeAddress: "Bangalore",
            gstIn: "29AAACM4363F1Z6" // Example default for Bangalore Depot if known, else blank
        });
        setDcModalVisible(true);
    };

    const handleGenerateDC = async (values) => {
        try {
            setDcSubmitting(true);
            const {
                consigneeName, consigneeAddress, gstIn,
                boxes, dimensions, weight, modeOfShipment,
                transporterName, transporterId,
                items: formItems // captured from table inputs
            } = values;

            // Transporter Saving Logic
            if (isNewTransporter && transporterName) {
                // Logic to save new transporter if needed, or just let backend handle it implicitly if API supports
                // Here we mirror UnrepairedPage logic:
                const tId = transporterId || null; // If user typed ID
                if (tId) {

                }
            }

            const formattedItems = dcTableData.map((item, index) => ({
                slNo: index + 1,
                product: item.product,     // Required for PDF Description
                model: item.model,         // Required for PDF Description
                serialNo: item.serialNo,   // Required for "Material Code" column
                rate: (formItems?.[index]?.rate || 0).toString(), // Required for Value/Rate
                itemId: item.id
            }));

            const payload = {
                rmaNo: selectedRmaNo,
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
                // Refresh transporters list in case a new one was added
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

    const handleMarkReceived = async (itemsToReceive, rmaNo = null) => {
        // Ensure specific items array
        const itemsArray = Array.isArray(itemsToReceive) ? itemsToReceive : [itemsToReceive];
        const ids = itemsArray.map(i => i.id);
        const groupKey = rmaNo ? `GROUP_${rmaNo}` : null;

        try {
            if (rmaNo) {
                // setReceiveSubmitting(true);
            }

            setReceivingIds(prev => {
                const next = new Set(prev);
                if (groupKey) next.add(groupKey);
                ids.forEach(id => next.add(id));
                return next;
            });

            const payload = {
                itemIds: ids
            };
            const result = await RmaApi.markDepotReceived(payload);
            if (result.success) {
                message.success("Items marked as Received at Depot");
                loadItems();
            } else {
                message.error(result.error || "Failed to mark as received");
            }
        } catch (err) {
            console.error(err);
            message.error("Error marking received");
        } finally {
            // setReceiveSubmitting(false);
            setReceivingIds(prev => {
                const next = new Set(prev);
                if (groupKey) next.delete(groupKey);
                ids.forEach(id => next.delete(id));
                return next;
            });
        }
    };

    return (
        <RmaLayout>
            <div className="depot-page">
                <div className="depot-header">
                    <div className="header-content">
                        <div className="header-title">
                            <CarOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Depot Dispatch & Receiving
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Manage dispatch to Bangalore and confirm receipt
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
                </div>

                <div className="depot-content">
                    <Tabs
                        activeKey={activeTab}
                        onChange={setActiveTab}
                        items={[
                            {
                                key: "1",
                                label: `Ready to Dispatch (${totalItems})`,
                                children: (
                                    <>
                                        {loading ? (
                                            <div className="loading-container">
                                                <Spin size="large" />
                                                <Text style={{ marginTop: 16 }}>Loading items...</Text>
                                            </div>
                                        ) : totalRma === 0 ? (
                                            <Empty
                                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                                description="No items pending dispatch"
                                                className="empty-state"
                                            />
                                        ) : (
                                            <div className="rma-groups">
                                                {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                                    <Card
                                                        key={rmaNo}
                                                        className="rma-group-card"
                                                        title={
                                                            <div className="rma-card-header">
                                                                <span className="rma-number">
                                                                    <Tag color="#9254de">
                                                                        RMA: {rmaNo}
                                                                    </Tag>
                                                                </span>
                                                                <Badge
                                                                    count={rmaItems.length}
                                                                    style={{ backgroundColor: "#9254de" }}
                                                                />
                                                            </div>
                                                        }
                                                        extra={
                                                            <Space>
                                                                <Button
                                                                    className="btn-generate-dc"
                                                                    icon={<FilePdfOutlined />}
                                                                    onClick={() => openDcModal(rmaNo, rmaItems)}
                                                                >
                                                                    Generate DC
                                                                </Button>
                                                                <Button
                                                                    type="primary"
                                                                    className="btn-dispatch"
                                                                    icon={<SendOutlined />}
                                                                    onClick={() => openDispatchModal(rmaNo, rmaItems)}
                                                                >
                                                                    Dispatch to Bangalore ({rmaItems.length})
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
                                                                        actions={[
                                                                            !item.itemRmaNo && (
                                                                                <Button
                                                                                    key="add-rma"
                                                                                    icon={<EditOutlined />}
                                                                                    onClick={() => openEditRmaModal(item)}
                                                                                >
                                                                                    Add RMA
                                                                                </Button>
                                                                            )
                                                                        ].filter(Boolean)}
                                                                        title={
                                                                            <div style={{ display: "flex", justifyContent: "space-between" }}>
                                                                                <Text strong>{item.product || "Item"}</Text>
                                                                                <Tag color="purple">Depot Repair</Tag>
                                                                            </div>
                                                                        }
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
                                                                                <Text type="secondary">Model</Text>
                                                                                <Text>{item.model || "N/A"}</Text>
                                                                            </div>
                                                                            {item.itemRmaNo && (
                                                                                <div className="item-row">
                                                                                    <Text type="secondary">RMA No</Text>
                                                                                    <Tag color="purple">{item.itemRmaNo}</Tag>
                                                                                </div>
                                                                            )}
                                                                            <div className="item-row">
                                                                                <Text type="secondary">Fault</Text>
                                                                                <Paragraph ellipsis={{ rows: 1 }} style={{ marginBottom: 0 }}>{item.faultDescription}</Paragraph>
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
                                    </>
                                )
                            },
                            {
                                key: "2",
                                label: `In Transit / Dispatched (${totalInTransitItems})`,
                                children: (
                                    <>
                                        {loading ? (
                                            <div className="loading-container"><Spin /></div>
                                        ) : totalInTransitRma === 0 ? (
                                            <Empty description="No items in transit" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                                        ) : (
                                            <div className="rma-groups">
                                                {Object.entries(groupedInTransit).map(([rmaNo, rmaItems]) => (
                                                    <Card
                                                        key={rmaNo}
                                                        className="rma-group-card"
                                                        title={
                                                            <div className="rma-card-header">
                                                                <span className="rma-number">
                                                                    <Tag color="#9254de">
                                                                        RMA: {rmaNo}
                                                                    </Tag>
                                                                </span>
                                                                <Badge
                                                                    count={rmaItems.length}
                                                                    style={{ backgroundColor: "#9254de" }}
                                                                />
                                                            </div>
                                                        }
                                                        extra={
                                                            <Space>
                                                                <Tag color="cyan" icon={<SendOutlined />}>Out for Delivery</Tag>
                                                            </Space>
                                                        }
                                                    >
                                                        <div className="in-transit-box">
                                                            <CarOutlined style={{ fontSize: 24, color: '#9254de' }} />
                                                            <Space size="large" style={{ flex: 1 }}>
                                                                <div>
                                                                    <Text type="secondary" style={{ display: 'block', fontSize: 12 }}>DC Number</Text>
                                                                    <Text strong>{rmaItems[0]?.dcNo || 'N/A'}</Text>
                                                                </div>
                                                                <div>
                                                                    <Text type="secondary" style={{ display: 'block', fontSize: 12 }}>E-Way Bill</Text>
                                                                    <Text strong>{rmaItems[0]?.ewayBillNo || 'N/A'}</Text>
                                                                </div>
                                                                <Tag color="processing">In Transit to Depot</Tag>
                                                            </Space>
                                                        </div>
                                                        <Row gutter={[16, 16]}>
                                                            {rmaItems.map((item) => (
                                                                <Col xs={24} md={12} lg={8} key={item.id}>
                                                                    <Card
                                                                        className="item-card"
                                                                        size="small"
                                                                        actions={[
                                                                            item.depotStage === 'IN_TRANSIT_TO_DEPOT' && (
                                                                                <Button
                                                                                    key="receive-item"
                                                                                    type="text"
                                                                                    icon={<CheckCircleOutlined />}
                                                                                    onClick={() => handleMarkReceived(item)}
                                                                                    loading={receivingIds.has(item.id)}
                                                                                    style={{ color: '#52c41a' }}
                                                                                >
                                                                                    Receive Item
                                                                                </Button>
                                                                            )
                                                                        ].filter(Boolean)}
                                                                    >
                                                                        <div className="item-content">
                                                                            <div style={{ marginBottom: 8 }}>
                                                                                {item.depotStage === 'IN_TRANSIT_TO_DEPOT' ? (
                                                                                    <Tag color="processing">In Transit</Tag>
                                                                                ) : (
                                                                                    <Tag color="success">Received at Depot</Tag>
                                                                                )}
                                                                            </div>
                                                                            <div className="item-row">
                                                                                <Text type="secondary">Product</Text>
                                                                                <Text strong>{item.product || "N/A"}</Text>
                                                                            </div>
                                                                            <div className="item-row">
                                                                                <Text type="secondary">Serial No.</Text>
                                                                                <Text code>{item.serialNo || "N/A"}</Text>
                                                                            </div>
                                                                            <div className="item-row">
                                                                                <Text type="secondary">Model</Text>
                                                                                <Text>{item.model || "N/A"}</Text>
                                                                            </div>
                                                                            {item.itemRmaNo && (
                                                                                <div className="item-row">
                                                                                    <Text type="secondary">RMA Number</Text>
                                                                                    <Tag color="purple">{item.itemRmaNo}</Tag>
                                                                                </div>
                                                                            )}
                                                                        </div>
                                                                    </Card>
                                                                </Col>
                                                            ))}
                                                        </Row>
                                                    </Card>
                                                ))}
                                            </div>
                                        )}
                                    </>
                                )
                            }
                        ]}
                    />
                </div>

                <Modal
                    title={`Dispatch to Bangalore - ${selectedRmaNo || ""}`}
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
                                You are dispatching <strong>{selectedRmaItems.length}</strong> item(s) for this RMA to Bangalore depot.
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
                                        onSelect={(value, option) => {
                                            const t = transporters.find(tr => tr.name === value);
                                            if (t) {
                                                dcForm.setFieldsValue({ transporterId: t.transporterId });
                                                setIsNewTransporter(false);
                                            }
                                        }}
                                        onSearch={(val) => {
                                            // If user types something that doesn't match, we treat as potentially new
                                            const exists = transporters.some(t => t.name.toLowerCase() === val.toLowerCase());
                                            if (!exists) {
                                                dcForm.setFieldsValue({ transporterId: "" });
                                                setIsNewTransporter(true);
                                            }
                                        }}
                                        onChange={(val) => {
                                            // Handle when user clears or selects
                                            // If it's a selection, onSelect handles it.
                                            // If it's a clear or custom input kept...
                                            // Note: AntD Select in default mode doesn't keep custom text on blur unless valid option.
                                            // But using onSearch we can capture intent.
                                            // For simple "Type New", a Combobox mode is better, 
                                            // but let's stick to this and assume user picks "Other" or we add a "New" feature?
                                            // No, user wants to add new.
                                            // Let's change mode to "tags" with maxTagCount 1 to allow custom text?
                                            // Or simpler: Just leave as is but emphasize filling ID?
                                            // The issue is if I type "New Trans", and click away, it disappears if not an option.
                                            // Let's use allowClear and onSearch. 
                                            // Actually RmaService expects name.
                                        }}
                                        // Adding tags mode allows creating new entries on the fly
                                        mode="tags"
                                        maxTagCount={1}
                                    >
                                        {transporters.map(t => (
                                            <Select.Option key={t.id} value={t.name}>
                                                {t.name}
                                            </Select.Option>
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
                {/* Add RMA Modal */}
                <Modal
                    title="Add RMA Number"
                    open={editRmaModalVisible}
                    onCancel={() => setEditRmaModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setEditRmaModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="add"
                            type="primary"
                            onClick={handleUpdateRma}
                            loading={updatingRma}
                            style={{ backgroundColor: "#1890ff", borderColor: "#1890ff" }}
                        >
                            Add
                        </Button>
                    ]}
                >
                    <div style={{ padding: "20px 0" }}>
                        <Text strong style={{ display: "block", marginBottom: 8 }}>RMA Number:</Text>
                        <Input
                            placeholder="Enter RMA Number"
                            value={newRmaNo}
                            onChange={(e) => setNewRmaNo(e.target.value)}
                            size="large"
                        />
                    </div>
                </Modal>
            </div>
        </RmaLayout >
    );
}
