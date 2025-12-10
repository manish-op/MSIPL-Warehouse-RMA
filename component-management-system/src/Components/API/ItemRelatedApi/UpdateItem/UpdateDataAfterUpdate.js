import Cookies from "js-cookie";
import { message } from "antd";
import { URL } from "../../URL";


async function UpdateDataAfterUpdate(values, setItemDetails) { 
  const token = atob(Cookies.get("authToken"));      
      try {
        const response = await fetch(URL + "/componentDetails/serialno", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: values,
        });

        if (!response.ok) {
          if (response.status === 404) {
            return null;
          }else {
          }
          return null;
        }else{
        const data = await response.json();
        setItemDetails(data);
        return null;
      }
      } catch (error) {
        return message.error("An error occurred.");
      }
    }

export default UpdateDataAfterUpdate;