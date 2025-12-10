import Cookies from "js-cookie";
import { URL as manualURL } from "../URL";
import { message } from "antd";

async function ExportCsvApi(data) {
  const token = atob(Cookies.get("authToken"));


  const params = new URLSearchParams();

  // 2. Get the regionName from the 'data' object (which is the form values)
  const regionName = data.regionName;

  // 3. If a regionName exists (for Admin), add it to the params
  if (regionName) {
    params.append('region', regionName);
  }

  const queryString = params.toString();
  const url = `${manualURL}/itemList/CSVFile/export${queryString ? `?${queryString}` : ''}`;


  await fetch(url, {
    method: "GET", 
    headers: {
      Authorization: `Bearer ${token}`,
      // 6. Remove 'Content-Type' header, it's not needed for a GET request
    },
    // 7. Remove the 'body' property entirely
  })
 

    .then(async (response) => {
      if (!response.ok) {
        const mess = await response.text();
        message.warning(mess || "An error occurred during export.", 5);
        throw new Error(`Server error: ${response.status} - ${mess}`);
      } else {
        // Your existing file download logic is perfect, no changes needed here
        const csvBlob = await response.blob();
        const contentDisposition = response.headers.get('Content-Disposition');
        const filename = getFilenameFromContentDisposition(contentDisposition) || 'ItemList.csv';
        const blobUrl = URL.createObjectURL(csvBlob);
        const link = document.createElement('a');
        link.href = blobUrl;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(blobUrl);
        message.success(`CSV file "${filename}" downloaded successfully!`, 2);
      }
    })
    .catch((error) => {
      console.log(error.message);
      return message.error("API Error:" + error.message, 5);
    });
}

export default ExportCsvApi;

// This helper function is fine, no changes needed
const getFilenameFromContentDisposition = (contentDisposition) => {
  if (!contentDisposition) return 'download.csv';
  const filenameMatch = contentDisposition.match(/filename\*?=['"]?(?:UTF-\d['']*)?([^;\n\r"]*)['"]?;?/i);
  if (filenameMatch && filenameMatch[1]) {
    try {
      return decodeURIComponent(filenameMatch[1].replace(/^UTF-8''/, ''));
    } catch (e) {
      console.error("Error decoding filename from Content-Disposition:", e);
      return filenameMatch[1];
    }
  }
  return 'download.csv';
};