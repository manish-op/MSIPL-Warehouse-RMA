import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";
import GetSingleAssignTicketFullDetailAPI from "./GetSingleAssignTicketFullDetailAPI";

async function UpdateTicketDetailsAPI(data, setTicketUpdateDetailsForEmployee) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL+"/item_details/repairing/ticket/update/forEngineer", {
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
            
            message.success("update sucessfully",1);
            return GetSingleAssignTicketFullDetailAPI(data.id, setTicketUpdateDetailsForEmployee);
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

export default UpdateTicketDetailsAPI;
