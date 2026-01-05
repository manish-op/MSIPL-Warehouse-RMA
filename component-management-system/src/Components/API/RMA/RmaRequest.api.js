import { message } from "antd";
import { URL } from "../URL";
import { getAuthToken, apiGet } from "./ApiClient";

export const RmaRequestApi = {
    createRmaRequest: async (rmaData) => {
        const token = getAuthToken();

        if (!token) {
            message.error("Authentication required. Please login first.", 5);
            return { success: false, error: "No authentication token found" };
        }

        try {
            const response = await fetch(`${URL}/rma/create`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(rmaData),
            });

            if (!response.ok) {
                const errorText = await response.text();
                message.error(errorText || "Failed to create RMA request", 5);
                return { success: false, error: errorText };
            }

            const responseData = await response.json();
            message.success("RMA Request submitted successfully!", 5);
            return { success: true, data: responseData };
        } catch (error) {
            message.error("Network Error: " + error.message, 5);
            return { success: false, error: error.message };
        }
    },
    //serial no history
    getSerialHistory: async (serialNo) => {
        return apiGet(`/rma/serial-history?serialNo=${encodeURIComponent(serialNo)}`);
    }
};
