import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function GetListOfAssignItemAPI(data, SetAssignTicketDetailList) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL + "/item_details/repairing/get/assign/ticketList", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess = await response.text();
        return message.warning(mess, 5);
      } else {
        if (response.status === 200) {
            const data = await response.json();
            return SetAssignTicketDetailList(data);
        } else {
          const mess = await response.text();
          return message.warning(mess, 5);
        }
      }
    })
    .catch((error) => {
      return message.error("API Error:" + error.message, 5);
    });
}

export default GetListOfAssignItemAPI;