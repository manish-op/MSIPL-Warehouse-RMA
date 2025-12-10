import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./RMA.css";

const RepairRequestForm = ({ formData, items, onFormSubmit }) => {
  const [showModal, setShowModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [missingFields, setMissingFields] = useState([]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const required = [
      { label: "Company Name", value: formData.companyName },
      { label: "Email Address", value: formData.email },
      { label: "Contact Name", value: formData.contactName },
      { label: "Telephone Number", value: formData.telephone },
      { label: "Mobile Number", value: formData.mobile },
      { label: "Return Address", value: formData.returnAddress },
      { label: "Date", value: formData.date },
      { label: "Mode Of Transport", value: formData.modeOfTransport },
      { label: "Returning Shipment Shipping Method", value: formData.shippingMethod }
    ];

    if (formData.invoiceCompanyName || formData.invoiceEmail || formData.invoiceContactName || formData.invoiceTelephone || formData.invoiceMobile || formData.invoiceAddress) {
      required.push(
        { label: "Invoice Company Name", value: formData.invoiceCompanyName },
        { label: "Invoice Email Address", value: formData.invoiceEmail },
        { label: "Invoice Contact Name", value: formData.invoiceContactName },
        { label: "Invoice Telephone Number", value: formData.invoiceTelephone },
        { label: "Invoice Mobile Number", value: formData.invoiceMobile },
        { label: "Invoice Address", value: formData.invoiceAddress }
      );
    }

    items.forEach((item, index) => {
      required.push(
        { label: `Item ${index + 1} Product`, value: item.product },
        { label: `Item ${index + 1} Serial No`, value: item.serialNo },
        { label: `Item ${index + 1} Fault Description`, value: item.faultDescription },
        { label: `Item ${index + 1} FM/UL/ATEX`, value: item.fmUlatex }
      );
    });

    const missing = required.filter(r => !r.value).map(r => r.label);
    if (missing.length > 0) {
      setMissingFields(missing);
      setShowModal(true);
      return;
    }
    // Show success modal instead of just calling onFormSubmit
    setShowSuccessModal(true);
    onFormSubmit();
  };

  const navigate = useNavigate();

  return (
    <div>
      {/* Action Buttons */}
      <div className="action-buttons-container">
        <button className="action-btn back-btn" onClick={() => navigate(-1)}>
          &larr; Back to Edit
        </button>
        <button className="action-btn print-btn" onClick={() => window.print()}>
          Print Form
        </button>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-popup">
            <h3>Missing Required Fields</h3>
            <ul>
              {missingFields.map(field => (
                <li key={field}>{field}</li>
              ))}
            </ul>
            <button className="close-modal-btn" onClick={() => setShowModal(false)}>
              Close
            </button>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="modal-backdrop">
          <div className="modal-popup">
            <h3 style={{ color: "green" }}>Success</h3>
            <p style={{ fontSize: "1.1rem", margin: "20px 0" }}>
              Repair Request Form Submitted Successfully!
            </p>
            <button className="close-modal-btn" onClick={() => setShowSuccessModal(false)}>
              Close
            </button>
          </div>
        </div>
      )}

      {/* Header Section */}
      <h1 className="rma-main-title">RMA Form</h1>
      <div className="address-section">
        <div className="address-block">
          <strong>SERVICE CENTER ADDRESS:</strong>
          <div>
            <b>Motorola Solutions India Private Limited</b><br />
            C/O Communications Test Design India Pvt. Ltd.<br />
            No.48/1, 2nd Main Road,<br />
            Peenya Industrial Area,<br />
            Bangalore - 560 058<br />
            Karnataka, Land Mark: RBL Bank<br />
            <b>GST #: </b> 29AAACM9343D1ZG
          </div>
        </div>
        <div className="center-header-block">
          <strong>REPAIR REQUEST FORM</strong>
          <div>(To be completed electronically)</div>
          <div><b>EMAIL:</b> repair.apac@motorolasolutions.com</div>
          <div><b>PHONE NUMBER:</b> 000-800-9190337</div>
        </div>
        <div className="address-block">
          <strong>OFFICE ADDRESS:</strong>
          <div>
            <b>Motorola Solutions India Private Limited</b><br />
            9th Floor, Mfar, Manyata Tech Park<br />
            GreenHeart Phase IV,<br />
            Nagawara,<br />
            Bangalore - 560 045<br />
            <b>GST #: </b> 29AAACM9343D1ZG
          </div>
        </div>
      </div>
      <div className="address-note-block">
        <div style={{
          color: "red",
          fontWeight: 600,
          textAlign: "center"
        }}>
          Please send all materials for repair/replacement to our Service Center address as mentioned above<br />
          Please send all corresponding letters to our Office address as mentioned above
        </div>
      </div>

      {/* Top Aligned Rows */}
      <div className="horizontal-fields">
        <div className="field-group"><label>DPL LICENSE:</label>
          <input type="text" name="dplLicense" value={formData.dplLicense} readOnly />
        </div>
        <div className="field-group"><label>DATE:</label>
          <input type="date" name="date" value={formData.date} readOnly />
        </div>
        <div className="field-group"><label>Mode Of Transport:</label>
          <input type="text" name="modeOfTransport" value={formData.modeOfTransport} readOnly />
        </div>
      </div>
      <div className="horizontal-fields">
        <div className="field-group" style={{ flex: 2 }}>
          <label>RETURNING SHIPMENT SHIPPING METHOD (MOTOROLA COURIER SERVICE / OTHER COURIER SERVICE):</label>
          <input type="text" name="shippingMethod" value={formData.shippingMethod} readOnly />
        </div>
        <div className="field-group" style={{ flex: 2 }}>
          <label>COURIER COMPANY NAME (IF OTHER):</label>
          <input type="text" name="courierCompanyName" value={formData.courierCompanyName} readOnly />
        </div>
      </div>

      {/* Table Form */}
      <form onSubmit={handleSubmit}>
        <table className="repair-table">
          <tbody>
            <tr className="section-header">
              <td colSpan={6}>RETURN ADDRESS DETAILS</td>
            </tr>
            <tr>
              <td>COMPANY NAME *</td>
              <td><input name="companyName" value={formData.companyName} readOnly /></td>
              <td>EMAIL ADDRESS *</td>
              <td><input name="email" value={formData.email} readOnly /></td>
              <td>CONTACT NAME *</td>
              <td><input name="contactName" value={formData.contactName} readOnly /></td>
            </tr>
            <tr>
              <td>TELEPHONE NUMBER *</td>
              <td><input name="telephone" value={formData.telephone} readOnly /></td>
              <td>MOBILE NUMBER *</td>
              <td><input name="mobile" value={formData.mobile} readOnly /></td>
            </tr>
            <tr>
              <td>RETURN ADDRESS *</td>
              <td colSpan={5}><input name="returnAddress" value={formData.returnAddress} readOnly style={{ width: "100%" }} /></td>
            </tr>
            <tr className="section-header-1">
              <td colSpan={6}>Invoice Address Details - If different from above</td>
            </tr>
            <tr>
              <td>COMPANY NAME *</td>
              <td><input name="invoiceCompanyName" value={formData.invoiceCompanyName} readOnly /></td>
              <td>EMAIL ADDRESS *</td>
              <td><input name="invoiceEmail" value={formData.invoiceEmail} readOnly /></td>
              <td>CONTACT NAME *</td>
              <td><input name="invoiceContactName" value={formData.invoiceContactName} readOnly /></td>
            </tr>
            <tr>
              <td>TELEPHONE NUMBER *</td>
              <td><input name="invoiceTelephone" value={formData.invoiceTelephone} readOnly /></td>
              <td>MOBILE NUMBER *</td>
              <td><input name="invoiceMobile" value={formData.invoiceMobile} readOnly /></td>
            </tr>
            <tr>
              <td>INVOICE ADDRESS *</td>
              <td colSpan={5}><input name="invoiceAddress" value={formData.invoiceAddress} readOnly style={{ width: "100%" }} /></td>
            </tr>
            <tr className="section-header">
              <td colSpan={15}>FAULT DETAILS (Please complete as much as possible). Fields with * are mandatory</td>
            </tr>
            <tr className="column-header">
              <td>ITEM NO.</td>
              <td>PRODUCT *</td>
              <td>MODEL NO./PART NO.</td>
              <td>SERIAL NO. *</td>
              <td>RMA NO.</td>
              <td>FAULT DESCRIPTION *</td>
              <td>CODEPLUG PROGRAMMING (DEFAULT/CUSTOMER CODEPLUG)</td>
              <td>FLASH CODE (OPTIONAL)</td>
              <td>STATUS (WARR/OWA/AMC/SFS)</td>
              <td>INVOICE NO. (FOR ACCESSORY)</td>
              <td>DATE CODE (FOR ACCESSORY)</td>
              <td>FM/UL/ATEX (Mandatory Y/N) *</td>
              <td>ENCRYPTION (Tetra/Astro)</td>
              <td>FIRMWARE VERSION (Tetra/Astro)</td>
              <td>LOWER FIRMWARE VERSION (Mototrbo)</td>
              <td>REMARKS</td>
            </tr>
            {items.map((item, index) => (
              <tr key={index}>
                <td>{index + 1}</td>
                <td><input name="product" value={item.product} readOnly /></td>
                <td><input name="model" value={item.model} readOnly /></td>
                <td><input name="serialNo" value={item.serialNo} readOnly /></td>
                <td><input name="rmaNo" value={item.rmaNo} disabled /></td>
                <td><input name="faultDescription" value={item.faultDescription} readOnly /></td>
                <td>
                  <select name="codeplug" value={item.codeplug} disabled>
                    <option value="">Select</option>
                    <option value="Default">Default</option>
                    <option value="Customer Codeplug">Customer Codeplug</option>
                  </select>
                </td>
                <td><input name="flashCode" value={item.flashCode} readOnly /></td>
                <td><input name="status" value={item.status} readOnly /></td>
                <td><input name="invoiceNo" value={item.invoiceNo} readOnly /></td>
                <td><input name="dateCode" value={item.dateCode} readOnly /></td>
                <td>
                  <select name="fmUlatex" value={item.fmUlatex} disabled>
                    <option value="">Select…</option>
                    <option value="N">N</option>
                    <option value="Y - FM, Repair and Return with NO FM Label">Y - FM, Repair and Return with NO FM Label</option>
                    <option value="Y - FM, Return Unrepaired">Y - FM, Return Unrepaired</option>
                    <option value="Y - UL">Y - UL</option>
                    <option value="Y - ATEX">Y - ATEX</option>
                  </select>
                </td>
                <td><input name="encryption" value={item.encryption} readOnly /></td>
                <td><input name="firmwareVersion" value={item.firmwareVersion} readOnly /></td>
                <td><input name="lowerFirmwareVersion" value={item.lowerFirmwareVersion} readOnly /></td>
                <td><input name="remarks" value={item.remarks} readOnly /></td>
              </tr>
            ))}
          </tbody>
        </table>
        <button type="submit" className="form-submit-btn">Submit Repair Request</button>
      </form>
      <div className="terms-section">
        <div className="terms-header">Terms and Conditions: </div>
        <ol className="terms-list">
          <li>Once the RMA number is issued, please send the repair request form together with the unit to the service center address as stated above</li>
          <li>Please be aware that the warranty may be void if any unauthorised activites are detected.</li>
          <li>Do not send any accessories i.e bettery, antenna and manuals along with the radios if these accessoris are found to be ok.</li>
          <li>For non-warranty radios, we will not provide any seperate quotation, rather these radios would be repaired on a flat-rate basis as per the rates share with you.</li>
          <li>Please provide the Flash code for the repair of ASTRO & APCO products.</li>
          <li>Kindly provide your quotation approval within 30 days, otherwise the device will be returned unrepaired.</li>
          <li>Please provide the date code for accessory replacement request and proof of purchase/tax invoice softcopy for antenna replacement request.
            Beside, please attach the proof of purchase/tax invoice softcopy if the date code is not available. </li>
          <li>By signing this form you have read and agree to the Terms and Conditions stated above.</li>
        </ol>
        <div className="signature-line">
          <b>PRINT NAME(Authorised Signature):</b>
          <span className="sign-box">
            <input
              type="text"
              name="signature"
              value={formData.signature}
              readOnly
            />
          </span>
        </div>
      </div>
    </div>
  );
};

export default RepairRequestForm;
