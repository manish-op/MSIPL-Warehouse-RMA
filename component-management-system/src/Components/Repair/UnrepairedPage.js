import React, { useState, useEffect } from "react";
import {
    Typography,
    Tag,
    message,
    Button,
    Modal,
    Input,
    Space,
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
} from "antd";
import {
    UserAddOutlined,
    ToolOutlined,
    ExclamationCircleOutlined,
    AppstoreOutlined,
    ReloadOutlined,
    EditOutlined,
    FileTextOutlined,
    PrinterOutlined,
    EyeOutlined,
    DownloadOutlined,
    FilePdfOutlined,
} from "@ant-design/icons";
import { RmaApi } from "../API/RMA/RmaCreateAPI";
import { URL as API_URL } from "../API/URL";
import RmaLayout from "../RMA/RmaLayout";
import "./UnrepairedPage.css";

const { Title, Text, Paragraph } = Typography;

const PREDEFINED_TRANSPORTERS = {
    "Blue Dart Express": "27AAACB0446L1ZS",
    "Safe Express": "27AAECS4363H2Z7"
};

export default function UnrepairedPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [assignModalVisible, setAssignModalVisible] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [assigneeName, setAssigneeName] = useState("");
    const [assigneeEmail, setAssigneeEmail] = useState("");
    const [assigning, setAssigning] = useState(false);

    // Current user state for role-based assignment
    const [currentUser, setCurrentUser] = useState(null);
    // Check both role (from DTO) and roleName (legacy/alternative)
    const isAdmin = currentUser?.role?.toLowerCase() === "admin" || currentUser?.roleName?.toLowerCase() === "admin";
    // Bulk assign state
    const [bulkAssignModalVisible, setBulkAssignModalVisible] = useState(false);
    const [selectedRmaNo, setSelectedRmaNo] = useState(null);
    const [selectedRmaItemCount, setSelectedRmaItemCount] = useState(0);
    const [bulkAssignItems, setBulkAssignItems] = useState([]);
    // Edit RMA state
    const [editRmaModalVisible, setEditRmaModalVisible] = useState(false);
    const [newRmaNo, setNewRmaNo] = useState("");
    const [updatingRma, setUpdatingRma] = useState(false);
    // Gatepass state
    const [generatingGatepass, setGeneratingGatepass] = useState(null);
    // FRU Sticker state
    const [stickerModalVisible, setStickerModalVisible] = useState(false);
    const [stickerItems, setStickerItems] = useState([]);
    // Gatepass Preview state
    const [gatepassPreviewVisible, setGatepassPreviewVisible] = useState(false);
    const [gatepassItems, setGatepassItems] = useState([]);
    const [gatepassRmaNo, setGatepassRmaNo] = useState("");

    // DC Modal State
    const [dcModalVisble, setDcModalVisible] = useState(false);
    const [dcForm] = Form.useForm();
    const [dcSubmitting, setDcSubmitting] = useState(false);
    const [selectedDcRmaNo] = useState("");
    const [dcTableData] = useState([]);


    // Transporter State
    const [transporters, setTransporters] = useState([]);
    const [isNewTransporter, setIsNewTransporter] = useState(false);

    const fetchTransporters = async () => {
        try {
            const response = await fetch(`${API_URL}/api/transporters`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            if (response.ok) {
                const data = await response.json();
                setTransporters(data);
            }
        } catch (error) {
            console.error("Failed to fetch transporters", error);
        }
    };



    const handleGenerateDC = async (values) => {
        setDcSubmitting(true);
        try {
            // Save new transporter if needed
            if (isNewTransporter && values.transporterName && values.transporterId) {
                // If it's an array (from mode="tags"), get the last one or the string
                const name = Array.isArray(values.transporterName) ? values.transporterName[values.transporterName.length - 1] : values.transporterName;

                const newTransporter = {
                    name: name,
                    transporterId: values.transporterId
                };

                // Save to backend
                await fetch(`${API_URL}/api/transporters`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    },
                    body: JSON.stringify(newTransporter)
                });
                // Refresh list
                await fetchTransporters();

                values.transporterName = name;
            } else if (Array.isArray(values.transporterName)) {
                values.transporterName = values.transporterName[0];
            }

            // Merge the form values (which might only have 'rate') with the full item details
            // We use dcTableData as the source of truth for the items list structure
            const currentItems = dcTableData;

            // formatting items to ensure all fields are present
            const formattedItems = currentItems.map((item, index) => ({
                ...item,
                // Ensure rate is taken from the form submission values if present, otherwise fallback to item.rate
                rate: values.items && values.items[index] && values.items[index].rate ? values.items[index].rate : item.rate,
                // Explicitly ensure these fields are passed
                product: item.product,
                model: item.model,
                serialNo: item.serialNo,
                slNo: item.slNo,
                itemRmaNo: item.itemRmaNo
            }));

            await RmaApi.generateDeliveryChallan({
                rmaNo: selectedDcRmaNo,
                ...values,
                items: formattedItems
            });
            message.success("Delivery Challan generated successfully");
            setDcModalVisible(false);
        } catch (error) {
            console.error("DC Generation Error:", error);
            message.error("Failed to generate Delivery Challan");
        } finally {
            setDcSubmitting(false);
        }
    };



    const loadItems = async () => {
        setLoading(true);
        const result = await RmaApi.getUnassignedItems();
        if (result.success) {
            // Filter out Depot Repair items as they are moved to Depot Dispatch page
            const filteredItems = (result.data || []).filter(item => item.repairType !== 'Depot Repair');
            setItems(filteredItems);
        } else {
            message.error("Failed to load unassigned items");
        }
        setLoading(false);
    };

    // Employees state for technician dropdown
    const [employees, setEmployees] = useState([]);

    const loadEmployees = async () => {
        try {
            // Use unified API call
            const result = await RmaApi.getAllUsers();
            let allEmployees = [];

            if (result.success) {
                allEmployees = result.data || [];
                setEmployees(allEmployees);
            }

            // Identify current user from token
            const encodedToken = document.cookie.split("authToken=")[1]?.split(";")[0];
            let userEmail = null;

            if (encodedToken) {
                try {
                    const token = atob(encodedToken);
                    const tokenParts = token.split('.');
                    if (tokenParts.length > 1) {
                        const payload = JSON.parse(atob(tokenParts[1]));
                        userEmail = payload.sub || payload.email;
                    }
                } catch (e) {
                    console.error("Could not decode JWT payload", e);
                }
            }

            // Set current user logic
            if (userEmail && allEmployees.length > 0) {
                const currentEmp = allEmployees.find(e => e.email?.toLowerCase() === userEmail?.toLowerCase());
                if (currentEmp) {
                    setCurrentUser(currentEmp);
                }
            }

        } catch (error) {
            console.error("Failed to load employees debug:", error);
        }
    };

    useEffect(() => {
        loadItems();
        loadEmployees();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

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

    const openAssignModal = (item) => {
        // Check if RMA number is available and valid
        // Strict validation: Allow only if a valid RMA number exists matches
        const hasItemRma = item.itemRmaNo && item.itemRmaNo.trim() !== "";
        // Legacy support: also check rmaNo, but ensure it's not "Unknown"
        const hasRmaNo = item.rmaNo && item.rmaNo.trim() !== "" && item.rmaNo !== "Unknown";

        if (!hasItemRma && !hasRmaNo) {
            message.warning("RMA Number is required. Please update the item with a valid RMA Number before assigning.");
            return;
        }

        setSelectedItem(item);

        // If non-admin, auto-select self
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
        if (!assigneeName.trim()) {
            message.warning("Please enter technician name");
            return;
        }
        if (!assigneeEmail.trim()) {
            message.warning("Please enter technician email");
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

    // Bulk assign handlers
    // Bulk assign handlers
    const openBulkAssignModal = (rmaNo, rmaItems) => {
        const status = checkRmaStatus(rmaItems);

        // If no items have RMA numbers, show warning and return
        if (!status.hasAnyRma) {
            Modal.warning({
                title: 'RMA Number Not Available',
                content: (
                    <div>
                        <p>None of the items have an RMA number assigned.</p>
                        <p>Please assign RMA numbers to items before assigning technicians.</p>
                    </div>
                ),
                okText: 'OK',
                centered: true,
            });
            return;
        }

        const proceedWithItems = (validItems) => {
            // We'll store the specific items to assign, not just the RMA group
            // This allows us to assign ONLY the valid ones
            setSelectedItem(validItems); // Reusing selectedItem to store the array, or should add new state?
            // Actually UnrepairedPage has 'selectedItem' for single assign.
            // Let's use a new state or repurpose 'itemsToAssign'
            // But verify existing state usage.
            // existing: selectedRmaNo, selectedRmaItemCount.
            // Let's add 'bulkAssignItems' state.
            // For now, I'll store it in a temporary variable inside the modal? No, handled in 'handleBulkAssign'.
            // I'll attach the items to a ref or state.
            // Let's modify 'selectedItem' to be 'selectedItems' (array) for bulk?
            // Or easier: just add a new state `bulkAssignItems`

            setBulkAssignItems(validItems);
            setSelectedRmaNo(rmaNo);
            setSelectedRmaItemCount(validItems.length);

            // If non-admin, auto-select self
            if (!isAdmin && currentUser) {
                setAssigneeName(currentUser.name);
                setAssigneeEmail(currentUser.email);
            } else {
                setAssigneeName("");
                setAssigneeEmail("");
            }

            setBulkAssignModalVisible(true);
        };

        // If some items are missing RMA numbers, show confirmation
        if (!status.allHaveRma) {
            Modal.confirm({
                title: 'Some Items Missing RMA Numbers',
                icon: <ExclamationCircleOutlined style={{ color: '#faad14' }} />,
                content: (
                    <div>
                        <p style={{ marginBottom: 8 }}>
                            <strong>{status.missingCount}</strong> out of <strong>{status.totalCount}</strong> item(s)
                            do not have RMA numbers assigned and will be <strong>excluded</strong>.
                        </p>
                        <p>
                            Only <strong>{status.availableCount}</strong> item(s) with RMA numbers will be assigned.
                        </p>
                    </div>
                ),
                okText: 'Proceed',
                cancelText: 'Cancel',
                centered: true,
                onOk: () => proceedWithItems(status.itemsWithRma)
            });
            return;
        }

        // All items valid
        proceedWithItems(status.itemsWithRma);
    };

    const handleBulkAssign = async () => {
        if (!assigneeName.trim()) {
            message.warning("Please enter technician name");
            return;
        }
        if (!assigneeEmail.trim()) {
            message.warning("Please enter technician email");
            return;
        }

        setAssigning(true);
        try {
            // Iterate and assign only the validated items
            // We use Promise.all to run them concurrently (or could do sequential if preferred)
            const promises = bulkAssignItems.map(item =>
                RmaApi.assignItem(item.id, assigneeEmail, assigneeName)
            );

            const results = await Promise.all(promises);

            // Check if all succeeded
            const allSuccess = results.every(r => r.success);
            const failureCount = results.filter(r => !r.success).length;

            if (allSuccess) {
                message.success(`All ${bulkAssignItems.length} items assigned successfully!`);
                setBulkAssignModalVisible(false);
                loadItems();
            } else {
                message.warning(`Assigned with some errors. ${failureCount} items failed.`);
                setBulkAssignModalVisible(false); // Close anyway so they can refresh/see status
                loadItems();
            }

        } catch (error) {
            console.error("Bulk assign error:", error);
            message.error("An error occurred during bulk assignment");
        }
        setAssigning(false);
    };

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

        // Save scroll position before update
        const scrollY = window.scrollY;

        setUpdatingRma(true);
        const result = await RmaApi.updateItemRmaNumber(selectedItem.id, newRmaNo);
        if (result.success) {
            message.success("RMA Number updated successfully!");
            setEditRmaModalVisible(false);
            await loadItems();
            // Restore scroll position after data reloads
            setTimeout(() => window.scrollTo(0, scrollY), 50);
        } else {
            message.error(result.error || "Failed to update RMA Number");
        }
        setUpdatingRma(false);
    };



    // Generate Inward Gatepass PDF
    const handleGenerateGatepass = async (requestNumber) => {
        if (!requestNumber || requestNumber === "Unknown") {
            message.error("Cannot generate Gatepass: Invalid or Missing RMA Number. Please update the items with a valid RMA Number.");
            return;
        }
        setGeneratingGatepass(requestNumber);
        const result = await RmaApi.generateInwardGatepass(requestNumber);
        if (result.success && result.blob) {
            // Create download link
            const url = window.URL.createObjectURL(result.blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `InwardGatepass_${requestNumber}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('Gatepass generated and downloaded!');
        } else {
            message.error(result.error || 'Failed to generate gatepass');
        }
        setGeneratingGatepass(null);
    };

    // Filter items that have RMA numbers assigned
    const getItemsWithRmaNumbers = (rmaItems) => {
        return rmaItems.filter(item => item.itemRmaNo && item.itemRmaNo.trim() !== "");
    };

    // Check RMA assignment status
    const checkRmaStatus = (rmaItems) => {
        const itemsWithRma = getItemsWithRmaNumbers(rmaItems);
        const itemsWithoutRma = rmaItems.filter(item => !item.itemRmaNo || item.itemRmaNo.trim() === "");
        return {
            itemsWithRma,
            itemsWithoutRma,
            hasAnyRma: itemsWithRma.length > 0,
            allHaveRma: itemsWithoutRma.length === 0,
            missingCount: itemsWithoutRma.length,
            availableCount: itemsWithRma.length,
            totalCount: rmaItems.length
        };
    };

    // Open Gatepass Preview Modal
    const openGatepassPreview = (rmaItems, rmaNo) => {
        const status = checkRmaStatus(rmaItems);

        // If no items have RMA numbers, show warning and return
        if (!status.hasAnyRma) {
            Modal.warning({
                title: 'RMA Number Not Available',
                content: (
                    <div>
                        <p>None of the items have an RMA number assigned.</p>
                        <p>Please assign RMA numbers to items before generating gatepass.</p>
                    </div>
                ),
                okText: 'OK',
                centered: true,
            });
            return;
        }

        // If some items are missing RMA numbers, show confirmation
        if (!status.allHaveRma) {
            Modal.confirm({
                title: 'Some Items Missing RMA Numbers',
                icon: <ExclamationCircleOutlined style={{ color: '#faad14' }} />,
                content: (
                    <div>
                        <p style={{ marginBottom: 8 }}>
                            <strong>{status.missingCount}</strong> out of <strong>{status.totalCount}</strong> item(s)
                            do not have RMA numbers assigned and will be <strong>excluded</strong>.
                        </p>
                        <p>
                            Only <strong>{status.availableCount}</strong> item(s) with RMA numbers will be included in the gatepass.
                        </p>
                    </div>
                ),
                okText: 'Proceed',
                cancelText: 'Cancel',
                centered: true,
                onOk: () => {
                    setGatepassItems(status.itemsWithRma);
                    setGatepassRmaNo(rmaNo);
                    setGatepassPreviewVisible(true);
                }
            });
            return;
        }

        // All items have RMA numbers, proceed directly
        setGatepassItems(rmaItems);
        setGatepassRmaNo(rmaNo);
        setGatepassPreviewVisible(true);
    };

    // Open FRU Sticker Modal for an RMA request
    const openStickerModal = (rmaItems, rmaNo) => {
        const status = checkRmaStatus(rmaItems);

        // Prepare items with display RMA number
        const prepareItemsForStickers = (items) => {
            return items.map(item => ({
                ...item,
                displayRmaNo: item.itemRmaNo || rmaNo
            }));
        };

        // If no items have RMA numbers, show warning and return
        if (!status.hasAnyRma) {
            Modal.warning({
                title: 'RMA Number Not Available',
                content: (
                    <div>
                        <p>None of the items have an RMA number assigned.</p>
                        <p>Please assign RMA numbers to items before printing stickers.</p>
                    </div>
                ),
                okText: 'OK',
                centered: true,
            });
            return;
        }

        // If some items are missing RMA numbers, show confirmation
        if (!status.allHaveRma) {
            Modal.confirm({
                title: 'Some Items Missing RMA Numbers',
                icon: <ExclamationCircleOutlined style={{ color: '#faad14' }} />,
                content: (
                    <div>
                        <p style={{ marginBottom: 8 }}>
                            <strong>{status.missingCount}</strong> out of <strong>{status.totalCount}</strong> item(s)
                            do not have RMA numbers assigned and will be <strong>excluded</strong>.
                        </p>
                        <p>
                            Only <strong>{status.availableCount}</strong> item(s) with RMA numbers will have stickers printed.
                        </p>
                    </div>
                ),
                okText: 'Proceed',
                cancelText: 'Cancel',
                centered: true,
                onOk: () => {
                    setStickerItems(prepareItemsForStickers(status.itemsWithRma));
                    setStickerModalVisible(true);
                }
            });
            return;
        }

        // All items have RMA numbers, proceed directly
        setStickerItems(prepareItemsForStickers(rmaItems));
        setStickerModalVisible(true);
    };

    // Print stickers function
    const handlePrintStickers = () => {
        const printWindow = window.open('', '_blank');
        if (!printWindow) {
            message.error('Please allow popups to print stickers');
            return;
        }

        const stickersHtml = stickerItems.map((item, index) => `
            <div class="sticker">
                <div class="sticker-header">
                    <img src="/companyLogo.png" alt="Company Logo" class="company-logo" onerror="this.style.display='none'" />
                    <span class="company-name">Motorola Solutions India Pvt. Ltd.</span>
                </div>
                <div class="sticker-divider"></div>
                <div class="sticker-content">
                    <div class="sticker-row">
                        <span class="label">RMA No:</span>
                        <span class="value">${item.displayRmaNo || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Customer:</span>
                        <span class="value">${item.companyName || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Received:</span>
                        <span class="value">${item.receivedDate ? new Date(item.receivedDate).toLocaleDateString('en-IN') : 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Product:</span>
                        <span class="value">${item.product || 'N/A'}</span>
                    </div>
                    <div class="sticker-row">
                        <span class="label">Serial No:</span>
                        <span class="value">${item.serialNo || 'N/A'}</span>
                    </div>
                    <div class="sticker-row fault-row">
                        <span class="label">Fault:</span>
                        <span class="value fault-text">${item.faultDescription || 'No description'}</span>
                    </div>
                </div>
            </div>
        `).join('');

        printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>FRU Stickers - ${stickerItems[0]?.displayRmaNo || 'RMA'}</title>
                <style>
                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                    }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        padding: 10mm;
                        background: #f5f5f5;
                    }
                    .stickers-container {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 10mm;
                    }
                    .sticker {
                        width: 85mm;
                        min-height: 55mm;
                        border: 1.5px solid #333;
                        border-radius: 4px;
                        background: #fff;
                        padding: 3mm;
                        page-break-inside: avoid;
                    }
                    .sticker-header {
                        display: flex;
                        align-items: center;
                        gap: 3mm;
                        padding-bottom: 2mm;
                    }
                    .company-logo {
                        width: 8mm;
                        height: 8mm;
                        object-fit: contain;
                    }
                    .company-name {
                        font-weight: 600;
                        font-size: 9pt;
                        color: #333;
                    }
                    .sticker-divider {
                        height: 1px;
                        background: #333;
                        margin-bottom: 2mm;
                    }
                    .sticker-content {
                        font-size: 8pt;
                    }
                    .sticker-row {
                        display: flex;
                        margin-bottom: 1.5mm;
                    }
                    .label {
                        font-weight: 600;
                        width: 20mm;
                        color: #555;
                        flex-shrink: 0;
                    }
                    .value {
                        color: #333;
                        word-break: break-word;
                    }
                    .fault-row {
                        margin-top: 1mm;
                    }
                    .fault-text {
                        font-size: 7.5pt;
                        line-height: 1.3;
                    }
                    @media print {
                        body {
                            background: #fff;
                            padding: 5mm;
                        }
                        .stickers-container {
                            gap: 5mm;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="stickers-container">
                    ${stickersHtml}
                </div>
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
                {/* Header Section */}
                <div className="unrepaired-header">
                    <div className="header-content">
                        <div className="header-title">
                            <ToolOutlined className="header-icon" />
                            <div>
                                <Title level={2} style={{ margin: 0, color: "#fff" }}>
                                    Unrepaired Items
                                </Title>
                                <Text style={{ color: "rgba(255,255,255,0.85)" }}>
                                    Items awaiting technician assignment
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
                                <AppstoreOutlined />
                                <div>
                                    <div className="stat-value">{totalRmaRequests}</div>
                                    <div className="stat-label">RMA Requests</div>
                                </div>
                            </div>
                        </Col>
                        <Col xs={12} sm={8}>
                            <div className="stat-box">
                                <ExclamationCircleOutlined />
                                <div>
                                    <div className="stat-value">{totalItems}</div>
                                    <div className="stat-label">Pending Items</div>
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
                            description="No unassigned items found"
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
                                            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                                                <Tag color="#1890ff" style={{ fontSize: 14, padding: "4px 12px" }}>
                                                    RMA: {rmaItems[0]?.itemRmaNo || rmaNo}
                                                </Tag>
                                                <Badge
                                                    count={rmaItems.length}
                                                    style={{ backgroundColor: "#1890ff" }}
                                                    overflowCount={99}
                                                />
                                                {/* Display Repair Type if available */}
                                                {rmaItems[0]?.repairType && (
                                                    <Tag color={rmaItems[0].repairType === "Local Repair" ? "purple" : "orange"} style={{ fontSize: 13, marginLeft: 8 }}>
                                                        {rmaItems[0].repairType}
                                                    </Tag>
                                                )}
                                            </div>
                                            <Space>
                                                {rmaItems[0]?.repairType !== "Depot Repair" && (
                                                    <>
                                                        <Button
                                                            icon={<FileTextOutlined />}
                                                            onClick={() => openGatepassPreview(rmaItems, rmaItems[0]?.itemRmaNo || rmaNo)}
                                                            size="small"
                                                        >
                                                            Preview Gatepass
                                                        </Button>
                                                        <Button
                                                            icon={<PrinterOutlined />}
                                                            onClick={() => openStickerModal(rmaItems, rmaItems[0]?.itemRmaNo || rmaNo)}
                                                            size="small"
                                                            style={{ background: "#722ed1", borderColor: "#722ed1", color: "#fff" }}
                                                        >
                                                            Print Stickers ({rmaItems.length})
                                                        </Button>
                                                    </>
                                                )}
                                                <Button
                                                    type="primary"
                                                    icon={<UserAddOutlined />}
                                                    onClick={() => openBulkAssignModal(rmaItems[0]?.itemRmaNo || rmaNo, rmaItems)}
                                                    size="small"
                                                    style={{ background: "#52c41a", borderColor: "#52c41a" }}
                                                >
                                                    Assign All ({rmaItems.length})
                                                </Button>
                                            </Space>
                                        </div>
                                    }
                                >
                                    <Row gutter={[16, 16]}>
                                        {rmaItems.map((item) => (
                                            <Col xs={24} md={12} lg={8} key={item.id}>
                                                <Card
                                                    className="item-card"
                                                    size="small"
                                                    hoverable
                                                    actions={[
                                                        <Button
                                                            type="primary"
                                                            icon={<UserAddOutlined />}
                                                            onClick={() => openAssignModal(item)}
                                                            className="assign-btn"
                                                        >
                                                            Assign Technician
                                                        </Button>,
                                                        !item.itemRmaNo && (
                                                            <Button
                                                                key="add-rma"
                                                                icon={<EditOutlined />}
                                                                onClick={() => openEditRmaModal(item)}
                                                            >
                                                                Add RMA
                                                            </Button>
                                                        )
                                                    ]}
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
                                                        <Divider style={{ margin: "8px 0" }} />
                                                        <div className="fault-section">
                                                            <Text type="secondary">Fault Description</Text>
                                                            <Paragraph
                                                                ellipsis={{ rows: 2, expandable: true }}
                                                                className="fault-text"
                                                            >
                                                                {item.faultDescription || "No description"}
                                                            </Paragraph>
                                                        </div>
                                                        {item.itemRmaNo && (
                                                            <div className="item-row" style={{ marginTop: 8 }}>
                                                                <Text type="secondary">RMA Number</Text>
                                                                <Tag color="green">{item.itemRmaNo}</Tag>
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
                </div>

                {/* Assign Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <UserAddOutlined style={{ color: "#1890ff" }} />
                            <span>Assign Technician</span>
                        </div>
                    }
                    open={assignModalVisible}
                    onCancel={() => setAssignModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setAssignModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="assign"
                            type="primary"
                            loading={assigning}
                            onClick={handleAssign}
                            style={{ backgroundColor: "#1890ff", borderColor: "#1890ff" }}
                        >
                            Assign Technician
                        </Button>
                    ]}
                    className="assign-modal"
                >
                    <div className="modal-item-info">
                        <Card size="small" style={{ marginBottom: 16, backgroundColor: "#e6f7ff" }}>
                            <Row gutter={16}>
                                <Col span={12}>
                                    <Text type="secondary">Product</Text>
                                    <div><Text strong>{selectedItem?.product}</Text></div>
                                </Col>
                                <Col span={12}>
                                    <Text type="secondary">Serial No.</Text>
                                    <div><Text code>{selectedItem?.serialNo}</Text></div>
                                </Col>
                            </Row>
                            <div style={{ marginTop: 12 }}>
                                <Text type="secondary">Fault</Text>
                                <Paragraph style={{ margin: 0 }}>{selectedItem?.faultDescription}</Paragraph>
                            </div>
                        </Card>
                    </div>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>Select Technician *</Text>
                            {!isAdmin && currentUser && (
                                <div style={{ marginTop: 4, padding: 8, backgroundColor: "#fff7e6", borderRadius: 4 }}>
                                    <Text type="secondary" style={{ fontSize: 12 }}>
                                        <ExclamationCircleOutlined style={{ marginRight: 4 }} />
                                        You can only assign items to yourself
                                    </Text>
                                </div>
                            )}
                            {!currentUser && (
                                <div style={{ marginTop: 4, padding: 8, backgroundColor: "#ffccc7", borderRadius: 4 }}>
                                    <Text type="secondary" style={{ fontSize: 12, color: "#a8071a" }}>
                                        <ExclamationCircleOutlined style={{ marginRight: 4 }} />
                                        Unable to verify user identity. Please refresh or relogin.
                                    </Text>
                                </div>
                            )}
                            <Select
                                style={{ width: "100%", marginTop: 4 }}
                                size="large"
                                placeholder="Select a technician"
                                value={assigneeEmail || undefined}
                                onChange={(value) => {
                                    const emp = employees.find(e => e.email === value);
                                    if (emp) {
                                        setAssigneeEmail(emp.email);
                                        setAssigneeName(emp.name);
                                    }
                                }}
                                showSearch
                                filterOption={(input, option) =>
                                    option.children.toLowerCase().includes(input.toLowerCase())
                                }
                                disabled={!isAdmin}
                            >
                                {/* Admin sees all employees, non-admin sees only self */}
                                {(isAdmin
                                    ? employees
                                    : (currentUser ? employees.filter(emp => emp.email === currentUser.email) : [])
                                ).map(emp => (
                                    <Select.Option key={emp.email} value={emp.email}>
                                        {emp.name} ({emp.email})
                                    </Select.Option>
                                ))}
                            </Select>
                        </div>
                    </Space>
                </Modal>

                {/* Bulk Assign Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <UserAddOutlined style={{ color: "#52c41a" }} />
                            <span>Assign All Items</span>
                        </div>
                    }
                    open={bulkAssignModalVisible}
                    onCancel={() => setBulkAssignModalVisible(false)}
                    footer={[
                        <Button key="cancel" onClick={() => setBulkAssignModalVisible(false)}>
                            Cancel
                        </Button>,
                        <Button
                            key="assign"
                            type="primary"
                            loading={assigning}
                            onClick={handleBulkAssign}
                            style={{ backgroundColor: "#52c41a", borderColor: "#52c41a" }}
                        >
                            Assign All Items
                        </Button>
                    ]}
                    className="assign-modal"
                >
                    <Card size="small" style={{ marginBottom: 16, backgroundColor: "#f6ffed", border: "1px solid #b7eb8f" }}>
                        <div style={{ textAlign: "center" }}>
                            <Tag color="#1890ff" style={{ fontSize: 16, padding: "8px 16px" }}>
                                RMA: {selectedRmaNo}
                            </Tag>
                            <div style={{ marginTop: 12 }}>
                                <Text type="secondary">Total items to assign: </Text>
                                <Text strong style={{ fontSize: 18, color: "#52c41a" }}>{selectedRmaItemCount}</Text>
                            </div>
                        </div>
                    </Card>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <div>
                            <Text strong>Select Technician *</Text>
                            {!isAdmin && currentUser && (
                                <div style={{ marginTop: 4, padding: 8, backgroundColor: "#fff7e6", borderRadius: 4 }}>
                                    <Text type="secondary" style={{ fontSize: 12 }}>
                                        <ExclamationCircleOutlined style={{ marginRight: 4 }} />
                                        You can only assign items to yourself
                                    </Text>
                                </div>
                            )}
                            {!currentUser && (
                                <div style={{ marginTop: 4, padding: 8, backgroundColor: "#ffccc7", borderRadius: 4 }}>
                                    <Text type="secondary" style={{ fontSize: 12, color: "#a8071a" }}>
                                        <ExclamationCircleOutlined style={{ marginRight: 4 }} />
                                        Unable to verify user identity. Please refresh or relogin.
                                    </Text>
                                </div>
                            )}
                            <Select
                                style={{ width: "100%", marginTop: 4 }}
                                size="large"
                                placeholder="Select a technician"
                                value={assigneeEmail || undefined}
                                onChange={(value) => {
                                    const emp = employees.find(e => e.email === value);
                                    if (emp) {
                                        setAssigneeEmail(emp.email);
                                        setAssigneeName(emp.name);
                                    }
                                }}
                                showSearch
                                filterOption={(input, option) =>
                                    option.children.toLowerCase().includes(input.toLowerCase())
                                }
                                disabled={!isAdmin}
                            >
                                {/* Admin sees all employees, non-admin sees only self */}
                                {(isAdmin
                                    ? employees
                                    : (currentUser ? employees.filter(emp => emp.email === currentUser.email) : [])
                                ).map(emp => (
                                    <Select.Option key={emp.email} value={emp.email}>
                                        {emp.name} ({emp.email})
                                    </Select.Option>
                                ))}
                            </Select>
                        </div>
                    </Space>
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
                            key="submit"
                            type="primary"
                            loading={updatingRma}
                            onClick={handleUpdateRma}
                        >
                            Add
                        </Button>
                    ]}
                >
                    <Text strong>RMA Number:</Text>
                    <Input
                        placeholder="Enter RMA Number"
                        value={newRmaNo}
                        onChange={(e) => setNewRmaNo(e.target.value)}
                        style={{ marginTop: 8 }}
                    />
                </Modal>

                {/* FRU Sticker Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <PrinterOutlined style={{ color: "#722ed1" }} />
                            <span>FRU Stickers Preview</span>
                        </div>
                    }
                    open={stickerModalVisible}
                    onCancel={() => setStickerModalVisible(false)}
                    width={800}
                    footer={[
                        <Button key="cancel" onClick={() => setStickerModalVisible(false)}>
                            Close
                        </Button>,
                        <Button
                            key="print"
                            type="primary"
                            icon={<PrinterOutlined />}
                            onClick={handlePrintStickers}
                            style={{ background: "#722ed1", borderColor: "#722ed1" }}
                        >
                            Print All Stickers ({stickerItems.length})
                        </Button>
                    ]}
                    className="sticker-modal"
                >
                    <div className="sticker-preview-container">
                        <Row gutter={[16, 16]}>
                            {stickerItems.map((item, index) => (
                                <Col xs={24} md={12} key={item.id || index}>
                                    <div className="sticker-preview">
                                        <div className="sticker-preview-header">
                                            <img
                                                src="/companyLogo.png"
                                                alt="Logo"
                                                className="sticker-preview-logo"
                                                onError={(e) => { e.target.style.display = 'none'; }}
                                            />
                                            <span className="sticker-preview-company">
                                                Motorola Solutions India Pvt. Ltd.
                                            </span>
                                        </div>
                                        <Divider style={{ margin: "8px 0" }} />
                                        <div className="sticker-preview-content">
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">RMA No:</Text>
                                                <Text strong>{item.displayRmaNo || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Customer:</Text>
                                                <Text>{item.companyName || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Received:</Text>
                                                <Text>
                                                    {item.receivedDate
                                                        ? new Date(item.receivedDate).toLocaleDateString('en-IN')
                                                        : 'N/A'}
                                                </Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Product:</Text>
                                                <Text>{item.product || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Serial No:</Text>
                                                <Text code>{item.serialNo || 'N/A'}</Text>
                                            </div>
                                            <div className="sticker-preview-row">
                                                <Text type="secondary" className="sticker-label">Fault:</Text>
                                                <Paragraph
                                                    ellipsis={{ rows: 2 }}
                                                    style={{ margin: 0, fontSize: 12 }}
                                                >
                                                    {item.faultDescription || 'No description'}
                                                </Paragraph>
                                            </div>
                                        </div>
                                    </div>
                                </Col>
                            ))}
                        </Row>
                    </div>
                </Modal>

                {/* Gatepass Preview Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <EyeOutlined style={{ color: "#1890ff" }} />
                            <span>Gatepass Preview - {gatepassRmaNo}</span>
                        </div>
                    }
                    open={gatepassPreviewVisible}
                    onCancel={() => setGatepassPreviewVisible(false)}
                    width={900}
                    footer={[
                        <Button key="cancel" onClick={() => setGatepassPreviewVisible(false)}>
                            Close
                        </Button>,
                        <Button
                            key="download"
                            type="primary"
                            icon={<DownloadOutlined />}
                            onClick={() => {
                                handleGenerateGatepass(gatepassRmaNo);
                                setGatepassPreviewVisible(false);
                            }}
                            loading={generatingGatepass === gatepassRmaNo}
                        >
                            Download PDF
                        </Button>
                    ]}
                    className="gatepass-modal"
                >
                    <div className="gatepass-preview-container">
                        {/* Header Info */}
                        <Card size="small" className="gatepass-info-card">
                            <Row gutter={16}>
                                <Col span={8}>
                                    <Text type="secondary">RMA Request Number</Text>
                                    <div>
                                        <Tag color="blue" style={{ fontSize: 14, padding: "4px 12px" }}>
                                            {gatepassRmaNo}
                                        </Tag>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Customer</Text>
                                    <div>
                                        <Text strong>{gatepassItems[0]?.companyName || 'N/A'}</Text>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <Text type="secondary">Total Items</Text>
                                    <div>
                                        <Badge
                                            count={gatepassItems.length}
                                            style={{ backgroundColor: "#52c41a" }}
                                            showZero
                                        />
                                    </div>
                                </Col>
                            </Row>
                        </Card>

                        {/* Items Table */}
                        <Table
                            dataSource={gatepassItems.map((item, index) => ({ ...item, key: item.id || index, slNo: index + 1 }))}
                            columns={[
                                {
                                    title: 'Sl.No',
                                    dataIndex: 'slNo',
                                    key: 'slNo',
                                    width: 60,
                                    align: 'center',
                                },
                                {
                                    title: 'Product',
                                    dataIndex: 'product',
                                    key: 'product',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Model',
                                    dataIndex: 'model',
                                    key: 'model',
                                    render: (text) => text || 'N/A',
                                },
                                {
                                    title: 'Serial No',
                                    dataIndex: 'serialNo',
                                    key: 'serialNo',
                                    render: (text) => <Text code>{text || 'N/A'}</Text>,
                                },
                                {
                                    title: 'Fault Description',
                                    dataIndex: 'faultDescription',
                                    key: 'faultDescription',
                                    ellipsis: true,
                                    render: (text) => text || 'No description',
                                },
                                {
                                    title: 'RMA No',
                                    dataIndex: 'itemRmaNo',
                                    key: 'itemRmaNo',
                                    render: (text) => text ? <Tag color="green">{text}</Tag> : <Text type="secondary">Not assigned</Text>,
                                },
                            ]}
                            pagination={false}
                            size="small"
                            bordered
                            style={{ marginTop: 16 }}
                        />
                    </div>
                </Modal>

                {/* Delivery Challan Modal */}
                <Modal
                    title={
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <FilePdfOutlined style={{ color: "#faad14" }} />
                            <span>Generate Delivery Challan</span>
                        </div>
                    }
                    open={dcModalVisble}
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
                                        onSelect={(value) => {
                                            const t = transporters.find(t => t.name === value);
                                            if (t) {
                                                dcForm.setFieldsValue({ transporterId: t.transporterId });
                                                setIsNewTransporter(false);
                                            }
                                        }}
                                        onSearch={(val) => {
                                            // Ensure we can type new values. 
                                            // Logic: if val not in transporters, we set a flag
                                            const exists = transporters.some(t => t.name.toLowerCase() === val.toLowerCase());
                                            setIsNewTransporter(!exists && val.length > 0);
                                        }}
                                        onChange={(val) => {
                                            // If user clears or types custom
                                            // val might be array in tags mode, take last
                                            const selectedValue = Array.isArray(val) ? val[val.length - 1] : val;

                                            const exists = transporters.find(t => t.name === selectedValue);

                                            if (!exists) {
                                                setIsNewTransporter(true);
                                                // Check predefined
                                                if (PREDEFINED_TRANSPORTERS[selectedValue]) {
                                                    dcForm.setFieldsValue({ transporterId: PREDEFINED_TRANSPORTERS[selectedValue] });
                                                } else {
                                                    dcForm.setFieldsValue({ transporterId: '' });
                                                }
                                            } else {
                                                setIsNewTransporter(false);
                                                dcForm.setFieldsValue({ transporterId: exists.transporterId });
                                            }
                                        }}
                                        mode="tags" // Allows creating new items
                                        notFoundContent="Type to add new transporter"
                                    >
                                        {/* Combine fetched transporters with predefined ones, removing duplicates */}
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
                        <Row gutter={16} justify="end" style={{ marginTop: 16 }}>
                            <Col>
                                <Button onClick={() => setDcModalVisible(false)} style={{ marginRight: 8 }}>Cancel</Button>
                                <Button type="primary" htmlType="submit" loading={dcSubmitting}>Generate DC</Button>
                            </Col>
                        </Row>
                    </Form>
                </Modal>
            </div >
        </RmaLayout >
    );
}
