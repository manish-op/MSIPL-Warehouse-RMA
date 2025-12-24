import React, { useEffect, useState } from "react";
import Cookies from "js-cookie";
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
  Radio,
  Upload,
  Dropdown,
  Alert,
} from "antd";
import {
  CarOutlined,
  ReloadOutlined,
  SendOutlined,
  FilePdfOutlined,
  CheckCircleOutlined,
  EditOutlined,
  DownOutlined,
  BarcodeOutlined,
  CalendarOutlined,
} from "@ant-design/icons";
import RmaLayout from "./RmaLayout";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import { URL } from "../API/URL";
import "./DepotDispatchPage.css";

const { Title, Text } = Typography;

const PREDEFINED_TRANSPORTERS = {
  "Blue Dart Express": "27AAACB0446L1ZS",
  "Blue Dart": "27AAACB0446L1ZS",
  "Safe Express": "27AAECS4363H2Z7",
};

export default function DepotDispatchPage() {
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState([]);
  const [inTransitItems, setInTransitItems] = useState([]);
  const [activeTab, setActiveTab] = useState("1");

  // Edit RMA State
  const [editRmaModalVisible, setEditRmaModalVisible] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [newRmaNo, setNewRmaNo] = useState("");
  const [updatingRma, setUpdatingRma] = useState(false);

  // Bangalore dispatch modal
  const [dispatchModalVisible, setDispatchModalVisible] = useState(false);
  const [dispatchForm] = Form.useForm();
  const [selectedRmaNo, setSelectedRmaNo] = useState(null);
  const [selectedRmaItems, setSelectedRmaItems] = useState([]);
  const [selectedDispatchItemIds, setSelectedDispatchItemIds] = useState([]);
  const [dispatchSubmitting, setDispatchSubmitting] = useState(false);
  const [receivingIds, setReceivingIds] = useState(new Set());

  // DC Generation State
  const [dcModalVisible, setDcModalVisible] = useState(false);
  const [dcForm] = Form.useForm();
  const [dcSubmitting, setDcSubmitting] = useState(false);
  const [transporters, setTransporters] = useState([]);
  const [dcTableData, setDcTableData] = useState([]);
  const [isNewTransporter, setIsNewTransporter] = useState(false); // Added this based on handleTransporterChange usage

  //Gurgaon side state
  const [ggnPlanModalVisible, setGgnPlanModalVisible] = useState(false);
  const [ggnProofModalVisible, setGgnProofModalVisible] = useState(false);
  const [ggnDispatchMode, setGgnDispatchMode] = useState("HAND");
  const [ggnCourierName, setGgnCourierName] = useState("");
  const [ggnTrackingNo, setGgnTrackingNo] = useState("");
  const [ggnHandlerName, setGgnHandlerName] = useState("");
  const [ggnHandlerContact, setGgnHandlerContact] = useState("");
  // New DC Fields
  const [ggnAddress, setGgnAddress] = useState("");
  const [ggnState, setGgnState] = useState("");
  const [ggnCity, setGgnCity] = useState(""); // Added this based on openGgnDispatchModal usage
  const [ggnTransporterId, setGgnTransporterId] = useState("");
  const [ggnGst, setGgnGst] = useState("");
  const [ggnValue, setGgnValue] = useState("");
  const [ggnProofFileId, setGgnProofFileId] = useState(null);
  const [ggnProofRemarks, setGgnProofRemarks] = useState("");
  const [ggnSubmitting, setGgnSubmitting] = useState(false);

  // Dispatch logic flags
  const [isReturnDispatch, setIsReturnDispatch] = useState(false);
  const [isCustomerDispatch, setIsCustomerDispatch] = useState(false);

  const loadItems = async () => {
    setLoading(true);
    try {
      const [dispatchedResult, inTransitResult] = await Promise.all([
        RmaApi.getDepotReadyToDispatch(),
        RmaApi.getDepotInTransit(),
      ]);

      if (dispatchedResult.success) {
        setItems(dispatchedResult.data || []);
      } else {
        message.error("Failed to load dispatched items");
      }

      if (inTransitResult.success) {
        setInTransitItems(inTransitResult.data || []);
      } else {
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
      if (result && result.success !== false) {
        setTransporters(
          Array.isArray(result) ? result : result.data || []
        );
      }
    } catch (error) {
      console.error("Failed to fetch transporters", error);
    }
  };

  /* Access Control Removed by User Request */
  useEffect(() => {
    loadItems();
    fetchTransporters();
  }, []);

  const openEditRmaModal = (item) => {
    setSelectedItem(item);
    setNewRmaNo("");
    setEditRmaModalVisible(true);
  };

  const handleUpdateRma = async () => {
    if (!newRmaNo.trim()) {
      message.warning("Please enter RMA Number");
      return;
    }

    setUpdatingRma(true);
    const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);

    if (result.success) {
      message.success("RMA Number updated successfully!");
      setEditRmaModalVisible(false);
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
  const totalInTransitRma = Object.keys(groupedInTransit).length;
  const totalInTransitItems = inTransitItems.length;

  const openDispatchModal = async (rmaNo, rmaItems) => {
    const validItems = rmaItems.filter(item => item.itemRmaNo && item.itemRmaNo.trim() !== "");

    if (validItems.length === 0) {
      message.error("No items have an RMA Number. Cannot dispatch.");
      return;
    }

    if (validItems.length < rmaItems.length) {
      const excludedCount = rmaItems.length - validItems.length;
      message.warning(`${excludedCount} item(s) excluded due to missing RMA Number.`);
    }

    setSelectedRmaNo(rmaNo);
    setSelectedRmaItems(validItems);
    setSelectedDispatchItemIds(validItems.map((item) => item.id));
    dispatchForm.resetFields();

    let nextDcNo = "";
    try {
      const res = await RmaApi.getNextDcNo();
      if (res.success && res.data && res.data.dcNo) {
        nextDcNo = res.data.dcNo;
      }
    } catch (error) {
      console.error("Failed to fetch next DC No", error);
    }

    dispatchForm.setFieldsValue({
      dispatchDate: null,
      courierName: "",
      trackingNo: "",
      dcNo: nextDcNo || "",
      ewayBillNo: validItems[0]?.ewayBillNo || "",
      remarks: "Dispatch to Bangalore depot for repair.",
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
        rmaNo: selectedRmaNo,
        itemIds: selectedDispatchItemIds,
        dispatchDate: values.dispatchDate?.format("YYYY-MM-DD"),
        courierName: values.courierName,
        trackingNo: values.trackingNo,
        dcNo: values.dcNo,
        ewayBillNo: values.ewayBillNo,
        remarks: values.remarks,
      };

      const result = await RmaApi.dispatchToBangalore(payload);
      if (result.success) {
        message.success(
          `${selectedDispatchItemIds.length} item(s) dispatched to Bangalore`
        );
        setDispatchModalVisible(false);
        loadItems();
      } else {
        message.error(result.error || "Failed to dispatch");
      }
    } catch {
      // validation errors via antd
    } finally {
      setDispatchSubmitting(false);
    }
  };

  const handleTransporterChange = (val) => {
    const selectedValue = Array.isArray(val) ? val[val.length - 1] : val;
    const exists = transporters.find((t) => t.transporterName === selectedValue);

    if (!exists) {
      setIsNewTransporter(true);
      if (PREDEFINED_TRANSPORTERS[selectedValue]) {
        dcForm.setFieldsValue({
          transporterId: PREDEFINED_TRANSPORTERS[selectedValue],
        });
      } else {
        dcForm.setFieldsValue({ transporterId: "" });
      }
    } else {
      setIsNewTransporter(false);
      dcForm.setFieldsValue({
        transporterId: exists.transporterId,
      });
    }
  };

  const openDcModal = async (rmaNo, rmaItems, isCustomerDispatch = false, isReturn = false) => {
    let validItems = rmaItems.filter(item => item.itemRmaNo && item.itemRmaNo.trim() !== "");

    if (isReturn) {
      validItems = validItems.filter(item => item.depotStage === "AT_DEPOT_REPAIRED");
    }

    if (validItems.length === 0) {
      message.error("No eligible items (Repaired & with RMA No) found for dispatch.");
      return;
    }

    if (validItems.length < rmaItems.length) {
      const excludedCount = rmaItems.length - validItems.length;
      message.warning(`${excludedCount} item(s) excluded (missing RMA No or not Repaired).`);
    }

    setSelectedRmaNo(rmaNo);
    setIsReturnDispatch(isReturn);
    setIsCustomerDispatch(isCustomerDispatch);

    setDcTableData(
      validItems.map((item, index) => ({
        ...item,
        slNo: index + 1,
        qty: 1,
      }))
    );

    dcForm.resetFields();

    let nextDcNo = "";
    try {
      const res = await RmaApi.getNextDcNo();
      if (res.success && res.data && res.data.dcNo) {
        nextDcNo = res.data.dcNo;
      }
    } catch (error) {
      console.error("Failed to fetch next DC No", error);
    }

    if (isCustomerDispatch) {
      const customerName = validItems[0]?.customerName || "";
      dcForm.setFieldsValue({
        modeOfShipment: "ROAD",
        boxes: "1",
        consigneeName: customerName,
        consigneeAddress: "",
        gstIn: "",
        dcNo: nextDcNo,
      });
    } else {
      const consigneeName = isReturn ? "Motorola Solutions India Pvt Ltd (Gurgaon)" : "Motorola Solutions India Pvt Ltd";
      const consigneeAddress = isReturn ? "Gurgaon" : "Bangalore";

      dcForm.setFieldsValue({
        modeOfShipment: "ROAD",
        boxes: "1",
        consigneeName: consigneeName,
        consigneeAddress: consigneeAddress,
        gstIn: "29AAACM4363F1Z6",
        dcNo: nextDcNo,
      });
    }

    try {
      const itemsToFetch = rmaItems.map(i => ({ product: i.product, model: i.model }));
      const rateRes = await RmaApi.getProductRates(itemsToFetch);

      if (rateRes.success) {
        const ratesMap = rateRes.data;
        const itemFormValues = rmaItems.map(item => {
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
      const {
        consigneeName,
        consigneeAddress,
        gstIn,
        boxes,
        dimensions,
        weight,
        modeOfShipment,
        transporterName,
        transporterId,
        dcNo,
        items: formItems,
      } = values;

      const formattedItems = dcTableData.map((item, index) => ({
        slNo: index + 1,
        product: item.product,
        model: item.model,
        serialNo: item.serialNo,
        qty: 1,
        rate:
          formItems && formItems[index] && formItems[index].rate
            ? formItems[index].rate
            : 0,
        itemRmaNo: item.itemRmaNo,
      }));

      const itemIds = dcTableData.map(item => item.id);

      const payload = {
        rmaNo: selectedRmaNo,
        consigneeName,
        consigneeAddress,
        gstIn,
        boxes: parseInt(boxes, 10),
        dimensions,
        weight,
        modeOfShipment,
        transporterName: Array.isArray(transporterName)
          ? transporterName[transporterName.length - 1]
          : transporterName,
        transporterId,
        dcNo,
        items: formattedItems,
      };

      if (isReturnDispatch) {
        const returnPayload = {
          itemIds: itemIds,
          dcNo: dcNo,
          ewayBillNo: "",
          dispatchMode: modeOfShipment === "HAND_CARRY" ? "HAND" : "COURIER",
          courierName: payload.transporterName,
          trackingNo: "",
          dispatchTo: isCustomerDispatch ? "CUSTOMER" : "GURGAON",
          dispatchDate: values.dispatchDate ? values.dispatchDate.format("YYYY-MM-DD") : null,
          remarks: `Docket: ${dcNo}`,
        };

        const dispatchRes = await RmaApi.dispatchReturn(returnPayload);
        if (!dispatchRes.success) {
          message.error(dispatchRes.error || "Failed to update Dispatch Status");
          setDcSubmitting(false);
          return;
        }
        message.success("Items dispatched successfully!");

        setDcModalVisible(false);
        loadItems();
        setDcSubmitting(false);
        return;
      }

      const result = await RmaApi.generateDeliveryChallan(payload);
      if (result.success) {
        message.success("Delivery Challan Generated Successfully");
        setDcModalVisible(false);
        fetchTransporters();
      } else {
        message.error("Failed to Generate DC PDF");
      }
    } catch (error) {
      console.error("DC Gen Error:", error);
      message.error("An error occurred");
    } finally {
      if (!isReturnDispatch) {
        setDcSubmitting(false);
      }
    }
  };

  const handleMarkReceived = async (itemsToReceive, rmaNo = null) => {
    const itemsArray = Array.isArray(itemsToReceive)
      ? itemsToReceive
      : [itemsToReceive];
    const ids = itemsArray.map((i) => i.id);
    const groupKey = rmaNo ? `GROUP_${rmaNo}` : null;

    try {
      setReceivingIds((prev) => {
        const next = new Set(prev);
        if (groupKey) next.add(groupKey);
        ids.forEach((id) => next.add(id));
        return next;
      });

      const payload = {
        itemIds: ids,
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
      setReceivingIds((prev) => {
        const next = new Set(prev);
        if (groupKey) next.delete(groupKey);
        ids.forEach((id) => next.delete(id));
        return next;
      });
    }
  };

  // == GGN Workflow Handlers ==
  const handleMarkReceivedGgn = async (item) => {
    try {
      const res = await RmaApi.markDepotReceivedAtGurgaon({ itemIds: [item.id] });
      if (res.success) {
        message.success("Marked as Received at Gurgaon");
        loadItems();
      } else {
        message.error(res.error || "Failed to mark received");
      }
    } catch (e) {
      message.error("Error: " + e.message);
    }
  };

  const openGgnDispatchModal = (item) => {
    setSelectedItem(item);
    setGgnDispatchMode("HAND");
    setGgnCourierName("");
    setGgnTrackingNo("");
    setGgnHandlerName("");
    setGgnHandlerContact("");
    setGgnAddress(item.customerAddress || "");
    setGgnState(item.state || "");
    setGgnCity(item.city || "");
    setGgnValue("0");
    setGgnGst(item.gstin || "");
    setGgnTransporterId("");
    setGgnPlanModalVisible(true);
  };


  return (
    <RmaLayout>
      <div className="depot-page">
        <div className="unrepaired-header header-depot">
          <div className="header-content">
            <div className="header-title">
              <CarOutlined className="header-icon" />
              <div>
                <Title level={2} style={{ margin: 0 }}>
                  Depot Dispatch &amp; Receiving
                </Title>
                <Text type="secondary">
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
                label: `Ready to Dispatch (${items.length})`,
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
                        {Object.entries(groupedItems)
                          .sort(([rmaNoA, itemsA], [rmaNoB, itemsB]) => {
                            const isAllCompletedA = itemsA.every(i => i.depotStage === "GGN_DELIVERED_TO_CUSTOMER" || i.depotCycleClosed);
                            const isAllCompletedB = itemsB.every(i => i.depotStage === "GGN_DELIVERED_TO_CUSTOMER" || i.depotCycleClosed);
                            if (!isAllCompletedA && isAllCompletedB) return -1;
                            if (isAllCompletedA && !isAllCompletedB) return 1;
                            return 0;
                          })
                          .map(([rmaNo, rmaItems]) => {
                            return (
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
                                        Dispatch to Bangalore
                                      </Button>
                                    </div>
                                  </div>
                                }
                              >
                                {/* MODERN CARD GRID LAYOUT START */}
                                <div className="rma-items-grid">
                                  {rmaItems.map((item) => (
                                    <div key={item.id} className="rma-item-card-modern">
                                      {/* Header Strip */}
                                      <div className="item-header">
                                        <span className="item-product">{item.product || "Product"}</span>
                                        <Tag color="purple">Depot Repair</Tag>
                                      </div>

                                      {/* Details Grid */}
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
                                          <span className="label">RMA Number</span>
                                          {item.itemRmaNo ? <Tag color="purple">{item.itemRmaNo}</Tag> : <Text type="secondary" style={{ fontSize: '12px' }}>None</Text>}
                                        </div>
                                      </div>

                                      {/* Fault Box */}
                                      <div className="fault-box">
                                        <span className="label">Fault Description</span>
                                        <p className="fault-desc">{item.faultDescription}</p>
                                      </div>

                                      {/* Actions Footer */}
                                      <div className="item-footer">
                                        {!item.itemRmaNo && (
                                          <Button
                                            block
                                            icon={<EditOutlined />}
                                            onClick={() => openEditRmaModal(item)}
                                          >
                                            Add RMA No.
                                          </Button>
                                        )}
                                      </div>
                                    </div>
                                  ))}
                                </div>
                                {/* MODERN CARD GRID LAYOUT END */}
                              </Card>
                            );
                          })}
                      </div>
                    )}
                  </>
                ),
              },
              {
                key: "2",
                label: `In Transit / Dispatched (${totalInTransitItems})`,
                children: (
                  <>
                    {loading ? (
                      <div className="loading-container">
                        <Spin />
                      </div>
                    ) : totalInTransitRma === 0 ? (
                      <Empty
                        description="No items in transit"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                      />
                    ) : (
                      <div className="rma-groups">
                        {Object.entries(groupedInTransit)
                          .map(([rmaNo, rmaItems]) => {
                            const sortedItems = [...rmaItems];
                            return (
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
                                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '5px' }}>
                                        <Tag color="cyan" icon={<SendOutlined />}>Out for Delivery</Tag>
                                        {!rmaItems.every(i => i.depotStage === "GGN_DELIVERED_TO_CUSTOMER" || i.depotCycleClosed) && (
                                          <Button
                                            size="small"
                                            icon={<FilePdfOutlined />}
                                            onClick={() => openDcModal(rmaNo, rmaItems, true)}
                                          >
                                            Generate Customer DC
                                          </Button>
                                        )}
                                      </div>
                                    </div>
                                  </div>
                                }
                              >
                                <div className="fault-box" style={{ margin: '0 0 16px 0', display: 'flex', gap: '20px', alignItems: 'center' }}>
                                  <CarOutlined />
                                  <div>
                                    <Text type="secondary" style={{ fontSize: 12 }}>DC Number</Text>
                                    <div style={{ fontWeight: 600 }}>
                                      {rmaItems[0]?.depotStage === "IN_TRANSIT_FROM_DEPOT" || rmaItems[0]?.depotStage?.includes("GGN_")
                                        ? rmaItems[0]?.depotReturnDcNo || "N/A"
                                        : rmaItems[0]?.dcNo || "N/A"}
                                    </div>
                                  </div>
                                  <div>
                                    <Text type="secondary" style={{ fontSize: 12 }}>E-Way Bill</Text>
                                    <div style={{ fontWeight: 600 }}>
                                      {rmaItems[0]?.depotStage === "IN_TRANSIT_FROM_DEPOT" || rmaItems[0]?.depotStage?.includes("GGN_")
                                        ? rmaItems[0]?.depotReturnEwayBillNo || "N/A"
                                        : rmaItems[0]?.ewayBillNo || "N/A"}
                                    </div>
                                  </div>
                                  <div>
                                    {rmaItems[0]?.depotStage === "IN_TRANSIT_FROM_DEPOT" ? (
                                      <Tag color="orange">In Transit from Depot</Tag>
                                    ) : (
                                      <Tag color="processing">In Transit to Depot</Tag>
                                    )}
                                  </div>
                                </div>

                                {/* MODERN CARD GRID FOR TRANSIT ITEMS */}
                                <div className="rma-items-grid">
                                  {sortedItems.map((item) => (
                                    <div key={item.id} className="rma-item-card-modern">
                                      {/* Header */}
                                      <div className="item-header">
                                        <span className="item-product">{item.product}</span>
                                        {item.depotStage === "IN_TRANSIT_TO_DEPOT" && <Tag color="processing">In Transit</Tag>}
                                        {item.depotStage === "AT_DEPOT_RECEIVED" && <Tag color="success">Received at Depot</Tag>}
                                        {item.depotStage === "AT_DEPOT_REPAIRED" && (
                                          item.repairStatus?.includes("BER") ? <Tag color="red">BER</Tag> : <Tag color="orange">Repaired</Tag>
                                        )}
                                        {item.depotStage === "IN_TRANSIT_FROM_DEPOT" && <Tag color="geekblue">Returning</Tag>}
                                        {item.depotStage === "GGN_RECEIVED_FROM_DEPOT" && <Tag color="purple">At GGN</Tag>}
                                        {(item.depotStage === "GGN_DELIVERED_TO_CUSTOMER" || item.depotCycleClosed) && <Tag color="green">Completed</Tag>}
                                      </div>

                                      {/* Details */}
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
                                          <span className="label">RMA</span>
                                          <Tag color="purple">{item.itemRmaNo}</Tag>
                                        </div>
                                        {(item.depotReturnDispatchDate || item.dispatchedDate) && (
                                          <div className="detail-box">
                                            <span className="label"><CalendarOutlined /> Dispatch Date</span>
                                            <span className="value">
                                              {new Date(item.depotReturnDispatchDate || item.dispatchedDate).toLocaleDateString()}
                                            </span>
                                          </div>
                                        )}
                                        {item.depotReturnDeliveredDate && (
                                          <div className="detail-box">
                                            <span className="label"><CalendarOutlined /> Received Date</span>
                                            <span className="value">
                                              {new Date(item.depotReturnDeliveredDate).toLocaleDateString()}
                                            </span>
                                          </div>
                                        )}
                                      </div>

                                      {/* Fault/Remarks */}
                                      <div className="fault-box">
                                        <span className="label">Fault Description</span>
                                        <p className="fault-desc" style={{ marginBottom: '4px' }}>{item.faultDescription}</p>
                                        {item.repairRemarks && (
                                          <>
                                            <span className="label">Repair Note</span>
                                            <p className="fault-desc">{item.repairRemarks}</p>
                                          </>
                                        )}
                                      </div>

                                      {/* Action Footer */}
                                      <div className="item-footer" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>

                                        {/* Receive at Depot Button */}
                                        {item.depotStage === "IN_TRANSIT_TO_DEPOT" && (
                                          <Button type="primary" size="small" icon={<CheckCircleOutlined />}
                                            loading={receivingIds.has(item.id)}
                                            onClick={() => handleMarkReceived(item)}
                                          >
                                            Receive Item
                                          </Button>
                                        )}

                                        {/* Status Dropdown at Depot */}
                                        {item.depotStage === "AT_DEPOT_RECEIVED" && (
                                          <Dropdown
                                            menu={{
                                              items: [
                                                { label: "Repaired", key: "REPAIRED" },
                                                { label: "Replaced", key: "REPLACED" },
                                                { label: "BER", key: "BER" },
                                                { label: "Unrepaired", key: "UNREPAIRED" },
                                              ],
                                              onClick: async ({ key }) => {
                                                const payload = { itemIds: [item.id], repairStatus: key };
                                                const res = await RmaApi.markDepotRepaired(payload);
                                                if (res.success) { message.success("Status Updated"); loadItems(); }
                                                else { message.error(res.error || "Failed"); }
                                              }
                                            }}
                                          >
                                            <Button size="small" type="primary">
                                              Mark Status <DownOutlined />
                                            </Button>
                                          </Dropdown>
                                        )}

                                        {/* Dispatch Buttons (Depot -> GGN/Customer) */}
                                        {item.depotStage === "AT_DEPOT_REPAIRED" && (
                                          <div style={{ display: 'flex', gap: '5px' }}>
                                            <Button type="primary" size="small" style={{ flex: 1 }} onClick={() => openDcModal(rmaNo, [item], false, true)}>
                                              To Gurgaon
                                            </Button>
                                            <Button type="primary" size="small" style={{ flex: 1 }} onClick={() => openDcModal(rmaNo, [item], true, true)}>
                                              To Customer
                                            </Button>
                                          </div>
                                        )}

                                        {/* Receive at Gurgaon */}
                                        {item.depotStage === "IN_TRANSIT_FROM_DEPOT" && item.dispatchTo === "GURGAON" && (
                                          <Button size="small" type="primary" onClick={() => handleMarkReceivedGgn(item)}>
                                            Mark Received (GGN)
                                          </Button>
                                        )}

                                        {/* Dispatch GGN -> Customer */}
                                        {item.depotStage === "GGN_RECEIVED_FROM_DEPOT" && (
                                          <Button size="small" type="primary" onClick={() => openGgnDispatchModal(item)}>
                                            Dispatch to Customer
                                          </Button>
                                        )}

                                        {/* Upload Signed DC */}
                                        {(["GGN_DISPATCHED_TO_CUSTOMER_HAND", "GGN_DISPATCHED_TO_CUSTOMER_COURIER"].includes(item.depotStage) ||
                                          (item.depotStage === "IN_TRANSIT_FROM_DEPOT" && item.dispatchTo === "CUSTOMER")) && (
                                            <Button size="small" type="primary"
                                              onClick={() => {
                                                setSelectedItem(item);
                                                setGgnProofFileId(null);
                                                setGgnProofRemarks("");
                                                setGgnProofModalVisible(true);
                                              }}
                                            >
                                              Upload Signed DC
                                            </Button>
                                          )}

                                        {/* Create New RMA for BER/Faulty */}
                                        {item.depotStage === "GGN_RETURNED_FAULTY" && (
                                          <Button danger size="small" onClick={async () => {
                                            const res = await RmaApi.markDepotFaultyAndCreateNewRma(item.id);
                                            if (res.success) { message.success("New RMA Created"); loadItems(); }
                                            else { message.error("Failed"); }
                                          }}>
                                            Create New RMA
                                          </Button>
                                        )}
                                      </div>
                                    </div>
                                  ))}
                                </div>
                                {/* MODERN CARD GRID END */}
                              </Card>
                            );
                          })}
                      </div>
                    )}
                  </>
                ),
              },
            ]}
          />
        </div>

        {/* Dispatch to Bangalore Modal */}
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

            <Divider>Select Items to Dispatch</Divider>
            <Table
              size="small"
              rowKey="id"
              dataSource={selectedRmaItems}
              pagination={false}
              rowSelection={{
                selectedRowKeys: selectedDispatchItemIds,
                onChange: (keys) => setSelectedDispatchItemIds(keys),
              }}
              columns={[
                { title: "Serial No", dataIndex: "serialNo", key: "serialNo" },
                { title: "Product", dataIndex: "product", key: "product" },
                { title: "Model", dataIndex: "model", key: "model" },
                { title: "RMA No", dataIndex: "itemRmaNo", key: "itemRmaNo" },
              ]}
              style={{ marginBottom: 16 }}
            />
            <Space direction="vertical" style={{ width: "100%" }}>
              <Text type="secondary">
                Selected <strong>{selectedDispatchItemIds.length}</strong> of{" "}
                <strong>{selectedRmaItems.length}</strong> item(s) to dispatch
                to Bangalore.
              </Text>
            </Space>
          </Form>
        </Modal>

        {/* Delivery Challan / Return Dispatch Modal */}
        {/* Delivery Challan / Return Dispatch Modal */}
        <Modal
          title={
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              {isReturnDispatch ? <SendOutlined /> : <FilePdfOutlined />}
              <Title level={4} style={{ margin: 0 }}>{isReturnDispatch ? "Dispatch Details" : "Generate Delivery Challan"}</Title>
            </div>
          }
          open={dcModalVisible}
          onCancel={() => setDcModalVisible(false)}
          width={isReturnDispatch ? 600 : 1000}
          style={{ top: 20 }}
          className="dc-modal"
          footer={[
            <Button
              key="cancel"
              onClick={() => setDcModalVisible(false)}
            >
              Cancel
            </Button>,
            <Button
              key="generate"
              type="primary"
              icon={isReturnDispatch ? <SendOutlined /> : <FilePdfOutlined />}
              onClick={() => dcForm.submit()}
              loading={dcSubmitting}
              style={isReturnDispatch ? {} : { background: "#faad14", borderColor: "#faad14" }}
            >
              {isReturnDispatch ? "Dispatch" : "Generate DC"}
            </Button>,
          ]}
        >
          <Form
            form={dcForm}
            layout="vertical"
            onFinish={handleGenerateDC}
            initialValues={{
              modeOfShipment: "ROAD",
              boxes: "1",
            }}
          >
            {!isReturnDispatch && (
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                  <Card
                    title="Consignor Details"
                    size="small"
                  >
                    <p>
                      <strong>Motorola Solutions India</strong>
                    </p>
                    <p>A, Building 8, DLF</p>
                    <p>Gurgaon, Haryana, India</p>
                  </Card>
                </Col>
                <Col xs={24} md={12}>
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
                    <Form.Item name="dcNo" label="Delivery Challan No.">
                      <Input placeholder="Auto-generated" />
                    </Form.Item>
                  </Card>
                </Col>
              </Row>
            )}

            {/* Courier Details - Visible for BOTH */}
            <Divider orientation="left">Shipment Details</Divider>
            {isReturnDispatch && (
              <div style={{ marginBottom: 16 }}>
                <Alert message="Enter details for return dispatch. No DC PDF will be generated." type="info" showIcon />
              </div>
            )}

            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Form.Item
                  name="transporterName"
                  label={isReturnDispatch ? "Name" : "Courier Name"}
                  rules={[{ required: true, message: "Required" }]}
                >
                  <Select
                    mode="tags"
                    style={{ width: "100%" }}
                    placeholder="Select or Type"
                    onChange={handleTransporterChange}
                    options={[
                      ...transporters.map((t) => ({
                        label: t.transporterName,
                        value: t.transporterName,
                      })),
                      { label: "Blue Dart", value: "Blue Dart" },
                      { label: "Safe Express", value: "Safe Express" },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="transporterId" label="Transporter ID">
                  <Input placeholder="Auto-filled ID" />
                </Form.Item>
              </Col>

              {isReturnDispatch && (
                <>
                  <Col xs={24} md={12}>
                    <Form.Item
                      name="dcNo"
                      label="Docket NO"
                      rules={[{ required: true, message: "Required" }]}
                    >
                      <Input placeholder="Enter Docket Number" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={12}>
                    <Form.Item
                      name="dispatchDate"
                      label="Date of Dispatch"
                      rules={[{ required: true, message: "Required" }]}
                    >
                      <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
                    </Form.Item>
                  </Col>
                </>
              )}

              {!isReturnDispatch && (
                <Col xs={24} md={12}>
                  <Form.Item
                    name="modeOfShipment"
                    label="Mode of Shipment"
                    rules={[{ required: true }]}
                  >
                    <Select>
                      <Select.Option value="ROAD">Road</Select.Option>
                      <Select.Option value="AIR">Air</Select.Option>
                      <Select.Option value="TRAIN">Train</Select.Option>
                      <Select.Option value="HAND_CARRY">Hand Carry</Select.Option>
                    </Select>
                  </Form.Item>
                </Col>
              )}
            </Row>

            {/* Box Details - Only for !isReturnDispatch */}
            {!isReturnDispatch && (
              <Row gutter={[16, 16]}>
                <Col xs={24} md={8}>
                  <Form.Item
                    name="boxes"
                    label="No of Boxes"
                    rules={[{ required: true }]}
                  >
                    <Input type="number" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    name="dimensions"
                    label="Dimensions"
                  >
                    <Input placeholder="e.g. 10x10x10" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    name="weight"
                    label="Weight (kg)"
                  >
                    <Input placeholder="e.g. 5kg" />
                  </Form.Item>
                </Col>
              </Row>
            )}

            {!isReturnDispatch && (
              <>
                <Divider orientation="left">Item Details</Divider>
                <Table
                  dataSource={dcTableData}
                  pagination={false}
                  size="small"
                  scroll={{ x: 'max-content' }}
                  columns={[
                    { title: "Sr No", dataIndex: "slNo", key: "slNo", width: 60 },
                    { title: "Material Code", dataIndex: "serialNo", key: "serialNo" },
                    {
                      title: "Description",
                      key: "product",
                      render: (_, record) =>
                        `${record.product || ""}${record.model ? " - " + record.model : ""
                        }`,
                    },
                    {
                      title: "Qty",
                      dataIndex: "qty",
                      key: "qty",
                      render: () => 1,
                    },
                    {
                      title: "Rate (Value)",
                      key: "rate",
                      render: (_, record, index) => (
                        <Form.Item
                          name={["items", index, "rate"]}
                          rules={[{ required: true, message: "Required" }]}
                          style={{ margin: 0 }}
                        >
                          <Input prefix="₹" type="number" placeholder="Value" />
                        </Form.Item>
                      ),
                    },
                  ]}
                />
              </>
            )}
          </Form>
        </Modal>


        {/* Add RMA Modal */}
        <Modal
          title="Add RMA Number"
          open={editRmaModalVisible}
          onCancel={() => setEditRmaModalVisible(false)}
          footer={[
            <Button
              key="cancel"
              onClick={() => setEditRmaModalVisible(false)}
            >
              Cancel
            </Button>,
            <Button
              key="add"
              type="primary"
              onClick={handleUpdateRma}
              loading={updatingRma}
              style={{
                backgroundColor: "#1890ff",
                borderColor: "#1890ff",
              }}
            >
              Add
            </Button>,
          ]}
        >
          <div style={{ padding: "20px 0" }}>
            <Text
              strong
              style={{ display: "block", marginBottom: 8 }}
            >
              RMA Number:
            </Text>
            <Input
              placeholder="Enter RMA Number"
              value={newRmaNo}
              onChange={(e) => setNewRmaNo(e.target.value)}
              size="large"
            />
          </div>
        </Modal>

        {/* NEW: Plan dispatch from Gurgaon to customer */}
        <Modal
          title="Plan Dispatch from Gurgaon to Customer"
          open={ggnPlanModalVisible}
          onCancel={() => setGgnPlanModalVisible(false)}
          confirmLoading={ggnSubmitting}
          onOk={async () => {
            if (!selectedItem) return;
            setGgnSubmitting(true);
            const payload = {
              itemIds: [selectedItem.id],
              dispatchMode: ggnDispatchMode,
              courierName: ggnCourierName,
              trackingNo: ggnTrackingNo,
              handlerName: ggnHandlerName,
              handlerContact: ggnHandlerContact,
            };
            const res = await RmaApi.planDispatchFromGurgaon(payload);
            setGgnSubmitting(false);
            if (res.success) {
              message.success(res.message || "Dispatch planned");
              setGgnPlanModalVisible(false);
              loadItems();

              // Generate DC PDF for GGN -> Customer dispatch
              try {
                message.loading("Generating Delivery Challan...", 1.5);
                await RmaApi.generateDeliveryChallan({
                  rmaNo: selectedItem.rmaNo,
                  transporterName: ggnDispatchMode === "COURIER" ? ggnCourierName : ggnHandlerName,
                  modeOfShipment: ggnDispatchMode === "COURIER" ? "ROAD" : "HAND_CARRY",
                  dcNo: "TEMP-GGN",
                  consigneeName: selectedItem.customerName || "Customer",
                  address: ggnAddress || selectedItem.address || "Customer Address",
                  gstin: ggnGst,
                  state: ggnState,
                  stateCode: "", // Add logic if needed
                  contactPerson: "",
                  contactNumber: "",
                  transporterId: ggnTransporterId,
                  dispatchDate: null,
                  boxes: 1,
                  dimensions: "",
                  weight: "",
                  items: [{
                    slNo: 1,
                    serialNo: selectedItem.serialNo,
                    product: selectedItem.product,
                    model: selectedItem.model,
                    qty: 1,
                    rate: ggnValue || 0
                  }]
                });
              } catch (e) {
                console.error("Failed to generate DC PDF", e);
                message.warning("Dispatch saved, but failed to generate DC PDF");
              }

            } else {
              message.error(
                res.error || "Failed to plan dispatch from Gurgaon"
              );
            }
          }}
        >
          <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Alert message="Please fill all details for Delivery Challan generation." type="info" showIcon />

            <div>
              <Text strong>Delivery Method</Text>
              <Radio.Group
                value={ggnDispatchMode}
                onChange={(e) => setGgnDispatchMode(e.target.value)}
                style={{ marginTop: 4 }}
              >
                <Radio value="HAND">By Hand</Radio>
                <Radio value="COURIER">By Courier</Radio>
              </Radio.Group>
            </div>

            <Divider orientation="left">DC Details</Divider>

            <Row gutter={16}>
              <Col span={24}>
                <Text strong>Address</Text>
                <Input.TextArea rows={2} value={ggnAddress} onChange={(e) => setGgnAddress(e.target.value)} placeholder="Full Customer Address" />
              </Col>
            </Row>
            <Row gutter={16} style={{ marginTop: 8 }}>
              <Col span={12}>
                <Text strong>State</Text>
                <Input value={ggnState} onChange={(e) => setGgnState(e.target.value)} placeholder="State" />
              </Col>
              <Col span={12}>
                <Text strong>GSTIN</Text>
                <Input value={ggnGst} onChange={(e) => setGgnGst(e.target.value)} placeholder="GST Number" />
              </Col>
            </Row>
            <Row gutter={16} style={{ marginTop: 8 }}>
              <Col span={12}>
                <Text strong>Value (Rate)</Text>
                <Input value={ggnValue} onChange={(e) => setGgnValue(e.target.value)} placeholder="Declared Value" />
              </Col>
            </Row>

            <Divider orientation="left">Transporter / Handler</Divider>

            {ggnDispatchMode === "COURIER" && (
              <>
                <Row gutter={16}>
                  <Col span={12}>
                    <Text strong>Courier Name</Text>
                    <Select
                      style={{ width: "100%" }}
                      placeholder="Select or Type"
                      value={ggnCourierName}
                      onChange={(val) => {
                        setGgnCourierName(val);
                        const selectedT = transporters.find(t => t.transporterName === val);
                        if (selectedT) {
                          setGgnTransporterId(selectedT.transporterId);
                        } else {
                          setGgnTransporterId("");
                        }
                      }}
                      options={[
                        ...transporters.map((t) => ({
                          label: t.transporterName,
                          value: t.transporterName,
                        })),
                        { label: "Blue Dart", value: "Blue Dart" },
                        { label: "Safe Express", value: "Safe Express" },
                      ]}
                    />
                  </Col>
                  <Col span={12}>
                    <Text strong>Transporter ID</Text>
                    <Input value={ggnTransporterId} onChange={(e) => setGgnTransporterId(e.target.value)} placeholder="Transporter ID" />
                  </Col>
                </Row>
                <div style={{ marginTop: 8 }}>
                  <Text strong>AWB / Tracking No.</Text>
                  <Input
                    value={ggnTrackingNo}
                    onChange={(e) => setGgnTrackingNo(e.target.value)}
                  />
                </div>
              </>
            )}

            {ggnDispatchMode === "HAND" && (
              <>
                <div>
                  <Text strong>Handled By (Name)</Text>
                  <Input
                    value={ggnHandlerName}
                    onChange={(e) => setGgnHandlerName(e.target.value)}
                  />
                </div>
                <div>
                  <Text strong>Contact No.</Text>
                  <Input
                    value={ggnHandlerContact}
                    onChange={(e) => setGgnHandlerContact(e.target.value)}
                  />
                </div>
              </>
            )}
          </Space>
        </Modal>

        {/* NEW: Upload signed DC / proof of delivery */}
        <Modal
          title="Upload Signed DC (Proof of Delivery)"
          open={ggnProofModalVisible}
          onCancel={() => setGgnProofModalVisible(false)}
          confirmLoading={ggnSubmitting}
          onOk={async () => {
            if (!selectedItem) return;
            if (!ggnProofFileId) {
              message.warning("Please upload the signed DC first");
              return;
            }
            setGgnSubmitting(true);
            const res = await RmaApi.uploadDepotProofOfDelivery(
              selectedItem.id,
              ggnProofFileId,
              ggnProofRemarks
            );
            setGgnSubmitting(false);
            if (res.success) {
              message.success(
                res.message || "Proof uploaded; depot cycle closed"
              );
              setGgnProofModalVisible(false);
              loadItems();
            } else {
              message.error(
                res.error || "Failed to upload proof of delivery"
              );
            }
          }}
        >
          <Upload
            name="file"
            accept="image/*,application/pdf"
            action={`${URL}/files/upload`} // adjust if different
            headers={{
              Authorization:
                "Bearer " +
                (Cookies.get("authToken")
                  ? atob(Cookies.get("authToken"))
                  : ""),
            }}
            listType="picture"
            maxCount={1}
            onChange={(info) => {
              if (info.file.status === "done") {
                const id =
                  info.file.response?.fileId || info.file.response?.id;
                setGgnProofFileId(id);
                message.success("File uploaded");
              } else if (info.file.status === "error") {
                message.error("Upload failed");
              }
            }}
          >
            <Button>Click to Upload Signed DC</Button>
          </Upload>

          <div style={{ marginTop: 12 }}>
            <Text strong>Remarks</Text>
            <Input.TextArea
              rows={3}
              value={ggnProofRemarks}
              onChange={(e) => setGgnProofRemarks(e.target.value)}
            />
          </div>
        </Modal>
      </div>
    </RmaLayout>
  );
}