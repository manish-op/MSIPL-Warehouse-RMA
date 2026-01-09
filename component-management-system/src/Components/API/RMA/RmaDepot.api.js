import { apiGet, apiPost } from "./ApiClient";

export const RmaDepotApi = {
    // ---------- Depot Dispatch APIs (to Depot / Bangalore) ----------
    getDepotReadyToDispatch: async () => apiGet("/rma/depot/ready-to-dispatch"),

    getNextDcNo: async () => apiGet("/rma/depot/next-dc-no"),

    dispatchToBangalore: async (payload) => {
        return apiPost("/rma/depot/dispatch-to-bangalore", payload, "text");
    },

    getDepotInTransit: async () => apiGet("/rma/depot/in-transit"),

    markDepotReceived: async (payload) => {
        return apiPost("/rma/depot/mark-received", payload, "text");
    },

    markDepotRepaired: async (payload) => {
        return apiPost("/rma/depot/mark-repaired", payload, "text");
    },

    // Dispatch LOCAL repaired items to Customer
    dispatchToCustomer: async (payload) => {
        return apiPost("/rma/dispatch-to-customer", payload, "text");
    },

    // ---------- Depot: Gurgaon side flow ----------
    // Mark repaired depot items as received at Gurgaon
    markDepotReceivedAtGurgaon: async (payload) => {
        return apiPost("/rma/depot/mark-received-gurgaon", payload, "text");
    },

    // Plan dispatch from Gurgaon to customer (HAND / COURIER)
    planDispatchFromGurgaon: async (payload) => {
        return apiPost("/rma/depot/ggn-dispatch-plan", payload, "text");
    },

    // Dispatch return from Depot to GGN or Customer
    dispatchReturn: async (payload) => {
        return apiPost("/rma/depot/dispatch-return", payload, "text");
    },

    // Upload signed DC / proof of delivery and close depot cycle
    uploadDepotProofOfDelivery: async (itemId, fileId, remarks) => {
        return apiPost("/rma/depot/upload-proof-of-delivery", { itemId, fileId, remarks }, "text");
    },

    // Delivery Confirmation
    confirmDelivery: async (itemIds, deliveredTo, deliveredBy, deliveryNotes) => {
        return apiPost("/rma/confirm-delivery", { itemIds, deliveredTo, deliveredBy, deliveryNotes }, "text");
    },

    // Device returned to Gurgaon faulty again -> close old RMA & create new
    markDepotFaultyAndCreateNewRma: async (itemId) => {
        const result = await apiPost("/rma/depot/faulty-new-rma", { itemIds: [itemId] }, "text");
        // Original: return { success: true, message: data }; where data is text. 
        // apiPost("text") returns { success: true, message: ... } so it matches.
        return result;
    },
};
