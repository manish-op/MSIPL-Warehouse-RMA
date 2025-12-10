import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function UserChangePassword(changePass) {
  const token = atob(Cookies.get("authToken"));

  fetch(URL+"/admin/user/change/password", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(changePass),
  })
    .then(async (response) => {
      if (!response.ok) {
        // This will handle any response that is not in the 200-299 range
        return response.text().then((text) => {
          return message.warning(text, 5);
        });
      } else {
        return message.success('password changed successfully!', 3);
      }
    })
    .catch((error) => {
      // alert("API Error:", error.message); 
     return message.error("Api Error: "+error.message,5);
      // Log only the error message to the console
      //... display the error message to the user...
    });
}

export default UserChangePassword;
