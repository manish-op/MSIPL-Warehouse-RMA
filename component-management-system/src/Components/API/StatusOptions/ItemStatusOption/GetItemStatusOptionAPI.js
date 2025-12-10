import Cookies from "js-cookie";
import { URL } from "../../URL";

async function GetItemStatusOptionAPI() {
  const token = atob(Cookies.get("authToken"));

  try {
    const response = await fetch(
      URL+"/option/item-status",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    if (!response.ok) {
      // Handle non-2xx responses (e.g., 404, 500)

      //console.error('API Error:', response.status, response.statusText);
      return null;
    } else {
      const json = await response.json();
      // console.log(json);
      return json;
    }
  } catch (error) {
    //console.error('API Error:', error.message);
    return null;
  }
}

export default GetItemStatusOptionAPI;
