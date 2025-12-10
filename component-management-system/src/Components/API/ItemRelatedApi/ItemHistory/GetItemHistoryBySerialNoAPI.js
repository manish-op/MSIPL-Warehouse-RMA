import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function GetItemHistoryBySerialNoAPI(values, setItemHistory, navigate) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL+"/componentDetails/history", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: values.serialNo,
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess= await response.text(); 
          return message.warning(mess, 5);
        
      } else {
        const data = await response.json();
        if(data){
        setItemHistory(data);
        return navigate("/dashboard/historyTable");
        }else{
          message.warning("Not get any data",5);
        }
        
      }
    })
    .catch((error) => {
      
        message.error("API Error:"+ error.message,5);
    });
}

export default GetItemHistoryBySerialNoAPI;
