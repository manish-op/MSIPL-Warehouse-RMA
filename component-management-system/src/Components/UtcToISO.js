function UtcToISO(zonedDateTimeString) {
  try {
    const date = new Date(zonedDateTimeString);

    if (isNaN(date.getTime())) {
      throw new Error("Invalid date string");
    }

    const istDate = new Date(date.toLocaleString("en-US", { timeZone: "Asia/Kolkata" }));

    const year = istDate.getFullYear();
    const month = String(istDate.getMonth() + 1).padStart(2, "0");
    const day = String(istDate.getDate()).padStart(2, "0");
    const hours = String(istDate.getHours()).padStart(2, "0");
    const minutes = String(istDate.getMinutes()).padStart(2, "0");
    const seconds = String(istDate.getSeconds()).padStart(2, "0");
    const milliseconds = String(istDate.getMilliseconds()).padStart(3, "0");

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}.${milliseconds}`;
  } catch (error) {
    return `Error: ${error.message}`;
  }
}
export default UtcToISO;

