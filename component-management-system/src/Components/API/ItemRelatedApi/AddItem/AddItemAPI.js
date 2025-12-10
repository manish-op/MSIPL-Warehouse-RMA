import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function AddItemAPI(addItem) {
  const token = atob(Cookies.get("authToken"));

  await fetch(URL + "/componentDetails/add", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(addItem),
  })
    .then(async (response) => {
      if (!response.ok) {
        const mess = await response.text();
        return message.warning(mess, 5);
      } else {
        if (response.status === 200) {
          return message.success("Item Added successfully", 3);
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

export default AddItemAPI;
