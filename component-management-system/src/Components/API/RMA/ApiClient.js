import Cookies from "js-cookie";
import { URL } from "../URL";

// Helper function to safely get and decode the auth token
export const getAuthToken = () => {
    const encodedToken = Cookies.get("authToken");

    if (!encodedToken || encodedToken === "undefined" || encodedToken === "null") {
        console.error("No authToken cookie found. User may not be logged in.");
        return null;
    }

    try {
        return atob(encodedToken);
    } catch (error) {
        console.error("Failed to decode authToken:", error);
        return null;
    }
};

const getHeaders = (token, contentType = "application/json") => {
    const headers = {
        Authorization: `Bearer ${token}`,
    };
    if (contentType) {
        headers["Content-Type"] = contentType;
    }
    return headers;
};

export const apiGet = async (endpoint) => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
        const response = await fetch(`${URL}${endpoint}`, {
            method: "GET",
            headers: getHeaders(token, null),
        });
        if (!response.ok) {
            const errorText = await response.text();
            return { success: false, error: errorText };
        }
        const data = await response.json();
        return { success: true, data };
    } catch (error) {
        return { success: false, error: error.message };
    }
};

export const apiPut = async (endpoint, body) => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
        const response = await fetch(`${URL}${endpoint}`, {
            method: "PUT",
            headers: getHeaders(token),
            body: JSON.stringify(body),
        });
        if (!response.ok) {
            const errorText = await response.text();
            return { success: false, error: errorText };
        }
        const text = await response.text();
        return { success: true, message: text };
    } catch (error) {
        return { success: false, error: error.message };
    }
};

export const apiPost = async (endpoint, body, returnType = "json") => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
        const response = await fetch(`${URL}${endpoint}`, {
            method: "POST",
            headers: getHeaders(token),
            body: JSON.stringify(body),
        });

        if (!response.ok) {
            const errorText = await response.text();
            return { success: false, error: errorText };
        }

        if (returnType === "json") {
            const data = await response.json();
            return { success: true, data };
        } else if (returnType === "text") {
            const message = await response.text();
            return { success: true, message };
        } else if (returnType === "blob") {
            const blob = await response.blob();
            return { success: true, blob };
        }

        return { success: true };

    } catch (error) {
        return { success: false, error: error.message };
    }
};
