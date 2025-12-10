// src/components/HistoryTable/HistoryTable.js

import "../GetItem/TableCss.css";
import "./PrintHistoryTable.css"; // We will add the theme styles here
import React from "react";
import { useItemDetails } from "../UpdateItem/ItemContext";
import UtcToISO from "../../UtcToISO";

function HistoryTable() {
  const { itemHistory } = useItemDetails();
  const data = itemHistory;

  const handlePrint = () => {
    window.print();
  };

  const changeTimeZone = (date) => {
    return UtcToISO(date);
  };

  return (
    <>
      <title>Item History Table</title>
      {data && (
        <span>
          {/* Use a className for styling */}
          <button
            onClick={handlePrint}
            type="button" // Use "button" for non-submitting buttons
            className="print-button"
          >
            Print
          </button>
        </span>
      )}
      {/* Removed the inline style and added a className.
        The styles are now in PrintHistoryTable.css 
      */}
      <div id="historyTable" className="history-table-wrapper">
        <h2 style={{ textAlign: "center" }}>
          History Table for Item serialNo "{data[0]?.serial_No}"
        </h2>

        {/* Add a className to the table to apply themes */}
        <table className="history-table-themed">
          <thead>
            {/* Removed all inline styles */}
            <tr>
              <th>Serial No</th>
              <th>Rack No</th>
              <th>Part No</th>
              <th>Box No</th>
              <th>Model No</th>
              <th>System Name</th>
              <th>System Version</th>
              <th>Module For</th>
              <th>Spare Location</th>
              <th>Keyword</th>
              <th>Sub Keyword</th>
              <th>Region</th>
              <th>Item Status</th>
              <th>Available Status</th>
              <th>Item Description</th>
              <th>Party Name</th>
              <th>Remark</th>
              <th>Last Updated By</th>
              <th>Last Updated Date</th>
            </tr>
          </thead>
          <tbody>
            {data.map((details) => (
              <tr key={details?.id}>
                {/* Removed all inline styles */}
                <td>{details?.serial_No}</td>
                <td>{details?.rackNo}</td>
                <td>{details?.partNo}</td>
                <td>{details?.boxNo}</td>
                <td>{details?.modelNo}</td>
                <td>{details?.system}</td>
                <td>{details?.system_Version}</td>
                <td>{details?.moduleFor}</td>
                <td>{details?.spare_Location}</td>
                <td>{details?.keywordEntity?.keywordName}</td>
                <td>{details?.subKeyWordEntity?.subKeyword}</td>
                <td>{details?.region?.city}</td>
                <td>{details?.itemStatusId?.itemStatus}</td>
                <td>{details?.availableStatusId?.itemAvailableOption}</td>
                <td>{details?.itemDescription}</td>
                <td>{details?.partyName}</td>
                <td>{details?.remark}</td>
                <td>{details?.updatedByEmail}</td>
                <td>{changeTimeZone(details?.update_Date)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

export default HistoryTable;