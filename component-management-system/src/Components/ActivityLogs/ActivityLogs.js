import React, { useState } from "react";
import ChatComponent from "./Chat/ChatComponent.jsx";
import EmployeeList from "../EmployeeList/EmployeeList.js";

function ActivityLogs() {
 
  return (
    <>
      <ChatComponent />
      <EmployeeList/>
     
    </>
  );
}

export default ActivityLogs;