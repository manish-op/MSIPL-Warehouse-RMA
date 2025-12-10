import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

async function GatepassOutwardAPI(data) {
  const token = atob(Cookies.get("authToken"));

  try {
    const response = await fetch(manualURL + "/gatepass/outwardGatepass", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    // Get the Content-Type header to check if the response is a PDF
    const contentType = response.headers.get("content-type");

    // If the response is NOT OK, it's a clear server error.
    if (!response.ok) {
      const errorText = await response.text();
      message.error(errorText, 5);
      return; // Stop execution
    }

    // If the response IS OK but NOT a PDF, it's a hidden error.
    if (!contentType || !contentType.includes("application/pdf")) {
      const errorJson = await response.json(); // Assuming the backend sends a JSON error
      console.error("Backend sent a non-PDF success response:", errorJson);
      message.error(
        errorJson.message || "Server returned an unexpected error.",
        5
      );
      return; // Stop execution
    }

    if (!response.ok) {
      const mess = await response.text();
      return message.warning(mess, 5);
    }

    const pdfBlob = await response.blob();

    //  Fix: Use lowercase header name
    const contentDispositionHeader =
      response.headers.get("content-disposition") ||
      response.headers.get("Content-Disposition");

    let filename = "OutwardGatepass.pdf";
    if (contentDispositionHeader) {
      const match = contentDispositionHeader.match(/filename="?([^"]+)"?/);
      if (match && match[1]) {
        filename = decodeURIComponent(match[1].replace(/^UTF-8''/, ""));
      }
    }

    //  Create download link
    const blobUrl = URL.createObjectURL(pdfBlob);
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(blobUrl);

    message.success(`PDF "${filename}" downloaded successfully!`, 2);
  } catch (error) {
    console.error(error);
    message.error("API Error: " + error.message, 5);
  }
}

export default GatepassOutwardAPI;
