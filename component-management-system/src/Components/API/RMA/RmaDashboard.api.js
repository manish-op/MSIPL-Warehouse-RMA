import { apiGet } from "./ApiClient";

export const RmaDashboardApi = {
    // Dashboard Statistics
    getRmaDashboardStats: async () => apiGet("/rma/stats"),

    // TAT Compliance Report
    getTatComplianceReport: async () => apiGet("/rma/tat-compliance-report"),

    // Dashboard Interactive Modals
    getRmaRequests: async (filter) => apiGet(`/rma/requests?timeFilter=${filter || "all"}`),

    getAllItems: async () => apiGet("/rma/items/all"),

    searchItems: async (query) => apiGet(`/rma/search/items?q=${encodeURIComponent(query || "")}`),

    // Audit Logs
    getAuditLogs: async () => apiGet("/rma/audit-logs"),

    getAuditLogsByItemId: async (itemId) => apiGet(`/rma/audit-logs/item/${itemId}`),
};
