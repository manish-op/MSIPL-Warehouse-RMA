import Cookies from "js-cookie";
import { URL } from "../../URL";

async function GetRegionAPI() {
  const token = atob(Cookies.get("authToken"));

  try {
    const response = await fetch(URL + "/region/getList", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      // Handle non-2xx responses (e.g., 404, 500)

      //console.error('API Error:', response.status, response.statusText);
      return null;
    } else {
      const json = await response.json();

      return json;
    }
  } catch (error) {
    //alert('API Error:', error.message);
    return null;
  }
}

export default GetRegionAPI;
