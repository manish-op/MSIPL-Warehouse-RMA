import Cookies from "js-cookie";
import { URL } from "../URL";
import { message } from "antd";

async function ChangeEmployeePasswordAPI(changeEmpPassword) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL + "/admin/user/change/password/admin", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(changeEmpPassword),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess = await response.text();
        return message.warning(mess, 5);
      } else {
        const mess=response.text();
        if(response.status===200){
        return message.success(mess,3);
        }else{
          return message.warning(mess, 5)
        }
      }
    })
    .catch((error) => {
      message.error("API Error:" + error.message, 5);
    });
}

export default ChangeEmployeePasswordAPI;
