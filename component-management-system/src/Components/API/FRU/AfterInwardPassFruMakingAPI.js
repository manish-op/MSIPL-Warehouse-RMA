import Cookies from "js-cookie";
import { URL as manualURL } from "../URL" ;
import { message } from "antd";

async function AfterInwardPassFruMakingAPI(data) {
  const token = atob(Cookies.get("authToken"));

  await fetch(manualURL + "/gatepass/generate/ticket/after/inwardpass", {
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

          const pdfBlob = await response.blob(); // It's a PDF, so name it pdfBlob
      
      // *** CRITICAL FIX 1: Correct Header Name ***
      const contentDispositionHeader = response.headers.get('Content-Disposition');
      
      // *** CRITICAL FIX 2: Get filename from Content-Disposition header ***
      const filename = getFilenameFromContentDisposition(contentDispositionHeader); 
      
      const blobUrl = URL.createObjectURL(pdfBlob); // Create object URL for the PDF blob
      
      const link = document.createElement('a');
      link.href = blobUrl;
      link.setAttribute('download', filename); // Set the download attribute with the extracted filename
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(blobUrl); // Clean up the object URL

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

export default AfterInwardPassFruMakingAPI;

// Refined helper function to extract filename
const getFilenameFromContentDisposition = (contentDisposition) => {
  if (!contentDisposition) {
    console.warn("Content-Disposition header is missing.");
    return null;
  }

  // Regex to extract filename, handling quoted and unquoted filenames, and UTF-8 encoding.
  // This regex is more robust.
  const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
  if (filenameMatch && filenameMatch[1]) {
    let filename = filenameMatch[1].replace(/['"]/g, ''); // Remove quotes
    try {
      // Decode URL-encoded characters if any (e.g., %20 for space)
      filename = decodeURIComponent(filename.replace(/^UTF-8''/, ''));
    } catch (e) {
      console.error("Error decoding filename from Content-Disposition:", e);
    }
    return filename;
  }
  
  console.warn("Filename not found in Content-Disposition:", contentDisposition);
  return null;
};
