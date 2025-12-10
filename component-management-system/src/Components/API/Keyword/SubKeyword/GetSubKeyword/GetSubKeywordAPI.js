import Cookies from "js-cookie";
import { URL } from "../../../URL";

async function GetSubKeywordAPI(data) {
  const token = atob(Cookies.get('authToken'));

  try {
    const response = await fetch(
      URL+"/keyword/getSubKeyword",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      }
    );

    if (!response.ok) {
      // Handle non-2xx responses (e.g., 404, 500)

      return null;
    } else {
      const json = await response.json();
      return json;
    }
  } catch (error) {
    return null;
  }
}

export default GetSubKeywordAPI;
