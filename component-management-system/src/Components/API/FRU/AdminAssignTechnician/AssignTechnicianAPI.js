import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";
import TicketDetailsUpdateAPI from "../GetTicketDetails/TicketDetailsUpdateAPI";

async function AssignTechnicianAPI(data, setTicketUpdateDetails) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL+"/item_details/repairing/ticket/assignEngineer/forTicket", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess= await response.text(); 
          return message.warning(mess, 5);
        
      } else {
        if (response.status === 200){
            const mess = await response.text();
            message.success(mess, 2);
            return TicketDetailsUpdateAPI(data.ticketId,setTicketUpdateDetails);
        }else{
            const mess= await response.text();
          return message.warning(mess,5);
        }
        
      }
    })
    .catch((error) => {
      
        message.error("API Error:"+ error.message,5);
    });
}

export default AssignTechnicianAPI;
