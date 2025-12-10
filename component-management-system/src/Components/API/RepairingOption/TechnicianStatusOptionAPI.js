import Cookies from "js-cookie";
import { URL } from "../URL";

async function TechnicianOptionAPI() {
  const token = atob(Cookies.get("authToken"));

  try {
    const response = await fetch(
      URL+"/repairing/option/technician/status",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    if (!response.ok) {
      
      return null;
    } else {
      const json = await response.json();
     
      return json;
    }
  } catch (error) {
    console.error('API Error:', error.message);
    

  }
}

export default TechnicianOptionAPI;
