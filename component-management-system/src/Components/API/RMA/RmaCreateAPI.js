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
  //Depot Dispatch APIs
  getDepotReadyToDispatch: async () => apiGet("/rma/depot/ready-to-dispatch"),
  dispatchToBangalore: async (payload) => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
      const response = await fetch(`${URL}/rma/depot/dispatch-to-bangalore`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const errorText = await response.text();
        return { success: false, error: errorText || "Failed to dispatch to Bangalore" };
      }
      const text = await response.text();
      return { success: true, message: text };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },
  getDepotInTransit: async () => apiGet("/rma/depot/in-transit"),
  markDepotReceived: async (payload) => {
    const token = getAuthToken();
    if (!token) return { success: false, error: "No authentication token found" };

    try {
      const response = await fetch(`${URL}/rma/depot/mark-received`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const errorText = await response.text();
        return { success: false, error: errorText || "Failed to mark as received" };
      }
      const text = await response.text();
      return { success: true, message: text };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },

  getAllTransporters: async () => apiGet("/transporters"),

  // Get product catalog from ItemDetailsEntity
  getProductCatalog: async () => apiGet("/rma/product-catalog"),

  // Assign item to technician
  assignItem: async (itemId, assigneeEmail, assigneeName) => {
    return apiPut(`/rma/items/${itemId}/assign`, { assigneeEmail, assigneeName });
  },

  // Update item status
  updateItemStatus: async (itemId, status, remarks, issueFixed) => {
    return apiPut(`/rma/items/${itemId}/status`, { status, remarks, issueFixed });
  },

  // Reassign item to a different technician with reason
  reassignItem: async (itemId, assigneeEmail, assigneeName, reason) => {
    return apiPut(`/rma/items/${itemId}/reassign`, { assigneeEmail, assigneeName, reason });
  },

  // Update item RMA Number
  updateItemRmaNumber: async (itemId, rmaNo) => {
    return apiPut(`/rma/items/${itemId}/rma-number`, { rmaNo });
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

  // Generate Inward Gatepass PDF
  generateInwardGatepass: async (requestNumber) => {
    const token = getAuthToken();
    if (!token) {
      return { success: false, error: "No authentication token" };
    }
    try {
      const response = await fetch(
        `${URL}/rma/gatepass/generate/${requestNumber}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!response.ok) {
        const errorText = await response.text();
        return { success: false, error: errorText };
      }
      // Return blob for PDF download
      const blob = await response.blob();
      return { success: true, blob };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },

  // Generate Outward Gatepass PDF
  generateOutwardGatepass: async (requestNumber) => {
    const token = getAuthToken();
    if (!token) {
      return { success: false, error: "No authentication token" };
    }
    try {
      const response = await fetch(
        `${URL}/rma/outward-gatepass/generate/${requestNumber}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!response.ok) {
        const errorText = await response.text();
        return { success: false, error: errorText };
      }
      // Return blob for PDF download
      const blob = await response.blob();
      return { success: true, blob };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },

  // Generate Delivery Challan PDF
  generateDeliveryChallan: async (data) => {
    const token = getAuthToken();
    if (!token) {
      throw new Error("No authentication token");
    }
    try {
      const response = await fetch(`${URL}/rma/delivery-challan/generate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(data)
      });
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }
      const blob = await response.blob();
      // Trigger download
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `DeliveryChallan_${data.rmaNo}.pdf`;
      document.body.appendChild(a);
      a.click();

      // Delay cleanup to allow download to start
      setTimeout(() => {
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }, 1000);

      return { success: true };
    } catch (error) {
      throw error;
    }
  },

  // Get all users (employees)
  getAllUsers: async () => apiGet("/all-users"),

  // Get valid repair statuses
  getRepairStatuses: async () => apiGet("/repair-statuses"),

  getAllTransporters: async () => apiGet("/transporters"),
};
