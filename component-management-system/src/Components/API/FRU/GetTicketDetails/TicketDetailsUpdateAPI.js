import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function TicketDetailsUpdateAPI(id, setTicketUpdateDetails) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL+"/item_details/repairing/ticket/details/byId", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: id,
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess= await response.text(); 
          return message.warning(mess, 5);
        
      } else {
        if (response.status === 200){
            const data = await response.json();
            console.log(data);
            return setTicketUpdateDetails(data);
        }else{
            const mess= await response.text();
          message.warning(mess,5);
        }
        
      }
    })
    .catch((error) => {
      
        message.error("API Error:"+ error.message,5);
    });
}

export default TicketDetailsUpdateAPI;
