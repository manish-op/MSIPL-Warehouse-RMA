// LoginApiCall.js
import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

/**
 * LoginApiCall(loginDetails)
 * - loginDetails: { email, password, service? }
 * - DOES NOT navigate internally.
 * - Returns { success: boolean, data?, message? }
 */
async function LoginApiCall(loginDetails) {
  const url = URL + "/admin/user/login";

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      // include the service in the payload if provided
      body: JSON.stringify(loginDetails),
      credentials: "include", // ensure cookies allowed if backend sets them
    });

    if (response.ok) {
      const json = await response.json();

      // Clear sensitive fields in the object reference (optional)
      try {
        loginDetails.email = "";
        loginDetails.password = "";
      } catch (e) { /* ignore */ }

      // Normalize token handling: server may or may not return a token
      const tokenId = json.authToken;
      if (tokenId) {
        try {
          const encodedData = btoa(tokenId);
          Cookies.set("authToken", encodedData, { expires: 6, path: "/" });
          localStorage.setItem("authToken", tokenId);
        } catch (e) {
          // ignore cookie write errors
        }
      }

      // Save user info
      if (json.email) localStorage.setItem("email", json.email);
      if (json.name) localStorage.setItem("name", json.name);
      if (json.mobileNo) localStorage.setItem("mobile", json.mobileNo);
      if (json.region) localStorage.setItem("region", json.region);
      if (json.role) localStorage.setItem("_User_role_for_MSIPL", json.role);

      message.success("Login successful", 1);

      return { success: true, data: json };
    } else {
      const mess = await response.text();
      message.warning(mess, 5);
      return { success: false, message: mess };
    }
  } catch (error) {
    message.error("Error: " + (error.message || "Network error"), 5);
    return { success: false, message: error.message || "Network error" };
  }
}

export default LoginApiCall;
