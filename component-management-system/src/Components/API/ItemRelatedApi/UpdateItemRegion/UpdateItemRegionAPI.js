import { message } from "antd";
import Cookies from "js-cookie";
import { URL } from "../../URL";

/**
 * Updates only the region of an item and logs the action.
 * @param {string} serialNo - The serial number of the item.
 * @param {string} newRegion - The target region name.
 * @returns {Promise<boolean>} - True if successful, false otherwise.
 */
const UpdateItemRegionAPI = async (serialNo, newRegion) => {
  try {
    const authCookie = Cookies.get("authToken");
    const token = authCookie ? atob(authCookie) : null;

    if (!token) {
      message.error("Authentication token missing. Please log in.");
      return false;
    }

    const payload = {
      serialNo,
      newRegion,
    };

    const response = await fetch(`${URL}/componentDetails/regionUpdate`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    // Safely read JSON or text
    const contentType = response.headers.get("content-type") || "";
    let responseBody;

    if (contentType.includes("application/json")) {
      responseBody = await response.json();
    } else {
      responseBody = await response.text();
    }

    if (!response.ok) {
      const errMsg =
        typeof responseBody === "string"
          ? responseBody
          : responseBody?.message || "Region update failed";

      message.error(errMsg);
      return false;
    }

    message.success(`Stock Transfer successful for Serial No: ${serialNo}.`);
    return true;
  } catch (error) {
    message.error(`API Error during Region Update: ${error.message}`);
    return false;
  }
};

export default UpdateItemRegionAPI;
