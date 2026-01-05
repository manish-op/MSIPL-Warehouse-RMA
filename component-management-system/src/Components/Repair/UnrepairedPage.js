import React, { useState, useEffect } from "react";
import Cookies from "js-cookie";
import {
    Typography,
    Tag,
    message,
    Button,
    Modal,
    Input,
    Spin,
    Card,
    Row,
    Col,
    Badge,
    Empty,
    Divider,
    Table,
    Form,
    Select,
    Collapse,
    Tooltip,
} from "antd";
import {
    UserAddOutlined,
    ToolOutlined,
    ReloadOutlined,
    EditOutlined,
    FileTextOutlined,
    PrinterOutlined,
    EyeOutlined,
    DownloadOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA"; // Ensure this path is correct
import { URL as API_URL } from "../API/URL"; // Ensure this path is correct
import RmaLayout from "../RMA/RmaLayout"; // Ensure this path is correct
import "./UnrepairedPage.css";

import companyLogo from "../../images/image.png";

const { Title, Text, Paragraph } = Typography;
const { Panel } = Collapse;

const PREDEFINED_TRANSPORTERS = {
    "Blue Dart Express": "27AAACB0446L1ZS",
    "Safe Express": "27AAECS4363H2Z7"
};

export default function UnrepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);

    // --- Assign Modal State ---
    const [assignModalVisible, setAssignModalVisible] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [assigneeName, setAssigneeName] = useState("");
    const [assigneeEmail, setAssigneeEmail] = useState("");
    const [assigning, setAssigning] = useState(false);

    // --- User/Role State ---
    const [currentUser, setCurrentUser] = useState(null);
    const isAdmin = currentUser?.role?.toLowerCase() === "admin" || currentUser?.roleName?.toLowerCase() === "admin";
    const [employees, setEmployees] = useState([]);

    // --- Bulk Assign State ---
    const [bulkAssignModalVisible, setBulkAssignModalVisible] = useState(false);
    const [selectedRmaNo, setSelectedRmaNo] = useState(null);
    const [selectedRmaItemCount, setSelectedRmaItemCount] = useState(0);
    const [bulkAssignItems, setBulkAssignItems] = useState([]);

    // --- Edit RMA State ---
    const [editRmaModalVisible, setEditRmaModalVisible] = useState(false);
    const [newRmaNo, setNewRmaNo] = useState("");
    const [updatingRma, setUpdatingRma] = useState(false);

    // --- Gatepass State ---
    const [generatingGatepass, setGeneratingGatepass] = useState(null);
    const [gatepassPreviewVisible, setGatepassPreviewVisible] = useState(false);
    const [gatepassItems, setGatepassItems] = useState([]);
    const [gatepassRmaNo, setGatepassRmaNo] = useState("");

    // --- Sticker State ---
    const [stickerModalVisible, setStickerModalVisible] = useState(false);
    const [stickerItems, setStickerItems] = useState([]);

    // --- Delivery Challan (DC) State ---
    const [dcModalVisible, setDcModalVisible] = useState(false);
    const [dcForm] = Form.useForm();
    const [dcSubmitting, setDcSubmitting] = useState(false);
    const [selectedDcRmaNo, setSelectedDcRmaNo] = useState("");
    const [dcTableData, setDcTableData] = useState([]);
    const [transporters, setTransporters] = useState([]);
    const [isNewTransporter, setIsNewTransporter] = useState(false);

    // --- Load Data ---
    const fetchTransporters = async () => {
        try {
            const result = await RmaApi.getAllTransporters();
            if (result.success && Array.isArray(result.data)) {
                setTransporters(result.data);
            } else {
                console.error("Failed to fetch transporters:", result.error);
            }
        } catch (error) {
            console.error("Error fetching transporters:", error);
        }
    };

    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getUnassignedItems();
        if (result.success) {
            // Filter out Depot Repair items
            const filteredItems = (result.data || []).filter(item => item.repairType !== 'Depot Repair');
            setItems(filteredItems);
        } else {
            message.error("Failed to load unassigned items");
        }
        setLoading(false);
    };

    const loadEmployees = async () => {
        try {
            const result = await RmaApi.getAllUsers();
            let allEmployees = [];

            if (result.success) {
                allEmployees = result.data || [];
                setEmployees(allEmployees);
            }

            // Decode token safely to find current user
            const encodedToken = Cookies.get("authToken");
            let userEmail = null;
            if (encodedToken) {
                try {
                    const tokenParts = atob(encodedToken).split('.');
                    if (tokenParts.length > 1) {
                        const payload = JSON.parse(atob(tokenParts[1]));
                        userEmail = payload.sub || payload.email;
                    }
                } catch (e) { console.error("Token decode error", e); }
            }

            if (userEmail && allEmployees.length > 0) {
                const currentEmp = allEmployees.find(e => e.email?.toLowerCase() === userEmail?.toLowerCase());
                if (currentEmp) setCurrentUser(currentEmp);
            }
        } catch (error) { console.error("Employee load error", error); }
    };

    useEffect(() => {
        loadItems();
        loadEmployees();
        fetchTransporters();
    }, []);

    // --- Group Items ---
    const groupedItems = items.reduce((acc, item) => {
        const rmaNo = item.rmaNo || "Unknown";
        if (!acc[rmaNo]) acc[rmaNo] = [];
        acc[rmaNo].push(item);
        return acc;
    }, {});

    const totalRmaRequests = Object.keys(groupedItems).length;
    const totalItems = items.length;

    // --- Helper Logic ---
    const checkRmaStatus = (rmaItems) => {
        const itemsWithRma = rmaItems.filter(item => item.itemRmaNo && item.itemRmaNo.trim() !== "");
        return {
            itemsWithRma,
            hasAnyRma: itemsWithRma.length > 0,
            allHaveRma: itemsWithRma.length === rmaItems.length
        };
    };

    // --- Modals Handlers ---

    // 1. Assign Single
    const openAssignModal = (item) => {
        if ((!item.itemRmaNo || item.itemRmaNo === "") && (!item.rmaNo || item.rmaNo === "Unknown")) {
            message.warning("RMA Number is required before assigning.");
            return;
        }
        setSelectedItem(item);
        if (!isAdmin && currentUser) {
            setAssigneeName(currentUser.name);
            setAssigneeEmail(currentUser.email);
        } else {
            setAssigneeName("");
            setAssigneeEmail("");
        }
        setAssignModalVisible(true);
    };

    const handleAssign = async () => {
        if (!assigneeName.trim() || !assigneeEmail.trim()) {
            message.warning("Please select a technician");
            return;
        }
        setAssigning(true);
        const result = await RmaApi.assignItem(selectedItem.id, assigneeEmail, assigneeName);
        if (result.success) {
            message.success("Item assigned successfully!");
            setAssignModalVisible(false);
            loadItems();
        } else {
            message.error(result.error || "Failed to assign item");
        }
        setAssigning(false);
    };

    // 2. Bulk Assign
    const openBulkAssignModal = (rmaNo, rmaItems) => {
        const status = checkRmaStatus(rmaItems);
        if (!status.hasAnyRma) {
            Modal.warning({ title: 'Missing RMA Numbers', content: 'Please assign RMA numbers first.' });
            return;
        }

        const setupModal = (validItems) => {
            setBulkAssignItems(validItems);
            setSelectedRmaNo(rmaNo);
            setSelectedRmaItemCount(validItems.length);
            if (!isAdmin && currentUser) {
                setAssigneeName(currentUser.name);
                setAssigneeEmail(currentUser.email);
            } else {
                setAssigneeName("");
                setAssigneeEmail("");
            }
            setBulkAssignModalVisible(true);
        };

        if (!status.allHaveRma) {
            Modal.confirm({
                title: 'Partial Assignment',
                content: `Only ${status.itemsWithRma.length} items have valid RMA numbers. Proceed?`,
                onOk: () => setupModal(status.itemsWithRma)
            });
        } else {
            setupModal(status.itemsWithRma);
        }
    };

    const handleBulkAssign = async () => {
        if (!assigneeName.trim()) { message.warning("Technician required"); return; }
        setAssigning(true);
        try {
            const promises = bulkAssignItems.map(item => RmaApi.assignItem(item.id, assigneeEmail, assigneeName));
            await Promise.all(promises);
            message.success(`Assigned ${bulkAssignItems.length} items.`);
            setBulkAssignModalVisible(false);
            loadItems();
        } catch (e) { message.error("Bulk assign failed"); }
        setAssigning(false);
    };

    // 3. Edit RMA
    const openEditRmaModal = (item) => {
        setSelectedItem(item);
        setNewRmaNo("");
        setEditRmaModalVisible(true);
    };

    const handleUpdateRma = async () => {
        if (!newRmaNo.trim()) return;
        setUpdatingRma(true);
        const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);
        if (result.success) {
            message.success("Updated!");
            setEditRmaModalVisible(false);
            loadItems();
        } else {
            message.error("Update failed");
        }
        setUpdatingRma(false);
    };

    // 4. Gatepass
    const openGatepassPreview = (rmaItems, rmaNo) => {
        const status = checkRmaStatus(rmaItems);
        if (!status.hasAnyRma) { message.warning("RMA Numbers required"); return; }
        setGatepassItems(status.itemsWithRma);
        setGatepassRmaNo(rmaNo);
        setGatepassPreviewVisible(true);
    };

    const handleGenerateGatepass = async (requestNumber) => {
        setGeneratingGatepass(requestNumber);
        const result = await RmaApi.generateInwardGatepass(requestNumber);
        if (result.success && result.blob) {
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `Gatepass_${requestNumber}.pdf`;
            link.click();
            window.URL.revokeObjectURL(url);
            message.success('Downloaded!');
        } else {
            message.error('Generation failed');
        }
        setGeneratingGatepass(null);
    };

    // 5. Delivery Challan (Fixed)
    const openDcModal = async (rmaItems, rmaNo) => {
        const status = checkRmaStatus(rmaItems);
        if (!status.hasAnyRma) {
            message.warning("RMA Number is required to generate a Delivery Challan.");
            return;
        }

        // Map items for DC Table
        const tableData = status.itemsWithRma.map((item, index) => ({
            key: item.id || index,
            slNo: index + 1,
            serialNo: item.serialNo, // Maps to 'Material Code'
            product: item.product,
            model: item.model,
            qty: 1,
            rate: item.value || 0,
            itemRmaNo: item.itemRmaNo
        }));

        setSelectedDcRmaNo(rmaNo);
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

        dcForm.setFieldsValue({
            modeOfShipment: "ROAD",
            boxes: "1",
            consigneeName: rmaItems[0]?.companyName || "",
            consigneeAddress: rmaItems[0]?.returnAddress || "",
            transporterName: [],
            dcNo: nextDcNo
        });
        setDcModalVisible(true);
    };

    const handleGenerateDC = async (values) => {
        setDcSubmitting(true);
        try {
            // Save Transporter Logic
            if (isNewTransporter && values.transporterName && values.transporterId) {
                const name = Array.isArray(values.transporterName) ? values.transporterName[values.transporterName.length - 1] : values.transporterName;
                const result = await RmaApi.createTransporter({ name, transporterId: values.transporterId });
                if (!result.success) {
                    message.error("Failed to save transporter: " + result.error);
                }
                await fetchTransporters();
                values.transporterName = name;
            } else if (Array.isArray(values.transporterName)) {
                values.transporterName = values.transporterName[0];
            }

            // Merge Items with Form Data
            const formattedItems = dcTableData.map((item, index) => ({
                ...item,
                rate: values.items && values.items[index]?.rate ? values.items[index].rate : item.rate
            }));

            // Commented out DC Generation as per request
            /*
            await RmaApi.generateDeliveryChallan({
                rmaNo: selectedDcRmaNo,
                ...values,
                items: formattedItems
            });
            message.success("Delivery Challan generated!");
            setDcModalVisible(false);
            */

        } catch (error) {
            console.error(error);
            message.error("Failed to generate DC");
        }
        setDcSubmitting(false);
    };

    // 6. Stickers
    const openStickerModal = (rmaItems, rmaNo) => {
        const status = checkRmaStatus(rmaItems);
        if (!status.hasAnyRma) { message.warning("RMA Numbers required"); return; }
        setStickerItems(status.itemsWithRma.map(i => ({ ...i, displayRmaNo: i.itemRmaNo || rmaNo })));
        setStickerModalVisible(true);
    };

    const handlePrintStickers = async () => {
        const printWindow = window.open('', '_blank');

        // Convert image to Base64 to ensure it shows up in the print window (which might be about:blank)
        const getBase64FromUrl = async (url) => {
            const data = await fetch(url);
            const blob = await data.blob();
            return new Promise((resolve) => {
                const reader = new FileReader();
                reader.readAsDataURL(blob);
                reader.onloadend = () => {
                    const base64data = reader.result;
                    resolve(base64data);
                }
            });
        }

        let logoSrc = "";
        try {
            logoSrc = await getBase64FromUrl(companyLogo);
        } catch (e) {
            console.error("Could not load logo", e);
            logoSrc = companyLogo; // Fallback
        }

        const stickersHtml = `
            <div class="sticker-container">
                ${stickerItems.map(item => `
                    <div class="sticker">
                        <div class="header">
                            <img src="${logoSrc}" alt="Logo" class="logo"/>
                            <span class="company-name">MOTOROLA</span>
                        </div>
                        <div class="content">
                            <div class="row">
                                <span class="label">Serial No:</span> 
                                <span class="value bold">${item.serialNo || 'N/A'}</span>
                            </div>
                            <div class="row">
                                <span class="label">Model:</span> 
                                <span class="value">${item.model || 'N/A'}</span>
                            </div>
                            <div class="row">
                                <span class="label">Fault:</span> 
                                <span class="value">${item.faultDescription ? item.faultDescription.substring(0, 50) + (item.faultDescription.length > 50 ? '...' : '') : 'N/A'}</span>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
         `;

        printWindow.document.write(`
            <html>
                <head>
                    <title>Print Stickers</title>
                    <style>
                        @media print {
                            @page {
                                size: A4 portrait;
                                margin: 10mm;
                            }
                            body { margin: 0; }
                        }
                        body {
                            font-family: 'Arial', sans-serif;
                            margin: 0;
                            padding: 0;
                        }
                        .sticker-container {
                            display: flex;
                            flex-wrap: wrap;
                            justify-content: flex-start;
                            gap: 4mm;
                        }
                        .sticker {
                            width: 90mm; /* Approx half of A4 width minus margins */
                            height: 48mm;
                            border: 3px double #000;
                            box-sizing: border-box;
                            padding: 5px;
                            /* margin-bottom: 5px; Removed to rely on gap */
                            display: flex;
                            flex-direction: column;
                            position: relative;
                            page-break-inside: avoid;
                        }
                        .header {
                            display: flex;
                            align-items: center;
                            border-bottom: 2px solid #000;
                            padding-bottom: 5px;
                            margin-bottom: 5px;
                        }
                        .logo {
                            height: 30px;
                            width: 30px;
                            margin-right: 10px;
                        }
                        .company-name {
                            font-weight: 800;
                            font-size: 18px;
                            letter-spacing: 1px;
                            font-style: italic;
                        }       
                        .content {
                            flex-grow: 1;
                            display: flex;
                            flex-direction: column;
                            gap: 4px;
                        }
                        .row {
                            display: flex;
                            align-items: baseline;
                            font-size: 12px;
                        }
                        .label {
                            width: 60px;
                            font-weight: 600;
                            color: #333;
                            flex-shrink: 0;
                        }
                        .value {
                            flex-grow: 1;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }
                        .value.bold {
                            font-weight: bold;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    ${stickersHtml}
                    <script>
                        window.onload = function() {
                            window.print();
                        }
                    </script>
                </body>
            </html>
         `);
        printWindow.document.close();
    };

    return (
        <RmaLayout>
            <div className="unrepaired-page">
                {/* Header */}
                <div className="unrepaired-header header-unrepaired">
                    <div className="header-content">
                        <div className="header-title">
                            <ToolOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0 }}>Unrepaired Items</Title>
                                <Text type="secondary">Assign technicians and manage documentation</Text>
                            </div>
                        </div>
                        <Button icon={<ReloadOutlined />} onClick={loadItems} loading={loading} className="refresh-btn">
                            Refresh
                        </Button>
                    </div>
                </div>

                {/* Content */}
                {/* Content */}
                <div className="unrepaired-content">
                    {loading ? (
                        <div className="loading-container"><Spin size="large" /></div>
                    ) : totalRmaRequests === 0 ? (
                        <Empty description="No unassigned items found" />
                    ) : (
                        <Collapse
                            className="rma-collapse"
                            defaultActiveKey={[]}
                            expandIconPosition="end"
                            ghost
                        >
                            {Object.entries(groupedItems).map(([rmaNo, rmaItems]) => {
                                // Extract Group Details
                                const firstItem = rmaItems[0];
                                const createdDate = firstItem.receivedDate ? new Date(firstItem.receivedDate).toLocaleDateString() : "N/A";
                                const tatDays = firstItem.tat || "N/A";
                                const itemCount = rmaItems.length;
                                const isDepot = firstItem.repairType === "Depot Repair";

                                // Header Component
                                const headerContent = (
                                    <div className="rma-collapse-header">
                                        <div className="header-main-info">
                                            <div className="info-block">
                                                <Title level={5} style={{ margin: 0, color: 'var(--primary-color)' }}>{rmaNo !== "Unknown" ? rmaNo : "No RMA Number"}</Title>
                                                <Text type="secondary" style={{ fontSize: '12px' }}>
                                                    {firstItem.userName || "Unknown User"}
                                                </Text>
                                            </div>

                                            <Divider type="vertical" />

                                            <div className="info-block">
                                                <Text type="secondary" style={{ fontSize: '11px' }}>Created Date</Text>
                                                <Text strong>{createdDate}</Text>
                                            </div>

                                            <Divider type="vertical" />

                                            <div className="info-block">
                                                <Text type="secondary" style={{ fontSize: '11px' }}>TAT</Text>
                                                <Tag color={tatDays !== "N/A" ? "blue" : "default"}>
                                                    {tatDays !== "N/A" ? `${tatDays} Days` : "N/A"}
                                                </Tag>
                                            </div>

                                            <Divider type="vertical" />

                                            <div className="info-block">
                                                <Text type="secondary" style={{ fontSize: '11px' }}>Items</Text>
                                                <Badge count={itemCount} />
                                            </div>

                                            <Divider type="vertical" />

                                            <Tag color={firstItem.repairType === "Local Repair" ? "purple" : "orange"}>
                                                {firstItem.repairType || "Standard"}
                                            </Tag>
                                        </div>

                                        <div className="header-actions" onClick={e => e.stopPropagation()}>
                                            {!isDepot && (
                                                <>
                                                    <Tooltip title="Preview Gatepass">
                                                        <Button
                                                            type="text"
                                                            icon={<FileTextOutlined />}
                                                            onClick={() => openGatepassPreview(rmaItems, rmaNo)}
                                                        />
                                                    </Tooltip>
                                                    <Tooltip title="Print Stickers">
                                                        <Button
                                                            type="text"
                                                            icon={<PrinterOutlined />}
                                                            onClick={() => openStickerModal(rmaItems, rmaNo)}
                                                        />
                                                    </Tooltip>
                                                </>
                                            )}
                                            <Button
                                                type="primary"
                                                ghost
                                                size="small"
                                                icon={<UserAddOutlined />}
                                                onClick={() => openBulkAssignModal(firstItem.itemRmaNo || rmaNo, rmaItems)}
                                            >
                                                Assign All
                                            </Button>
                                        </div>
                                    </div>
                                );

                                // Header Component with Responsive Design
                                const headerContentResponsive = (
                                    <div className="rma-collapse-header" style={{ width: '100%', padding: '4px 0' }}>
                                        <Row gutter={[16, 16]} align="middle" style={{ width: '100%' }}>
                                            {/* Column 1: RMA Identity */}
                                            <Col xs={24} sm={12} md={7} lg={6} xl={5}>
                                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                        <Title level={5} style={{ margin: 0, color: 'var(--primary-color)' }}>
                                                            {rmaNo !== "Unknown" ? rmaNo : "No RMA #"}
                                                        </Title>
                                                        {isDepot && <Tag color="orange">Depot</Tag>}
                                                    </div>
                                                    <Text type="secondary" style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                                        <span role="img" aria-label="user">👤</span> {firstItem.userName || "Unknown"}
                                                    </Text>
                                                </div>
                                            </Col>

                                            {/* Column 2: Key Stats (Date, TAT) */}
                                            <Col xs={12} sm={12} md={5} lg={4} xl={4}>
                                                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                                    <Text type="secondary" style={{ fontSize: '11px' }}>Created Date</Text>
                                                    <Text strong style={{ fontSize: '13px' }}>{createdDate}</Text>
                                                </div>
                                            </Col>

                                            <Col xs={12} sm={8} md={3} lg={3} xl={2}>
                                                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                                    <Text type="secondary" style={{ fontSize: '11px' }}>TAT</Text>
                                                    <Tag color={tatDays !== "N/A" ? (tatDays > 7 ? "red" : "blue") : "default"} style={{ margin: 0, width: 'fit-content' }}>
                                                        {tatDays !== "N/A" ? `${tatDays} Days` : "N/A"}
                                                    </Tag>
                                                </div>
                                            </Col>

                                            {/* Column 3: Items & Type */}
                                            <Col xs={8} sm={8} md={2} lg={2} xl={2}>
                                                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                                    <Text type="secondary" style={{ fontSize: '11px' }}>Items</Text>
                                                    <div>
                                                        <Badge
                                                            count={itemCount}
                                                            showZero
                                                        />
                                                    </div>
                                                </div>
                                            </Col>

                                            <Col xs={16} sm={8} md={3} lg={3} xl={3}>
                                                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                                    <Text type="secondary" style={{ fontSize: '11px' }}>Type</Text>
                                                    <Tag color={firstItem.repairType === "Local Repair" ? "purple" : "cyan"} style={{ margin: 0, width: 'fit-content' }}>
                                                        {firstItem.repairType === "Local Repair" ? "Local" : "Local"}
                                                    </Tag>
                                                </div>
                                            </Col>

                                            {/* Column 4: Actions */}
                                            <Col xs={24} sm={24} md={4} lg={6} xl={8} style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                                <div
                                                    className="header-actions"
                                                    onClick={e => e.stopPropagation()}
                                                    style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', justifyContent: 'flex-end', width: '100%', paddingRight: '10px' }}
                                                >
                                                    {!isDepot && (
                                                        <>
                                                            <Tooltip title="Preview Gatepass">
                                                                <Button
                                                                    size="middle"
                                                                    icon={<FileTextOutlined />}
                                                                    onClick={() => openGatepassPreview(rmaItems, rmaNo)}
                                                                />
                                                            </Tooltip>
                                                            <Tooltip title="Print Stickers">
                                                                <Button
                                                                    size="middle"
                                                                    icon={<PrinterOutlined />}
                                                                    onClick={() => openStickerModal(rmaItems, rmaNo)}
                                                                />
                                                            </Tooltip>
                                                        </>
                                                    )}
                                                    <Button
                                                        type="primary"
                                                        ghost
                                                        size="middle"
                                                        icon={<UserAddOutlined />}
                                                        onClick={() => openBulkAssignModal(firstItem.itemRmaNo || rmaNo, rmaItems)}
                                                    >
                                                        Assign All
                                                    </Button>
                                                </div>
                                            </Col>
                                        </Row>
                                    </div>
                                );

                                return (
                                    <Panel
                                        header={headerContentResponsive}
                                        key={rmaNo}
                                        className="rma-panel"
                                        style={{
                                            marginBottom: 16,
                                            overflow: 'hidden'
                                        }}
                                    >
                                        <div className="rma-items-grid">
                                            {rmaItems.map((item) => (
                                                <div key={item.id} className="rma-item-card-modern">
                                                    <div className="item-header">
                                                        <span className="item-product">{item.product || "Unknown Product"}</span>
                                                        {!item.itemRmaNo ? (
                                                            <Button
                                                                size="small"
                                                                className="add-rma-btn"
                                                                icon={<EditOutlined />}
                                                                onClick={() => openEditRmaModal(item)}
                                                            >
                                                                Add RMA
                                                            </Button>
                                                        ) : (
                                                            <Tag color="cyan">{item.itemRmaNo}</Tag>
                                                        )}
                                                    </div>

                                                    <div className="item-details-grid">
                                                        <div className="detail-box">
                                                            <span className="label">Serial No</span>
                                                            <span className="value monospace">{item.serialNo || "N/A"}</span>
                                                        </div>
                                                        <div className="detail-box">
                                                            <span className="label">Model</span>
                                                            <span className="value">{item.model || "N/A"}</span>
                                                        </div>
                                                    </div>

                                                    <div className="fault-box">
                                                        <span className="label">Reported Fault</span>
                                                        <p className="fault-desc">{item.faultDescription || "No description provided."}</p>
                                                    </div>

                                                    <div className="item-footer">
                                                        <Button
                                                            block
                                                            type="primary"
                                                            ghost
                                                            icon={<UserAddOutlined />}
                                                            onClick={() => openAssignModal(item)}
                                                        >
                                                            Assign Technician
                                                        </Button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </Panel>
                                );
                            })}
                        </Collapse>
                    )}
                </div>

                {/* --- ALL MODALS --- */}

                {/* 1. Assign Modal */}
                <Modal
                    title="Assign Technician"
                    open={assignModalVisible}
                    onCancel={() => setAssignModalVisible(false)}
                    onOk={handleAssign}
                    confirmLoading={assigning}
                >
                    <Select
                        style={{ width: "100%" }}
                        placeholder="Select Technician"
                        value={assigneeEmail}
                        optionLabelProp="value"
                        showSearch
                        filterOption={(input, option) =>
                            (option?.children ?? "").toString().toLowerCase().includes(input.toLowerCase())
                        }
                        onChange={(val) => {
                            const emp = employees.find(e => e.email === val);
                            if (emp) { setAssigneeEmail(emp.email); setAssigneeName(emp.name); }
                        }}
                        disabled={!isAdmin}
                    >
                        {(isAdmin ? employees : (currentUser ? [currentUser] : [])).map(e => (
                            <Select.Option key={e.email} value={e.email}>{`${e.name} (${e.email})`}</Select.Option>
                        ))}
                    </Select>
                </Modal>

                {/* 2. Bulk Assign Modal */}
                <Modal
                    title={`Assign All Items (${selectedRmaItemCount})`}
                    open={bulkAssignModalVisible}
                    onCancel={() => setBulkAssignModalVisible(false)}
                    onOk={handleBulkAssign}
                    confirmLoading={assigning}
                >
                    <p>Assigning all items in RMA <strong>{selectedRmaNo}</strong></p>
                    <Select
                        style={{ width: "100%" }}
                        placeholder="Select Technician"
                        value={assigneeEmail}
                        optionLabelProp="value"
                        showSearch
                        filterOption={(input, option) =>
                            (option?.children ?? "").toString().toLowerCase().includes(input.toLowerCase())
                        }
                        onChange={(val) => {
                            const emp = employees.find(e => e.email === val);
                            if (emp) { setAssigneeEmail(emp.email); setAssigneeName(emp.name); }
                        }}
                        disabled={!isAdmin}
                    >
                        {(isAdmin ? employees : (currentUser ? [currentUser] : [])).map(e => (
                            <Select.Option key={e.email} value={e.email}>{`${e.name} (${e.email})`}</Select.Option>
                        ))}
                    </Select>
                </Modal>

                {/* 3. Delivery Challan Modal (THE FIX) */}
                <Modal
                    title="Generate Delivery Challan"
                    open={dcModalVisible}
                    onCancel={() => setDcModalVisible(false)}
                    onOk={() => dcForm.submit()}
                    confirmLoading={dcSubmitting}
                    width={900}
                >
                    <Form form={dcForm} layout="vertical" onFinish={handleGenerateDC}>
                        <Row gutter={16}>
                            <Col span={12}>
                                <Card size="small" title="Consignee">
                                    <Form.Item name="consigneeName" label="Name"><Input /></Form.Item>
                                    <Form.Item name="consigneeAddress" label="Address"><Input.TextArea /></Form.Item>
                                    <Form.Item name="gstIn" label="GSTIN"><Input /></Form.Item>
                                </Card>
                            </Col>
                            <Col span={12}>
                                <Card size="small" title="Shipment">
                                    <Row gutter={8}>
                                        <Col span={12}><Form.Item name="boxes" label="Boxes" rules={[{ required: true }]}><Input type="number" /></Form.Item></Col>
                                        <Col span={12}><Form.Item name="weight" label="Weight"><Input /></Form.Item></Col>
                                    </Row>
                                    <Row gutter={8}>
                                        <Col span={12}><Form.Item name="dimensions" label="Dimensions"><Input /></Form.Item></Col>
                                        <Col span={12}>
                                            <Form.Item name="modeOfShipment" label="Mode">
                                                <Select>
                                                    <Select.Option value="ROAD">ROAD</Select.Option>
                                                    <Select.Option value="AIR">AIR</Select.Option>
                                                </Select>
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item name="dcNo" label="DC Number" rules={[{ required: true }]}><Input placeholder="DC Number" /></Form.Item>
                                    <Form.Item name="transporterName" label="Transporter">
                                        <Select
                                            mode="tags"
                                            onChange={(val) => {
                                                const v = Array.isArray(val) ? val[val.length - 1] : val;
                                                const t = transporters.find(x => x.name === v);
                                                setIsNewTransporter(!t);
                                                if (t) dcForm.setFieldValue('transporterId', t.transporterId);
                                            }}
                                        >
                                            {transporters.map(t => <Select.Option key={t.id} value={t.name}>{t.name}</Select.Option>)}
                                        </Select>
                                    </Form.Item>
                                    <Form.Item name="transporterId" label="Transporter ID"><Input /></Form.Item>
                                </Card>
                            </Col>
                        </Row>
                        <Divider>Items</Divider>
                        <Table
                            dataSource={dcTableData}
                            pagination={false}
                            size="small"
                            columns={[
                                { title: 'Product', dataIndex: 'product' },
                                { title: 'Serial', dataIndex: 'serialNo' },
                                {
                                    title: 'Value (₹)',
                                    render: (_, __, idx) => (
                                        <Form.Item name={['items', idx, 'rate']} noStyle rules={[{ required: true }]}>
                                            <Input type="number" placeholder="0" />
                                        </Form.Item>
                                    )
                                }
                            ]}
                        />
                    </Form>
                </Modal>

                {/* 4. Edit RMA Modal */}
                <Modal
                    title="Update RMA Number"
                    open={editRmaModalVisible}
                    onOk={handleUpdateRma}
                    onCancel={() => setEditRmaModalVisible(false)}
                    confirmLoading={updatingRma}
                >
                    <Input value={newRmaNo} onChange={e => setNewRmaNo(e.target.value)} placeholder="Enter new RMA Number" />
                </Modal>

                {/* 5. Gatepass Modal */}
                <Modal
                    title="Gatepass Preview"
                    open={gatepassPreviewVisible}
                    onCancel={() => setGatepassPreviewVisible(false)}
                    footer={[
                        <Button key="dl" type="primary" icon={<DownloadOutlined />} onClick={() => handleGenerateGatepass(gatepassRmaNo)}>Download</Button>
                    ]}
                >
                    <Table
                        dataSource={gatepassItems}
                        size="small"
                        columns={[{ title: 'Product', dataIndex: 'product' }, { title: 'Serial', dataIndex: 'serialNo' }]}
                    />
                </Modal>

                {/* 6. Sticker Modal */}
                <Modal
                    title="Print Stickers"
                    open={stickerModalVisible}
                    onCancel={() => setStickerModalVisible(false)}
                    onOk={handlePrintStickers}
                    okText="Print"
                >
                    <p>Ready to print {stickerItems.length} stickers.</p>
                </Modal>
            </div>
        </RmaLayout>
    );
}