package com.serverManagement.server.management.request.itemDetails;

public class ReplacementRequest {
    private String rmaNumber;
    private String modelNo;
    private String replacementSerial;

    public ReplacementRequest() {
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public String getModelNo() {
        return modelNo;
    }

    public void setModelNo(String modelNo) {
        this.modelNo = modelNo;
    }

    public String getReplacementSerial() {
        return replacementSerial;
    }

    public void setReplacementSerial(String replacementSerial) {
        this.replacementSerial = replacementSerial;
    }
}
