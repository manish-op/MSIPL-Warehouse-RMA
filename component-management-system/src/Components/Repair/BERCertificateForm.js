import React, { useState, useRef } from 'react';
import jsPDF from 'jspdf';
import html2pdf from 'html2pdf.js';
import './BERCertificateForm.css';

const BERCertificateForm = React.forwardRef(({ productData, onClose }, ref) => {
  const formRef = useRef(null);
  const [photoPreview, setPhotoPreview] = useState(null);


  const [formData, setFormData] = useState({
    customer: productData?.customer || '',
    consignee: productData?.consignee || '',
    dateIn: productData?.dateIn || new Date().toISOString().split('T')[0],
    warrantyStatus: productData?.warrantyStatus || '',
    isStatus: productData?.isStatus || '',
    incomingGIN: productData?.incomingGIN || '',
    jobID: productData?.jobID || '',
    serialNo: productData?.serialNo || '',
    tanapa: productData?.tanapa || '',
    faultFromCustomer: productData?.faultFromCustomer || '',
    damageType: [], // Multiple damage types can be selected
    otherDamage: '',
    technicianStatement: '',
    assessment: '', // To scrap / Return as-is / Further repair / Other
    assessmentDetails: '',
    customerInstruction: '',
    otherRemarks: '',
    technicianName: '',
    verificationDate: new Date().toISOString().split('T')[0],
  });

  // Generate PDF
  const generatePDF = async () => {
    const element = formRef.current;
    
    const options = {
      margin: 10,
      filename: `BER-Certificate-${formData.serialNo}-${new Date().getTime()}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2 },
      jsPDF: { orientation: 'portrait', unit: 'mm', format: 'a4' }
    };

    // Use html2pdf to generate PDF with images
    html2pdf().set(options).from(element).save().then(() => {
        if(onClose) onClose();
    });
  };

  // Expose function to parent via ref
  React.useImperativeHandle(ref, () => ({
    handleDownloadPDF: () => {
      if (!formData.customer || !formData.serialNo) {
        alert('Please fill in required fields: Customer and Serial No.');
        return;
      }
      generatePDF();
    }
  }));

  // Handle form input changes
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    if (type === 'checkbox') {
      setFormData(prev => ({
        ...prev,
        damageType: checked 
          ? [...prev.damageType, value]
          : prev.damageType.filter(item => item !== value)
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  // Handle photo upload
  const handlePhotoUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Create preview
      const reader = new FileReader();
      reader.onload = (event) => {
        setPhotoPreview(event.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // Remove uploaded photo
  const handleRemovePhoto = () => {
    setPhotoPreview(null);
  };



  return (
    <div className="ber-certificate-container">
      {/* Printable Form */}
      <div className="ber-form-content-wrapper">
        <div ref={formRef} className="ber-form-printable" style={{ padding: '20px' }}>
        
          {/* Header Info */}
          <div className="header-section">
            <h2>Notice to Customers of Unrepairable Unit (Beyond Economical Repair)</h2>
          </div>

          {/* Customer Details Table */}
          <table className="info-table">
            <tbody>
              <tr>
                <td><strong>1. CUSTOMER:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="customer" 
                    value={formData.customer} 
                    onChange={handleInputChange}
                    placeholder="Enter customer name"
                    required
                  />
                </td>
                <td><strong>6. INCOMING GIN #:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="incomingGIN" 
                    value={formData.incomingGIN} 
                    onChange={handleInputChange}
                    placeholder="Enter GIN"
                  />
                </td>
              </tr>
              <tr>
                <td><strong>2. CONSIGNEE:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="consignee" 
                    value={formData.consignee} 
                    onChange={handleInputChange}
                    placeholder="Enter consignee"
                  />
                </td>
                <td><strong>7. JOB ID (RO #):</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="jobID" 
                    value={formData.jobID} 
                    onChange={handleInputChange}
                    placeholder="Enter Job ID"
                  />
                </td>
              </tr>
              <tr>
                <td><strong>3. Date In:</strong></td>
                <td>
                  <input 
                    type="date" 
                    name="dateIn" 
                    value={formData.dateIn} 
                    onChange={handleInputChange}
                  />
                </td>
                <td><strong>8. SERIAL NO.:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="serialNo" 
                    value={formData.serialNo} 
                    onChange={handleInputChange}
                    placeholder="Enter serial number"
                    required
                  />
                </td>
              </tr>
              <tr>
                <td><strong>4. WARRANTY STATUS:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="warrantyStatus" 
                    value={formData.warrantyStatus} 
                    onChange={handleInputChange}
                    placeholder="e.g., OOW, IW"
                  />
                </td>
                <td><strong>9. TANAPA:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="tanapa" 
                    value={formData.tanapa} 
                    onChange={handleInputChange}
                    placeholder="Enter model"
                  />
                </td>
              </tr>
              <tr>
                <td><strong>5. IS Status:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="isStatus" 
                    value={formData.isStatus} 
                    onChange={handleInputChange}
                    placeholder="YES/NO"
                  />
                </td>
                <td><strong>10. FAULT FROM CUSTOMER:</strong></td>
                <td>
                  <input 
                    type="text" 
                    name="faultFromCustomer" 
                    value={formData.faultFromCustomer} 
                    onChange={handleInputChange}
                    placeholder="Describe fault"
                  />
                </td>
              </tr>
            </tbody>
          </table>

          <p className="intro-text">
            <strong>DEAR CUSTOMER:</strong><br />
            ON RECEIPT OF THIS PRODUCT YOU RETURNED FOR REPAIR, WE HAVE FOUND THE FOLLOWING DAMAGE, 
            WHICH IS NOT OF PRODUCT QUALITY ISSUE AND VOIDS WARRANTY. HENCE WE DECLARE THIS PRODUCT AS 
            BER (BEYOND ECONOMICAL REPAIR)
          </p>

          {/* Damage Section */}
          <div className="section">
            <h3>11. DAMAGE SEEN AND SUPPORTING INFO:</h3>
            <div className="damage-checklist">
            <div className="damage-checklist">
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="PHYSICAL DAMAGE"
                  checked={formData.damageType.includes('PHYSICAL DAMAGE')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('PHYSICAL DAMAGE') ? 'active' : ''}`}></span>
                PHYSICAL DAMAGE
              </label>
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="HUMID RUSTING"
                  checked={formData.damageType.includes('HUMID RUSTING')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('HUMID RUSTING') ? 'active' : ''}`}></span>
                HUMID RUSTING
              </label>
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="MISSING COMPONENT"
                  checked={formData.damageType.includes('MISSING COMPONENT')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('MISSING COMPONENT') ? 'active' : ''}`}></span>
                MISSING COMPONENT
              </label>
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="BROKEN WARRANTY SEAL"
                  checked={formData.damageType.includes('BROKEN WARRANTY SEAL')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('BROKEN WARRANTY SEAL') ? 'active' : ''}`}></span>
                BROKEN WARRANTY SEAL
              </label>
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="WATER INGRESS"
                  checked={formData.damageType.includes('WATER INGRESS')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('WATER INGRESS') ? 'active' : ''}`}></span>
                WATER INGRESS
              </label>
              <label className="custom-checkbox-label">
                <input 
                  type="checkbox" 
                  value="FOREIGN OBJECT"
                  checked={formData.damageType.includes('FOREIGN OBJECT')}
                  onChange={handleInputChange}
                />
                <span className={`checkmark ${formData.damageType.includes('FOREIGN OBJECT') ? 'active' : ''}`}></span>
                FOREIGN OBJECT
              </label>
            </div>
            </div>

            <div className="form-group">
              <label><strong>Others, Please Specify:</strong></label>
              <textarea 
                name="otherDamage" 
                value={formData.otherDamage}
                onChange={handleInputChange}
                placeholder="Specify other damage"
                rows="2"
              />
            </div>
          </div>

          {/* Technician Statement */}
          <div className="section">
            <h3>12. TECHNICIAN'S STATEMENT:</h3>
            <textarea 
              name="technicianStatement" 
              value={formData.technicianStatement}
              onChange={handleInputChange}
              placeholder="Describe damage and supporting information"
              rows="4"
              className="large-textarea"
            />

            {/* Photo Evidence */}
            <div className="photo-section">
              <label><strong>Attached Photo/Evidence:</strong></label>
              <div className="photo-upload">
                <input 
                  type="file" 
                  accept="image/*" 
                  onChange={handlePhotoUpload}
                  id="photo-input"
                />
                <label htmlFor="photo-input" className="upload-btn">
                   Upload Photo
                </label>
              </div>
              
              {photoPreview && (
                <div className="photo-preview">
                  <img src={photoPreview} alt="Evidence" />
                  <button onClick={handleRemovePhoto} className="remove-photo-btn">Remove</button>
                </div>
              )}
            </div>


          </div>

          {/* Assessment */}
          <div className="section">
            <h3>13. ASSESSMENT BY:</h3>
            <div className="assessment-options">
            <div className="assessment-options">
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="assessment" 
                  value="Scrap in CTDI"
                  checked={formData.assessment === 'Scrap in CTDI'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.assessment === 'Scrap in CTDI' ? 'active' : ''}`}></span>
                To scrap in CTDI
              </label>
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="assessment" 
                  value="Return as-is"
                  checked={formData.assessment === 'Return as-is'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.assessment === 'Return as-is' ? 'active' : ''}`}></span>
                To return-to customer as-is
              </label>
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="assessment" 
                  value="Further repair"
                  checked={formData.assessment === 'Further repair'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.assessment === 'Further repair' ? 'active' : ''}`}></span>
                To further repair by
                <input 
                  type="text" 
                  name="assessmentDetails" 
                  value={formData.assessmentDetails}
                  onChange={handleInputChange}
                  placeholder="Specify"
                  disabled={formData.assessment !== 'Further repair'}
                />
              </label>
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="assessment" 
                  value="Other"
                  checked={formData.assessment === 'Other'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.assessment === 'Other' ? 'active' : ''}`}></span>
                Other, please specify
                <input 
                  type="text" 
                  name="assessmentDetails" 
                  value={formData.assessmentDetails}
                  onChange={handleInputChange}
                  placeholder="Specify"
                  disabled={formData.assessment !== 'Other'}
                />
              </label>
            </div>
            </div>
          </div>

          {/* Customer Instruction */}
          <div className="section">
            <h3>14. CUSTOMER'S INSTRUCTION ON DISPOSAL:</h3>
            <div className="instruction-options">
            <div className="instruction-options">
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="customerInstruction" 
                  value="Returning as-is"
                  checked={formData.customerInstruction === 'Returning as-is'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.customerInstruction === 'Returning as-is' ? 'active' : ''}`}></span>
                Returning it back to you as it is
              </label>
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="customerInstruction" 
                  value="Continue repair"
                  checked={formData.customerInstruction === 'Continue repair'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.customerInstruction === 'Continue repair' ? 'active' : ''}`}></span>
                Continue repair, on R&R, in the case where the damage is minor
              </label>
              <label className="custom-radio-label">
                <input 
                  type="radio" 
                  name="customerInstruction" 
                  value="Other"
                  checked={formData.customerInstruction === 'Other'}
                  onChange={handleInputChange}
                />
                <span className={`checkmark radio ${formData.customerInstruction === 'Other' ? 'active' : ''}`}></span>
                Others
              </label>
            </div>
            </div>

            <div className="form-group">
              <label><strong>Other Remarks:</strong></label>
              <textarea 
                name="otherRemarks" 
                value={formData.otherRemarks}
                onChange={handleInputChange}
                placeholder="Enter any other remarks"
                rows="2"
              />
            </div>
          </div>

          {/* Verification */}
          <div className="section">
            <h3>15. CUSTOMER SERVICES TEAM VERIFICATION:</h3>
            <div className="verification-row">
              <div>
                <label><strong>Technician Name:</strong></label>
                <input 
                  type="text" 
                  name="technicianName" 
                  value={formData.technicianName}
                  onChange={handleInputChange}
                  placeholder="Enter name"
                />
              </div>
              <div>
                <label><strong>Date:</strong></label>
                <input 
                  type="date" 
                  name="verificationDate" 
                  value={formData.verificationDate}
                  onChange={handleInputChange}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});

export default BERCertificateForm;
 