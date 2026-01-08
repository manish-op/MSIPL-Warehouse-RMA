import { apiGet, apiPost } from "./ApiClient";

export const RmaCommonApi = {
    // Get all users (employees)
    getAllUsers: async () => apiGet("/all-users"),

    // Get valid repair statuses
    getRepairStatuses: async () => apiGet("/repair-statuses"),

    // Get product catalog from ItemDetailsEntity
    getProductCatalog: async () => apiGet("/rma/product-catalog"),

    // Customer APIs
    getAllCustomers: async () => apiGet("/rma/customers"),

    searchCustomers: async (searchTerm) =>
        apiGet(`/rma/customers/search?q=${encodeURIComponent(searchTerm || "")}`),

    getCustomerById: async (customerId) => apiGet(`/rma/customers/${customerId}`),

    getAllTransporters: async () => apiGet("/transporters"),

    getAllCourierCompanies: async () => apiGet("/rma/courier-companies"),

    createTransporter: async (payload) => {
        // The original implementation returned { success: true } on success without data/message wrapper in one case

        const result = await apiPost("/transporters", payload, "text");
        if (result.success) {
            // Normalized to match original return structure strictly if consumers rely on it
            return { success: true, message: result.message };
        }
        return result;
    },

    getProductRates: async (items) => {
        return apiPost("/rma/product-rates", items, "json");
    }
};
