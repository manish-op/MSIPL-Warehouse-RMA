package com.serverManagement.server.management.response.rma;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RmaResponse {

    private Long rmaRequestId;
    private String rmaNo; // Shared RMA number for all items in this request
    private String message;
    private ZonedDateTime timestamp;
    private List<RmaItemResponse> items;

    // Constructors
    public RmaResponse() {
        super();
    }

    public RmaResponse(Long rmaRequestId, String rmaNo, String message, ZonedDateTime timestamp,
            List<RmaItemResponse> items) {
        super();
        this.rmaRequestId = rmaRequestId;
        this.rmaNo = rmaNo;
        this.message = message;
        this.timestamp = timestamp;
        this.items = items;
    }

    // Getters and Setters
    public Long getRmaRequestId() {
        return rmaRequestId;
    }

    public void setRmaRequestId(Long rmaRequestId) {
        this.rmaRequestId = rmaRequestId;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<RmaItemResponse> getItems() {
        return items;
    }

    public void setItems(List<RmaItemResponse> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "RmaResponse [rmaRequestId=" + rmaRequestId + ", rmaNo=" + rmaNo + ", message=" + message
                + ", timestamp=" + timestamp + ", items=" + items + "]";
    }

    // Nested class for item response
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RmaItemResponse {
        private String product;
        private String serialNo;
        private String rmaNo;

        public RmaItemResponse() {
            super();
        }

        public RmaItemResponse(String product, String serialNo, String rmaNo) {
            super();
            this.product = product;
            this.serialNo = serialNo;
            this.rmaNo = rmaNo;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public String getRmaNo() {
            return rmaNo;
        }

        public void setRmaNo(String rmaNo) {
            this.rmaNo = rmaNo;
        }

        @Override
        public String toString() {
            return "RmaItemResponse [product=" + product + ", serialNo=" + serialNo + ", rmaNo=" + rmaNo + "]";
        }
    }
}
