import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

export const CustomerSlaApi = {

    createCustomerSla: async (payload) => {
        const tokenRaw = Cookies.get("authToken");
        if (!tokenRaw) return null;
        const token = atob(tokenRaw);

        try {
            const response = await fetch(`${manualURL}/project-core/create`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const mess = await response.text();
                // We'll let the component handle success messages, but show error here if needed or just return status
                // But following AlertApi pattern, it shows error message.
                // However, for form submission, we might want to know if it succeeded or failed to show specific notification?
                // AlertApi returns null on failure.
                message.error(mess || "Failed to create Customer SLA.", 5);
                return null;
            }

            return await response.json();

        } catch (error) {
            message.error("API Network Error: " + error.message, 5);
            return null;
        }
    },

    getAllCustomerSla: async () => {
        const tokenRaw = Cookies.get("authToken");
        if (!tokenRaw) return null;
        const token = atob(tokenRaw);

        try {
            const response = await fetch(`${manualURL}/project-core/all`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const mess = await response.text();
                message.error(mess || "Failed to fetch Customer SLAs.", 5);
                return null;
            }

            return await response.json();

        } catch (error) {
            message.error("API Network Error: " + error.message, 5);
            return null;
        }
    }
};
