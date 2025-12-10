import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function ChangeEmployeeRoleAPI(empChangeRole) {
  // 1. Get the ENCODED token from the cookie
  const encodedToken = Cookies.get("authToken");

  // 2. Check if the encoded token exists
  if (!encodedToken || encodedToken === "undefined" || encodedToken === "null") {
    message.error("Authentication token not found. Please log in again.", 5);
    return;
  }

  let token;
  try {
    // 3. Decode the token
    token = atob(encodedToken); 
  } catch (e) {
    // This catches errors if the token is malformed
    console.error("Failed to decode auth token:", e);
    message.error("Invalid authentication token. Please log in again.", 5);
    return;
  }

  // 4. Make the API call with the now DECODED token
  try {
    const response = await fetch(URL + "/admin/role/change/employeeRole", {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`, // Send the raw, decoded token
        "Content-Type": "application/json",
      },
      body: JSON.stringify(empChangeRole),
    });

    const responseText = await response.text();

    if (!response.ok) {
      // Handle backend errors (4xx, 5xx)
      message.warning(responseText || "An error occurred.", 5);
    } else {
      // Handle success (2xx)
      message.success(responseText, 3);
    }
  } catch (error) {
    // Handle network errors
    console.error("API Fetch Error:", error);
    message.error("Network Error: " + error.message, 5);
  }
}

export default ChangeEmployeeRoleAPI;