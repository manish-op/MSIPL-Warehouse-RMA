import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

async function CreateThresholdApi(data) {
  // data object will be { partNo, minQuantity, regionName }
  const token = atob(Cookies.get("authToken"));

  try {
    const response = await fetch(manualURL + "/thresholds", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data), // Send data as a JSON string
    });

    if (!response.ok) {
      const mess = await response.text();
      message.error(mess || "Failed to create rule.", 5);
      return false;
    } else {
      const mess = await response.text();
      message.success(mess, 3);
      return true; // Return true on success
    }
  } catch (error) {
    message.error("API Error: " + error.message, 5);
    return false;
  }
}

export default CreateThresholdApi;