import Cookies from "js-cookie";
import { URL } from "../URL";
import { message } from "antd";

async function AddEmployeeAPI(employeeDetails) {
  const token = atob(Cookies.get("authToken"));
  await fetch(URL+"/admin/user/addUser", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(employeeDetails),
  })
    .then(async (response) => {
      if (!response.ok) {
        // This will handle any response that is not in the 200-299 range
        const mess = await response.text();         
          message.warning(mess, 5);
          return null;
         
      } else {
        if(response.status===200){
        return message.success("Employee Added",3);
      } else{
        const mess= await response.text(); 
        return message.warning(mess,5);
      }
    }
    })
    .catch((error) => {      
        message.error("API Error:"+error.message, 5);
    });
}

export default AddEmployeeAPI;
