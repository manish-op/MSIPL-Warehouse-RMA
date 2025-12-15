import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function GetItemHistoryBySerialNoAPI(values, setItemHistory, navigate) {
  const authCookie = Cookies.get("authToken");
  if (!authCookie) {
    message.error("Not authenticated. Please login first.", 5);
    return;
  }

  let token;
  try {
    token = atob(authCookie);
  } catch (e) {
    message.error("Invalid authentication token. Please login again.", 5);
    return;
  }

  console.log("Fetching history for serialNo:", values.serialNo);

  await fetch(URL + "/componentDetails/history", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(values.serialNo),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess = await response.text();
        console.error("API Error:", mess);
        return message.warning(mess, 5);

      } else {
        const data = await response.json();
        console.log("History data received:", data);
        if (data && Array.isArray(data) && data.length > 0) {
          // Save to localStorage first to ensure data persists across navigation
          localStorage.setItem('itemHistory', JSON.stringify(data));
          setItemHistory(data);
          // Small delay to ensure state update completes before navigation
          setTimeout(() => {
            navigate("/dashboard/historyTable");
          }, 100);
        } else {
          message.warning("No history found for this serial number", 5);
        }

      }
    })
    .catch((error) => {
      console.error("API Error:", error);
      message.error("API Error:" + error.message, 5);
    });
}

export default GetItemHistoryBySerialNoAPI;
