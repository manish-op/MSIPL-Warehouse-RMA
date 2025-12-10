import { saveAs } from 'file-saver';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081/api';

/**
 * Get auth token from localStorage
 */
const getToken = () => {
    return localStorage.getItem('token') || localStorage.getItem('authToken');
};

/**
 * Export RMA form data to Excel using backend template
 * Preserves original template formatting and images
 * @param {Object} formData - The form header data (company info, shipping, etc.)
 * @param {Array} items - Array of item objects with fault details
 */
export const exportRmaToExcel = async (formData, items) => {
    try {
        const token = getToken();
        const headers = {
            'Content-Type': 'application/json',
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_URL}/rma/export-excel`, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                formData,
                items
            })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to export Excel');
        }

        // Get the blob from response
        const blob = await response.blob();

        // Generate filename with date
        const today = new Date().toISOString().split('T')[0];
        const filename = `RMA_Request_${today}.xlsx`;

        // Trigger download
        saveAs(blob, filename);

        return { success: true };
    } catch (error) {
        console.error('Excel export failed:', error);
        alert('Failed to export Excel: ' + error.message);
        return { success: false, error: error.message };
    }
};

/**
 * Export multiple RMA requests to a single Excel file (client-side fallback)
 * @param {Array} rmaRequests - Array of RMA request objects containing formData and items
 */
export const exportMultipleRmaToExcel = async (rmaRequests) => {
    // For now, export each one separately
    for (const request of rmaRequests) {
        await exportRmaToExcel(request.formData, request.items);
    }
};
