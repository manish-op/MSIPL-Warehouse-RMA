import { URL } from "../URL";
import Cookies from "js-cookie";

// Helper function to safely get and decode the auth token
const getAuthToken = () => {
  const encodedToken = Cookies.get("authToken");

  if (!encodedToken || encodedToken === "undefined" || encodedToken === "null") {
    console.error("No authToken cookie found. User may not be logged in.");
    return null;
  }

  try {
    return atob(encodedToken);
  } catch (error) {
    console.error("Failed to decode authToken:", error);
    return null;
  }
};

// Get all RMA items grouped by RMA request number
export async function getRmaItemsGrouped() {
  const token = getAuthToken();

  if (!token) {
    throw new Error("Authentication required. Please login first.");
  }

  const response = await fetch(`${URL}/rma/items/grouped`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch RMA items: ${response.statusText}`);
  }

  return await response.json();
}