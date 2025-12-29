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
    Tabs,
} from "antd";
import {
    CheckCircleOutlined,
    TrophyOutlined,
    ReloadOutlined,
    EyeOutlined,
    DownloadOutlined,
    FilePdfOutlined,
    SendOutlined,
    WarningOutlined,
    UserOutlined,
    CalendarOutlined,
    BarcodeOutlined,
    TruckOutlined
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css"; // Ensures styles match the previous page

const { Title, Text, Paragraph } = Typography;

const PREDEFINED_TRANSPORTERS = {
    "BlueDart": "BD001",
    "DTDC": "DT001",
    "Delhivery": "DL001"
};

export default function RepairedPage() {
    const [items, setItems] = useState([]);
    const [dispatchedItems, setDispatchedItems] = useState([]);
    const [activeTab, setActiveTab] = useState("1");
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
    const [selectedDispatchItemIds, setSelectedDispatchItemIds] = useState([]);
    const [dispatchForm] = Form.useForm();

    // Delivery Confirmation Modal State
    const [deliveryModalVisible, setDeliveryModalVisible] = useState(false);
    const [deliverySubmitting, setDeliverySubmitting] = useState(false);
    const [selectedDeliveryItems, setSelectedDeliveryItems] = useState([]);
    const [deliveryForm] = Form.useForm();

    const loadItems = async () => {
        setLoading(true);
        try {
            const [repairedResult, dispatchedResult, cantRepairedResult] = await Promise.all([
                RmaApi.getRepairedItems(),
                RmaApi.getDispatchedItems(),
                RmaApi.getCantBeRepairedItems()
            ]);

            let allLocalItems = [];

            if (repairedResult.success) {
                const localRepaired = (repairedResult.data || []).filter(item =>
                    item.repairType !== 'DEPOT' && item.isDispatched !== true
                );
                allLocalItems = [...allLocalItems, ...localRepaired];
            } else {
                message.error("Failed to load repaired items");
            }

            if (cantRepairedResult.success) {
                const berItems = (cantRepairedResult.data || []).filter(item =>
                    item.repairStatus === "BER" && item.isDispatched !== true
                );
                allLocalItems = [...allLocalItems, ...berItems];
            }

            setItems(allLocalItems);

            if (dispatchedResult.success) {
                const localDispatched = (dispatchedResult.data || []).filter(item => item.repairType !== 'DEPOT');
                setDispatchedItems(localDispatched);
            }
        } catch (error) {
            console.error("Load items error:", error);
            message.error("Failed to load items");
        } finally {
            setLoading(false);
        }
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

    // ... [KEEPING EXISTING MODAL LOGIC SAME AS PROVIDED] ...

    // Open DC Modal
    const openDcModal = async (rmaNo, rmaItems) => {
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before generating Delivery Challan");
            return;
        }
        setSelectedRmaForDc(rmaNo);
        const tableData = rmaItems.map((item, index) => ({ ...item, slNo: index + 1, qty: 1 }));
        setDcTableData(tableData);

        dcForm.resetFields();

        // Fetch Next DC Number
        let nextDcNo = "";
        try {
            const res = await RmaApi.getNextDcNo();
            if (res.success && res.data && res.data.dcNo) {
                nextDcNo = res.data.dcNo;
            }
        } catch (error) {
            console.error("Failed to fetch next DC No", error);
        }

        const customerDetails = rmaItems[0]?.rmaRequest;
        dcForm.setFieldsValue({
            modeOfShipment: "ROAD",
            boxes: "1",
            consigneeName: customerDetails?.companyName || "",
            consigneeAddress: customerDetails?.returnAddress || "",
            dcNo: nextDcNo // Auto-fill DC Number
        });

        try {
            const itemsToFetch = rmaItems.map(i => ({ product: i.product, model: i.model }));
            const rateRes = await RmaApi.getProductRates(itemsToFetch);

            if (rateRes.success) {
                const ratesMap = rateRes.data;
                const itemFormValues = tableData.map(item => {
                    const key = `${item.product?.trim() || ""}::${(item.model || "").trim()}`;
                    return { rate: ratesMap[key] || "" };
                });
                dcForm.setFieldsValue({ items: itemFormValues });
            }
        } catch (e) {
            console.error("Failed to fetch rates", e);
        }

        setDcModalVisible(true);
    };

    const handleGenerateDC = async (values) => {
        try {
            setDcSubmitting(true);
            setDcSubmitting(true);
            const { consigneeName, consigneeAddress, gstIn, boxes, dimensions, weight, modeOfShipment, courierName, transporterId, dcNo, items: formItems } = values;

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
                consigneeName, consigneeAddress, gstIn,
                boxes: parseInt(boxes), dimensions, weight, modeOfShipment,

                transporterName: Array.isArray(courierName) ? courierName[courierName.length - 1] : courierName,
                transporterId,
                dcNo, // Include manually or auto-filled DC No
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

    const openPreview = (rmaItems, rmaNo) => {
        setPreviewData({ rmaNo, items: rmaItems });
        setPreviewVisible(true);
    };

    const openDispatchModal = (rmaNo, rmaItems) => {
        const pendingRma = rmaItems.some(item => !item.itemRmaNo || item.itemRmaNo.trim() === '');
        if (pendingRma) {
            message.warning("RMA Number must be assigned to all items before dispatching");
            return;
        }
        setSelectedRmaNo(rmaNo);
        setSelectedRmaItems(rmaItems);
        setSelectedDispatchItemIds(rmaItems.map(item => item.id));
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

    const handleConfirmDispatch = async () => {
        try {
            if (selectedDispatchItemIds.length === 0) {
                message.warning("Please select at least one item to dispatch");
                return;
            }
            const values = await dispatchForm.validateFields();
            setDispatchSubmitting(true);

            const payload = {
                itemIds: selectedDispatchItemIds,
                remarks: values.remarks || 'Dispatched to Customer',
                dcNo: values.dcNo,
                ewayBillNo: values.ewayBillNo,
                courierName: values.courierName,
                trackingNo: values.trackingNo,
                dispatchDate: values.dispatchDate?.format("YYYY-MM-DD"),
            };

            const result = await RmaApi.dispatchToCustomer(payload);

            if (result.success) {
                message.success(`${selectedDispatchItemIds.length} item(s) dispatched successfully!`);
                setDispatchModalVisible(false);
                loadItems();
            } else {
                message.error(result.error || "Failed to dispatch items");
            }
        } catch (error) {
            console.error("Dispatch error:", error);
            message.error("Failed to dispatch items");
        } finally {
            setDispatchSubmitting(false);
        }
    };

    const confirmGenerateGatepass = async () => {
        const { rmaNo } = previewData;
        if (!rmaNo || rmaNo === "Unknown") {
            message.error("Cannot generate Gatepass: Invalid RMA Number.");
            return;
        }
        setGeneratingGatepass(rmaNo);
        const result = await RmaApi.generateOutwardGatepass(rmaNo);
        if (result.success && result.blob) {
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `OutwardGatepass_${rmaNo}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('Outward Gatepass generated and downloaded!');
            setPreviewVisible(false);
        } else {
            message.error(result.error || 'Failed to generate gatepass');
        }
        setGeneratingGatepass(null);
    };

    const openDeliveryModal = (rmaItems) => {
        const undeliveredItems = rmaItems.filter(item => !item.isDelivered);
        if (undeliveredItems.length === 0) {
            message.info("All items in this RMA have already been confirmed for delivery.");
            return;
        }
        setSelectedDeliveryItems(undeliveredItems);
        deliveryForm.resetFields();
        setDeliveryModalVisible(true);
    };

    const handleConfirmDelivery = async () => {
        try {
            const values = await deliveryForm.validateFields();
            setDeliverySubmitting(true);
            const itemIds = selectedDeliveryItems.map(item => item.id);
            const result = await RmaApi.confirmDelivery(
                itemIds,
                values.deliveredTo,
                values.deliveredBy,
                values.deliveryNotes
            );

            if (result.success) {
                message.success(result.message || "Delivery confirmed successfully!");
                setDeliveryModalVisible(false);
                loadItems();
            } else {
                message.error(result.error || "Failed to confirm delivery");
            }
        } catch (error) {
            console.error("Delivery confirmation error:", error);
            message.error("Failed to confirm delivery");
        } finally {
            setDeliverySubmitting(false);
        }
    };

    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) acc[rmaNo] = [];
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const groupedDispatchedItems = dispatchedItems.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) acc[rmaNo] = [];
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRmaRequests = Object.keys(groupedItems).length;
    const totalItems = items.length;
    const totalDispatchedRma = Object.keys(groupedDispatchedItems).length;
    const totalDispatchedItems = dispatchedItems.length;

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                <div className="unrepaired-header header-repaired">
                    <div className="header-content">
                        <div className="header-title">
                            <CheckCircleOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0 }}>Local Repaired Items</Title>
                                <Text type="secondary">Items repaired locally and ready for dispatch</Text>
                            </div>
                        </div>
                        <Button icon={<ReloadOutlined />} onClick={loadItems} loading={loading} className="refresh-btn">Refresh</Button>
                    </div>
                    {/* Stats */}
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

                <div className="unrepaired-content">
                    <Tabs
                        activeKey={activeTab}
                        onChange={setActiveTab}
                        items={[
                            {
                                key: "1",
                                label: `Ready to Dispatch (${totalItems})`,
                                children: loading ? (
                                    <div className="loading-container"><Spin size="large" /></div>
                                ) : totalRmaRequests === 0 ? (
                                    <Empty description="No local repaired items found" />
                                ) : (
                                    <div className="rma-groups">
                                        {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => (
                                            <Card
                                                key={rmaNo}
                                                className="rma-group-card"
                                                title={
                                                    <div className="rma-card-header-flex">
                                                        <div className="rma-identity">
                                                            <div className="rma-id-box">
                                                                <span className="rma-label">RMA Request</span>
                                                                <span className="rma-value">{rmaNo}</span>
                                                            </div>
                                                            <Badge count={rmaItems.length} />
                                                        </div>
                                                        <div className="rma-actions">
                                                            <Button icon={<EyeOutlined />} onClick={() => openPreview(rmaItems, rmaNo)}>Outpass</Button>
                                                            <Button icon={<FilePdfOutlined />} onClick={() => openDcModal(rmaNo, rmaItems)}>DC</Button>
                                                            <Button type="primary" icon={<SendOutlined />} onClick={() => openDispatchModal(rmaNo, rmaItems)}>Dispatch</Button>
                                                        </div>
                                                    </div>
                                                }
                                            >
                                                {/* MODIFIED CARD LAYOUT */}
                                                <div className="rma-items-grid">
                                                    {rmaItems.map((item) => (
                                                        <div key={item.id} className="rma-item-card-modern">
                                                            <div className="item-header">
                                                                <span className="item-product">{item.product || "Product"}</span>
                                                                {item.repairStatus?.toUpperCase() === "BER" ?
                                                                    <Tag color="red" icon={<WarningOutlined />}>BER</Tag> :
                                                                    item.repairStatus?.toUpperCase() === "REPLACED" ?
                                                                        <Tag color="cyan" icon={<ReloadOutlined />}>REPLACED</Tag> :
                                                                        <Tag color="green" icon={<CheckCircleOutlined />}>REPAIRED</Tag>
                                                                }
                                                            </div>

                                                            <div className="item-details-grid">
                                                                <div className="detail-box">
                                                                    <span className="label"><BarcodeOutlined /> Serial No</span>
                                                                    <span className="value monospace">{item.serialNo}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label">Model</span>
                                                                    <span className="value">{item.model}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label"><UserOutlined /> Repaired By</span>
                                                                    <span className="value">{item.repairedByName || item.assignedToName || "N/A"}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label"><CalendarOutlined /> Date</span>
                                                                    <span className="value">{item.repairedDate ? new Date(item.repairedDate).toLocaleDateString() : "N/A"}</span>
                                                                </div>
                                                            </div>

                                                            <div className="fault-box">
                                                                <span className="label">Repair Remarks</span>
                                                                <p className="fault-desc">{item.repairRemarks || "No remarks provided."}</p>
                                                            </div>

                                                            <div className="item-footer">
                                                                <span style={{ fontSize: '12px', color: '#8c8c8c' }}>RMA: </span>
                                                                <Tag color="green">{item.itemRmaNo || "N/A"}</Tag>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            </Card>
                                        ))}
                                    </div>
                                ),
                            },
                            {
                                key: "2",
                                label: `Dispatched (${totalDispatchedItems})`,
                                children: loading ? (
                                    <div className="loading-container"><Spin size="large" /></div>
                                ) : totalDispatchedRma === 0 ? (
                                    <Empty description="No dispatched items found" />
                                ) : (
                                    <div className="rma-groups">
                                        {Object.entries(groupedDispatchedItems).map(([rmaNo, rmaItems]) => (
                                            <Card
                                                key={rmaNo}
                                                className="rma-group-card"
                                                title={
                                                    <div className="rma-card-header-flex">
                                                        <div className="rma-identity">
                                                            <div className="rma-id-box">
                                                                <span className="rma-label">RMA Request</span>
                                                                <span className="rma-value">{rmaNo}</span>
                                                            </div>
                                                            <Badge count={rmaItems.length} />
                                                        </div>
                                                        <div className="rma-actions">
                                                            <Tag color="blue" icon={<SendOutlined />}>DISPATCHED</Tag>
                                                            {rmaItems.some(item => !item.isDelivered) && (
                                                                <Button type="primary" icon={<CheckCircleOutlined />} onClick={() => openDeliveryModal(rmaItems)}>
                                                                    Confirm Delivery
                                                                </Button>
                                                            )}
                                                        </div>
                                                    </div>
                                                }
                                            >
                                                {/* MODIFIED DISPATCHED CARD LAYOUT */}
                                                <div className="rma-items-grid">
                                                    {rmaItems.map((item) => (
                                                        <div key={item.id} className="rma-item-card-modern">
                                                            <div className="item-header">
                                                                <span className="item-product">{item.product}</span>
                                                                {item.isDelivered ?
                                                                    <Tag color="green">DELIVERED</Tag> :
                                                                    <Tag color="blue">IN TRANSIT</Tag>
                                                                }
                                                            </div>

                                                            <div className="item-details-grid">
                                                                <div className="detail-box">
                                                                    <span className="label">Serial No</span>
                                                                    <span className="value monospace">{item.serialNo}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label">Model</span>
                                                                    <span className="value">{item.model}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label"><TruckOutlined /> Courier</span>
                                                                    <span className="value">{item.courierName || "N/A"}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label">Tracking</span>
                                                                    <span className="value">{item.trackingNo || "N/A"}</span>
                                                                </div>
                                                                <div className="detail-box">
                                                                    <span className="label"><CalendarOutlined /> Dispatch Date</span>
                                                                    <span className="value">{item.dispatchedDate ? new Date(item.dispatchedDate).toLocaleDateString() : "N/A"}</span>
                                                                </div>
                                                            </div>

                                                            {item.isDelivered && (
                                                                <div className="fault-box">
                                                                    <span className="label">Delivery Details</span>
                                                                    <p className="fault-desc">
                                                                        Received by: <strong>{item.deliveredTo}</strong><br />
                                                                        Date: {new Date(item.deliveryDate).toLocaleDateString()}
                                                                    </p>
                                                                </div>
                                                            )}

                                                            <div className="item-footer">
                                                                <span style={{ fontSize: '12px', color: '#8c8c8c' }}>RMA: </span>
                                                                <Tag color="blue">{item.itemRmaNo}</Tag>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            </Card>
                                        ))}
                                    </div>
                                ),
                            },
                        ]}
                    />
                </div>

                {/* --- MODALS (Dispatch, DC, Gatepass, Delivery) --- */}
                {/* [Modals are kept structurally same as your original, just rendering them here] */}

                <Modal title={`Dispatch to Customer - ${selectedRmaNo || ""}`} open={dispatchModalVisible} onCancel={() => setDispatchModalVisible(false)} confirmLoading={dispatchSubmitting} onOk={handleConfirmDispatch} okText="Confirm Dispatch" width={700}>
                    <Form form={dispatchForm} layout="vertical">
                        <Row gutter={16}>
                            <Col span={12}><Form.Item name="dispatchDate" label="Dispatch Date" rules={[{ required: true }]}><DatePicker style={{ width: "100%" }} /></Form.Item></Col>
                            <Col span={12}><Form.Item name="courierName" label="Courier Company"><Input /></Form.Item></Col>
                        </Row>
                        <Row gutter={16}>
                            <Col span={12}><Form.Item name="trackingNo" label="Tracking / AWB No."><Input /></Form.Item></Col>
                            <Col span={12}><Form.Item name="dcNo" label="Delivery Challan No."><Input /></Form.Item></Col>
                        </Row>
                        <Form.Item name="ewayBillNo" label="E-Way Bill No."><Input /></Form.Item>
                        <Form.Item name="remarks" label="Remarks"><Input.TextArea rows={3} /></Form.Item>
                        <Divider>Select Items to Dispatch</Divider>
                        <Table size="small" rowKey="id" dataSource={selectedRmaItems} pagination={false} rowSelection={{ selectedRowKeys: selectedDispatchItemIds, onChange: (keys) => setSelectedDispatchItemIds(keys) }} columns={[{ title: 'Serial No', dataIndex: 'serialNo' }, { title: 'Product', dataIndex: 'product' }, { title: 'Model', dataIndex: 'model' }]} />
                    </Form>
                </Modal>

                <Modal title="Generate Delivery Challan" open={dcModalVisible} onCancel={() => setDcModalVisible(false)} width={1000} footer={[<Button key="cancel" onClick={() => setDcModalVisible(false)}>Cancel</Button>, <Button key="gen" type="primary" icon={<FilePdfOutlined />} onClick={() => dcForm.submit()} loading={dcSubmitting}>Generate DC</Button>]}>
                    <Form form={dcForm} layout="vertical" onFinish={handleGenerateDC} initialValues={{ modeOfShipment: "ROAD", boxes: "1" }}>
                        <Row gutter={16}>
                            <Col span={12}>
                                <Card title="Consignor Details" size="small">
                                    <p><strong>Motorola Solutions India</strong></p>
                                    <p>A, Building 8, DLF</p>
                                    <p>Gurgaon, Haryana, India</p>
                                </Card>
                            </Col>
                            <Col span={12}>
                                <Card title="Consignee Details" size="small">
                                    <Form.Item name="consigneeName" label="Name"><Input /></Form.Item>
                                    <Form.Item name="consigneeAddress" label="Address"><Input.TextArea rows={2} /></Form.Item>
                                    <Form.Item name="gstIn" label="GST IN"><Input /></Form.Item>
                                    <Form.Item name="dcNo" label="Delivery Challan No." rules={[{ required: true }]}><Input placeholder="Auto-generated" /></Form.Item>
                                </Card>
                            </Col>
                        </Row>
                        <Divider orientation="left">Shipment</Divider>
                        <Row gutter={16}>
                            <Col span={6}><Form.Item name="boxes" label="Boxes" rules={[{ required: true }]}><Input type="number" /></Form.Item></Col>
                            <Col span={6}><Form.Item name="dimensions" label="Dims"><Input /></Form.Item></Col>
                            <Col span={6}><Form.Item name="weight" label="Weight"><Input /></Form.Item></Col>
                            <Col span={6}><Form.Item name="modeOfShipment" label="Mode"><Select><Select.Option value="ROAD">ROAD</Select.Option><Select.Option value="AIR">AIR</Select.Option></Select></Form.Item></Col>
                        </Row>
                        <Row gutter={16}>
                            <Col span={12}><Form.Item name="courierName" label="Courier Name" rules={[{ required: true, message: 'Required' }]}><Select mode="tags" onChange={(val) => { const v = Array.isArray(val) ? val[val.length - 1] : val; const t = transporters.find(x => x.name === v); if (t) dcForm.setFieldValue('transporterId', t.transporterId); }}>{transporters.map(t => <Select.Option key={t.id} value={t.name}>{t.name}</Select.Option>)}</Select></Form.Item></Col>
                            <Col span={12}><Form.Item name="transporterId" label="Transporter ID"><Input /></Form.Item></Col>
                        </Row>
                        <Divider>Items</Divider>
                        <Table dataSource={dcTableData} pagination={false} size="small" columns={[{ title: 'Sr No', dataIndex: 'slNo', width: 60 }, { title: 'Material', dataIndex: 'serialNo' }, { title: 'Desc', dataIndex: 'product' }, { title: 'Value', render: (_, __, idx) => <Form.Item name={['items', idx, 'rate']} style={{ margin: 0 }} rules={[{ required: true, message: 'Required' }]}><Input prefix="₹" type="number" /></Form.Item> }]} />
                    </Form>
                </Modal>

                <Modal title="Gatepass Preview" open={previewVisible} onCancel={() => setPreviewVisible(false)} width={800} footer={[<Button key="c" onClick={() => setPreviewVisible(false)}>Close</Button>, <Button key="d" type="primary" icon={<DownloadOutlined />} onClick={confirmGenerateGatepass} loading={generatingGatepass === previewData.rmaNo}>Download PDF</Button>]}>
                    <Table dataSource={previewData.items} size="small" columns={[{ title: 'Product', dataIndex: 'product' }, { title: 'Serial', dataIndex: 'serialNo' }, { title: 'Remarks', dataIndex: 'repairRemarks' }]} />
                </Modal>

                <Modal title="Confirm Delivery" open={deliveryModalVisible} onCancel={() => setDeliveryModalVisible(false)} onOk={handleConfirmDelivery} confirmLoading={deliverySubmitting} okButtonProps={{ style: { background: "#52c41a", borderColor: "#52c41a" } }}>
                    <p>Confirming delivery for {selectedDeliveryItems.length} items.</p>
                    <Form form={deliveryForm} layout="vertical">
                        <Form.Item name="deliveredTo" label="Receiver Name" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="deliveredBy" label="Delivered By"><Input /></Form.Item>
                        <Form.Item name="deliveryNotes" label="Notes"><Input.TextArea /></Form.Item>
                    </Form>
                </Modal>
            </div>
        </RmaLayout>
    );
}