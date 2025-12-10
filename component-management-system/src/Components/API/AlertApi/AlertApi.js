import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

// This object will hold all our alert-related API calls
export const AlertApi = {

  getActiveAlerts: async () => {
    const token = atob(Cookies.get("authToken"));

    try {
      // This calls the GET /api/alerts/active endpoint
      const response = await fetch(manualURL + "/alerts/active", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const mess = await response.text();
        message.error(mess || "Failed to fetch alerts.", 5);
        return null;
      }
      
      return await response.json(); // Returns { count, messages }
    
    } catch (error) {
      // This will catch network errors
      message.error("API Network Error: " + error.message, 5);
      return null;
    }
  },

};