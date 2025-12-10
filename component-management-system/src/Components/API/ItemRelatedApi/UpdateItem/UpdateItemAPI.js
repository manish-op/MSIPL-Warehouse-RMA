import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";
import UpdateDataAfterUpdate from "./UpdateDataAfterUpdate";

async function UpdateItemAPI(updateItem, setItemDetails) {
  const token = atob(Cookies.get("authToken"));
  const serial=updateItem.serialNo;
  
  await fetch(URL+"/componentDetails/update/item", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(updateItem),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess= await response.text();
        return message.warning(mess, 5);
      } else {
        const mess= await response.text();
        if(response.status===200){
          UpdateDataAfterUpdate(serial, setItemDetails);
          return message.success(mess,3);
        }else{
          return message.warning(mess, 5)
        }
      }
    })
    .catch((error) => {
      return message.error("Api Error: "+error.message,5);
    });
}

export default UpdateItemAPI;
