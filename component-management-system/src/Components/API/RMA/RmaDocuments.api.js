import { URL } from "../URL";
import { getAuthToken } from "./ApiClient";

const generatePdf = async (endpoint) => {
    const token = getAuthToken();
    if (!token) {
        return { success: false, error: "No authentication token" };
    }
    try {
        const response = await fetch(`${URL}${endpoint}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) {
            const errorText = await response.text();
            return { success: false, error: errorText };
        }
        const blob = await response.blob();
        return { success: true, blob };
    } catch (error) {
        return { success: false, error: error.message };
    }
};

export const RmaDocumentsApi = {
    // Generate Inward Gatepass PDF
    generateInwardGatepass: async (requestNumber) => {
        return generatePdf(`/rma/gatepass/generate/${requestNumber}`);
    },

    // Generate Outward Gatepass PDF
    generateOutwardGatepass: async (requestNumber) => {
        return generatePdf(`/rma/outward-gatepass/generate/${requestNumber}`);
    },

    // Generate Delivery Challan PDF
    generateDeliveryChallan: async (payload) => {
        const token = getAuthToken();
        if (!token) {
            throw new Error("No authentication token");
        }
        try {
            const response = await fetch(`${URL}/rma/delivery-challan/generate`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `DeliveryChallan_${payload.rmaNo}.pdf`;
            document.body.appendChild(a);
            a.click();

            setTimeout(() => {
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            }, 1000);

            return { success: true };
        } catch (error) {
            throw error;
        }
    },
};
