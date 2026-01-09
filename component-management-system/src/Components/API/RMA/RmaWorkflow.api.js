import { apiGet, apiPut, apiPost } from "./ApiClient";

export const RmaWorkflowApi = {
    getUnassignedItems: async () => apiGet("/rma/items/unassigned"),
    getAssignedItems: async () => apiGet("/rma/items/assigned"),
    getRepairedItems: async () => apiGet("/rma/items/repaired"),
    getCantBeRepairedItems: async () => apiGet("/rma/items/cant-be-repaired"),
    getDispatchedItems: async () => apiGet("/rma/items/dispatched"),

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
        // Original returned plain text as data. 
        // "return { success: true, data };" where data = response.text()
        const result = await apiPost("/rma/bulk-assign", { rmaNo, assigneeEmail, assigneeName }, "text");
        if (result.success) {
            return { success: true, data: result.message }; // Map message -> data to match original property name
        }
        return result;
    },
};
