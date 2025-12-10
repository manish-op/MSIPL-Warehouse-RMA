import Cookies from "js-cookie";
import { URL } from "../URL";
import { message } from "antd";

// Helper function to safely get and decode the auth token
const getAuthToken = () => {
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

// Helper for GET requests
const apiGet = async (endpoint) => {
  const token = getAuthToken();
  if (!token) return { success: false, error: "No authentication token found" };

  try {
    const response = await fetch(`${URL}${endpoint}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
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

// Helper for PUT requests
const apiPut = async (endpoint, body) => {
  const token = getAuthToken();
  if (!token) return { success: false, error: "No authentication token found" };

  try {
    const response = await fetch(`${URL}${endpoint}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
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

export const RmaApi = {
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

  // Dashboard Statistics
  getRmaDashboardStats: async () => apiGet("/rma/stats"),

  // Workflow APIs
  getUnassignedItems: async () => apiGet("/rma/items/unassigned"),
  getAssignedItems: async () => apiGet("/rma/items/assigned"),
  getRepairedItems: async () => apiGet("/rma/items/repaired"),
  getCantBeRepairedItems: async () => apiGet("/rma/items/cant-be-repaired"),

  // Get product catalog from ItemDetailsEntity
  getProductCatalog: async () => apiGet("/rma/product-catalog"),

  // Assign item to technician
  assignItem: async (itemId, assigneeEmail, assigneeName) => {
    return apiPut(`/rma/items/${itemId}/assign`, { assigneeEmail, assigneeName });
  },

  // Update item status
  updateItemStatus: async (itemId, status, remarks) => {
    return apiPut(`/rma/items/${itemId}/status`, { status, remarks });
  },

  // Bulk assign all items in an RMA to a technician
  bulkAssignByRmaNo: async (rmaNo, assigneeEmail, assigneeName) => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
      const response = await fetch(`${URL}/rma/bulk-assign`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ rmaNo, assigneeEmail, assigneeName }),
      });
      if (!response.ok) {
        const errorText = await response.text();
        return { success: false, error: errorText };
      }
      const data = await response.text();
      return { success: true, data };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },

  // Get all audit logs
  getAuditLogs: async () => apiGet("/rma/audit-logs"),

  // Get audit logs for specific item
  getAuditLogsByItemId: async (itemId) => apiGet(`/rma/audit-logs/item/${itemId}`),

  // Customer APIs for auto-complete
  getAllCustomers: async () => apiGet("/rma/customers"),

  searchCustomers: async (searchTerm) => apiGet(`/rma/customers/search?q=${encodeURIComponent(searchTerm || '')}`),

  getCustomerById: async (customerId) => apiGet(`/rma/customers/${customerId}`),
};

