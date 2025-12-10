import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

// Helper function (no changes needed, it's good)
const getFilenameFromContentDisposition = (contentDisposition) => {
  if (!contentDisposition) {
    console.warn("Content-Disposition header is missing.");
    return null;
  }
  const filenameMatch = contentDisposition.match(
    /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
  );
  if (filenameMatch && filenameMatch[1]) {
    let filename = filenameMatch[1].replace(/['"]/g, ""); // Remove quotes
    try {
      filename = decodeURIComponent(filename.replace(/^UTF-8''/, ""));
    } catch (e) {
      console.error("Error decoding filename from Content-Disposition:", e);
    }
    return filename;
  }
  console.warn(
    "Filename not found in Content-Disposition:",
    contentDisposition
  );
  return null;
};

// API Function
async function GatepassInwardAPI(data) {

  const token = atob(Cookies.get("authToken"));
  // const token = Cookies.get("authToken"); // <-- Use this if it's a normal JWT

  await fetch(manualURL + "/gatepass/inwardGatepass", {
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
          const pdfBlob = await response.blob();
          const contentDispositionHeader =
            response.headers.get("Content-Disposition");

          // *** FIX: Add a fallback filename ***
          const filename =
            getFilenameFromContentDisposition(contentDispositionHeader) ||
            "gatepass.pdf";

          const blobUrl = URL.createObjectURL(pdfBlob);
          const link = document.createElement("a");
          link.href = blobUrl;
          link.setAttribute("download", filename); // Set the download attribute
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(blobUrl); // Clean up

          message.success(`PDF file "${filename}" downloaded successfully!`, 2);
        } else {
          const mess = await response.text();
          return message.warning(mess, 5);
        }
      }
    })
    .catch((error) => {
      console.log(error.message);
      return message.error("API Error:" + error.message, 5);
    });
}

export default GatepassInwardAPI;